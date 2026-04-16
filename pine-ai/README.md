# pine-ai — Python FastAPI AI Service

All AI logic lives here. Handles YouTube transcript extraction, text chunking,
sentence-transformer embeddings, and FAISS similarity search.

## Endpoints

| Method | Path              | Description                              |
|--------|-------------------|------------------------------------------|
| POST   | `/process-video`  | Ingest, chunk, embed and index a video   |
| POST   | `/search`         | Vector similarity search over chunks     |
| GET    | `/health`         | Health check                             |

## Environment Variables

Copy `.env.example` to `.env` and fill in values.

| Variable       | Description                             |
|----------------|-----------------------------------------|
| `DATABASE_URL` | PostgreSQL connection string            |
| `FAISS_PATH`   | Path to persist FAISS index (optional)  |
| `MODEL_NAME`   | Sentence-transformer model (default: all-MiniLM-L6-v2) |
| `TOP_K`        | Number of FAISS results to return (default: 5) |

## Running Locally

```bash
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Service starts on port **8000**.

## Package Structure

```
pine-ai/
├── main.py              # App entry point — lifespan startup/shutdown
├── app/
│   ├── config.py        # Settings (pydantic-settings)
│   ├── dependencies.py  # Shared FastAPI dependencies
│   ├── routers/
│   │   ├── video.py     # /process-video endpoint
│   │   └── search.py    # /search endpoint
│   ├── services/
│   │   ├── transcript_service.py   # YouTube transcript fetch
│   │   ├── chunking_service.py     # Text chunking with timestamps
│   │   ├── embedding_service.py    # Sentence-transformer wrapper
│   │   └── faiss_service.py        # FAISS index management
│   ├── db/
│   │   ├── database.py  # SQLAlchemy engine + session
│   │   └── models.py    # ORM models
│   ├── models/
│   │   └── schemas.py   # Pydantic request/response schemas
│   └── utils/
│       └── logging.py   # Structured logger setup
└── logs/
```
