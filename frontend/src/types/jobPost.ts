import type { DriveResponse } from './drive'
import type { UserResponse } from './user'

export interface CreateJobPostRequest {
  driveId: string
  title: string
  description: string
  location?: string
  salaryRange?: string
  vacancies: number
}

export interface UpdateJobPostRequest {
  title?: string
  description?: string
  location?: string
  salaryRange?: string
  vacancies?: number
}

export interface JobPostResponse {
  id: string
  drive: DriveResponse
  recruiter: UserResponse
  title: string
  description: string
  location: string | null
  salaryRange: string | null
  vacancies: number
  status: 'OPEN' | 'FILLED' | 'CANCELLED'
}
