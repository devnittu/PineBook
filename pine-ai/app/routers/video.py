"""
POST /process-video

Full pipeline:
  1. Validate: video not already indexed (idempotent)
  2. Fetch YouTube transcript
  3. Chunk into 200–500 word segments with timestamps
  4. Generate sentence-transformer embeddings for all chunks
  5. Add embeddings + metadata to FAISS index
  6. Persist chunk records to PostgreSQL
  7. Return { video_id, chunks_indexed, status }

Error handling:
  - Missing/disabled transcript → 422 with clear message
  - Empty chunks after splitting → 422
  - Embedding failure → retried once inside EmbeddingService
  - FAISS not ready → 503
  - Any unexpected error → caught by global handler → 500
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select, delete
from sqlalchemy.ext.asyncio import AsyncSession

from app.db.database import get_db
from app.db.models import Chunk, Video
from app.dependencies import get_correlation_id
from app.models.schemas import (
    ProcessVideoRequest,
    ProcessVideoResponse,
    ErrorResponse,
)
from app.services.chunking_service import ChunkingService
from app.services.embedding_service import EmbeddingService
from app.services.faiss_service import FaissService
from app.services.transcript_service import TranscriptService
from app.utils.logging import get_logger

logger = get_logger(__name__)
router = APIRouter(tags=["video"])


@router.post(
    "/process-video",
    response_model=ProcessVideoResponse,
    responses={
        422: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
        500: {"model": ErrorResponse},
    },
)
async def process_video(
    request: ProcessVideoRequest,
    db: AsyncSession = Depends(get_db),
    correlation_id: str = Depends(get_correlation_id),
):
    video_id = request.video_id
    logger.info(
        "POST /process-video video_id=%s correlation_id=%s",
        video_id,
        correlation_id,
    )

    # ── Guard: FAISS must be ready ────────────────────────────────────────────
    if not FaissService.is_ready():
        logger.error("FAISS index not ready — cannot process video_id=%s", video_id)
        raise HTTPException(status_code=503, detail="FAISS index not initialised")

    # ── Idempotency: skip if already processed ────────────────────────────────
    existing = await db.execute(
        select(Video).where(Video.video_id == video_id)
    )
    existing_video = existing.scalar_one_or_none()

    if existing_video and existing_video.status == "done":
        existing_chunks = await db.execute(
            select(Chunk).where(Chunk.video_id == video_id)
        )
        chunk_count = len(existing_chunks.scalars().all())
        logger.info(
            "Video already indexed video_id=%s chunks=%d — skipping",
            video_id,
            chunk_count,
        )
        return ProcessVideoResponse(
            video_id=video_id,
            chunks_indexed=chunk_count,
            status="already_indexed",
        )

    # ── Step 1: Fetch transcript ──────────────────────────────────────────────
    logger.info("Step 1/5 — fetching transcript video_id=%s", video_id)
    try:
        transcript = TranscriptService.fetch(video_id)
    except ValueError as e:
        logger.error("Transcript fetch failed video_id=%s error=%s", video_id, e)
        raise HTTPException(status_code=422, detail=str(e))

    # ── Step 2: Chunk ─────────────────────────────────────────────────────────
    logger.info("Step 2/5 — chunking transcript entries=%d", len(transcript))
    chunks = ChunkingService.chunk(transcript)

    if not chunks:
        logger.error(
            "No chunks produced from transcript video_id=%s entries=%d",
            video_id,
            len(transcript),
        )
        raise HTTPException(
            status_code=422,
            detail=f"Transcript for video {video_id} was too short to chunk",
        )

    logger.info("Chunking complete video_id=%s chunks=%d", video_id, len(chunks))

    # ── Step 3: Embed ─────────────────────────────────────────────────────────
    logger.info("Step 3/5 — generating embeddings chunks=%d", len(chunks))
    texts = [c["text"] for c in chunks]

    try:
        embeddings = EmbeddingService.embed(texts)
    except Exception as e:
        logger.error("Embedding failed video_id=%s error=%s", video_id, e)
        raise HTTPException(
            status_code=500,
            detail=f"Embedding generation failed: {e}",
        )

    logger.info(
        "Embeddings generated video_id=%s shape=%s",
        video_id,
        embeddings.shape,
    )

    # ── Step 4: Add to FAISS ──────────────────────────────────────────────────
    logger.info("Step 4/5 — adding to FAISS index")
    metadata_entries = [
        {
            "video_id":  video_id,
            "timestamp": c["timestamp"],
            "text":      c["text"],
        }
        for c in chunks
    ]
    FaissService.add(embeddings, metadata_entries)
    logger.info(
        "FAISS updated video_id=%s total_vectors=%d",
        video_id,
        FaissService.total_vectors(),
    )

    # ── Step 5: Persist to PostgreSQL ─────────────────────────────────────────
    logger.info("Step 5/5 — persisting chunks to DB")

    # Clean up any stale chunks from a previous failed run
    await db.execute(delete(Chunk).where(Chunk.video_id == video_id))

    chunk_rows = [
        Chunk(
            video_id=video_id,
            text=c["text"],
            timestamp=c["timestamp"],
        )
        for c in chunks
    ]
    db.add_all(chunk_rows)

    # Upsert video status
    if existing_video:
        existing_video.status = "done"
    else:
        db.add(Video(video_id=video_id, status="done"))

    await db.commit()
    logger.info(
        "DB commit complete video_id=%s chunks_stored=%d",
        video_id,
        len(chunk_rows),
    )

    return ProcessVideoResponse(
        video_id=video_id,
        chunks_indexed=len(chunk_rows),
        status="ok",
    )
