# PineBook — AI Learning Assistant

PineBook is a production-grade AI system that turns any topic into a structured YouTube learning path with timestamps, best explanations, and confusion resolution.

---

## Architecture

```
pine-web/          React + TypeScript + Vite frontend
pine-api/          Spring Boot 3 API gateway (Java 17)
pine-ai/           Python FastAPI AI service
render.yaml        One-click Render deployment
```

## Services

| Service | Stack | Port |
|---|---|---|
| `pine-web` | React 18 + Vite + Tailwind CSS | 5173 (dev) |
| `pine-api` | Spring Boot 3.2 + Java 17 | 8080 |
| `pine-ai` | Python 3.12 + FastAPI + FAISS | 8000 |

---

## Local Development

### Prerequisites
- Node.js 20+ (for frontend)
- Java 17 (Temurin) 
- Python 3.12
- PostgreSQL (or use Supabase)

### Frontend
```bash
cd pine-web
cp .env.example .env
npm install
npm run dev      # http://localhost:5173
```

### Spring Boot API
```bash
cd pine-api
cp .env.example .env    # fill in DB_URL, DB_USERNAME, DB_PASSWORD
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
mvn spring-boot:run
```

### Python AI Service
```bash
cd pine-ai
cp .env.example .env    # fill in DATABASE_URL
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload
```

---

## Tests

| Service | Command | Status |
|---|---|---|
| `pine-ai` | `.\venv\Scripts\python -m pytest tests/ -v` | 17/17 ✅ |
| `pine-api` | `mvn test` (with Java 17) | 28/28 ✅ |

---

## Deployment (Render)

1. Push to GitHub
2. Go to [render.com](https://render.com) → **New Blueprint**
3. Connect your `devnittu/PineBook` repository
4. Render reads `render.yaml` and deploys all 3 services
5. Set secrets in Render dashboard:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (from Supabase)
   - `DATABASE_URL` (Python service, same DB)

---

## Environment Variables

| Service | File | Key Vars |
|---|---|---|
| `pine-web` | `.env` | `VITE_USE_MOCK`, `VITE_API_BASE_URL` |
| `pine-api` | `.env` | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `AI_SERVICE_BASE_URL` |
| `pine-ai` | `.env` | `DATABASE_URL`, `MODEL_NAME`, `FAISS_INDEX_PATH` |

---

## API Endpoints

### Spring Boot (`pine-api`)
| Method | Path | Description |
|---|---|---|
| `POST` | `/ask` | AI query — returns learning path + explanations |
| `POST` | `/add-video` | Submit YouTube URL for processing |
| `GET` | `/status/:id` | Get video processing status |
| `GET` | `/actuator/health` | Health check |

### Python FastAPI (`pine-ai`)
| Method | Path | Description |
|---|---|---|
| `POST` | `/process-video` | Transcribe + chunk + embed video |
| `POST` | `/search` | FAISS similarity search |
| `GET` | `/health` | Health check |
