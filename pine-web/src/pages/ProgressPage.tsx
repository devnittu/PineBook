import { useQuery } from '@tanstack/react-query'
import { motion } from 'framer-motion'
import { BarChart2, Flame, BookOpen, Target, TrendingUp } from 'lucide-react'
import { service } from '@/services/api'
import { CardSkeleton } from '@/components/ui/Skeleton'
import type { ProgressData } from '@/types'

export default function ProgressPage() {
  const { data, isLoading } = useQuery<ProgressData>({
    queryKey: ['progress'],
    queryFn: service.getProgress,
    staleTime: 60_000,
  })

  const stats = data
    ? [
        { icon: BookOpen, label: 'Paths Started', value: data.total_paths, sub: `${data.completed_paths} completed` },
        { icon: Target, label: 'Lessons Done', value: data.completed_lessons, sub: `of ${data.total_lessons} total` },
        { icon: Flame, label: 'Day Streak', value: data.streak_days, sub: data.last_active },
        { icon: TrendingUp, label: 'Completion', value: `${Math.round((data.completed_lessons / data.total_lessons) * 100)}%`, sub: 'overall progress' },
      ]
    : []

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-8">
      <div className="space-y-1">
        <h1 className="font-serif text-2xl text-foreground">Progress</h1>
        <p className="text-sm text-muted">Track your learning journey</p>
      </div>

      {isLoading && (
        <div className="grid grid-cols-2 gap-3">
          {[0,1,2,3].map(i => <CardSkeleton key={i} />)}
        </div>
      )}

      {data && (
        <>
          {/* Stats grid */}
          <div className="grid grid-cols-2 gap-3">
            {stats.map(({ icon: Icon, label, value, sub }, i) => (
              <motion.div
                key={label}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.06 }}
                className="card space-y-3"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs text-muted">{label}</span>
                  <Icon size={14} className="text-muted" />
                </div>
                <p className="text-3xl font-light text-foreground">{value}</p>
                <p className="text-xs text-muted">{sub}</p>
              </motion.div>
            ))}
          </div>

          {/* Weekly activity */}
          <div className="card space-y-4">
            <div className="flex items-center gap-2">
              <BarChart2 size={14} className="text-muted" />
              <p className="text-xs font-mono text-muted uppercase tracking-widest">Weekly Activity</p>
            </div>
            <div className="flex items-end gap-1.5 h-16">
              {['M','T','W','T','F','S','S'].map((day, i) => {
                const heights = [60, 100, 45, 80, 100, 30, 70]
                return (
                  <div key={i} className="flex-1 flex flex-col items-center gap-1">
                    <div
                      className="w-full rounded-sm bg-foreground-3 transition-all"
                      style={{ height: `${heights[i]}%`, opacity: i < 5 ? 1 : 0.35 }}
                    />
                    <span className="text-xs font-mono text-muted">{day}</span>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Overall progress */}
          <div className="card space-y-3">
            <div className="flex justify-between items-center">
              <p className="text-sm text-foreground-2">Overall completion</p>
              <span className="text-sm font-mono text-foreground-3">
                {data.completed_lessons}/{data.total_lessons}
              </span>
            </div>
            <div className="h-2 bg-surface-3 rounded-full overflow-hidden">
              <motion.div
                initial={{ width: 0 }}
                animate={{ width: `${Math.round((data.completed_lessons / data.total_lessons) * 100)}%` }}
                transition={{ duration: 0.8, ease: 'easeOut' }}
                className="h-full bg-foreground rounded-full"
              />
            </div>
          </div>
        </>
      )}
    </div>
  )
}
