import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, Checkbox, Group, NativeSelect, Pagination, Stack, Table, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router';
import type { FollowUpTaskStatus } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { taskApi } from './api';
import { taskStatusColors, taskStatusLabels } from './status';

export function TasksPage() {
  const [searchParams] = useSearchParams();
  const rawApplicationId = searchParams.get('applicationId');
  const applicationId = rawApplicationId === null ? undefined : Number(rawApplicationId);
  const validApplicationId = applicationId === undefined || (Number.isSafeInteger(applicationId) && applicationId > 0);
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState<FollowUpTaskStatus | ''>('');
  const [overdueOnly, setOverdueOnly] = useState(false);
  useEffect(() => setPage(1), [applicationId]);
  const query = useQuery({
    queryKey: ['tasks', { page, applicationId, status, overdueOnly }],
    queryFn: () => taskApi.list({ page: page - 1, size: 10, applicationId, status: status || undefined, overdueOnly }),
    enabled: validApplicationId,
  });

  if (!validApplicationId) return <Alert color="red" title="无效的申请编号">请从申请详情页进入任务列表。</Alert>;

  return <Stack gap="lg">
    <Group justify="space-between" align="end">
      <div><Text c="indigo" fw={700} size="sm">FOLLOW UPS</Text><Title order={1}>跟进任务</Title><Text c="dimmed">管理待办、截止日期和每次跟进。</Text></div>
      {applicationId && <Button component={Link} to={`/applications/${applicationId}/tasks/new`}>新建任务</Button>}
    </Group>
    {applicationId && <Text component={Link} to={`/applications/${applicationId}`} size="sm" c="indigo">← 返回申请 #{applicationId}</Text>}
    <Card withBorder radius="lg" p="lg">
      <Group mb="lg" align="end">
        <NativeSelect label="状态" value={status} data={[{ value: '', label: '全部状态' }, ...Object.entries(taskStatusLabels).map(([value, label]) => ({ value, label }))]} onChange={(event) => { const nextStatus = event.currentTarget.value as FollowUpTaskStatus | ''; setStatus(nextStatus); if (nextStatus !== 'TODO') setOverdueOnly(false); setPage(1); }} />
        <Checkbox label="只看逾期" checked={overdueOnly} onChange={(event) => { const checked = event.currentTarget.checked; setOverdueOnly(checked); if (checked) setStatus('TODO'); setPage(1); }} mb="xs" />
        {(status || overdueOnly || applicationId) && <Button component={Link} to="/tasks" variant="subtle" size="sm" onClick={() => { setStatus(''); setOverdueOnly(false); setPage(1); }}>查看全部任务</Button>}
      </Group>
      <QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
        <Table.ScrollContainer minWidth={620}><Table striped highlightOnHover verticalSpacing="md">
          <Table.Thead><Table.Tr><Table.Th>任务</Table.Th><Table.Th>申请</Table.Th><Table.Th>截止时间</Table.Th><Table.Th>状态</Table.Th><Table.Th /></Table.Tr></Table.Thead>
          <Table.Tbody>{query.data?.content.map((item) => <Table.Tr key={item.id}>
            <Table.Td fw={600}>{item.title}</Table.Td>
            <Table.Td><Text component={Link} to={`/applications/${item.applicationId}`} size="sm" c="indigo">#{item.applicationId}</Text></Table.Td>
            <Table.Td>{formatDate(item.dueAt)}</Table.Td>
            <Table.Td><Badge color={item.overdue ? 'red' : taskStatusColors[item.status]} variant="light">{item.overdue ? '已逾期' : taskStatusLabels[item.status]}</Badge></Table.Td>
            <Table.Td><Button component={Link} to={`/applications/${item.applicationId}/tasks/${item.id}`} size="xs" variant="subtle">查看详情</Button></Table.Td>
          </Table.Tr>)}</Table.Tbody>
        </Table></Table.ScrollContainer>
      </QueryState>
      {query.data && query.data.totalPages > 1 && <Group justify="flex-end" mt="lg"><Pagination total={query.data.totalPages} value={page} onChange={setPage} /></Group>}
    </Card>
  </Stack>;
}
