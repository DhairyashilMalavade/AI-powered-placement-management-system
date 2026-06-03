import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import type { StatusCountDTO } from '../types/insights'

interface FunnelChartProps {
  data: StatusCountDTO[]
}

const STATUS_COLORS: Record<string, string> = {
  APPLIED: '#93c5fd',
  UNDER_REVIEW: '#60a5fa',
  SHORTLISTED: '#3b82f6',
  ACCEPTED: '#22c55e',
  REJECTED: '#ef4444',
  WITHDRAWN: '#9ca3af',
}

export default function FunnelChart({ data }: FunnelChartProps) {
  if (data.length === 0) {
    return <p className="text-gray-500 text-sm">No funnel data yet.</p>
  }

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} layout="vertical">
          <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
          <YAxis type="category" dataKey="status" tick={{ fontSize: 12 }} width={110} />
          <Tooltip />
          <Bar dataKey="count" radius={[0, 4, 4, 0]}>
            {data.map((entry) => (
              <rect key={entry.status} fill={STATUS_COLORS[entry.status] || '#3b82f6'} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
