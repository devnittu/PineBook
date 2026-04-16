import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Layout } from '@/components/layout/Layout'
import { Skeleton } from '@/components/ui/Skeleton'

// Lazy loaded pages — route-level code splitting
const HeroPage    = lazy(() => import('@/pages/HeroPage'))
const ChatPage    = lazy(() => import('@/pages/ChatPage'))
const HistoryPage = lazy(() => import('@/pages/HistoryPage'))
const ProgressPage = lazy(() => import('@/pages/ProgressPage'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 2,
      refetchOnWindowFocus: false,
    },
  },
})

function PageLoader() {
  return (
    <div className="p-8 space-y-4">
      <Skeleton className="h-8 w-48" />
      <Skeleton className="h-4 w-full max-w-lg" />
      <Skeleton className="h-4 w-4/5 max-w-md" />
    </div>
  )
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Suspense fallback={<PageLoader />}><HeroPage /></Suspense>} />
            <Route path="/chat" element={<Suspense fallback={<PageLoader />}><ChatPage /></Suspense>} />
            <Route path="/history" element={<Suspense fallback={<PageLoader />}><HistoryPage /></Suspense>} />
            <Route path="/progress" element={<Suspense fallback={<PageLoader />}><ProgressPage /></Suspense>} />
            <Route path="*" element={
              <div className="flex flex-col items-center justify-center h-full py-20 text-center">
                <p className="font-serif text-3xl text-foreground mb-3">404</p>
                <p className="text-muted text-sm">Page not found</p>
              </div>
            } />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
