import { useNavigate, useLocation } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import {
  Compass, Clock, Bookmark, BarChart2, Settings,
  ChevronLeft, ChevronRight, BookOpen, Hash
} from 'lucide-react'
import { cn } from '@/utils/cn'
import { useStore } from '@/store/useStore'

const links = [
  { icon: Compass, label: 'Discover', to: '/' },
  { icon: Clock, label: 'History', to: '/history' },
  { icon: Bookmark, label: 'Saved Paths', to: '/saved' },
  { icon: Hash, label: 'Topics', to: '/topics' },
  { icon: BarChart2, label: 'Progress', to: '/progress' },
]

const bottom = [
  { icon: Settings, label: 'Settings', to: '/settings' },
]

export function Sidebar() {
  const { sidebarOpen, setSidebarOpen } = useStore()
  const navigate = useNavigate()
  const { pathname } = useLocation()

  return (
    <AnimatePresence initial={false}>
      <motion.aside
        animate={{ width: sidebarOpen ? 220 : 56 }}
        transition={{ duration: 0.2, ease: 'easeInOut' }}
        className="hidden md:flex flex-col h-full bg-surface border-r border-border shrink-0 overflow-hidden"
      >
        {/* Logo */}
        <div className="flex items-center gap-2.5 px-4 h-14 border-b border-border shrink-0">
          <div className="w-6 h-6 rounded-md bg-foreground flex items-center justify-center shrink-0">
            <BookOpen size={13} className="text-background" />
          </div>
          {sidebarOpen && (
            <motion.span
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="font-serif text-foreground text-base font-normal tracking-tight"
            >
              PineBook
            </motion.span>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 p-2 space-y-0.5 overflow-y-auto scrollbar-none">
          {links.map(({ icon: Icon, label, to }) => {
            const active = pathname === to
            return (
              <button
                key={to}
                onClick={() => navigate(to)}
                className={cn(
                  'sidebar-link w-full',
                  active && 'active',
                  !sidebarOpen && 'justify-center px-0'
                )}
                title={!sidebarOpen ? label : undefined}
              >
                <Icon size={16} className="shrink-0" />
                {sidebarOpen && <span className="truncate">{label}</span>}
              </button>
            )
          })}
        </nav>

        {/* Bottom */}
        <div className="p-2 border-t border-border space-y-0.5">
          {bottom.map(({ icon: Icon, label, to }) => (
            <button
              key={to}
              onClick={() => navigate(to)}
              className={cn('sidebar-link w-full', !sidebarOpen && 'justify-center px-0')}
              title={!sidebarOpen ? label : undefined}
            >
              <Icon size={16} className="shrink-0" />
              {sidebarOpen && <span className="truncate">{label}</span>}
            </button>
          ))}

          {/* Collapse toggle */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className={cn('sidebar-link w-full', !sidebarOpen && 'justify-center px-0')}
          >
            {sidebarOpen
              ? <><ChevronLeft size={16} /><span>Collapse</span></>
              : <ChevronRight size={16} />}
          </button>
        </div>
      </motion.aside>
    </AnimatePresence>
  )
}
