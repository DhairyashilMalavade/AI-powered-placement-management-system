import type { SkillGapDTO } from '../types/insights'

interface SkillGapTableProps {
  data: SkillGapDTO[]
}

export default function SkillGapTable({ data }: SkillGapTableProps) {
  if (data.length === 0) {
    return <p className="text-gray-500 text-sm">No skill gap data.</p>
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b bg-gray-100">
            <th className="text-left px-3 py-2 font-medium">Skill</th>
            <th className="text-left px-3 py-2 font-medium">Required</th>
            <th className="text-left px-3 py-2 font-medium">Matched</th>
            <th className="text-left px-3 py-2 font-medium">Gap %</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item) => {
            const fillPct = Math.max(0, 100 - item.gapPercentage)
            const barColor = item.gapPercentage > 50 ? 'bg-red-500' : item.gapPercentage > 0 ? 'bg-orange-400' : 'bg-green-500'
            const textColor = item.gapPercentage > 50 ? 'text-red-700' : item.gapPercentage > 0 ? 'text-orange-700' : 'text-green-700'
            return (
              <tr key={item.skill} className="border-b hover:bg-gray-50 transition">
                <td className="px-3 py-2 font-medium">{item.skill}</td>
                <td className="px-3 py-2">{item.requiredCount}</td>
                <td className="px-3 py-2">{item.matchedCount}</td>
                <td className="px-3 py-2">
                  <div className="flex items-center gap-2">
                    <div className="flex-1 h-2.5 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all ${barColor}`}
                        style={{ width: `${fillPct}%` }}
                      />
                    </div>
                    <span className={`text-xs font-medium w-12 text-right ${textColor}`}>
                      {item.gapPercentage.toFixed(1)}%
                    </span>
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
