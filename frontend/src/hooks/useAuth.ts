import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { login as loginApi, register as registerApi } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import type { LoginRequest, RegisterRequest } from '../types/user'

export function useLogin() {
  const navigate = useNavigate()
  const storeLogin = useAuthStore((s) => s.login)

  return useMutation({
    mutationFn: (data: LoginRequest) => loginApi(data),
    onSuccess: (response) => {
      storeLogin(response.token, response.user)
      navigate('/dashboard')
    },
  })
}

export function useRegister() {
  const navigate = useNavigate()
  const storeLogin = useAuthStore((s) => s.login)

  return useMutation({
    mutationFn: (data: RegisterRequest) => registerApi(data),
    onSuccess: (response) => {
      storeLogin(response.token, response.user)
      navigate('/dashboard')
    },
  })
}

export function useLogout() {
  const navigate = useNavigate()
  const logout = useAuthStore((s) => s.logout)

  return () => {
    logout()
    navigate('/login')
  }
}
