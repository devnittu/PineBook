"""
Async SQLAlchemy engine and session factory.
"""

from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase

from app.config import settings


# Convert sync postgresql:// → async postgresql+asyncpg://
def _async_url(url: str) -> str:
    """Convert sync postgresql:// to async postgresql+asyncpg:// if needed."""
    if url.startswith("postgresql+asyncpg://"):
        return url
    return url.replace("postgresql://", "postgresql+asyncpg://", 1)


engine = create_async_engine(
    _async_url(settings.database_url),
    pool_size=3,
    max_overflow=5,
    echo=False,
    connect_args={"ssl": "require"},   # Required for Supabase
)

AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
)


class Base(DeclarativeBase):
    pass


async def init_db():
    """Create tables if they don't exist (idempotent)."""
    # Import models to register them with metadata
    import app.db.models  # noqa: F401
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def get_db():
    """FastAPI dependency: yields an async DB session."""
    async with AsyncSessionLocal() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
