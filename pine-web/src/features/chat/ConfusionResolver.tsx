import { useState } from 'react'
import { motion } from 'framer-motion'
import { HelpCircle, Loader2, ArrowRight, Tag } from 'lucide-react'
import { useMutation } from '@tanstack/react-query'
import { service } from '@/services/api'
import type { ConfusionResponse } from '@/types'

const QUICK = ['overfitting', 'gradient descent', 'attention mechanism', 'regularization']

export function ConfusionResolver() {
  const [concept, setConcept] = useState('')
  const [result, setResult] = useState<ConfusionResponse | null>(null)

  const { mutate, isPending } = useMutation({
    mutationFn: (c: string) => service.resolveConfusion(c),
    onSuccess: (data) => setResult(data),
  })

  const ask = (c: string) => {
    const clean = c.trim()
    if (clean) { setConcept(clean); mutate(clean) }
  }

  return (
    <div className="card space-y-4">
      <div className="flex items-center gap-2">
        <HelpCircle size={14} className="text-muted" />
        <p className="text-xs font-mono text-muted uppercase tracking-widest">Confusion Resolver</p>
      </div>

      {/* Input */}
      <div className="flex gap-2">
        <input
          value={concept}
          onChange={(e) => setConcept(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && ask(concept)}
          placeholder="What's confusing you?"
          className="input-field py-2 text-sm"
        />
        <button
          onClick={() => ask(concept)}
          disabled={isPending || !concept.trim()}
          className="btn-outline shrink-0 px-3"
        >
          {isPending ? <Loader2 size={14} className="animate-spin" /> : <ArrowRight size={14} />}
        </button>
      </div>

      {/* Quick chips */}
      <div className="flex flex-wrap gap-1.5">
        {QUICK.map((q) => (
          <button key={q} onClick={() => ask(q)} className="chip text-xs">
            {q}
          </button>
        ))}
      </div>

      {/* Result */}
      {result && (
        <motion.div
          initial={{ opacity: 0, y: 4 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-3 pt-2 border-t border-border"
        >
          <p className="text-xs font-mono text-muted capitalize">{result.concept}</p>
          <p className="text-sm text-foreground-2 leading-relaxed">{result.explanation}</p>
          {result.analogy && (
            <div className="bg-surface-2 border border-border rounded-lg px-3 py-2">
              <p className="text-xs text-muted mb-1">Analogy</p>
              <p className="text-sm text-foreground-3">{result.analogy}</p>
            </div>
          )}
          {result.related.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {result.related.map((r) => (
                <button key={r} onClick={() => ask(r)} className="chip">
                  <Tag size={10} />{r}
                </button>
              ))}
            </div>
          )}
        </motion.div>
      )}
    </div>
  )
}
