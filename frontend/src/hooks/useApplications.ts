import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getMyApplications, getApplicationsByJobPost, getApplicationsByDrive, withdrawApplication, updateApplicationStatus } from '../api/applications'
import { createApplication } from '../api/applications'
import toast from 'react-hot-toast'

export function useMyApplications(page = 0, size = 20) {
  return useQuery({
    queryKey: ['applications', 'my', page, size],
    queryFn: () => getMyApplications(page, size),
  })
}

export function useApplicationsByJobPost(jobPostId: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['applications', 'job-post', jobPostId, page, size],
    queryFn: () => getApplicationsByJobPost(jobPostId, page, size),
    enabled: !!jobPostId,
  })
}

export function useCreateApplication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (jobPostId: string) => createApplication(jobPostId),
    onSuccess: () => {
      toast.success('Application submitted')
      qc.invalidateQueries({ queryKey: ['applications'] })
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to apply')
    },
  })
}

export function useApplicationsByDrive(driveId: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['applications', 'drive', driveId, page, size],
    queryFn: () => getApplicationsByDrive(driveId, page, size),
    enabled: !!driveId,
  })
}

export function useWithdrawApplication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => withdrawApplication(id),
    onSuccess: () => {
      toast.success('Application withdrawn')
      qc.invalidateQueries({ queryKey: ['applications'] })
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Withdrawal failed')
    },
  })
}

export function useUpdateApplicationStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateApplicationStatus(id, status),
    onSuccess: () => {
      toast.success('Status updated')
      qc.invalidateQueries({ queryKey: ['applications'] })
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Status update failed')
    },
  })
}
