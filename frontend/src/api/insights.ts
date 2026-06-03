import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { SkillGapDTO, JobPostOverviewDTO } from '../types/insights'

export async function getSkillGaps(): Promise<SkillGapDTO[]> {
  const res = await apiClient.get<ApiResponse<SkillGapDTO[]>>('/insights/skill-gaps')
  return res.data.data
}

export async function getOverview(): Promise<JobPostOverviewDTO> {
  const res = await apiClient.get<ApiResponse<JobPostOverviewDTO>>('/insights/overview')
  return res.data.data
}
