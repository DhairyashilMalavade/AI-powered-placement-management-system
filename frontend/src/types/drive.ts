import type { UserResponse } from './user'

export interface CreateDriveRequest {
  title: string
  description?: string
  minGpa?: number | null
  allowedGraduationYears?: number[]
  requiredSkills?: string[]
  additionalCriteria?: string
  applicationDeadline: string
  driveDate?: string | null
}

export interface UpdateDriveRequest {
  title?: string
  description?: string
  minGpa?: number | null
  allowedGraduationYears?: number[]
  requiredSkills?: string[]
  additionalCriteria?: string
  applicationDeadline?: string
  driveDate?: string | null
}

export interface DriveResponse {
  id: string
  title: string
  description: string | null
  minGpa: number | null
  allowedGraduationYears: number[] | null
  requiredSkills: string[] | null
  additionalCriteria: string | null
  applicationDeadline: string
  driveDate: string | null
  status: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'COMPLETED'
  createdBy: UserResponse
  createdAt: string
  updatedAt: string
}
