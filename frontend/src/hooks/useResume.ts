import { useMutation, useQueryClient } from '@tanstack/react-query'
import { uploadResume, downloadResumeAsBlob } from '../api/resume'
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

export async function downloadResume(applicationId: string, filename: string) {
  try {
    const blob = await downloadResumeAsBlob(applicationId)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch {
    toast.error('Failed to download resume')
  }
}
