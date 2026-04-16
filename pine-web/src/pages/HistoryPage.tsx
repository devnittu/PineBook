import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { Clock, ArrowRight, Search } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { service } from '@/services/api'
import { ListSkeleton } from '@/components/ui/Skeleton'
import type { HistoryItem } from '@/types'

export default function HistoryPage() {
  const navigate = useNavigate()
  const { data, isLoading, isError } = useQuery<HistoryItem[]>({
    queryKey: ['history'],
    queryFn: service.getHistory,
    staleTime: 30_000,
  })

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <div className="space-y-1">
        <h1 className="font-serif text-2xl text-foreground">History</h1>
        <p className="text-sm text-muted">Your past learning sessions</p>
      </div>

      {isLoading && <ListSkeleton count={6} />}

      {isError && (
        <div className="card border-border text-center py-8">
          <p className="text-muted text-sm">Couldn't load history. Try again.</p>
        </div>
      )}

      {data && (data as HistoryItem[]).length === 0 && (
        <div className="card text-center py-12 space-y-3">
          <Clock size={28} className="text-border mx-auto" />
          <p className="text-foreground-3 text-sm">No history yet. Start learning!</p>
          <button onClick={() => navigate('/')} className="btn-outline text-sm mx-auto">
            Discover topics
          </button>
        </div>
      )}

      {data && data.length > 0 && (
        <div className="space-y-1">
          {data.map((item, i) => (
            <motion.button
              key={item.id}
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.04 }}
              onClick={() => navigate(`/chat?q=${encodeURIComponent(item.query)}`)}
              className="w-full flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-surface-2 border border-transparent hover:border-border transition-all text-left group"
            >
              <Search size={15} className="text-muted shrink-0 group-hover:text-foreground-3 transition-colors" />
              <div className="flex-1 min-w-0">
                <p className="text-sm text-foreground-2 truncate">{item.query}</p>
                <p className="text-xs text-muted">{item.created_at}</p>
              </div>
              <ArrowRight size={14} className="text-muted opacity-0 group-hover:opacity-100 transition-opacity shrink-0" />
            </motion.button>
          ))}
        </div>
      )}
    </div>
  )
}
