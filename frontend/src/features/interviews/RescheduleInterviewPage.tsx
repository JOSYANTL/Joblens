import { useState } from 'react';
import { Alert, Button, Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import type { InterviewDetails } from '../../shared/api/types';
import { interviewApi } from './api';
import { InterviewForm } from './InterviewForm';
import { toLocalDateTimeInput } from './time';

export function RescheduleInterviewPage() {
  const params = useParams();
  const applicationId = Number(params.applicationId);
  const interviewId = Number(params.interviewId);
  const validIds = [applicationId, interviewId].every((id) => Number.isSafeInteger(id) && id > 0);
  const detailPath = `/applications/${applicationId}/interviews/${interviewId}`;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [reloaded, setReloaded] = useState(false);
  const interview = useQuery({ queryKey: ['interview', applicationId, interviewId], queryFn: () => interviewApi.get(applicationId, interviewId), enabled: validIds });
  const reschedule = useMutation({
    mutationFn: (details: InterviewDetails) => interviewApi.reschedule(applicationId, interviewId, details, interview.data!.version),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['interview', applicationId, interviewId], saved);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['interviews'] }),
        queryClient.invalidateQueries({ queryKey: ['upcoming-interviews'] }),
      ]);
      navigate(detailPath);
    },
  });

  async function reloadLatest() {
    const result = await interview.refetch();
    if (result.isSuccess) {
      reschedule.reset();
      setReloaded(true);
    }
  }

  if (!validIds) return <Alert color="red" title="无效的面试编号">请从面试列表进入。</Alert>;
  const conflict = reschedule.error instanceof ApiError && reschedule.error.status === 409;

  return <Stack gap="lg" maw={760}>
    <div><Text component={Link} to={detailPath} size="sm" c="indigo">← 返回面试详情</Text><Title order={1} mt="sm">编辑面试安排</Title></div>
    <QueryState loading={interview.isPending} error={interview.error} empty={false}>
      {interview.data && <Card withBorder radius="lg" p="lg">
        {interview.data.status !== 'SCHEDULED' ? <Alert color="yellow">只有已安排的面试可以修改。</Alert> : <>
          {reloaded && <Alert color="blue" mb="md">已加载最新版本，请核对后重新提交。</Alert>}
          <InterviewForm
            key={`${interviewId}-${interview.data.version}`}
            initialValues={{
              round: String(interview.data.round), type: interview.data.type,
              startsAt: toLocalDateTimeInput(interview.data.startsAt),
              durationMinutes: String(interview.data.durationMinutes),
              contact: interview.data.contact ?? '', meetingUrl: interview.data.meetingUrl ?? '',
              location: interview.data.location ?? '',
            }}
            submitLabel="保存修改"
            cancelTo={detailPath}
            pending={reschedule.isPending}
            error={reschedule.error}
            errorAction={conflict ? <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={interview.isFetching}>加载最新版本</Button> : undefined}
            onSubmit={(details) => { setReloaded(false); reschedule.mutate(details); }}
          />
        </>}
      </Card>}
    </QueryState>
  </Stack>;
}
