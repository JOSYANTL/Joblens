import { useState } from 'react';
import { Alert, Badge, Button, Card, Divider, Group, Select, Stack, Text, Title } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import type { ApplicationStatus } from '../../shared/api/types';
import { applicationApi } from './api';
import { DeleteApplicationDialog } from './DeleteApplicationDialog';
import { statusColors, statusLabels } from './status';
import { interviewApi } from '../interviews/api';
import { interviewStatusColors, interviewStatusLabels, interviewTypeLabels } from '../interviews/status';
import { taskApi } from '../tasks/api';
import { taskStatusColors, taskStatusLabels } from '../tasks/status';

export function ApplicationDetailPage() {
  const { id: rawId } = useParams();
  const id = Number(rawId);
  const validId = Number.isSafeInteger(id) && id > 0;
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [deleteOpened, { open: openDelete, close: closeDelete }] = useDisclosure(false);
  const [nextStatus, setNextStatus] = useState<ApplicationStatus | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const application = useQuery({ queryKey: ['application', id], queryFn: () => applicationApi.get(id), enabled: validId });
  const statuses = useQuery({ queryKey: ['application-statuses', id], queryFn: () => applicationApi.availableStatuses(id), enabled: validId });
  const history = useQuery({ queryKey: ['application-status-history', id], queryFn: () => applicationApi.statusHistory(id), enabled: validId });
  const interviews = useQuery({ queryKey: ['interviews', { applicationId: id, page: 0 }], queryFn: () => interviewApi.list({ applicationId: id, page: 0, size: 5 }), enabled: validId });
  const tasks = useQuery({ queryKey: ['tasks', { applicationId: id, page: 0 }], queryFn: () => taskApi.list({ applicationId: id, page: 0, size: 5 }), enabled: validId });
  const changeStatus = useMutation({
    mutationFn: (status: ApplicationStatus) => applicationApi.updateStatus(id, status, application.data!.version),
    onSuccess: async () => {
      setNextStatus(null);
      setNotice('状态已更新。');
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['application', id] }),
        queryClient.invalidateQueries({ queryKey: ['application-statuses', id] }),
        queryClient.invalidateQueries({ queryKey: ['application-status-history', id] }),
        queryClient.invalidateQueries({ queryKey: ['applications'] }),
        queryClient.invalidateQueries({ queryKey: ['application-statistics'] }),
      ]);
    },
  });
  const remove = useMutation({
    mutationFn: () => applicationApi.delete(id),
    onSuccess: async () => {
      closeDelete();
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['applications'] }),
        queryClient.invalidateQueries({ queryKey: ['application-statistics'] }),
        queryClient.invalidateQueries({ queryKey: ['application', id], exact: true, refetchType: 'none' }),
        queryClient.invalidateQueries({ queryKey: ['application-statuses', id], exact: true, refetchType: 'none' }),
        queryClient.invalidateQueries({ queryKey: ['application-status-history', id], exact: true, refetchType: 'none' }),
        queryClient.invalidateQueries({ queryKey: ['interviews'] }),
        queryClient.invalidateQueries({ queryKey: ['upcoming-interviews'] }),
        queryClient.invalidateQueries({ queryKey: ['tasks'] }),
        queryClient.invalidateQueries({ queryKey: ['open-tasks'] }),
      ]);
      navigate('/applications');
    },
  });

  async function reloadLatest() {
    const result = await application.refetch();
    await Promise.all([statuses.refetch(), history.refetch()]);
    if (result.isSuccess) {
      changeStatus.reset();
      setNextStatus(null);
      setNotice('已加载最新状态，请重新选择下一步。');
    }
  }

  if (!validId) return <Alert color="red" title="无效的申请编号">请从职位申请列表进入详情页。</Alert>;

  return <Stack gap="lg">
    <Group justify="space-between" align="end">
      <div><Text component={Link} to="/applications" size="sm" c="indigo">← 返回职位申请</Text><Title order={1} mt="sm">申请详情</Title></div>
      <Group><Button component={Link} to={`/applications/${id}/edit`} variant="light" disabled={!application.data}>编辑信息</Button><Button color="red" variant="light" onClick={openDelete} disabled={!application.data}>删除申请</Button></Group>
    </Group>
    <QueryState loading={application.isPending} error={application.error} empty={false}>
      {application.data && <Card withBorder radius="lg" p="lg">
        <Group justify="space-between" align="start"><div><Text c="dimmed" size="sm">申请 #{application.data.id}</Text><Title order={2}>{application.data.company}</Title><Text size="lg">{application.data.position}</Text></div><Badge color={statusColors[application.data.status]} variant="light" size="lg">{statusLabels[application.data.status]}</Badge></Group>
        <Divider my="lg" />
        <Text fw={700} mb="xs">职位描述</Text><Text style={{ whiteSpace: 'pre-wrap' }}>{application.data.description}</Text>
        <Divider my="lg" />
        <Group gap="xl"><Text size="sm" c="dimmed">创建于 {formatDate(application.data.createdAt)}</Text><Text size="sm" c="dimmed">更新于 {formatDate(application.data.updatedAt)}</Text></Group>
      </Card>}
    </QueryState>
    <Card withBorder radius="lg" p="lg">
      <Title order={3} mb="xs">更新状态</Title>
      <Text c="dimmed" size="sm" mb="lg">只能选择当前状态允许的下一步；状态修改会记录在历史中。</Text>
      {notice && <Alert color="green" mb="md">{notice}</Alert>}
      {changeStatus.isError && <Alert color="red" title="更新失败" mb="md">{changeStatus.error.message}{changeStatus.error instanceof ApiError && changeStatus.error.status === 409 && <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={application.isFetching}>加载最新数据</Button>}</Alert>}
      <QueryState loading={statuses.isPending} error={statuses.error} empty={false}>
        {statuses.data && <Group align="end">
          <Select label="下一状态" placeholder={statuses.data.availableStatuses.length ? '选择状态' : '当前没有可切换的状态'} data={statuses.data.availableStatuses.map((status) => ({ value: status, label: statusLabels[status] }))} value={nextStatus} onChange={(value) => { setNextStatus(value as ApplicationStatus | null); setNotice(null); }} disabled={statuses.data.availableStatuses.length === 0 || changeStatus.isPending} w={220} />
          <Button onClick={() => nextStatus && changeStatus.mutate(nextStatus)} loading={changeStatus.isPending} disabled={!application.data || !nextStatus}>确认更新</Button>
        </Group>}
      </QueryState>
    </Card>
    <Card withBorder radius="lg" p="lg">
      <Title order={3} mb="md">状态历史</Title>
      <QueryState loading={history.isPending} error={history.error} empty={history.data?.length === 0}>
        <Stack gap="sm">{history.data?.map((entry) => <Group key={entry.id} justify="space-between" className="list-row"><Text>{entry.fromStatus ? `${statusLabels[entry.fromStatus]} → ` : '创建申请 → '}{statusLabels[entry.toStatus]}</Text><Text size="sm" c="dimmed">{formatDate(entry.changedAt)}</Text></Group>)}</Stack>
      </QueryState>
    </Card>
    <Card withBorder radius="lg" p="lg">
      <Group justify="space-between" mb="md"><Title order={3}>面试安排</Title><Button component={Link} to={`/applications/${id}/interviews/new`} size="sm" disabled={!application.data}>预约面试</Button></Group>
      <QueryState loading={interviews.isPending} error={interviews.error} empty={interviews.data?.content.length === 0}>
        <Stack gap="sm">{interviews.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text fw={600}>第 {item.round} 轮 · {interviewTypeLabels[item.type]}</Text><Text size="sm" c="dimmed">{formatDate(item.startsAt)}</Text></div><Group><Badge color={interviewStatusColors[item.status]} variant="light">{interviewStatusLabels[item.status]}</Badge><Button component={Link} to={`/applications/${id}/interviews/${item.id}`} variant="subtle" size="xs">查看详情</Button></Group></Group>)}</Stack>
      </QueryState>
      {interviews.data && interviews.data.totalElements > 5 && <Text component={Link} to={`/interviews?applicationId=${id}`} size="sm" c="indigo" mt="md">查看该申请的全部面试 →</Text>}
    </Card>
    <Card withBorder radius="lg" p="lg">
      <Group justify="space-between" mb="md"><Title order={3}>跟进任务</Title><Button component={Link} to={`/applications/${id}/tasks/new`} size="sm" disabled={!application.data}>新建任务</Button></Group>
      <QueryState loading={tasks.isPending} error={tasks.error} empty={tasks.data?.content.length === 0}>
        <Stack gap="sm">{tasks.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text fw={600}>{item.title}</Text><Text size="sm" c="dimmed">截止 {formatDate(item.dueAt)}</Text></div><Group><Badge color={item.overdue ? 'red' : taskStatusColors[item.status]} variant="light">{item.overdue ? '已逾期' : taskStatusLabels[item.status]}</Badge><Button component={Link} to={`/applications/${id}/tasks/${item.id}`} variant="subtle" size="xs">查看详情</Button></Group></Group>)}</Stack>
      </QueryState>
      {tasks.data && tasks.data.totalElements > 5 && <Text component={Link} to={`/tasks?applicationId=${id}`} size="sm" c="indigo" mt="md">查看该申请的全部任务 →</Text>}
    </Card>
    <DeleteApplicationDialog opened={deleteOpened} company={application.data?.company ?? ''} position={application.data?.position ?? ''} pending={remove.isPending} error={remove.error} onClose={closeDelete} onConfirm={() => remove.mutate()} />
  </Stack>;
}
