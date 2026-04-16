"""
FastAPI dependency factories shared across routers.
"""

from fastapi import Header
from app.db.database import get_db  # re-export for convenience
from app.utils.logging import request_id_var

__all__ = ["get_db", "get_correlation_id"]


async def get_correlation_id(
    x_correlation_id: str = Header(default="", alias="X-Correlation-ID")
) -> str:
    """
    Reads the X-Correlation-ID header forwarded by Spring Boot.
    Falls back to empty string — middleware will have already set request_id_var.
    """
    return x_correlation_id
