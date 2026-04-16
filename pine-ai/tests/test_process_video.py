"""
Integration tests for POST /process-video.
Uses httpx AsyncClient + mocked services — no real YouTube or GPU needed.
"""

import pytest
import pytest_asyncio
from unittest.mock import patch, MagicMock
from httpx import AsyncClient, ASGITransport

from main import app


@pytest.fixture
def mock_transcript():
    return [
        {"text": " ".join([f"word{i}" for i in range(100)]), "start": float(j * 10), "duration": 5.0}
        for j in range(6)  # 6 * 100 = 600 words → at least one chunk
    ]


@pytest.mark.asyncio
class TestProcessVideoEndpoint:

    @patch("app.routers.video.TranscriptService.fetch")
    @patch("app.routers.video.EmbeddingService.embed")
    @patch("app.routers.video.FaissService.is_ready", return_value=True)
    @patch("app.routers.video.FaissService.add")
    @patch("app.routers.video.FaissService.total_vectors", return_value=5)
    async def test_process_video_success(
        self,
        mock_total,
        mock_add,
        mock_ready,
        mock_embed,
        mock_fetch,
        mock_transcript,
    ):
        import numpy as np
        mock_fetch.return_value = mock_transcript
        mock_embed.return_value = np.random.rand(1, 384).astype("float32")

        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            # Override DB dependency
            from app.db.database import get_db
            from unittest.mock import AsyncMock

            mock_db = AsyncMock()
            mock_db.execute.return_value = MagicMock(scalar_one_or_none=lambda: None)
            app.dependency_overrides[get_db] = lambda: mock_db

            resp = await client.post(
                "/process-video",
                json={"video_id": "dQw4w9WgXcQ"},
            )

        app.dependency_overrides.clear()
        assert resp.status_code == 200
        data = resp.json()
        assert data["video_id"] == "dQw4w9WgXcQ"
        assert data["status"] in ("ok", "already_indexed")

    @patch("app.routers.video.FaissService.is_ready", return_value=False)
    async def test_process_video_faiss_not_ready(self, mock_ready):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            resp = await client.post(
                "/process-video",
                json={"video_id": "dQw4w9WgXcQ"},
            )
        assert resp.status_code == 503

    @patch("app.routers.video.TranscriptService.fetch",
           side_effect=ValueError("Transcripts are disabled"))
    @patch("app.routers.video.FaissService.is_ready", return_value=True)
    async def test_process_video_transcript_unavailable(self, mock_ready, mock_fetch):
        from app.db.database import get_db
        from unittest.mock import AsyncMock
        mock_db = AsyncMock()
        mock_db.execute.return_value = MagicMock(scalar_one_or_none=lambda: None)
        app.dependency_overrides[get_db] = lambda: mock_db

        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            resp = await client.post(
                "/process-video",
                json={"video_id": "dQw4w9WgXcQ"},
            )

        app.dependency_overrides.clear()
        assert resp.status_code == 422
        assert "disabled" in resp.json()["detail"].lower()

    async def test_process_video_invalid_video_id(self):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            resp = await client.post(
                "/process-video",
                json={"video_id": "short"},  # < 11 chars
            )
        assert resp.status_code == 422  # Pydantic validation error
