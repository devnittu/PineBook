// ── API Types ──────────────────────────────────────────────────────────────

export interface ChunkResult {
  text: string
  timestamp: number
  video_id: string
  score?: number
}

export interface LearningStep {
  topic: string
  result: ChunkResult
}

export interface QueryResponse {
  learning_path: LearningStep[]
  best_explanations: Record<string, ChunkResult>
  confusions: ChunkResult[]
}

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  data?: QueryResponse
}

export interface HistoryItem {
  id: string
  query: string
  created_at: string
  path_id: string
}

export interface LearningPath {
  id: string
  title: string
  phases: Phase[]
  progress: number
  created_at: string
}

export interface Phase {
  id: string
  label: string
  title: string
  lessons: Lesson[]
  completed: boolean
}

export interface Lesson {
  id: string
  title: string
  video_id: string
  timestamp: number
  duration_estimate: string
  completed: boolean
}

export interface ProgressData {
  total_paths: number
  completed_paths: number
  total_lessons: number
  completed_lessons: number
  streak_days: number
  last_active: string
}

export interface VideoSegment {
  video_id: string
  title: string
  channel: string
  thumbnail: string
  timestamp: number
  why_selected: string
  text: string
}

export interface ConfusionResponse {
  concept: string
  explanation: string
  analogy?: string
  related: string[]
}

export interface Recommendation {
  id: string
  title: string
  topic: string
  level: 'beginner' | 'intermediate' | 'advanced'
  video_count: number
}

export type ApiStatus = 'idle' | 'loading' | 'success' | 'error'
