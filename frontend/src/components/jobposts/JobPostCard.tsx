import type { JobPostResponse } from '../../types/jobPost'
import JobPostStatusBadge from './JobPostStatusBadge'
import { useAuthStore } from '../../store/authStore'
import { useDeleteJobPost, useUpdateJobPostStatus } from '../../hooks/useJobPosts'
import { useCreateApplication } from '../../hooks/useApplications'
import { useQueryClient } from '@tanstack/react-query'

export default function JobPostCard({ post }: { post: JobPostResponse }) {
  const user = useAuthStore((s) => s.user)
  const qc = useQueryClient()
  const deletePost = useDeleteJobPost()
  const updateStatus = useUpdateJobPostStatus()
  const apply = useCreateApplication()
  const isOwner = post.recruiter.id === user?.id
  const isRecruiter = user?.role === 'RECRUITER'
  const isStudent = user?.role === 'STUDENT'

  const handleApply = () => {
    apply.mutate(post.id, {
      onSuccess: () => qc.invalidateQueries({ queryKey: ['applications'] }),
    })
  }

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <div className="flex items-start justify-between mb-2">
        <div>
          <h4 className="font-medium">{post.title}</h4>
          <p className="text-xs text-gray-500">
            by {post.recruiter.fullName} · {post.vacancies} vacancy/vacancies
          </p>
        </div>
        <JobPostStatusBadge status={post.status} />
      </div>
      <p className="text-sm text-gray-600 mb-2">{post.description}</p>
      {(post.location || post.salaryRange) && (
        <div className="text-xs text-gray-400 space-x-3">
          {post.location && <span>{post.location}</span>}
          {post.salaryRange && <span>{post.salaryRange}</span>}
        </div>
      )}
      <div className="flex gap-2 mt-3">
        {isStudent && post.status === 'OPEN' && (
          <button
            onClick={handleApply}
            disabled={apply.isPending}
            className="text-xs px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {apply.isPending ? 'Applying...' : 'Apply'}
          </button>
        )}
        {isOwner && isRecruiter && (
          <>
            {post.status === 'OPEN' && (
              <button
                onClick={() => updateStatus.mutate({ id: post.id, status: 'FILLED' })}
                className="text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded"
              >
                Mark Filled
              </button>
            )}
            <button
              onClick={() => deletePost.mutate(post.id)}
              className="text-xs px-2 py-1 bg-red-100 text-red-700 rounded"
            >
              Delete
            </button>
          </>
        )}
      </div>
    </div>
  )
}
