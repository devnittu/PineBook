"""
Unit tests for TranscriptService error handling (youtube-transcript-api v1.x).
All network calls are mocked — no actual HTTP requests made.
"""

import pytest
from unittest.mock import patch, MagicMock
from youtube_transcript_api._errors import TranscriptsDisabled, NoTranscriptFound

from app.services.transcript_service import TranscriptService


def _make_snippet(text: str, start: float = 0.0, duration: float = 2.0) -> MagicMock:
    """Creates a mock FetchedTranscriptSnippet with attribute access."""
    snippet = MagicMock()
    snippet.text = text
    snippet.start = start
    snippet.duration = duration
    return snippet


class TestTranscriptService:

    @patch("app.services.transcript_service.YouTubeTranscriptApi")
    def test_fetch_success(self, MockAPI):
        snippets = [_make_snippet("hello world", 0.0, 2.0)]
        MockAPI.return_value.fetch.return_value = snippets

        result = TranscriptService.fetch("dQw4w9WgXcQ")

        assert len(result) == 1
        assert result[0]["text"] == "hello world"
        assert result[0]["start"] == 0.0
        MockAPI.return_value.fetch.assert_called_once_with("dQw4w9WgXcQ")

    @patch("app.services.transcript_service.YouTubeTranscriptApi")
    def test_fetch_transcripts_disabled(self, MockAPI):
        MockAPI.return_value.fetch.side_effect = TranscriptsDisabled("dQw4w9WgXcQ")
        with pytest.raises(ValueError, match="disabled"):
            TranscriptService.fetch("dQw4w9WgXcQ")

    @patch("app.services.transcript_service.YouTubeTranscriptApi")
    def test_fetch_no_transcript_found(self, MockAPI):
        MockAPI.return_value.fetch.side_effect = NoTranscriptFound(
            "dQw4w9WgXcQ", [], {}
        )
        with pytest.raises(ValueError, match="No transcript found"):
            TranscriptService.fetch("dQw4w9WgXcQ")

    @patch("app.services.transcript_service.YouTubeTranscriptApi")
    def test_fetch_generic_exception(self, MockAPI):
        MockAPI.return_value.fetch.side_effect = RuntimeError("network error")
        with pytest.raises(ValueError, match="Failed to fetch"):
            TranscriptService.fetch("dQw4w9WgXcQ")

    @patch("app.services.transcript_service.YouTubeTranscriptApi")
    def test_fetch_returns_list_of_dicts(self, MockAPI):
        snippets = [
            _make_snippet("a", 0.0, 1.0),
            _make_snippet("b", 1.0, 1.0),
        ]
        MockAPI.return_value.fetch.return_value = snippets

        result = TranscriptService.fetch("abc12345678")

        assert isinstance(result, list)
        assert len(result) == 2
        assert all(isinstance(r, dict) for r in result)
        assert all("text" in r and "start" in r and "duration" in r for r in result)
