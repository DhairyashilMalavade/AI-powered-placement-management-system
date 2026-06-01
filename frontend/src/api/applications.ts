import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { ApplicationResponse } from '../types/application'

export async function createApplication(jobPostId: string): Promise<ApplicationResponse> {
  const res = await apiClient.post<ApiResponse<ApplicationResponse>>('/applications', { jobPostId })
  return res.data.data
}

export async function getMyApplications(): Promise<ApplicationResponse[]> {
  const res = await apiClient.get<ApiResponse<ApplicationResponse[]>>('/applications/my')
  return res.data.data
}

export async function getApplicationsByJobPost(jobPostId: string): Promise<ApplicationResponse[]> {
  const res = await apiClient.get<ApiResponse<ApplicationResponse[]>>(`/applications/job-post/${jobPostId}`)
  return res.data.data
}

export async function updateApplicationStatus(id: string, status: string): Promise<ApplicationResponse> {
  const res = await apiClient.patch<ApiResponse<ApplicationResponse>>(`/applications/${id}/status`, { status })
  return res.data.data
}
