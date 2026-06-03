import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import type { ScoreDistributionDTO } from '../types/insights'

interface ScoreDistributionChartProps {
  data: ScoreDistributionDTO[]
}

export default function ScoreDistributionChart({ data }: ScoreDistributionChartProps) {
  if (data.length === 0) {
    return <p className="text-gray-500 text-sm">No score data yet.</p>
  }

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data}>
          <XAxis dataKey="bucket" tick={{ fontSize: 12 }} />
          <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
          <Tooltip />
          <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
