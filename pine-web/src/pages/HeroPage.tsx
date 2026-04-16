import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, TrendingUp, Zap, BookOpen, Code2, Brain, FlaskConical } from 'lucide-react'
import { cn } from '@/utils/cn'

const suggestions = [
  'Learn Machine Learning',
  'Master React from scratch',
  'Understand System Design',
  'Python for Data Science',
]

const trending = [
  { icon: Brain, label: 'Neural Networks', count: '2.4k learners' },
  { icon: Code2, label: 'TypeScript', count: '1.8k learners' },
  { icon: FlaskConical, label: 'LLM Fine-tuning', count: '1.2k learners' },
  { icon: BookOpen, label: 'DSA Patterns', count: '980 learners' },
  { icon: Zap, label: 'FastAPI', count: '730 learners' },
  { icon: TrendingUp, label: 'Data Engineering', count: '620 learners' },
]

export default function HeroPage() {
  const [value, setValue] = useState('')
  const navigate = useNavigate()
  const inputRef = useRef<HTMLInputElement>(null)

  const submit = (q: string) => {
    const clean = q.trim()
    if (clean) navigate(`/chat?q=${encodeURIComponent(clean)}`)
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-full px-4 py-20">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: 'easeOut' }}
        className="w-full max-w-2xl space-y-10"
      >
        {/* Heading */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center gap-2 chip mb-6">
            <Zap size={11} />
            <span>AI-powered learning paths</span>
          </div>
          <h1 className="font-serif text-4xl sm:text-5xl text-foreground leading-tight text-balance">
            What do you want to learn today?
          </h1>
          <p className="text-foreground-3 text-base max-w-md mx-auto">
            Type any topic. PineBook finds the best YouTube explanations, builds your roadmap, and guides you from zero to fluent.
          </p>
        </div>

        {/* Input */}
        <div className="space-y-3">
          <div className="relative flex items-center">
            <input
              ref={inputRef}
              value={value}
              onChange={(e) => setValue(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); submit(value) }
              }}
              placeholder="e.g. Learn Machine Learning..."
              className="input-field pr-14 text-base"
              autoFocus
            />
            <button
              onClick={() => submit(value)}
              disabled={!value.trim()}
              className={cn(
                'absolute right-2 flex items-center justify-center w-9 h-9 rounded-lg transition-all duration-150',
                value.trim()
                  ? 'bg-foreground text-background hover:bg-foreground-2 active:scale-95'
                  : 'bg-surface-3 text-muted cursor-not-allowed'
              )}
            >
              <ArrowRight size={16} />
            </button>
          </div>

          {/* Suggestion chips */}
          <div className="flex flex-wrap gap-2">
            {suggestions.map((s) => (
              <button
                key={s}
                onClick={() => submit(s)}
                className="chip text-xs"
              >
                {s}
              </button>
            ))}
          </div>
        </div>

        {/* Trending */}
        <div className="space-y-4">
          <p className="text-xs font-mono text-muted uppercase tracking-widest">
            Trending topics
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
            {trending.map(({ icon: Icon, label, count }) => (
              <button
                key={label}
                onClick={() => submit(`Learn ${label}`)}
                className="card-hover flex items-start gap-3 text-left group"
              >
                <div className="p-1.5 bg-surface-3 rounded-lg group-hover:bg-surface-2 transition-colors mt-0.5">
                  <Icon size={14} className="text-foreground-3" />
                </div>
                <div className="min-w-0">
                  <p className="text-sm text-foreground-2 font-medium truncate">{label}</p>
                  <p className="text-xs text-muted truncate">{count}</p>
                </div>
              </button>
            ))}
          </div>
        </div>
      </motion.div>
    </div>
  )
}
