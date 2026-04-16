"""
TranscriptService — fetches YouTube transcripts using youtube-transcript-api v1.x.

API change from v0.x → v1.x:
  - Old: YouTubeTranscriptApi.get_transcript(video_id)
  - New: YouTubeTranscriptApi().fetch(video_id)  (instance method, returns FetchedTranscript)
  - FetchedTranscript is iterable; each item has .text, .start, .duration attributes
"""

from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api._errors import TranscriptsDisabled, NoTranscriptFound

from app.utils.logging import get_logger

logger = get_logger(__name__)


class TranscriptService:

    @staticmethod
    def fetch(video_id: str) -> list[dict]:
        """
        Returns a list of transcript entries:
          [{"text": "...", "start": 12.5, "duration": 3.2}, ...]

        Raises:
            ValueError: if transcript unavailable or disabled.
        """
        logger.info("Fetching transcript for video_id=%s", video_id)
        try:
            api = YouTubeTranscriptApi()
            fetched = api.fetch(video_id)
            # v1.x: FetchedTranscript is iterable of FetchedTranscriptSnippet objects
            transcript = [
                {"text": snippet.text, "start": snippet.start, "duration": snippet.duration}
                for snippet in fetched
            ]
            logger.info(
                "Transcript fetched video_id=%s entries=%d", video_id, len(transcript)
            )
            return transcript
        except TranscriptsDisabled:
            logger.error("Transcripts disabled for video_id=%s", video_id)
            raise ValueError(f"Transcripts are disabled for video {video_id}")
        except NoTranscriptFound:
            logger.error("No transcript found for video_id=%s", video_id)
            raise ValueError(f"No transcript found for video {video_id}")
        except Exception as e:
            logger.error(
                "Unexpected error fetching transcript video_id=%s error=%s", video_id, e
            )
            raise ValueError(f"Failed to fetch transcript: {e}")
