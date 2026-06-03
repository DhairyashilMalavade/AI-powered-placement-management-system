import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useRankedApplications } from '../hooks/useApplications'
import RankingsTable from '../components/RankingsTable'
import Pagination from '../components/shared/Pagination'
import Spinner from '../components/shared/Spinner'

export default function JobPostRankings() {
  const { jobPostId } = useParams<{ jobPostId: string }>()
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const { data, isLoading } = useRankedApplications(jobPostId ?? '', page)

  return (
    <div>
      <div className="flex items-center gap-4 mb-6">
        <button onClick={() => navigate(-1)} className="text-sm text-blue-600 hover:underline">
          &larr; Back
        </button>
        <h1 className="text-xl font-bold">Candidate Rankings</h1>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : !data ? (
        <p className="text-gray-500">Failed to load rankings.</p>
      ) : (
        <>
          <RankingsTable data={data.content} />
          <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  )
}
