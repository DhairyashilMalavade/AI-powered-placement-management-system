export interface UserResponse {
  id: string
  email: string
  fullName: string
  role: 'STUDENT' | 'PO' | 'RECRUITER' | 'ADMIN'
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  fullName: string
  role: 'STUDENT' | 'PO' | 'RECRUITER' | 'ADMIN'
}

export interface AuthResponse {
  token: string
  user: UserResponse
}
