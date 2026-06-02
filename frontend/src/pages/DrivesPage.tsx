import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { useDrives, useCreateDrive } from '../hooks/useDrives'
import Spinner from '../components/shared/Spinner'
import { useDebounce } from '../hooks/useDebounce'
import DriveCard from '../components/drives/DriveCard'
import DriveForm from '../components/drives/DriveForm'
import Pagination from '../components/shared/Pagination'
import SearchInput from '../components/shared/SearchInput'
import EmptyState from '../components/shared/EmptyState'
import type { CreateDriveRequest } from '../types/drive'

const DRIVE_STATUSES = ['DRAFT', 'ACTIVE', 'CLOSED', 'COMPLETED']

export default function DrivesPage() {
  const user = useAuthStore((s) => s.user)
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const debouncedSearch = useDebounce(search)
  const { data: drivesPage, isLoading, isError, refetch } = useDrives(debouncedSearch || undefined, statusFilter || undefined, page)
  const drives = drivesPage?.content
  const createDrive = useCreateDrive()
  const [showForm, setShowForm] = useState(false)

  const handleCreate = async (data: CreateDriveRequest) => {
    await createDrive.mutateAsync(data)
    setShowForm(false)
    setPage(0)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Drives</h1>
        {user?.role === 'PO' && (
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            {showForm ? 'Cancel' : 'Create Drive'}
          </button>
        )}
      </div>

      {showForm && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="font-semibold mb-4">New Drive</h2>
          <DriveForm onSubmit={handleCreate} loading={createDrive.isPending} />
        </div>
      )}

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <SearchInput value={search} onChange={(v) => { setSearch(v); setPage(0) }} placeholder="Search drives..." />
        <div className="flex gap-2">
          <button
            onClick={() => { setStatusFilter(''); setPage(0) }}
            className={`text-xs px-3 py-1 rounded border transition ${!statusFilter ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-gray-50'}`}
          >
            All
          </button>
          {DRIVE_STATUSES.map((s) => (
            <button
              key={s}
              onClick={() => { setStatusFilter(s); setPage(0) }}
              className={`text-xs px-3 py-1 rounded border transition ${statusFilter === s ? 'bg-blue-600 text-white border-blue-600' : 'hover:bg-gray-50'}`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {isLoading && <div className="flex justify-center py-10"><Spinner /></div>}

      {isError && (
        <div className="text-center py-10">
          <p className="text-red-600 mb-2">Failed to load drives.</p>
          <button onClick={() => refetch()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">
            Try again
          </button>
        </div>
      )}

      {!isError && drives && drives.length === 0 && !isLoading && (
        <EmptyState title="No drives found" description={search || statusFilter ? 'Try adjusting your search or filters.' : undefined} />
      )}

      <div className="space-y-3">
        {drives?.map((drive) => (
          <DriveCard key={drive.id} drive={drive} />
        ))}
      </div>

      {drivesPage && (
        <Pagination page={page} totalPages={drivesPage.totalPages} onPageChange={setPage} />
      )}
    </div>
  )
}
