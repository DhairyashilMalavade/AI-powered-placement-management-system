import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { ProfileResponse, UpdateStudentProfileRequest, UpdateRecruiterProfileRequest, UpdatePlacementOfficerProfileRequest } from '../types/profile'

export async function getMyProfile(): Promise<ProfileResponse> {
  const res = await apiClient.get<ApiResponse<ProfileResponse>>('/profile/me')
  return res.data.data
}

export async function updateStudentProfile(data: UpdateStudentProfileRequest): Promise<ProfileResponse> {
  const res = await apiClient.put<ApiResponse<ProfileResponse>>('/profile/student', data)
  return res.data.data
}

export async function updateRecruiterProfile(data: UpdateRecruiterProfileRequest): Promise<ProfileResponse> {
  const res = await apiClient.put<ApiResponse<ProfileResponse>>('/profile/recruiter', data)
  return res.data.data
}

export async function updatePlacementOfficerProfile(data: UpdatePlacementOfficerProfileRequest): Promise<ProfileResponse> {
  const res = await apiClient.put<ApiResponse<ProfileResponse>>('/profile/po', data)
  return res.data.data
}
