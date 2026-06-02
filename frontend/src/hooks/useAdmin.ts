import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getUsers, updateUserRole, toggleUserActive, getStats } from '../api/admin'
import toast from 'react-hot-toast'

export function useUsers(role?: string, search?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['admin', 'users', role, search, page, size],
    queryFn: () => getUsers(role, search, page, size),
  })
}

export function useUpdateUserRole() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, role }: { id: string; role: string }) => updateUserRole(id, role),
    onSuccess: () => {
      toast.success('User role updated')
      qc.invalidateQueries({ queryKey: ['admin', 'users'] })
      qc.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to update role')
    },
  })
}

export function useToggleUserActive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => toggleUserActive(id),
    onSuccess: () => {
      toast.success('User status toggled')
      qc.invalidateQueries({ queryKey: ['admin', 'users'] })
      qc.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to toggle user status')
    },
  })
}

export function useStats() {
  return useQuery({
    queryKey: ['admin', 'stats'],
    queryFn: () => getStats(),
  })
}
