import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { ChatMessage, HistoryItem } from '@/types'

interface AppState {
  // Sidebar
  sidebarOpen: boolean
  setSidebarOpen: (v: boolean) => void
  toggleSidebar: () => void

  // Chat
  messages: ChatMessage[]
  addMessage: (msg: ChatMessage) => void
  clearMessages: () => void

  // Current query
  currentQuery: string
  setCurrentQuery: (q: string) => void

  // History (cached)
  history: HistoryItem[]
  setHistory: (h: HistoryItem[]) => void

  // Theme (future-proof)
  theme: 'dark'
}

export const useStore = create<AppState>()(
  persist(
    (set) => ({
      sidebarOpen: true,
      setSidebarOpen: (v) => set({ sidebarOpen: v }),
      toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),

      messages: [],
      addMessage: (msg) => set((s) => ({ messages: [...s.messages, msg] })),
      clearMessages: () => set({ messages: [] }),

      currentQuery: '',
      setCurrentQuery: (q) => set({ currentQuery: q }),

      history: [],
      setHistory: (h) => set({ history: h }),

      theme: 'dark',
    }),
    {
      name: 'pinebook-store',
      partialize: (s) => ({ history: s.history, sidebarOpen: s.sidebarOpen }),
    }
  )
)
