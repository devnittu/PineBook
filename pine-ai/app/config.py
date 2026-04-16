from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # Database
    database_url: str = "postgresql://postgres:changeme@localhost:5432/pinebook"

    # FAISS
    faiss_index_path: str  = "data/faiss.index"
    faiss_meta_path: str   = "data/faiss_meta.json"

    # Model
    model_name: str = "all-MiniLM-L6-v2"
    top_k: int      = 5

    # Logging
    log_level: str = "INFO"

    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
