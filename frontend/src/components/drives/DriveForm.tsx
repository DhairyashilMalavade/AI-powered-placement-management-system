import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import type { CreateDriveRequest } from '../../types/drive'

const driveSchema = z.object({
  title: z.string().min(1, 'Title is required').max(255),
  description: z.string().max(10000).optional().or(z.literal('')),
  minGpa: z.string().optional().or(z.literal('')),
  applicationDeadline: z.string().min(1, 'Deadline is required'),
  driveDate: z.string().optional().or(z.literal('')),
  additionalCriteria: z.string().max(10000).optional().or(z.literal('')),
  allowedGraduationYears: z.string().optional().or(z.literal('')),
  requiredSkills: z.string().optional().or(z.literal('')),
})

type DriveFormData = z.infer<typeof driveSchema>

interface Props {
  defaultValues?: Partial<CreateDriveRequest>
  onSubmit: (data: CreateDriveRequest) => void
  loading?: boolean
}

function toLocalDateTime(iso?: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 16)
}

export default function DriveForm({ defaultValues, onSubmit, loading }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DriveFormData>({
    resolver: zodResolver(driveSchema),
    defaultValues: {
      title: defaultValues?.title ?? '',
      description: defaultValues?.description ?? '',
      minGpa: defaultValues?.minGpa != null ? String(defaultValues.minGpa) : '',
      applicationDeadline: toLocalDateTime(defaultValues?.applicationDeadline),
      driveDate: toLocalDateTime(defaultValues?.driveDate),
      additionalCriteria: defaultValues?.additionalCriteria ?? '',
      allowedGraduationYears: defaultValues?.allowedGraduationYears?.join(', ') ?? '',
      requiredSkills: defaultValues?.requiredSkills?.join(', ') ?? '',
    },
  })

  const onFormSubmit = (data: DriveFormData) => {
    onSubmit({
      title: data.title,
      description: data.description || undefined,
      minGpa: data.minGpa ? Number(data.minGpa) : null,
      applicationDeadline: new Date(data.applicationDeadline).toISOString(),
      driveDate: data.driveDate ? new Date(data.driveDate).toISOString() : null,
      additionalCriteria: data.additionalCriteria || undefined,
      allowedGraduationYears: data.allowedGraduationYears
        ? data.allowedGraduationYears.split(',').map((s) => s.trim()).filter(Boolean).map(Number).filter((n) => !isNaN(n))
        : undefined,
      requiredSkills: data.requiredSkills
        ? data.requiredSkills.split(',').map((s) => s.trim()).filter(Boolean)
        : undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4">
      <div>
        <label htmlFor="title" className="block text-sm font-medium mb-1">Title *</label>
        <input id="title" {...register('title')} className="w-full px-3 py-2 border rounded-lg" />
        {errors.title && <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>}
      </div>
      <div>
        <label htmlFor="description" className="block text-sm font-medium mb-1">Description</label>
        <textarea id="description" {...register('description')} rows={3} className="w-full px-3 py-2 border rounded-lg" />
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label htmlFor="applicationDeadline" className="block text-sm font-medium mb-1">Application Deadline *</label>
          <input id="applicationDeadline" type="datetime-local" {...register('applicationDeadline')} className="w-full px-3 py-2 border rounded-lg" />
          {errors.applicationDeadline && <p className="text-red-500 text-sm mt-1">{errors.applicationDeadline.message}</p>}
        </div>
        <div>
          <label htmlFor="driveDate" className="block text-sm font-medium mb-1">Drive Date</label>
          <input id="driveDate" type="datetime-local" {...register('driveDate')} className="w-full px-3 py-2 border rounded-lg" />
        </div>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label htmlFor="minGpa" className="block text-sm font-medium mb-1">Min GPA</label>
          <input id="minGpa" type="number" step="0.1" min="0" max="10" {...register('minGpa')} className="w-full px-3 py-2 border rounded-lg" />
        </div>
        <div>
          <label htmlFor="allowedGraduationYears" className="block text-sm font-medium mb-1">Graduation Years (comma-separated)</label>
          <input id="allowedGraduationYears" {...register('allowedGraduationYears')} placeholder="2026, 2027" className="w-full px-3 py-2 border rounded-lg" />
        </div>
      </div>
      <div>
        <label htmlFor="requiredSkills" className="block text-sm font-medium mb-1">Required Skills (comma-separated)</label>
        <input id="requiredSkills" {...register('requiredSkills')} placeholder="Java, Python" className="w-full px-3 py-2 border rounded-lg" />
      </div>
      <div>
        <label htmlFor="additionalCriteria" className="block text-sm font-medium mb-1">Additional Criteria</label>
        <textarea id="additionalCriteria" {...register('additionalCriteria')} rows={2} className="w-full px-3 py-2 border rounded-lg" />
      </div>
      <button type="submit" disabled={loading} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition">
        {loading ? 'Saving...' : 'Save Drive'}
      </button>
    </form>
  )
}
