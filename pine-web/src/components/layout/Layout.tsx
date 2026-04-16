import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { X } from 'lucide-react'
import { Navbar } from './Navbar'
import { Sidebar } from './Sidebar'
import { useNavigate, useLocation } from 'react-router-dom'
import { BookOpen, Compass, Clock, Bookmark, BarChart2, Settings, Hash } from 'lucide-react'
import { cn } from '@/utils/cn'

const mobileLinks = [
  { icon: Compass, label: 'Discover', to: '/' },
  { icon: Clock, label: 'History', to: '/history' },
  { icon: Bookmark, label: 'Saved', to: '/saved' },
  { icon: Hash, label: 'Topics', to: '/topics' },
  { icon: BarChart2, label: 'Progress', to: '/progress' },
  { icon: Settings, label: 'Settings', to: '/settings' },
]

export function Layout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const navigate = useNavigate()
  const { pathname } = useLocation()

  return (
    <div className="flex flex-col h-screen overflow-hidden bg-background">
      <Navbar onMobileMenuOpen={() => setMobileOpen(true)} />

      <div className="flex flex-1 overflow-hidden">
        <Sidebar />

        {/* Page content */}
        <main className="flex-1 overflow-y-auto scrollbar-none">
          <Outlet />
        </main>
      </div>

      {/* Mobile drawer */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/60 z-40 md:hidden"
              onClick={() => setMobileOpen(false)}
            />
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 250 }}
              className="fixed inset-y-0 left-0 w-72 bg-surface border-r border-border z-50 md:hidden flex flex-col"
            >
              <div className="flex items-center justify-between px-4 h-14 border-b border-border">
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-md bg-foreground flex items-center justify-center">
                    <BookOpen size={13} className="text-background" />
                  </div>
                  <span className="font-serif text-foreground text-base">PineBook</span>
                </div>
                <button onClick={() => setMobileOpen(false)} className="btn-ghost p-1.5 rounded-lg">
                  <X size={16} />
                </button>
              </div>

              <nav className="flex-1 p-3 space-y-1 overflow-y-auto">
                {mobileLinks.map(({ icon: Icon, label, to }) => (
                  <button
                    key={to}
                    onClick={() => { navigate(to); setMobileOpen(false) }}
                    className={cn('sidebar-link w-full text-base py-3', pathname === to && 'active')}
                  >
                    <Icon size={18} />
                    <span>{label}</span>
                  </button>
                ))}
              </nav>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}
