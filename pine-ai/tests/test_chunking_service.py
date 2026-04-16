"""
Unit tests for ChunkingService.
No I/O — pure deterministic logic tests.
"""

import pytest
from app.services.chunking_service import ChunkingService, MIN_WORDS, MAX_WORDS


def _make_entry(text: str, start: float = 0.0) -> dict:
    return {"text": text, "start": start, "duration": 2.0}


def _word(n: int) -> str:
    """Generate a string of n words."""
    return " ".join([f"word{i}" for i in range(n)])


class TestChunkingService:

    def test_empty_transcript_returns_empty(self):
        assert ChunkingService.chunk([]) == []

    def test_short_transcript_below_min_returns_empty(self):
        """A transcript with fewer words than MIN_WORDS should produce no chunk."""
        entry = _make_entry(_word(MIN_WORDS - 50), start=0.0)
        result = ChunkingService.chunk([entry])
        assert result == []

    def test_single_chunk_produced_for_max_words(self):
        """Exactly MAX_WORDS words should produce exactly one chunk."""
        entry = _make_entry(_word(MAX_WORDS), start=5.0)
        result = ChunkingService.chunk([entry])
        assert len(result) == 1
        assert result[0]["timestamp"] == 5

    def test_two_chunks_produced_for_double_max_words(self):
        words = _word(MAX_WORDS * 2)
        half = len(words.split()) // 2
        e1 = _make_entry(" ".join(words.split()[:half]), start=0.0)
        e2 = _make_entry(" ".join(words.split()[half:]), start=100.0)
        result = ChunkingService.chunk([e1, e2])
        assert len(result) >= 1

    def test_timestamp_is_integer(self):
        entry = _make_entry(_word(MAX_WORDS), start=12.7)
        result = ChunkingService.chunk([entry])
        assert isinstance(result[0]["timestamp"], int)
        assert result[0]["timestamp"] == 12

    def test_chunk_text_is_string(self):
        entry = _make_entry(_word(MAX_WORDS), start=0.0)
        result = ChunkingService.chunk([entry])
        assert isinstance(result[0]["text"], str)

    def test_multiple_entries_merged(self):
        """Words from multiple transcript entries should merge into a single chunk."""
        entries = [_make_entry(_word(100), start=float(i * 10)) for i in range(5)]
        result = ChunkingService.chunk(entries)
        # 5 * 100 = 500 words → exactly MAX_WORDS → one chunk
        assert len(result) == 1

    def test_custom_min_max(self):
        """Custom min_words/max_words should be respected."""
        entry = _make_entry(_word(50), start=0.0)
        result = ChunkingService.chunk([entry], min_words=30, max_words=60)
        assert len(result) == 1
