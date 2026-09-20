import { Alert, Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { applicationApi } from '../applications/api';
import { QueryState } from '../../shared/components/QueryState';
import type { FollowUpTaskDetails } from '../../shared/api/types';
import { taskApi } from './api';
import { emptyTaskForm, TaskForm } from './TaskForm';

export function CreateTaskPage() {
  const applicationId = Number(useParams().applicationId);
  const validId = Number.isSafeInteger(applicationId) && applicationId > 0;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const application = useQuery({ queryKey: ['application', applicationId], queryFn: () => applicationApi.get(applicationId), enabled: validId });
  const create = useMutation({
    mutationFn: (details: FollowUpTaskDetails) => taskApi.create(applicationId, details),
    onSuccess: async (task) => {
      queryClient.setQueryData(['task', applicationId, task.id], task);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['tasks'] }),
        queryClient.invalidateQueries({ queryKey: ['open-tasks'] }),
      ]);
      navigate(`/applications/${applicationId}/tasks/${task.id}`);
    },
  });

  if (!validId) return <Alert color="red" title="无效的申请编号">请从申请详情页创建任务。</Alert>;

  return <Stack gap="lg" maw={760}>
    <div><Text component={Link} to={`/applications/${applicationId}`} size="sm" c="indigo">← 返回申请详情</Text><Title order={1} mt="sm">新建跟进任务</Title></div>
    <QueryState loading={application.isPending} error={application.error} empty={false}>
      {application.data && <Card withBorder radius="lg" p="lg">
        <Text c="dimmed" mb="lg">{application.data.company} · {application.data.position}</Text>
        <TaskForm initialValues={emptyTaskForm} submitLabel="创建任务" cancelTo={`/applications/${applicationId}`} pending={create.isPending} error={create.error} onSubmit={create.mutate} />
      </Card>}
    </QueryState>
  </Stack>;
}
