"""
EmbeddingService — sentence-transformer wrapper.
Model is loaded ONCE at startup via load_model(); never reloaded per request.
Full implementation in Phase 3.
"""

from __future__ import annotations

import numpy as np
from sentence_transformers import SentenceTransformer

from app.utils.logging import get_logger

logger = get_logger(__name__)

_model: SentenceTransformer | None = None


class EmbeddingService:

    @classmethod
    def load_model(cls, model_name: str) -> None:
        """Called once during lifespan startup."""
        global _model
        logger.info("Loading embedding model: %s", model_name)
        _model = SentenceTransformer(model_name)
        logger.info("Embedding model loaded successfully")

    @classmethod
    def embed(cls, texts: list[str]) -> np.ndarray:
        """
        Converts a list of strings → float32 embedding matrix.
        Shape: (len(texts), embedding_dim)
        """
        if _model is None:
            raise RuntimeError("Embedding model not loaded — call load_model() at startup")

        try:
            embeddings = _model.encode(texts, convert_to_numpy=True, normalize_embeddings=True)
            return embeddings.astype(np.float32)
        except Exception as e:
            logger.error("Embedding failed texts_count=%d error=%s", len(texts), e)
            # Retry once
            logger.info("Retrying embedding...")
            embeddings = _model.encode(texts, convert_to_numpy=True, normalize_embeddings=True)
            return embeddings.astype(np.float32)

    @classmethod
    def embed_single(cls, text: str) -> np.ndarray:
        """Convenience wrapper for a single query string."""
        return cls.embed([text])[0]
