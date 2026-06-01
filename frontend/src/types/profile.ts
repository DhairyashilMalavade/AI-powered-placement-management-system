export interface ProfileResponse {
  id: string
  userId: string
  collegeName?: string
  graduationYear?: number
  major?: string
  gpa?: number | null
  skills?: string[] | null
  resumeFilePath?: string | null
  phone?: string | null
  companyName?: string
  companyWebsite?: string | null
  companyDescription?: string | null
  department?: string | null
}

export interface UpdateStudentProfileRequest {
  collegeName: string
  graduationYear: number
  major: string
  gpa?: number | null
  skills?: string[]
  phone?: string
}

export interface UpdateRecruiterProfileRequest {
  companyName: string
  companyWebsite?: string
  companyDescription?: string
}

export interface UpdatePlacementOfficerProfileRequest {
  collegeName: string
  department?: string
}
