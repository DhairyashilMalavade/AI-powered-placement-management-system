import { useParams, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useDrive, useUpdateDrive, useDeleteDrive, useUpdateDriveStatus } from '../hooks/useDrives'
import { useJobPostsByDrive, useCreateJobPost } from '../hooks/useJobPosts'
import DriveStatusBadge from '../components/drives/DriveStatusBadge'
import JobPostCard from '../components/jobposts/JobPostCard'
import JobPostForm from '../components/jobposts/JobPostForm'
import { useState } from 'react'
import DriveForm from '../components/drives/DriveForm'
import type { CreateDriveRequest, DriveResponse } from '../types/drive'
import type { CreateJobPostRequest } from '../types/jobPost'

function toCreateRequest(drive: DriveResponse): Partial<CreateDriveRequest> {
  return {
    title: drive.title,
    description: drive.description ?? undefined,
    minGpa: drive.minGpa,
    allowedGraduationYears: drive.allowedGraduationYears ?? undefined,
    requiredSkills: drive.requiredSkills ?? undefined,
    additionalCriteria: drive.additionalCriteria ?? undefined,
    applicationDeadline: drive.applicationDeadline,
    driveDate: drive.driveDate,
  }
}

export default function DriveDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const { data: drive, isLoading: driveLoading } = useDrive(id!)
  const { data: jobPosts, isLoading: postsLoading } = useJobPostsByDrive(id!)
  const updateDrive = useUpdateDrive()
  const deleteDrive = useDeleteDrive()
  const updateStatus = useUpdateDriveStatus()
  const createJobPost = useCreateJobPost()
  const [editing, setEditing] = useState(false)
  const [showJobForm, setShowJobForm] = useState(false)

  if (driveLoading) return <p>Loading...</p>
  if (!drive) return <p>Drive not found.</p>

  const isOwner = drive.createdBy.id === user?.id
  const isPO = user?.role === 'PO'
  const isRecruiter = user?.role === 'RECRUITER'

  const handleDelete = async () => {
    if (!confirm('Delete this drive? This will also remove all associated job posts and applications.')) return
    await deleteDrive.mutateAsync(drive.id)
    navigate('/drives')
  }

  const handleStatusChange = (status: string) => {
    updateStatus.mutate({ id: drive.id, status })
  }

  const handleUpdate = async (data: CreateDriveRequest) => {
    await updateDrive.mutateAsync({ id: drive.id, data })
    setEditing(false)
  }

  const handleCreateJob = async (data: CreateJobPostRequest) => {
    await createJobPost.mutateAsync(data)
    setShowJobForm(false)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">{drive.title}</h1>
          <p className="text-gray-500 text-sm">
            Created by {drive.createdBy.fullName} · {new Date(drive.createdAt).toLocaleDateString()}
          </p>
        </div>
        <DriveStatusBadge status={drive.status} />
      </div>

      {isOwner && isPO && (
        <div className="flex gap-2 mb-4">
          <button onClick={() => setEditing(!editing)} className="px-3 py-1 text-sm border rounded hover:bg-gray-50">
            {editing ? 'Cancel' : 'Edit'}
          </button>
          {drive.status === 'DRAFT' && (
            <button onClick={() => handleStatusChange('ACTIVE')} className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700">
              Activate
            </button>
          )}
          {drive.status === 'ACTIVE' && (
            <button onClick={() => handleStatusChange('CLOSED')} className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700">
              Close
            </button>
          )}
          <button onClick={handleDelete} className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200">
            Delete
          </button>
        </div>
      )}

      {editing && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="font-semibold mb-4">Edit Drive</h2>
          <DriveForm defaultValues={toCreateRequest(drive)} onSubmit={handleUpdate} loading={updateDrive.isPending} />
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-6 mb-6 space-y-4">
        {drive.description && (
          <div>
            <h3 className="text-sm font-medium text-gray-500">Description</h3>
            <p>{drive.description}</p>
          </div>
        )}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Application Deadline</h3>
            <p>{new Date(drive.applicationDeadline).toLocaleString()}</p>
          </div>
          {drive.driveDate && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Drive Date</h3>
              <p>{new Date(drive.driveDate).toLocaleString()}</p>
            </div>
          )}
          {drive.minGpa != null && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Min GPA</h3>
              <p>{drive.minGpa}</p>
            </div>
          )}
          {drive.allowedGraduationYears && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Graduation Years</h3>
              <p>{drive.allowedGraduationYears.join(', ')}</p>
            </div>
          )}
        </div>
        {drive.requiredSkills && drive.requiredSkills.length > 0 && (
          <div>
            <h3 className="text-sm font-medium text-gray-500">Required Skills</h3>
            <div className="flex gap-2 mt-1">
              {drive.requiredSkills.map((s) => (
                <span key={s} className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded text-xs">{s}</span>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-xl font-semibold">Job Posts</h2>
        {isRecruiter && drive.status === 'ACTIVE' && (
          <button onClick={() => setShowJobForm(!showJobForm)} className="px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700">
            {showJobForm ? 'Cancel' : 'Post a Job'}
          </button>
        )}
      </div>

      {showJobForm && (
        <div className="bg-white rounded-lg shadow p-4 mb-4">
          <h3 className="font-medium mb-3">New Job Post</h3>
          <JobPostForm driveId={drive.id} onSubmit={handleCreateJob} loading={createJobPost.isPending} />
        </div>
      )}

      {postsLoading && <p className="text-gray-500 text-sm">Loading job posts...</p>}

      {jobPosts && jobPosts.length === 0 && (
        <p className="text-gray-500 text-sm">No job posts yet.</p>
      )}

      <div className="space-y-3">
        {jobPosts?.map((post) => (
          <JobPostCard key={post.id} post={post} />
        ))}
      </div>
    </div>
  )
}
