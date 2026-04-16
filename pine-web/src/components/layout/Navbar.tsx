import { useRef, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, Bell, Menu, BookOpen, X, Command } from 'lucide-react'
import { cn } from '@/utils/cn'
import { useStore } from '@/store/useStore'
import { useKeyboard, useDebounce } from '@/hooks/useDebounce'

interface NavbarProps {
  onMobileMenuOpen: () => void
}

export function Navbar({ onMobileMenuOpen }: NavbarProps) {
  const navigate = useNavigate()
  const { toggleSidebar } = useStore()
  const [searchOpen, setSearchOpen] = useState(false)
  const [searchValue, setSearchValue] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)
  const debouncedSearch = useDebounce(searchValue, 300)

  const openSearch = useCallback(() => {
    setSearchOpen(true)
    setTimeout(() => inputRef.current?.focus(), 50)
  }, [])

  useKeyboard('k', openSearch)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (debouncedSearch.trim()) {
      navigate(`/chat?q=${encodeURIComponent(debouncedSearch.trim())}`)
      setSearchOpen(false)
      setSearchValue('')
    }
  }

  return (
    <header className="sticky top-0 z-50 h-14 bg-background/80 backdrop-blur-md border-b border-border flex items-center px-4 gap-3 shrink-0">
      {/* Left */}
      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={onMobileMenuOpen}
          className="md:hidden btn-ghost p-2 rounded-lg"
        >
          <Menu size={18} />
        </button>
        <button
          onClick={toggleSidebar}
          className="hidden md:flex btn-ghost p-2 rounded-lg"
        >
          <Menu size={18} />
        </button>
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 group"
        >
          <div className="w-6 h-6 rounded-md bg-foreground flex items-center justify-center">
            <BookOpen size={13} className="text-background" />
          </div>
          <span className="font-serif text-foreground text-base hidden sm:block">PineBook</span>
        </button>
      </div>

      {/* Center — Search */}
      <form
        onSubmit={handleSubmit}
        className={cn(
          'flex-1 max-w-xl mx-auto transition-all duration-200',
          searchOpen ? 'opacity-100' : 'opacity-100'
        )}
      >
        <div className={cn(
          'flex items-center gap-2 bg-surface-2 border rounded-xl px-3 py-2 transition-all duration-150',
          searchOpen ? 'border-subtle' : 'border-border hover:border-subtle cursor-pointer'
        )}
          onClick={!searchOpen ? openSearch : undefined}
        >
          <Search size={14} className="text-muted shrink-0" />
          <input
            ref={inputRef}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onFocus={() => setSearchOpen(true)}
            onBlur={() => { if (!searchValue) setSearchOpen(false) }}
            placeholder="What do you want to learn?"
            className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted outline-none min-w-0"
          />
          {searchValue
            ? <button type="button" onClick={() => setSearchValue('')}><X size={13} className="text-muted hover:text-foreground" /></button>
            : <span className="hidden sm:flex items-center gap-1 text-xs text-muted font-mono shrink-0">
                <Command size={11} />K
              </span>
          }
        </div>
      </form>

      {/* Right */}
      <div className="flex items-center gap-1 shrink-0">
        <button className="btn-ghost p-2 rounded-lg relative">
          <Bell size={17} />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-foreground rounded-full" />
        </button>
        <button className="w-8 h-8 rounded-full bg-surface-2 border border-border flex items-center justify-center text-xs font-medium text-foreground-3 hover:border-border-hover transition-colors">
          D
        </button>
      </div>
    </header>
  )
}
