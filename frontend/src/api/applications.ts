import apiClient from './client'
import type { ApiResponse, PagedResponse } from '../types/api'
import type { ApplicationResponse } from '../types/application'

export async function createApplication(jobPostId: string): Promise<ApplicationResponse> {
  const res = await apiClient.post<ApiResponse<ApplicationResponse>>('/applications', { jobPostId })
  return res.data.data
}

export async function getMyApplications(page = 0, size = 20): Promise<PagedResponse<ApplicationResponse>> {
  const res = await apiClient.get<ApiResponse<PagedResponse<ApplicationResponse>>>('/applications/my', { params: { page, size } })
  return res.data.data
}

export async function getApplicationsByJobPost(jobPostId: string, page = 0, size = 20): Promise<PagedResponse<ApplicationResponse>> {
  const res = await apiClient.get<ApiResponse<PagedResponse<ApplicationResponse>>>(`/applications/job-post/${jobPostId}`, { params: { page, size } })
  return res.data.data
}

export async function getApplicationsByDrive(driveId: string, page = 0, size = 20): Promise<PagedResponse<ApplicationResponse>> {
  const res = await apiClient.get<ApiResponse<PagedResponse<ApplicationResponse>>>(`/applications/drive/${driveId}`, { params: { page, size } })
  return res.data.data
}

export async function withdrawApplication(id: string): Promise<ApplicationResponse> {
  const res = await apiClient.patch<ApiResponse<ApplicationResponse>>(`/applications/${id}/withdraw`)
  return res.data.data
}

export async function updateApplicationStatus(id: string, status: string): Promise<ApplicationResponse> {
  const res = await apiClient.patch<ApiResponse<ApplicationResponse>>(`/applications/${id}/status`, { status })
  return res.data.data
}
