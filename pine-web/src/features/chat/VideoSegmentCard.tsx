import { motion } from 'framer-motion'
import { ExternalLink, Play } from 'lucide-react'
import { formatTimestamp, youtubeUrl, youtubeThumbnail, truncate } from '@/utils/cn'
import type { ChunkResult } from '@/types'

interface VideoSegmentCardProps {
  chunk: ChunkResult
  label?: string
  whySelected?: string
}

export function VideoSegmentCard({ chunk, label, whySelected }: VideoSegmentCardProps) {
  const url = youtubeUrl(chunk.video_id, chunk.timestamp)
  const thumb = youtubeThumbnail(chunk.video_id)

  return (
    <motion.a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      className="card-hover flex gap-3 group text-left w-full block"
    >
      {/* Thumbnail */}
      <div className="relative shrink-0 w-24 h-16 rounded-lg overflow-hidden bg-surface-3">
        <img
          src={thumb}
          alt=""
          className="w-full h-full object-cover opacity-80 group-hover:opacity-100 transition-opacity"
          loading="lazy"
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="w-7 h-7 rounded-full bg-background/80 flex items-center justify-center">
            <Play size={12} className="text-foreground ml-0.5" />
          </div>
        </div>
        {/* Timestamp badge */}
        <div className="absolute bottom-1 right-1 bg-background/90 rounded px-1.5 py-0.5 text-xs font-mono text-foreground">
          {formatTimestamp(chunk.timestamp)}
        </div>
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0 space-y-1">
        {label && (
          <span className="text-xs font-mono text-muted uppercase tracking-widest">{label}</span>
        )}
        <p className="text-sm text-foreground-2 leading-snug line-clamp-2">
          {truncate(chunk.text, 120)}
        </p>
        {whySelected && (
          <p className="text-xs text-muted line-clamp-1">{whySelected}</p>
        )}
        {chunk.score && (
          <div className="flex items-center gap-1">
            <div className="h-1 rounded-full bg-surface-3 w-16 overflow-hidden">
              <div
                className="h-full bg-foreground-3 rounded-full"
                style={{ width: `${Math.round(chunk.score * 100)}%` }}
              />
            </div>
            <span className="text-xs font-mono text-muted">{Math.round(chunk.score * 100)}%</span>
          </div>
        )}
      </div>

      <ExternalLink size={13} className="shrink-0 text-muted group-hover:text-foreground-3 mt-1 transition-colors" />
    </motion.a>
  )
}
