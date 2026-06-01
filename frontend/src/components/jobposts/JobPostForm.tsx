import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import type { CreateJobPostRequest } from '../../types/jobPost'

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(255),
  description: z.string().min(1, 'Description is required').max(10000),
  location: z.string().max(255).optional().or(z.literal('')),
  salaryRange: z.string().max(100).optional().or(z.literal('')),
  vacancies: z.string().min(1, 'At least 1 vacancy required'),
})

type FormData = z.infer<typeof schema>

interface Props {
  driveId: string
  onSubmit: (data: CreateJobPostRequest) => void
  loading?: boolean
}

export default function JobPostForm({ driveId, onSubmit, loading }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { vacancies: '1' },
  })

  const onFormSubmit = (data: FormData) => {
    onSubmit({
      title: data.title,
      description: data.description,
      location: data.location || undefined,
      salaryRange: data.salaryRange || undefined,
      vacancies: Number(data.vacancies),
      driveId,
    })
  }

  return (
    <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-3">
      <div>
        <label className="block text-sm font-medium mb-1">Title *</label>
        <input {...register('title')} className="w-full px-3 py-2 border rounded-lg text-sm" />
        {errors.title && <p className="text-red-500 text-xs mt-1">{errors.title.message}</p>}
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Description *</label>
        <textarea {...register('description')} rows={2} className="w-full px-3 py-2 border rounded-lg text-sm" />
        {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
      </div>
      <div className="grid grid-cols-3 gap-3">
        <div>
          <label className="block text-sm font-medium mb-1">Location</label>
          <input {...register('location')} className="w-full px-3 py-2 border rounded-lg text-sm" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Salary</label>
          <input {...register('salaryRange')} placeholder="e.g. 12-15 LPA" className="w-full px-3 py-2 border rounded-lg text-sm" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Vacancies *</label>
          <input type="number" min="1" {...register('vacancies')} className="w-full px-3 py-2 border rounded-lg text-sm" />
          {errors.vacancies && <p className="text-red-500 text-xs mt-1">{errors.vacancies.message}</p>}
        </div>
      </div>
      <button type="submit" disabled={loading} className="px-3 py-1.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
        {loading ? 'Posting...' : 'Post Job'}
      </button>
    </form>
  )
}
