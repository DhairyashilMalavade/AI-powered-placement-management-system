import apiClient from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types/user'
import type { ApiResponse } from '../types/api'

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data)
  return res.data.data
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data)
  return res.data.data
}

export async function getMe(): Promise<import('../types/user').UserResponse> {
  const res = await apiClient.get<ApiResponse<import('../types/user').UserResponse>>('/auth/me')
  return res.data.data
}
