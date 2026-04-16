# PineBook — AI-Powered Learning Path Builder

PineBook is a production-grade backend + AI system that accepts a user learning query,
finds relevant YouTube content, extracts transcripts, identifies the most relevant segments,
and returns a structured learning path with timestamps.

---

## Architecture

```
Client → Spring Boot (Java 17) → Python FastAPI → FAISS → Response
```

| Service         | Technology       | Role                                         |
|-----------------|------------------|----------------------------------------------|
| `pine-api`      | Spring Boot 3.x  | API Gateway, Orchestration, DB, Async Trigger |
| `pine-ai`       | Python FastAPI   | AI Logic, Transcript, Embeddings, FAISS Search |
| Database        | PostgreSQL       | Video records, Chunk metadata                |
| Vector Store    | FAISS (in-memory)| Embedding similarity search                  |

---

## Services

### pine-api (Spring Boot)
- `POST /add-video` — Submit a YouTube URL for background processing
- `POST /ask` — Query the AI system for a structured learning path
- `GET /status/{videoId}` — Check processing status of a video

### pine-ai (FastAPI)
- `POST /process-video` — Ingest, chunk, embed and index a video
- `POST /search` — Vector similarity search over indexed chunks

---

## Getting Started

See individual service READMEs:
- [`pine-api/README.md`](pine-api/README.md)
- [`pine-ai/README.md`](pine-ai/README.md)

---

## Deployment
- `pine-api` → Render (Java Docker)
- `pine-ai` → Render / Railway (Python Docker)
- Database → Supabase / Neon (PostgreSQL)

All configuration via environment variables — no hardcoded secrets.
