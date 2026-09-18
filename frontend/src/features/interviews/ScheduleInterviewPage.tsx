import { Alert, Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { applicationApi } from '../applications/api';
import { QueryState } from '../../shared/components/QueryState';
import { interviewApi } from './api';
import { emptyInterviewForm, InterviewForm } from './InterviewForm';

export function ScheduleInterviewPage() {
  const { applicationId: rawId } = useParams();
  const applicationId = Number(rawId);
  const validId = Number.isSafeInteger(applicationId) && applicationId > 0;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const application = useQuery({ queryKey: ['application', applicationId], queryFn: () => applicationApi.get(applicationId), enabled: validId });
  const schedule = useMutation({
    mutationFn: (details: Parameters<typeof interviewApi.schedule>[1]) => interviewApi.schedule(applicationId, details),
    onSuccess: async (interview) => {
      queryClient.setQueryData(['interview', applicationId, interview.id], interview);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['interviews'] }),
        queryClient.invalidateQueries({ queryKey: ['upcoming-interviews'] }),
      ]);
      navigate(`/applications/${applicationId}/interviews/${interview.id}`);
    },
  });

  if (!validId) return <Alert color="red" title="无效的申请编号">请从申请详情页预约面试。</Alert>;

  return <Stack gap="lg" maw={760}>
    <div><Text component={Link} to={`/applications/${applicationId}`} size="sm" c="indigo">← 返回申请详情</Text><Title order={1} mt="sm">预约面试</Title></div>
    <QueryState loading={application.isPending} error={application.error} empty={false}>
      {application.data && <Card withBorder radius="lg" p="lg">
        <Text c="dimmed" mb="lg">{application.data.company} · {application.data.position}</Text>
        <InterviewForm initialValues={emptyInterviewForm} submitLabel="保存面试" cancelTo={`/applications/${applicationId}`} pending={schedule.isPending} error={schedule.error} onSubmit={schedule.mutate} />
      </Card>}
    </QueryState>
  </Stack>;
}
