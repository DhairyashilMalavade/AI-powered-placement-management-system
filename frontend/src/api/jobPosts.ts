import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { CreateJobPostRequest, JobPostResponse } from '../types/jobPost'

export async function getJobPostsByDrive(driveId: string): Promise<JobPostResponse[]> {
  const res = await apiClient.get<ApiResponse<JobPostResponse[]>>(`/job-posts/drive/${driveId}`)
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
