import { Badge, Button, Card, Group, Pagination, SegmentedControl, Select, Stack, Table, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import type { InterviewStatus } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { interviewApi } from './api';
import { interviewStatusColors, interviewStatusLabels, interviewTypeLabels } from './status';

export function InterviewsPage() {
  const [searchParams] = useSearchParams();
  const rawApplicationId = Number(searchParams.get('applicationId'));
  const applicationId = Number.isSafeInteger(rawApplicationId) && rawApplicationId > 0 ? rawApplicationId : undefined;
  const [page, setPage] = useState(1);
  const [view, setView] = useState<'all' | 'upcoming'>('all');
  const [status, setStatus] = useState<InterviewStatus | undefined>();
  useEffect(() => setPage(1), [applicationId]);
  const query = useQuery({
    queryKey: ['interviews', { view, page, status, applicationId }],
    queryFn: () => view === 'upcoming'
      ? interviewApi.upcoming({ page: page - 1, size: 10, applicationId })
      : interviewApi.list({ page: page - 1, status, applicationId }),
  });

  return <Stack gap="lg">
    <div><Text c="indigo" fw={700} size="sm">INTERVIEWS</Text><Title order={1}>面试安排</Title><Text c="dimmed">查看安排、更新进展并记录反馈。预约入口位于职位申请详情。</Text></div>
    {applicationId && <Group><Text size="sm">当前仅查看申请 #{applicationId}</Text><Button component={Link} to="/interviews" size="xs" variant="subtle">查看全部</Button></Group>}
    <Card withBorder radius="lg" p="lg">
      <Group justify="space-between" mb="lg" align="end">
        <SegmentedControl value={view} onChange={(value) => { setView(value as 'all' | 'upcoming'); setPage(1); }} data={[{ label: '全部面试', value: 'all' }, { label: '未来 7 天', value: 'upcoming' }]} />
        {view === 'all' && <Select label="状态" placeholder="全部状态" clearable w={170} value={status ?? null} data={Object.entries(interviewStatusLabels).map(([value, label]) => ({ value, label }))} onChange={(value) => { setStatus((value || undefined) as InterviewStatus | undefined); setPage(1); }} />}
      </Group>
      <QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
        <Table.ScrollContainer minWidth={680}><Table striped highlightOnHover verticalSpacing="md">
          <Table.Thead><Table.Tr><Table.Th>申请</Table.Th><Table.Th>轮次</Table.Th><Table.Th>方式</Table.Th><Table.Th>开始时间</Table.Th><Table.Th>状态</Table.Th><Table.Th>操作</Table.Th></Table.Tr></Table.Thead>
          <Table.Tbody>{query.data?.content.map((item) => <Table.Tr key={item.id}>
            <Table.Td><Text component={Link} to={`/applications/${item.applicationId}`} c="indigo">#{item.applicationId}</Text></Table.Td>
            <Table.Td>第 {item.round} 轮</Table.Td><Table.Td>{interviewTypeLabels[item.type]}</Table.Td>
            <Table.Td>{formatDate(item.startsAt)}</Table.Td>
            <Table.Td><Badge color={interviewStatusColors[item.status]} variant="light">{interviewStatusLabels[item.status]}</Badge></Table.Td>
            <Table.Td><Button component={Link} to={`/applications/${item.applicationId}/interviews/${item.id}`} size="xs" variant="subtle">查看详情</Button></Table.Td>
          </Table.Tr>)}</Table.Tbody>
        </Table></Table.ScrollContainer>
      </QueryState>
      {query.data && query.data.totalPages > 1 && <Group justify="space-between" mt="lg"><Text c="dimmed" size="sm">共 {query.data.totalElements} 场</Text><Pagination total={query.data.totalPages} value={page} onChange={setPage} /></Group>}
    </Card>
  </Stack>;
}
