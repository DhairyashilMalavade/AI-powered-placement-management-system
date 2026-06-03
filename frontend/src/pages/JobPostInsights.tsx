import { useSkillGaps, useOverview } from '../hooks/useInsights'
import ScoreDistributionChart from '../components/ScoreDistributionChart'
import FunnelChart from '../components/FunnelChart'
import SkillGapTable from '../components/SkillGapTable'
import Spinner from '../components/shared/Spinner'

export default function JobPostInsights() {
  const { data: skillGaps, isLoading: skillGapsLoading } = useSkillGaps()
  const { data: overview, isLoading: overviewLoading } = useOverview()

  const loading = skillGapsLoading || overviewLoading

  return (
    <div>
      <h1 className="text-xl font-bold mb-6">Insights</h1>

      {loading ? (
        <div className="flex justify-center py-12">
          <Spinner size="lg" />
        </div>
      ) : (
        <div className="space-y-8">
          <section>
            <h2 className="text-lg font-semibold mb-3">Score Distribution</h2>
            <div className="bg-white rounded-lg p-4 border">
              <ScoreDistributionChart data={overview?.scoreDistribution ?? []} />
            </div>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3">Application Funnel</h2>
            <div className="bg-white rounded-lg p-4 border">
              <FunnelChart data={overview?.funnel ?? []} />
            </div>
          </section>

          <section>
            <h2 className="text-lg font-semibold mb-3">Skill Gap Analysis</h2>
            <div className="bg-white rounded-lg p-4 border">
              <SkillGapTable data={skillGaps ?? []} />
            </div>
          </section>
        </div>
      )}
    </div>
  )
}
