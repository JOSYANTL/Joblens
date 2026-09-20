import { useState } from 'react';
import { Alert, Button, Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import type { FollowUpTaskDetails } from '../../shared/api/types';
import { toLocalDateTimeInput } from '../interviews/time';
import { taskApi } from './api';
import { TaskForm } from './TaskForm';

export function EditTaskPage() {
  const params = useParams();
  const applicationId = Number(params.applicationId);
  const taskId = Number(params.taskId);
  const validIds = [applicationId, taskId].every((id) => Number.isSafeInteger(id) && id > 0);
  const detailPath = `/applications/${applicationId}/tasks/${taskId}`;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [reloaded, setReloaded] = useState(false);
  const task = useQuery({ queryKey: ['task', applicationId, taskId], queryFn: () => taskApi.get(applicationId, taskId), enabled: validIds });
  const update = useMutation({
    mutationFn: (details: FollowUpTaskDetails) => taskApi.update(applicationId, taskId, details, task.data!.version),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['task', applicationId, taskId], saved);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['tasks'] }),
        queryClient.invalidateQueries({ queryKey: ['open-tasks'] }),
      ]);
      navigate(detailPath);
    },
  });

  async function reloadLatest() {
    const result = await task.refetch();
    if (result.isSuccess) {
      update.reset();
      setReloaded(true);
    }
  }

  if (!validIds) return <Alert color="red" title="无效的任务编号">请从任务列表进入。</Alert>;
  const conflict = update.error instanceof ApiError && update.error.status === 409;

  return <Stack gap="lg" maw={760}>
    <div><Text component={Link} to={detailPath} size="sm" c="indigo">← 返回任务详情</Text><Title order={1} mt="sm">编辑跟进任务</Title></div>
    <QueryState loading={task.isPending} error={task.error} empty={false}>
      {task.data && <Card withBorder radius="lg" p="lg">
        {reloaded && <Alert color="blue" mb="md">已加载最新版本，请核对后重新提交。</Alert>}
        <TaskForm
          key={`${taskId}-${task.data.version}`}
          initialValues={{ title: task.data.title, notes: task.data.notes ?? '', dueAt: toLocalDateTimeInput(task.data.dueAt) }}
          submitLabel="保存修改" cancelTo={detailPath} pending={update.isPending} error={update.error}
          errorAction={conflict ? <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={task.isFetching}>加载最新版本</Button> : undefined}
          onSubmit={(details) => { setReloaded(false); update.mutate(details); }}
        />
      </Card>}
    </QueryState>
  </Stack>;
}
