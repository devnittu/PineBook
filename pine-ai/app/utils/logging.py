"""
Structured logger factory for pine-ai.

Every logger produced here includes:
  - timestamp
  - service name (pine-ai)
  - request_id  (pulled from contextvars, set per request)

Usage:
    from app.utils.logging import get_logger
    logger = get_logger(__name__)
"""

import logging
import logging.handlers
import os
from contextvars import ContextVar

# Per-request correlation ID storage (set in middleware)
request_id_var: ContextVar[str] = ContextVar("request_id", default="-")

LOG_DIR = "logs"
os.makedirs(LOG_DIR, exist_ok=True)

_FORMAT = "%(asctime)s [pine-ai] %(levelname)-8s [%(request_id)s] %(name)s - %(message)s"
_DATE_FMT = "%Y-%m-%d %H:%M:%S"


class RequestIdFilter(logging.Filter):
    """Injects the current request_id into every log record."""

    def filter(self, record: logging.LogRecord) -> bool:
        record.request_id = request_id_var.get("-")
        return True


def _build_handler(path: str, level: int) -> logging.Handler:
    handler = logging.handlers.RotatingFileHandler(
        path, maxBytes=50 * 1024 * 1024, backupCount=10
    )
    handler.setLevel(level)
    handler.setFormatter(logging.Formatter(_FORMAT, datefmt=_DATE_FMT))
    handler.addFilter(RequestIdFilter())
    return handler


def _configure_root() -> None:
    root = logging.getLogger()
    if root.handlers:
        return  # already configured

    root.setLevel(logging.DEBUG)

    # Console
    console = logging.StreamHandler()
    console.setLevel(logging.INFO)
    console.setFormatter(logging.Formatter(_FORMAT, datefmt=_DATE_FMT))
    console.addFilter(RequestIdFilter())
    root.addHandler(console)

    # logs/ai.log — all levels
    root.addHandler(_build_handler(f"{LOG_DIR}/ai.log", logging.DEBUG))

    # logs/error.log — errors only
    error_handler = _build_handler(f"{LOG_DIR}/error.log", logging.ERROR)
    root.addHandler(error_handler)


_configure_root()


def get_logger(name: str) -> logging.Logger:
    return logging.getLogger(name)
