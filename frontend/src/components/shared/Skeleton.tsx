interface SkeletonProps {
  className?: string
  lines?: number
  width?: string
}

export default function Skeleton({ className = '', lines = 1, width }: SkeletonProps) {
  if (lines > 1) {
    return (
      <div className="space-y-2" role="status" aria-label="Loading">
        {Array.from({ length: lines }).map((_, i) => (
          <div key={i} className={`h-4 bg-gray-200 rounded animate-pulse ${width ? '' : i === lines - 1 ? 'w-3/4' : 'w-full'} ${className}`} style={width ? { width } : undefined} />
        ))}
      </div>
    )
  }
  return <div className={`h-4 bg-gray-200 rounded animate-pulse ${className}`} role="status" aria-label="Loading" style={width ? { width } : undefined} />
}
