import type { OverviewDTO } from '../types/analytics'

interface OverviewCardsProps {
  data: OverviewDTO | undefined
  loading: boolean
}

export default function OverviewCards({ data, loading }: OverviewCardsProps) {
  if (loading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="bg-white rounded-lg p-4 border animate-pulse h-24" />
        ))}
      </div>
    )
  }

  if (!data) return null

  const cards = [
    { label: 'Total Drives', value: data.totalDrives },
    { label: 'Total Job Posts', value: data.totalJobPosts },
    { label: 'Total Applications', value: data.totalApplications },
    { label: 'Placements', value: data.totalPlacements },
    { label: 'Avg Score', value: data.averageScore !== null ? data.averageScore?.toFixed(1) : '—' },
  ]

  return (
    <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
      {cards.map((card) => (
        <div key={card.label} className="bg-white rounded-lg p-4 border">
          <div className="text-xs text-gray-500 uppercase tracking-wide">{card.label}</div>
          <div className="text-2xl font-bold mt-1">{card.value}</div>
        </div>
      ))}
    </div>
  )
}
