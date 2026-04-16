import { useEffect, useRef, useState, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Send, Plus, Loader2, Bot, User } from 'lucide-react'
import { useMutation } from '@tanstack/react-query'
import { v4 as uuidv4 } from 'uuid'
import { service } from '@/services/api'
import { useStore } from '@/store/useStore'
import { LearningPathCard } from '@/features/chat/LearningPathCard'
import { VideoSegmentCard } from '@/features/chat/VideoSegmentCard'
import { ConfusionResolver } from '@/features/chat/ConfusionResolver'
import { ChatSkeleton } from '@/components/ui/Skeleton'
import type { ChatMessage, ChunkResult } from '@/types'
import { cn } from '@/utils/cn'

export default function ChatPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const { messages, addMessage, clearMessages } = useStore()
  const [input, setInput] = useState('')
  const bottomRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLTextAreaElement>(null)
  const initialQuery = params.get('q')
  const fired = useRef(false)

  const { mutate, isPending } = useMutation({
    mutationFn: (q: string) => service.query(q),
    onMutate: (q) => {
      addMessage({ id: uuidv4(), role: 'user', content: q, timestamp: new Date() })
    },
    onSuccess: (data, q) => {
      addMessage({
        id: uuidv4(), role: 'assistant',
        content: `Here's your personalised learning path for **${q}**:`,
        timestamp: new Date(), data,
      })
    },
    onError: (err) => {
      addMessage({
        id: uuidv4(), role: 'assistant',
        content: `Sorry, something went wrong: ${(err as Error).message}`,
        timestamp: new Date(),
      })
    },
  })

  // Fire initial query from URL
  useEffect(() => {
    if (initialQuery && !fired.current) {
      fired.current = true
      clearMessages()
      mutate(initialQuery)
    }
  }, [initialQuery])

  // Scroll to bottom on new message
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isPending])

  const send = useCallback(() => {
    const q = input.trim()
    if (!q || isPending) return
    setInput('')
    mutate(q)
  }, [input, isPending, mutate])

  const handleKey = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
  }

  return (
    <div className="flex h-full overflow-hidden">
      {/* ── Center column ─────────────────────────────── */}
      <div className="flex-1 flex flex-col overflow-hidden">

        {/* Messages */}
        <div className="flex-1 overflow-y-auto scrollbar-none px-4 py-6 space-y-8 max-w-3xl w-full mx-auto">
          {messages.length === 0 && !isPending && (
            <div className="flex flex-col items-center justify-center h-full text-center py-20">
              <Bot size={32} className="text-border mb-4" />
              <p className="text-foreground-3 text-sm">Start by asking what you want to learn</p>
            </div>
          )}

          <AnimatePresence initial={false}>
            {messages.map((msg) => (
              <MessageBubble key={msg.id} msg={msg} />
            ))}
          </AnimatePresence>

          {isPending && <ChatSkeleton />}
          <div ref={bottomRef} />
        </div>

        {/* Input bar */}
        <div className="border-t border-border bg-background/80 backdrop-blur-md px-4 py-3">
          <div className="max-w-3xl mx-auto flex items-end gap-3">
            <button
              onClick={() => { clearMessages(); navigate('/') }}
              className="btn-ghost p-2.5 rounded-xl shrink-0"
              title="New chat"
            >
              <Plus size={17} />
            </button>
            <div className="flex-1 relative flex items-end bg-surface-2 border border-border rounded-xl hover:border-subtle transition-colors focus-within:border-subtle">
              <textarea
                ref={inputRef}
                value={input}
                onChange={(e) => { setInput(e.target.value); e.target.style.height = 'auto'; e.target.style.height = Math.min(e.target.scrollHeight, 160) + 'px' }}
                onKeyDown={handleKey}
                placeholder="Ask a follow-up question… (Shift+Enter for new line)"
                rows={1}
                className="flex-1 bg-transparent px-4 py-3 text-sm text-foreground placeholder:text-muted outline-none resize-none max-h-40 scrollbar-none"
              />
              <button
                onClick={send}
                disabled={!input.trim() || isPending}
                className={cn(
                  'shrink-0 m-2 p-2 rounded-lg transition-all duration-150',
                  input.trim() && !isPending
                    ? 'bg-foreground text-background hover:bg-foreground-2 active:scale-95'
                    : 'bg-surface-3 text-muted cursor-not-allowed'
                )}
              >
                {isPending ? <Loader2 size={15} className="animate-spin" /> : <Send size={15} />}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Right rail (desktop) ───────────────────────── */}
      <aside className="hidden lg:flex flex-col w-80 border-l border-border overflow-y-auto scrollbar-none p-4 space-y-4 shrink-0">
        <ConfusionResolver />
        <ProgressMini />
      </aside>
    </div>
  )
}

function MessageBubble({ msg }: { msg: ChatMessage }) {
  const isUser = msg.role === 'user'
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2 }}
      className={cn('flex gap-3', isUser ? 'justify-end' : 'justify-start')}
    >
      {!isUser && (
        <div className="w-7 h-7 rounded-full bg-surface-2 border border-border flex items-center justify-center shrink-0 mt-0.5">
          <Bot size={14} className="text-foreground-3" />
        </div>
      )}

      <div className={cn('max-w-[85%] space-y-4', isUser && 'items-end flex flex-col')}>
        <div className={cn(
          'px-4 py-2.5 rounded-2xl text-sm leading-relaxed',
          isUser
            ? 'bg-foreground text-background rounded-br-sm'
            : 'bg-surface-2 text-foreground-2 rounded-bl-sm border border-border'
        )}>
          {msg.content}
        </div>

        {/* AI response data */}
        {msg.data && (
          <div className="w-full space-y-4">
            {msg.data.learning_path.length > 0 && (
              <LearningPathCard steps={msg.data.learning_path} />
            )}
            {Object.entries(msg.data.best_explanations).length > 0 && (
              <div className="space-y-2">
                <p className="text-xs font-mono text-muted uppercase tracking-widest">Best Explanations</p>
                {Object.entries(msg.data.best_explanations).map(([label, chunk]) => (
                  <VideoSegmentCard key={label} chunk={chunk} label={label} />
                ))}
              </div>
            )}
            {msg.data.confusions.length > 0 && (
              <div className="space-y-2">
                <p className="text-xs font-mono text-muted uppercase tracking-widest">Key Comparisons</p>
                {msg.data.confusions.map((c: ChunkResult, i: number) => (
                  <VideoSegmentCard key={i} chunk={c} />
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {isUser && (
        <div className="w-7 h-7 rounded-full bg-foreground flex items-center justify-center shrink-0 mt-0.5">
          <User size={13} className="text-background" />
        </div>
      )}
    </motion.div>
  )
}

function ProgressMini() {
  return (
    <div className="card space-y-3">
      <p className="text-xs font-mono text-muted uppercase tracking-widest">Your Progress</p>
      <div className="space-y-2">
        {[
          { label: 'Paths started', value: '5' },
          { label: 'Lessons done', value: '19' },
          { label: 'Day streak', value: '7 🔥' },
        ].map(({ label, value }) => (
          <div key={label} className="flex justify-between items-center">
            <span className="text-xs text-muted">{label}</span>
            <span className="text-sm font-medium text-foreground-2">{value}</span>
          </div>
        ))}
      </div>
      <div className="h-px bg-border" />
      <div className="space-y-1">
        <div className="flex justify-between text-xs">
          <span className="text-muted">Weekly goal</span>
          <span className="text-foreground-3 font-mono">4/7 days</span>
        </div>
        <div className="h-1.5 bg-surface-3 rounded-full overflow-hidden">
          <div className="h-full bg-foreground-2 rounded-full" style={{ width: '57%' }} />
        </div>
      </div>
    </div>
  )
}
