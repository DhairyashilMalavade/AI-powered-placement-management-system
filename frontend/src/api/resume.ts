import apiClient from './client'

export async function uploadResume(file: File): Promise<void> {
  const formData = new FormData()
  formData.append('file', file)
  await apiClient.post('/resumes/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export async function downloadResumeAsBlob(applicationId: string): Promise<Blob> {
  const res = await apiClient.get(`/applications/${applicationId}/resume`, {
    responseType: 'blob',
  })
  return res.data
}
