import { useState } from 'react'
import { useUsers, useUpdateUserRole, useToggleUserActive } from '../hooks/useAdmin'
import { useAuthStore } from '../store/authStore'
import { useDebounce } from '../hooks/useDebounce'
import Spinner from '../components/shared/Spinner'
import Pagination from '../components/shared/Pagination'
import SearchInput from '../components/shared/SearchInput'
import EmptyState from '../components/shared/EmptyState'

const ROLES = ['STUDENT', 'RECRUITER', 'PO', 'ADMIN']

export default function AdminUsersPage() {
  const currentUser = useAuthStore((s) => s.user)
  const [page, setPage] = useState(0)
  const [roleFilter, setRoleFilter] = useState<string>('')
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search)
  const { data: usersPage, isLoading, isError, refetch } = useUsers(roleFilter || undefined, debouncedSearch || undefined, page)
  const users = usersPage?.content
  const updateRole = useUpdateUserRole()
  const toggleActive = useToggleUserActive()

  const handleRoleChange = async (userId: string, userName: string, newRole: string) => {
    if (!confirm(`Change ${userName}'s role to ${newRole}?`)) return
    await updateRole.mutateAsync({ id: userId, role: newRole })
  }

  const handleToggleActive = async (userId: string, userName: string, isCurrentlyActive: boolean) => {
    if (userId === currentUser?.id) {
      if (!confirm(`You are about to deactivate YOUR account (${currentUser.email}). You will lose access. Continue?`)) return
    } else {
      const action = isCurrentlyActive ? 'deactivate' : 'activate'
      if (!confirm(`${action.charAt(0).toUpperCase() + action.slice(1)} ${userName}?`)) return
    }
    await toggleActive.mutateAsync(userId)
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">User Management</h1>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <SearchInput
          value={search}
          onChange={(v) => { setSearch(v); setPage(0) }}
          placeholder="Search by name or email..."
        />
        <div className="flex gap-2">
          <button
            onClick={() => { setRoleFilter(''); setPage(0) }}
            className={`text-xs px-3 py-1 rounded border transition ${!roleFilter ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-gray-50'}`}
          >
            All
          </button>
          {ROLES.map((r) => (
            <button
              key={r}
              onClick={() => { setRoleFilter(r); setPage(0) }}
              className={`text-xs px-3 py-1 rounded border transition ${roleFilter === r ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-gray-50'}`}
            >
              {r}
            </button>
          ))}
        </div>
      </div>

      {isLoading && <div className="flex justify-center py-10"><Spinner /></div>}

      {isError && (
        <div className="text-center py-10">
          <p className="text-red-600 mb-2">Failed to load users.</p>
          <button onClick={() => refetch()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">
            Try again
          </button>
        </div>
      )}

      {!isError && users && users.length === 0 && (
        <EmptyState title="No users found" description="Try adjusting your search or filters." />
      )}

      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b">
              <th className="text-left px-4 py-3 font-medium text-gray-600">Name</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Email</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Role</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users?.map((u) => (
              <tr key={u.id} className="border-b last:border-0 hover:bg-gray-50">
                <td className="px-4 py-3">{u.fullName}</td>
                <td className="px-4 py-3 text-gray-500">{u.email}</td>
                <td className="px-4 py-3">
                  <select
                    value={u.role}
                    onChange={(e) => handleRoleChange(u.id, u.fullName, e.target.value)}
                    disabled={updateRole.isPending}
                    className="text-xs border rounded px-2 py-1"
                  >
                    {ROLES.map((r) => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </select>
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => handleToggleActive(u.id, u.fullName, u.active)}
                    disabled={toggleActive.isPending}
                    className={`text-xs px-2 py-1 rounded border transition disabled:opacity-50 ${
                      u.active
                        ? 'border-red-300 text-red-600 hover:bg-red-50'
                        : 'border-green-300 text-green-600 hover:bg-green-50'
                    }`}
                  >
                    {u.active ? 'Deactivate' : 'Activate'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {usersPage && (
        <Pagination page={page} totalPages={usersPage.totalPages} onPageChange={setPage} />
      )}
    </div>
  )
}
