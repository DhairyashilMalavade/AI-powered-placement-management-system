import { useMutation } from '@tanstack/react-query'
import { useNavigate, useLocation } from 'react-router-dom'
import { login as loginApi, register as registerApi } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'
import type { LoginRequest, RegisterRequest } from '../types/user'

function useRedirectAfterAuth() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as { from?: { pathname: string } } | null
  const from = state?.from?.pathname
  const safeFrom = from && !['/login', '/register'].includes(from) ? from : '/dashboard'
  return { navigate, to: safeFrom }
}

export function useLogin() {
  const storeLogin = useAuthStore((s) => s.login)
  const { navigate, to } = useRedirectAfterAuth()

  return useMutation({
    mutationFn: (data: LoginRequest) => loginApi(data),
    onSuccess: (response) => {
      storeLogin(response.token, response.user)
      navigate(to, { replace: true })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Login failed')
    },
  })
}

export function useRegister() {
  const storeLogin = useAuthStore((s) => s.login)
  const { navigate, to } = useRedirectAfterAuth()

  return useMutation({
    mutationFn: (data: RegisterRequest) => registerApi(data),
    onSuccess: (response) => {
      storeLogin(response.token, response.user)
      navigate(to, { replace: true })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Registration failed')
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
