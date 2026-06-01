import { useMutation, useQueryClient } from '@tanstack/react-query'
import { uploadResume } from '../api/resume'
import toast from 'react-hot-toast'

export function useUploadResume() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (file: File) => uploadResume(file),
    onSuccess: () => {
      toast.success('Resume uploaded')
      qc.invalidateQueries({ queryKey: ['profile'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Upload failed')
    },
  })
}
