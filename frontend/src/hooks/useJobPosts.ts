import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getJobPostsByDrive, getJobPost, getMyJobPosts, createJobPost, updateJobPost, deleteJobPost, updateJobPostStatus } from '../api/jobPosts'
import toast from 'react-hot-toast'
import type { CreateJobPostRequest } from '../types/jobPost'

export function useMyJobPosts(search?: string, status?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['job-posts', 'my', search, status, page, size],
    queryFn: () => getMyJobPosts(search, status, page, size),
  })
}

export function useJobPostsByDrive(driveId: string, search?: string, status?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['job-posts', 'drive', driveId, search, status, page, size],
    queryFn: () => getJobPostsByDrive(driveId, search, status, page, size),
    enabled: !!driveId,
  })
}

export function useJobPost(id: string) {
  return useQuery({
    queryKey: ['job-posts', id],
    queryFn: () => getJobPost(id),
    enabled: !!id,
  })
}

export function useCreateJobPost() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateJobPostRequest) => createJobPost(data),
    onSuccess: () => {
      toast.success('Job post created')
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to create job post')
    },
  })
}

export function useUpdateJobPost() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<CreateJobPostRequest> }) => updateJobPost(id, data),
    onSuccess: () => {
      toast.success('Job post updated')
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to update job post')
    },
  })
}

export function useDeleteJobPost() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteJobPost(id),
    onSuccess: () => {
      toast.success('Job post deleted')
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to delete job post')
    },
  })
}

export function useUpdateJobPostStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateJobPostStatus(id, status),
    onSuccess: () => {
      toast.success('Status updated')
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to update status')
    },
  })
}
