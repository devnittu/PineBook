"""
SQLAlchemy ORM models for pine-ai.
Mirrors the same tables as the Spring Boot service.
Embeddings are NOT stored here — FAISS only.
"""

from sqlalchemy import Column, Integer, String, Text, DateTime, func
from app.db.database import Base


class Video(Base):
    __tablename__ = "videos"

    id       = Column(Integer, primary_key=True, autoincrement=True)
    video_id = Column(String(20), nullable=False, unique=True, index=True)
    status   = Column(String(20), nullable=False, default="processing")
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class Chunk(Base):
    __tablename__ = "chunks"

    id        = Column(Integer, primary_key=True, autoincrement=True)
    video_id  = Column(String(20), nullable=False, index=True)
    text      = Column(Text, nullable=False)
    timestamp = Column(Integer, nullable=False)
