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
          {data.map((item) => (
            <tr key={item.skill} className="border-b hover:bg-gray-50 transition">
              <td className="px-3 py-2 font-medium">{item.skill}</td>
              <td className="px-3 py-2">{item.requiredCount}</td>
              <td className="px-3 py-2">{item.matchedCount}</td>
              <td className="px-3 py-2">
                <span className={item.gapPercentage > 50 ? 'text-red-600 font-medium' : item.gapPercentage > 0 ? 'text-orange-600' : 'text-green-600'}>
                  {item.gapPercentage.toFixed(1)}%
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
