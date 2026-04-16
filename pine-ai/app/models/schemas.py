"""
Pydantic schemas for all request/response bodies in pine-ai.
"""

from pydantic import BaseModel, Field


# ── /process-video ────────────────────────────────────────────────────────────

class ProcessVideoRequest(BaseModel):
    video_id: str = Field(..., min_length=11, max_length=11, description="YouTube video ID")
    correlation_id: str | None = Field(None, description="Passed through from Spring Boot")


class ProcessVideoResponse(BaseModel):
    video_id: str
    chunks_indexed: int
    status: str = "ok"


# ── /search ───────────────────────────────────────────────────────────────────

class SearchRequest(BaseModel):
    query: str = Field(..., min_length=1, max_length=500)
    top_k: int   = Field(5, ge=1, le=20)
    correlation_id: str | None = None


class ChunkResult(BaseModel):
    text:      str
    timestamp: int
    video_id:  str
    score:     float


class SearchResponse(BaseModel):
    results: list[ChunkResult]


# ── Error ─────────────────────────────────────────────────────────────────────

class ErrorResponse(BaseModel):
    status:  str = "error"
    message: str
