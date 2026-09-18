import { useState } from 'react';
import { Alert, Badge, Button, Card, Divider, Group, Modal, Stack, Text, Title } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import type { FollowUpTaskStatus } from '../../shared/api/types';
import { taskApi } from './api';
import { taskStatusColors, taskStatusLabels } from './status';

export function TaskDetailPage() {
  const params = useParams();
  const applicationId = Number(params.applicationId);
  const taskId = Number(params.taskId);
  const validIds = [applicationId, taskId].every((id) => Number.isSafeInteger(id) && id > 0);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [deleteOpened, { open: openDelete, close: closeDelete }] = useDisclosure(false);
  const [notice, setNotice] = useState<string | null>(null);
  const task = useQuery({ queryKey: ['task', applicationId, taskId], queryFn: () => taskApi.get(applicationId, taskId), enabled: validIds });

  async function refreshLists() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['tasks'] }),
      queryClient.invalidateQueries({ queryKey: ['open-tasks'] }),
    ]);
  }

  const changeStatus = useMutation({
    mutationFn: (status: FollowUpTaskStatus) => taskApi.changeStatus(applicationId, taskId, status, task.data!.version),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['task', applicationId, taskId], saved);
      setNotice('任务状态已更新。');
      await refreshLists();
    },
  });
  const remove = useMutation({
    mutationFn: () => taskApi.remove(applicationId, taskId, task.data!.version),
    onSuccess: async () => {
      closeDelete();
      await refreshLists();
      queryClient.removeQueries({ queryKey: ['task', applicationId, taskId], exact: true });
      navigate(`/applications/${applicationId}`);
    },
  });

  async function reloadLatest() {
    const result = await task.refetch();
    if (result.isSuccess) {
      changeStatus.reset();
      remove.reset();
      closeDelete();
      setNotice('已加载最新版本，请核对后重试。');
    }
  }

  if (!validIds) return <Alert color="red" title="无效的任务编号">请从任务列表进入详情页。</Alert>;

  const item = task.data;
  const statusConflict = changeStatus.error instanceof ApiError && changeStatus.error.status === 409;
  const deleteConflict = remove.error instanceof ApiError && remove.error.status === 409;

  return <Stack gap="lg" maw={850}>
    <Group justify="space-between" align="end">
      <div><Text component={Link} to="/tasks" size="sm" c="indigo">← 返回任务列表</Text><Title order={1} mt="sm">跟进任务详情</Title></div>
      {item && <Group><Button component={Link} to={`/applications/${applicationId}/tasks/${taskId}/edit`} variant="light">编辑任务</Button><Button color="red" variant="light" onClick={openDelete}>删除任务</Button></Group>}
    </Group>
    {notice && <Alert color="blue">{notice}</Alert>}
    <QueryState loading={task.isPending} error={task.error} empty={false}>
      {item && <>
        <Card withBorder radius="lg" p="lg">
          <Group justify="space-between" align="start"><div><Text c="dimmed" size="sm">申请 #{item.applicationId} · 任务 #{item.id}</Text><Title order={2}>{item.title}</Title><Text component={Link} to={`/applications/${item.applicationId}`} size="sm" c="indigo">查看职位申请 →</Text></div><Badge color={item.overdue ? 'red' : taskStatusColors[item.status]} variant="light" size="lg">{item.overdue ? '已逾期' : taskStatusLabels[item.status]}</Badge></Group>
          <Divider my="lg" />
          <Stack gap="xs"><Text><strong>截止：</strong>{formatDate(item.dueAt)}</Text>{item.completedAt && <Text><strong>完成：</strong>{formatDate(item.completedAt)}</Text>}<Text><strong>备注：</strong></Text><Text style={{ whiteSpace: 'pre-wrap' }}>{item.notes || '暂无备注'}</Text></Stack>
        </Card>
        <Card withBorder radius="lg" p="lg">
          <Title order={3} mb="xs">任务进展</Title>
          {changeStatus.isError && <Alert color="red" title="状态更新失败" mb="md">{changeStatus.error.message}{statusConflict && <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={task.isFetching}>加载最新数据</Button>}</Alert>}
          <Group>
            {item.status === 'TODO' ? <><Button color="green" loading={changeStatus.isPending} onClick={() => { setNotice(null); changeStatus.mutate('DONE'); }}>标记完成</Button><Button color="gray" variant="light" disabled={changeStatus.isPending} onClick={() => { setNotice(null); changeStatus.mutate('CANCELLED'); }}>取消任务</Button></> :
              <Button variant="light" loading={changeStatus.isPending} onClick={() => { setNotice(null); changeStatus.mutate('TODO'); }}>重新打开</Button>}
          </Group>
        </Card>
      </>}
    </QueryState>
    <Modal opened={deleteOpened} onClose={closeDelete} title="确认删除任务" centered>
      <Stack gap="md"><Text>确定删除这条跟进任务吗？删除后无法恢复。</Text>{remove.isError && <Alert color="red">{remove.error.message}{deleteConflict && <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={task.isFetching}>加载最新数据</Button>}</Alert>}<Group justify="flex-end"><Button variant="default" onClick={closeDelete}>返回</Button><Button color="red" loading={remove.isPending} onClick={() => remove.mutate()}>确认删除</Button></Group></Stack>
    </Modal>
  </Stack>;
}
