import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getMyApplications, getApplicationsByJobPost, updateApplicationStatus } from '../api/applications'
import { createApplication } from '../api/applications'

export function useMyApplications() {
  return useQuery({
    queryKey: ['applications', 'my'],
    queryFn: getMyApplications,
  })
}

export function useApplicationsByJobPost(jobPostId: string) {
  return useQuery({
    queryKey: ['applications', 'job-post', jobPostId],
    queryFn: () => getApplicationsByJobPost(jobPostId),
    enabled: !!jobPostId,
  })
}

export function useCreateApplication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (jobPostId: string) => createApplication(jobPostId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['applications'] })
    },
  })
}

export function useUpdateApplicationStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateApplicationStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['applications'] })
    },
  })
}
