import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { ChevronDown, CheckCircle2, Circle, Play } from 'lucide-react'
import { formatTimestamp, youtubeUrl } from '@/utils/cn'
import type { LearningStep } from '@/types'

interface LearningPathCardProps {
  steps: LearningStep[]
}

const PHASES = [
  { label: 'Phase 01', title: 'Basics', range: [0, 1] },
  { label: 'Phase 02', title: 'Intermediate', range: [1, 3] },
  { label: 'Phase 03', title: 'Projects', range: [3, 99] },
]

export function LearningPathCard({ steps }: LearningPathCardProps) {
  const [openPhase, setOpenPhase] = useState<number>(0)

  return (
    <div className="card space-y-2">
      <p className="text-xs font-mono text-muted uppercase tracking-widest mb-3">Learning Path</p>

      {PHASES.map((phase, pi) => {
        const phaseSteps = steps.slice(phase.range[0], phase.range[1])
        if (!phaseSteps.length) return null
        const isOpen = openPhase === pi
        const done = 0
        const pct = Math.round((done / phaseSteps.length) * 100)

        return (
          <div key={pi} className="border border-border rounded-xl overflow-hidden">
            <button
              onClick={() => setOpenPhase(isOpen ? -1 : pi)}
              className="w-full flex items-center justify-between px-4 py-3 hover:bg-surface-2 transition-colors"
            >
              <div className="flex items-center gap-3">
                <span className="text-xs font-mono text-muted">{phase.label}</span>
                <span className="text-sm font-medium text-foreground-2">{phase.title}</span>
              </div>
              <div className="flex items-center gap-3">
                {/* Progress bar */}
                <div className="flex items-center gap-2">
                  <div className="w-16 h-1 bg-surface-3 rounded-full overflow-hidden">
                    <div className="h-full bg-foreground-3 rounded-full transition-all" style={{ width: `${pct}%` }} />
                  </div>
                  <span className="text-xs font-mono text-muted">{done}/{phaseSteps.length}</span>
                </div>
                <ChevronDown
                  size={14}
                  className={`text-muted transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`}
                />
              </div>
            </button>

            <AnimatePresence initial={false}>
              {isOpen && (
                <motion.div
                  initial={{ height: 0, opacity: 0 }}
                  animate={{ height: 'auto', opacity: 1 }}
                  exit={{ height: 0, opacity: 0 }}
                  transition={{ duration: 0.2 }}
                  className="overflow-hidden"
                >
                  <div className="border-t border-border divide-y divide-border">
                    {phaseSteps.map((step, si) => (
                      <a
                        key={si}
                        href={youtubeUrl(step.result.video_id, step.result.timestamp)}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-3 px-4 py-3 hover:bg-surface-2 transition-colors group"
                      >
                        <Circle size={15} className="text-border group-hover:text-muted shrink-0 transition-colors" />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm text-foreground-2 truncate capitalize">{step.topic}</p>
                          <p className="text-xs text-muted truncate">{step.result.text.slice(0, 60)}…</p>
                        </div>
                        <div className="flex items-center gap-1.5 shrink-0">
                          <Play size={11} className="text-muted" />
                          <span className="text-xs font-mono text-muted">{formatTimestamp(step.result.timestamp)}</span>
                        </div>
                      </a>
                    ))}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        )
      })}
    </div>
  )
}
