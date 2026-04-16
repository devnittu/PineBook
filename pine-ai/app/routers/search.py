"""
POST /search

Pipeline:
  1. Validate query (Pydantic, already done at schema level)
  2. Convert query string → embedding (EmbeddingService)
  3. FAISS similarity search → top-k results
  4. Return structured list of ChunkResult

Performance:
  - Model is pre-loaded at startup → embedding is fast (~10ms)
  - FAISS search is in-memory → < 1ms for small indexes
  - Entire handler is synchronous-on-thread (no DB I/O needed)

Error handling:
  - FAISS not ready → 503 with safe response
  - Empty index → 200 with empty list (not an error)
  - Embedding failure → 500 (very rare, model is pre-tested at startup)
"""

from fastapi import APIRouter, Depends, HTTPException

from app.dependencies import get_correlation_id
from app.models.schemas import ChunkResult, ErrorResponse, SearchRequest, SearchResponse
from app.services.embedding_service import EmbeddingService
from app.services.faiss_service import FaissService
from app.utils.logging import get_logger

logger = get_logger(__name__)
router = APIRouter(tags=["search"])


@router.post(
    "/search",
    response_model=SearchResponse,
    responses={
        503: {"model": ErrorResponse},
        500: {"model": ErrorResponse},
    },
)
async def search(
    request: SearchRequest,
    correlation_id: str = Depends(get_correlation_id),
):
    logger.info(
        "POST /search query='%s' top_k=%d correlation_id=%s",
        request.query,
        request.top_k,
        correlation_id,
    )

    # ── Guard: FAISS must be loaded ───────────────────────────────────────────
    if not FaissService.is_ready():
        logger.error("FAISS index not ready — cannot serve search")
        raise HTTPException(status_code=503, detail="Search index not available")

    # ── Early exit: nothing indexed yet ──────────────────────────────────────
    if FaissService.total_vectors() == 0:
        logger.warning("FAISS index is empty — returning empty results")
        return SearchResponse(results=[])

    # ── Step 1: Embed query ───────────────────────────────────────────────────
    try:
        query_embedding = EmbeddingService.embed_single(request.query)
    except Exception as e:
        logger.error("Query embedding failed query='%s' error=%s", request.query, e)
        raise HTTPException(
            status_code=500,
            detail=f"Failed to embed query: {e}",
        )

    logger.debug(
        "Query embedded dim=%d query='%s'",
        query_embedding.shape[0],
        request.query,
    )

    # ── Step 2: FAISS search ──────────────────────────────────────────────────
    raw_results = FaissService.search(query_embedding, top_k=request.top_k)

    logger.info(
        "FAISS search complete query='%s' hits=%d total_vectors=%d",
        request.query,
        len(raw_results),
        FaissService.total_vectors(),
    )

    # ── Step 3: Shape response ────────────────────────────────────────────────
    results = [
        ChunkResult(
            text=r.get("text", ""),
            timestamp=int(r.get("timestamp", 0)),
            video_id=r.get("video_id", ""),
            score=float(r.get("score", 0.0)),
        )
        for r in raw_results
    ]

    return SearchResponse(results=results)
