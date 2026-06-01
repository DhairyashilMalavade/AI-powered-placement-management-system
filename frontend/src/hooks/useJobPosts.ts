import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getJobPostsByDrive, getJobPost, getMyJobPosts, createJobPost, updateJobPost, deleteJobPost, updateJobPostStatus } from '../api/jobPosts'
import type { CreateJobPostRequest } from '../types/jobPost'

export function useMyJobPosts() {
  return useQuery({
    queryKey: ['job-posts', 'my'],
    queryFn: getMyJobPosts,
  })
}

export function useJobPostsByDrive(driveId: string) {
  return useQuery({
    queryKey: ['job-posts', 'drive', driveId],
    queryFn: () => getJobPostsByDrive(driveId),
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
    onSuccess: () => qc.invalidateQueries({ queryKey: ['job-posts'] }),
  })
}

export function useUpdateJobPost() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<CreateJobPostRequest> }) => updateJobPost(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['job-posts'] }),
  })
}

export function useDeleteJobPost() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteJobPost(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['job-posts'] }),
  })
}

export function useUpdateJobPostStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateJobPostStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['job-posts'] }),
  })
}
