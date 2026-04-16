"""
pine-ai — FastAPI entry point.

Startup (lifespan):
  1. Create DB tables
  2. Load sentence-transformer model into memory
  3. Load FAISS index from disk (or create fresh)

Shutdown:
  1. Persist FAISS index to disk

Middleware (in order):
  1. CorrelationIdMiddleware  — reads X-Correlation-ID, populates request_id_var
  2. CORS

Global exception handler:
  - Never crashes the service
  - Returns { "status": "error", "message": "..." } for all unhandled exceptions
"""

import uuid
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware

from app.config import settings
from app.db.database import init_db
from app.routers import video, search
from app.services.embedding_service import EmbeddingService
from app.services.faiss_service import FaissService
from app.utils.logging import get_logger, request_id_var

logger = get_logger(__name__)


# ── Lifespan ──────────────────────────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("pine-ai starting up...")

    await init_db()
    logger.info("Database tables verified")

    EmbeddingService.load_model(settings.model_name)
    logger.info("Embedding model loaded: %s", settings.model_name)

    FaissService.load_or_create(settings.faiss_index_path)
    logger.info("FAISS index ready — vectors=%d", FaissService.total_vectors())

    yield  # ← app is running

    FaissService.save(settings.faiss_index_path)
    logger.info("FAISS index saved — pine-ai shutting down cleanly")


# ── App factory ───────────────────────────────────────────────────────────────

app = FastAPI(
    title="PineBook AI Service",
    version="1.0.0",
    description="Transcript processing, embeddings, and FAISS search for PineBook",
    lifespan=lifespan,
)


# ── Correlation ID middleware ──────────────────────────────────────────────────

class CorrelationIdMiddleware(BaseHTTPMiddleware):
    """
    Reads X-Correlation-ID from inbound request (forwarded by Spring Boot).
    Generates a UUID if not present.
    Stores it in request_id_var (ContextVar) so it appears in all log lines.
    Echoes it back in the response header.
    """

    async def dispatch(self, request: Request, call_next):
        correlation_id = request.headers.get("X-Correlation-ID") or str(uuid.uuid4())
        token = request_id_var.set(correlation_id)
        try:
            response = await call_next(request)
            response.headers["X-Correlation-ID"] = correlation_id
            return response
        finally:
            request_id_var.reset(token)


app.add_middleware(CorrelationIdMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Global exception handler ──────────────────────────────────────────────────

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(
        "Unhandled exception path=%s method=%s error=%s",
        request.url.path,
        request.method,
        exc,
        exc_info=True,
    )
    return JSONResponse(
        status_code=500,
        content={"status": "error", "message": "An unexpected error occurred"},
    )


@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    logger.warning("ValueError path=%s error=%s", request.url.path, exc)
    return JSONResponse(
        status_code=422,
        content={"status": "error", "message": str(exc)},
    )


# ── Routers ───────────────────────────────────────────────────────────────────

app.include_router(video.router)
app.include_router(search.router)


# ── Health ────────────────────────────────────────────────────────────────────

@app.get("/health", tags=["meta"])
async def health():
    return {
        "status":        "ok",
        "service":       "pine-ai",
        "faiss_vectors": FaissService.total_vectors(),
        "model":         settings.model_name,
    }


# ── Entry ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=settings.host,
        port=settings.port,
        reload=False,
        log_level=settings.log_level.lower(),
    )
