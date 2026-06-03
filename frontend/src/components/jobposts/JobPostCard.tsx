import { useState } from 'react'
import { Link } from 'react-router-dom'
import type { JobPostResponse } from '../../types/jobPost'
import type { CreateJobPostRequest } from '../../types/jobPost'
import JobPostStatusBadge from './JobPostStatusBadge'
import JobPostForm from './JobPostForm'
import { useAuthStore } from '../../store/authStore'
import { useDeleteJobPost, useUpdateJobPostStatus, useUpdateJobPost } from '../../hooks/useJobPosts'
import { useCreateApplication } from '../../hooks/useApplications'

export default function JobPostCard({ post }: { post: JobPostResponse }) {
  const user = useAuthStore((s) => s.user)
  const [editing, setEditing] = useState(false)
  const deletePost = useDeleteJobPost()
  const updateStatus = useUpdateJobPostStatus()
  const updateJobPost = useUpdateJobPost()
  const apply = useCreateApplication()
  const isOwner = post.recruiter.id === user?.id
  const isRecruiter = user?.role === 'RECRUITER'
  const isStudent = user?.role === 'STUDENT'

  const handleApply = () => {
    apply.mutate(post.id)
  }

  const handleEdit = async (data: CreateJobPostRequest) => {
    await updateJobPost.mutateAsync({ id: post.id, data })
    setEditing(false)
  }

  if (editing) {
    return (
      <div className="bg-white rounded-lg shadow p-4">
        <h4 className="font-medium mb-3">Edit Job Post</h4>
        <JobPostForm
          defaultValues={post}
          onSubmit={handleEdit}
          loading={updateJobPost.isPending}
          submitLabel="Save Changes"
        />
        <button onClick={() => setEditing(false)} className="mt-2 text-xs text-gray-500 hover:underline">Cancel</button>
      </div>
    )
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
            <button
              onClick={() => setEditing(true)}
              className="text-xs px-2 py-1 border rounded hover:bg-gray-50"
            >
              Edit
            </button>
            {post.status === 'OPEN' && (
              <button
                onClick={() => updateStatus.mutate({ id: post.id, status: 'FILLED' })}
                disabled={updateStatus.isPending}
                className="text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded disabled:opacity-50"
              >
                {updateStatus.isPending ? 'Marking...' : 'Mark Filled'}
              </button>
            )}
            <button
              onClick={() => deletePost.mutate(post.id)}
              disabled={deletePost.isPending}
              className="text-xs px-2 py-1 bg-red-100 text-red-700 rounded disabled:opacity-50"
            >
              {deletePost.isPending ? 'Deleting...' : 'Delete'}
            </button>
            <Link
              to={`/jobs/${post.id}/rankings`}
              className="text-xs px-2 py-1 text-blue-600 hover:underline rounded"
            >
              View Rankings →
            </Link>
          </>
        )}
      </div>
    </div>
  )
}
