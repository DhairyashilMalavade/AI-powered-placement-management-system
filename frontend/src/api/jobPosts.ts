import apiClient from './client'
import type { ApiResponse, PagedResponse } from '../types/api'
import type { CreateJobPostRequest, JobPostResponse } from '../types/jobPost'

export async function getMyJobPosts(search?: string, status?: string, page = 0, size = 20): Promise<PagedResponse<JobPostResponse>> {
  const params: Record<string, string | number> = { page, size }
  if (search) params.search = search
  if (status) params.status = status
  const res = await apiClient.get<ApiResponse<PagedResponse<JobPostResponse>>>('/job-posts/my', { params })
  return res.data.data
}

export async function getJobPostsByDrive(driveId: string, search?: string, status?: string, page = 0, size = 20): Promise<PagedResponse<JobPostResponse>> {
  const params: Record<string, string | number> = { page, size }
  if (search) params.search = search
  if (status) params.status = status
  const res = await apiClient.get<ApiResponse<PagedResponse<JobPostResponse>>>(`/job-posts/drive/${driveId}`, { params })
  return res.data.data
}

export async function getJobPost(id: string): Promise<JobPostResponse> {
  const res = await apiClient.get<ApiResponse<JobPostResponse>>(`/job-posts/${id}`)
  return res.data.data
}

export async function createJobPost(data: CreateJobPostRequest): Promise<JobPostResponse> {
  const res = await apiClient.post<ApiResponse<JobPostResponse>>('/job-posts', data)
  return res.data.data
}

export async function updateJobPost(id: string, data: Partial<CreateJobPostRequest>): Promise<JobPostResponse> {
  const res = await apiClient.put<ApiResponse<JobPostResponse>>(`/job-posts/${id}`, data)
  return res.data.data
}

export async function deleteJobPost(id: string): Promise<void> {
  await apiClient.delete(`/job-posts/${id}`)
}

export async function updateJobPostStatus(id: string, status: string): Promise<JobPostResponse> {
  const res = await apiClient.patch<ApiResponse<JobPostResponse>>(`/job-posts/${id}/status`, { status })
  return res.data.data
}
