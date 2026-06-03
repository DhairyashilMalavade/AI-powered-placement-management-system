import type { ScoredApplicationResponse } from '../types/application'

interface RankingsTableProps {
  data: ScoredApplicationResponse[]
}

export default function RankingsTable({ data }: RankingsTableProps) {
  if (data.length === 0) {
    return <p className="text-gray-500 text-sm">No ranked applications yet.</p>
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b bg-gray-100">
            <th className="text-left px-3 py-2 font-medium">Rank</th>
            <th className="text-left px-3 py-2 font-medium">Student</th>
            <th className="text-left px-3 py-2 font-medium">Status</th>
            <th className="text-left px-3 py-2 font-medium">Score</th>
            <th className="text-left px-3 py-2 font-medium">Feedback</th>
            <th className="text-left px-3 py-2 font-medium">Rationale</th>
          </tr>
        </thead>
        <tbody>
          {data.map((app) => (
            <tr key={app.applicationId} className="border-b hover:bg-gray-50 transition">
              <td className="px-3 py-2">
                {app.rank !== null && app.rank > 0 ? (
                  <span className={`font-semibold ${app.rank <= 3 ? 'text-blue-600' : ''}`}>
                    #{app.rank}
                  </span>
                ) : (
                  <span className="text-gray-400">—</span>
                )}
              </td>
              <td className="px-3 py-2">{app.studentName}</td>
              <td className="px-3 py-2">
                <span className="inline-block px-2 py-0.5 text-xs rounded-full bg-gray-100">
                  {app.status}
                </span>
              </td>
              <td className="px-3 py-2">
                {app.aiScore !== null ? (
                  <span className={app.aiScore >= 60 ? 'text-green-600 font-medium' : 'text-orange-600 font-medium'}>
                    {app.aiScore}
                  </span>
                ) : (
                  <span className="text-gray-400">—</span>
                )}
              </td>
              <td className="px-3 py-2 text-gray-600 max-w-xs whitespace-normal break-words">{app.scoringFeedback || '—'}</td>
              <td className="px-3 py-2 text-gray-500 max-w-xs whitespace-normal break-words">{app.scoringRationale || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
