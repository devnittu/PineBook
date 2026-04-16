"""
ChunkingService — splits transcript entries into word-bounded chunks with timestamps.
Full implementation in Phase 3.
"""

from app.utils.logging import get_logger

logger = get_logger(__name__)

MIN_WORDS = 200
MAX_WORDS = 500


class ChunkingService:

    @staticmethod
    def chunk(transcript: list[dict], min_words: int = MIN_WORDS, max_words: int = MAX_WORDS) -> list[dict]:
        """
        Groups transcript entries into text chunks of MIN_WORDS–MAX_WORDS words.

        Each chunk: {"text": "...", "timestamp": <start_seconds_int>}

        Args:
            transcript: raw transcript entries from TranscriptService.fetch()
            min_words:  minimum words per chunk
            max_words:  maximum words per chunk

        Returns:
            list of chunk dicts
        """
        logger.info("Chunking transcript entries=%d", len(transcript))

        chunks: list[dict] = []
        current_words: list[str] = []
        current_start: float     = 0.0

        for entry in transcript:
            words = entry["text"].split()
            if not current_words:
                current_start = entry["start"]

            current_words.extend(words)

            if len(current_words) >= max_words:
                chunks.append({
                    "text":      " ".join(current_words),
                    "timestamp": int(current_start),
                })
                current_words = []
                current_start = 0.0

        # flush remaining words (> min threshold)
        if len(current_words) >= min_words:
            chunks.append({
                "text":      " ".join(current_words),
                "timestamp": int(current_start),
            })

        logger.info("Chunking complete chunks=%d", len(chunks))

        if not chunks:
            logger.warning("No chunks produced from transcript entries=%d", len(transcript))

        return chunks
