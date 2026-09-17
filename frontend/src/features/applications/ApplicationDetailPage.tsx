import { useState } from 'react';
import { Alert, Badge, Button, Card, Divider, Group, Select, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import type { ApplicationStatus } from '../../shared/api/types';
import { applicationApi } from './api';
import { statusColors, statusLabels } from './status';

export function ApplicationDetailPage() {
  const { id: rawId } = useParams();
  const id = Number(rawId);
  const validId = Number.isSafeInteger(id) && id > 0;
  const queryClient = useQueryClient();
  const [nextStatus, setNextStatus] = useState<ApplicationStatus | null>(null);
  const [updated, setUpdated] = useState(false);
  const application = useQuery({ queryKey: ['application', id], queryFn: () => applicationApi.get(id), enabled: validId });
  const statuses = useQuery({ queryKey: ['application-statuses', id], queryFn: () => applicationApi.availableStatuses(id), enabled: validId });
  const history = useQuery({ queryKey: ['application-status-history', id], queryFn: () => applicationApi.statusHistory(id), enabled: validId });
  const changeStatus = useMutation({
    mutationFn: (status: ApplicationStatus) => applicationApi.updateStatus(id, status, application.data!.version),
    onSuccess: async () => {
      setNextStatus(null);
      setUpdated(true);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['application', id] }),
        queryClient.invalidateQueries({ queryKey: ['application-statuses', id] }),
        queryClient.invalidateQueries({ queryKey: ['application-status-history', id] }),
        queryClient.invalidateQueries({ queryKey: ['applications'] }),
        queryClient.invalidateQueries({ queryKey: ['application-statistics'] }),
      ]);
    },
  });

  if (!validId) return <Alert color="red" title="无效的申请编号">请从职位申请列表进入详情页。</Alert>;

  return <Stack gap="lg">
    <div><Text component={Link} to="/applications" size="sm" c="indigo">← 返回职位申请</Text><Title order={1} mt="sm">申请详情</Title></div>
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
      {updated && <Alert color="green" mb="md">状态已更新。</Alert>}
      {changeStatus.isError && <Alert color="red" title="更新失败" mb="md">{changeStatus.error.message}{changeStatus.error instanceof ApiError && changeStatus.error.status === 409 ? ' 请刷新页面后再试。' : ''}</Alert>}
      <QueryState loading={statuses.isPending} error={statuses.error} empty={false}>
        {statuses.data && <Group align="end">
          <Select label="下一状态" placeholder={statuses.data.availableStatuses.length ? '选择状态' : '当前没有可切换的状态'} data={statuses.data.availableStatuses.map((status) => ({ value: status, label: statusLabels[status] }))} value={nextStatus} onChange={(value) => { setNextStatus(value as ApplicationStatus | null); setUpdated(false); }} disabled={statuses.data.availableStatuses.length === 0 || changeStatus.isPending} w={220} />
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
  </Stack>;
}
