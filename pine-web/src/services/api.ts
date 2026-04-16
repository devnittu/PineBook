import axios from 'axios'
import type {
  QueryResponse, HistoryItem, LearningPath,
  ProgressData, ConfusionResponse, Recommendation,
} from '@/types'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// ── Interceptors ───────────────────────────────────────────────────────────

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (!err.response) {
      return Promise.reject(new Error('Network error — check your connection'))
    }
    const msg = err.response.data?.message || err.response.statusText || 'Something went wrong'
    return Promise.reject(new Error(msg))
  }
)

// ── API Service ────────────────────────────────────────────────────────────

export const apiService = {

  /** POST /api/query — main AI query */
  async query(q: string): Promise<QueryResponse> {
    const { data } = await api.post<QueryResponse>('/query', { query: q })
    return data
  },

  /** POST /api/chat — conversational follow-up */
  async chat(message: string, history_id?: string): Promise<{ reply: string }> {
    const { data } = await api.post('/chat', { message, history_id })
    return data
  },

  /** GET /api/history */
  async getHistory(): Promise<HistoryItem[]> {
    const { data } = await api.get<HistoryItem[]>('/history')
    return data
  },

  /** GET /api/path/:id */
  async getPath(id: string): Promise<LearningPath> {
    const { data } = await api.get<LearningPath>(`/path/${id}`)
    return data
  },

  /** GET /api/progress */
  async getProgress(): Promise<ProgressData> {
    const { data } = await api.get<ProgressData>('/progress')
    return data
  },

  /** POST /api/confusion */
  async resolveConfusion(concept: string): Promise<ConfusionResponse> {
    const { data } = await api.post<ConfusionResponse>('/confusion', { concept })
    return data
  },

  /** GET /api/recommendations */
  async getRecommendations(): Promise<Recommendation[]> {
    const { data } = await api.get<Recommendation[]>('/recommendations')
    return data
  },
}

// ── Mock service (dev fallback) ────────────────────────────────────────────

export const mockService = {
  async query(q: string): Promise<QueryResponse> {
    await delay(1200)
    return {
      learning_path: [
        { topic: 'ML Basics', result: { text: 'Machine learning is a method of data analysis...', timestamp: 42, video_id: 'dQw4w9WgXcQ', score: 0.92 } },
        { topic: 'Types of ML', result: { text: 'There are three main types: supervised, unsupervised, and reinforcement learning...', timestamp: 180, video_id: 'dQw4w9WgXcQ', score: 0.88 } },
        { topic: 'ML Algorithms', result: { text: 'Common algorithms include linear regression, decision trees, and neural networks...', timestamp: 360, video_id: 'dQw4w9WgXcQ', score: 0.85 } },
        { topic: 'Applications', result: { text: 'ML powers recommendation engines, image recognition, and natural language processing...', timestamp: 540, video_id: 'dQw4w9WgXcQ', score: 0.82 } },
      ],
      best_explanations: {
        'Example': { text: 'For example, training a model to classify emails as spam or not spam...', timestamp: 90, video_id: 'dQw4w9WgXcQ' },
        'Basics': { text: 'At its core, ML lets computers learn from data without explicit programming...', timestamp: 30, video_id: 'dQw4w9WgXcQ' },
        'Deep Dive': { text: 'Gradient descent optimizes model weights by iteratively adjusting them in the direction that minimizes the loss function, computed via backpropagation through the network layers...', timestamp: 720, video_id: 'dQw4w9WgXcQ' },
      },
      confusions: [
        { text: 'Supervised learning uses labeled data; unsupervised learning finds patterns in unlabeled data...', timestamp: 200, video_id: 'dQw4w9WgXcQ' },
      ],
    }
  },

  async getHistory(): Promise<HistoryItem[]> {
    await delay(400)
    return [
      { id: '1', query: 'Learn machine learning', created_at: '2h ago', path_id: 'p1' },
      { id: '2', query: 'React hooks deep dive', created_at: '1d ago', path_id: 'p2' },
      { id: '3', query: 'System design fundamentals', created_at: '3d ago', path_id: 'p3' },
    ]
  },

  async getProgress(): Promise<ProgressData> {
    await delay(300)
    return { total_paths: 5, completed_paths: 2, total_lessons: 48, completed_lessons: 19, streak_days: 7, last_active: 'Today' }
  },

  async resolveConfusion(concept: string): Promise<ConfusionResponse> {
    await delay(800)
    return {
      concept,
      explanation: `${concept} is a fundamental concept in machine learning. It refers to...`,
      analogy: `Think of it like teaching a child...`,
      related: ['overfitting', 'bias-variance tradeoff', 'regularization'],
    }
  },

  async getRecommendations(): Promise<Recommendation[]> {
    await delay(250)
    return [
      { id: 'r1', title: 'Neural Networks', topic: 'Deep Learning', level: 'intermediate', video_count: 8 },
      { id: 'r2', title: 'Python for Data Science', topic: 'Programming', level: 'beginner', video_count: 12 },
      { id: 'r3', title: 'Transformers & LLMs', topic: 'NLP', level: 'advanced', video_count: 6 },
    ]
  },
}

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms))

// Switch to real service by setting VITE_USE_MOCK=false
export const service = import.meta.env.VITE_USE_MOCK === 'false' ? apiService : mockService
