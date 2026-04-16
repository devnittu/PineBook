"""
FaissService — in-memory FAISS index.
Index is loaded ONCE at startup; searches are in-memory and never recompute existing embeddings.
Full search implementation in Phase 4.
"""

from __future__ import annotations

import json
import os
from pathlib import Path

import faiss
import numpy as np

from app.utils.logging import get_logger

logger = get_logger(__name__)

_index: faiss.IndexFlatIP | None = None   # Inner Product (cosine on normalised embeddings)
_metadata: list[dict]            = []     # parallel list: [{video_id, timestamp, text}, ...]

EMBEDDING_DIM = 384  # all-MiniLM-L6-v2 output dimension


class FaissService:

    @classmethod
    def load_or_create(cls, index_path: str) -> None:
        """Called once during lifespan startup."""
        global _index, _metadata
        meta_path = index_path.replace(".index", "_meta.json")

        if os.path.exists(index_path):
            logger.info("Loading FAISS index from %s", index_path)
            _index = faiss.read_index(index_path)
            if os.path.exists(meta_path):
                with open(meta_path, "r") as f:
                    _metadata = json.load(f)
            logger.info("FAISS index loaded vectors=%d", _index.ntotal)
        else:
            logger.info("Creating fresh FAISS index dim=%d", EMBEDDING_DIM)
            _index    = faiss.IndexFlatIP(EMBEDDING_DIM)
            _metadata = []

    @classmethod
    def add(cls, embeddings: np.ndarray, metadata_entries: list[dict]) -> None:
        """
        Adds embeddings + metadata to the in-memory index.
        embeddings shape: (n, EMBEDDING_DIM), float32, normalised.
        metadata_entries: list of dicts with keys: video_id, timestamp, text
        """
        if _index is None:
            raise RuntimeError("FAISS index not loaded")

        _index.add(embeddings)
        _metadata.extend(metadata_entries)
        logger.info("FAISS add: %d vectors — total=%d", len(metadata_entries), _index.ntotal)

    @classmethod
    def search(cls, query_embedding: np.ndarray, top_k: int = 5) -> list[dict]:
        """
        Returns top-k most similar chunks for a query embedding.
        query_embedding shape: (EMBEDDING_DIM,), float32, normalised.
        """
        if _index is None:
            logger.warning("FAISS index not loaded — returning empty results")
            return []
        if _index.ntotal == 0:
            logger.warning("FAISS index is empty")
            return []

        query = query_embedding.reshape(1, -1).astype(np.float32)
        scores, indices = _index.search(query, min(top_k, _index.ntotal))

        results = []
        for score, idx in zip(scores[0], indices[0]):
            if idx < 0:
                continue
            entry = _metadata[idx].copy()
            entry["score"] = float(score)
            results.append(entry)

        return results

    @classmethod
    def save(cls, index_path: str) -> None:
        """Persist index and metadata to disk on shutdown."""
        if _index is None:
            return
        Path(index_path).parent.mkdir(parents=True, exist_ok=True)
        faiss.write_index(_index, index_path)
        meta_path = index_path.replace(".index", "_meta.json")
        with open(meta_path, "w") as f:
            json.dump(_metadata, f)
        logger.info("FAISS index saved to %s (vectors=%d)", index_path, _index.ntotal)

    @classmethod
    def is_ready(cls) -> bool:
        return _index is not None

    @classmethod
    def total_vectors(cls) -> int:
        return _index.ntotal if _index else 0
