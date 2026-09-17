import { Badge, Card, Group, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import { applicationApi } from '../applications/api';
import { statusColors, statusLabels } from '../applications/status';
import { getJson } from '../../shared/api/client';
import type { ApplicationStatus, FollowUpTask, Interview, Page } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';

export function DashboardPage() {
  const statistics = useQuery({ queryKey: ['application-statistics'], queryFn: applicationApi.statistics });
  const interviews = useQuery({ queryKey: ['upcoming-interviews'], queryFn: () => getJson<Page<Interview>>('/api/interviews/upcoming', { size: 3 }) });
  const tasks = useQuery({ queryKey: ['open-tasks'], queryFn: () => getJson<Page<FollowUpTask>>('/api/tasks', { status: 'TODO', size: 3 }) });

  return <Stack gap="xl">
    <div><Text c="indigo" fw={700} size="sm">OVERVIEW</Text><Title order={1}>求职总览</Title><Text c="dimmed">今天也向下一份机会更近一步。</Text></div>
    <QueryState loading={statistics.isPending} error={statistics.error} empty={false}>
      <SimpleGrid cols={{ base: 2, sm: 3, lg: 6 }}>
        <StatCard label="全部申请" value={statistics.data?.total ?? 0} />
        {(Object.keys(statusLabels) as ApplicationStatus[]).map((status) => <StatCard key={status} label={statusLabels[status]} value={statistics.data?.byStatus[status] ?? 0} color={statusColors[status]} />)}
      </SimpleGrid>
    </QueryState>
    <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
      <Card withBorder radius="lg" p="lg">
        <Group justify="space-between" mb="md"><Title order={3}>未来 7 天面试</Title><Text component={Link} to="/interviews" size="sm" c="indigo">查看全部 →</Text></Group>
        <QueryState loading={interviews.isPending} error={interviews.error} empty={interviews.data?.content.length === 0}>
          <Stack gap="sm">{interviews.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text fw={600}>第 {item.round} 轮 · 申请 #{item.applicationId}</Text><Text size="sm" c="dimmed">{formatDate(item.startsAt)}</Text></div><Badge variant="light">{item.type}</Badge></Group>)}</Stack>
        </QueryState>
      </Card>
      <Card withBorder radius="lg" p="lg">
        <Group justify="space-between" mb="md"><Title order={3}>待办跟进</Title><Text component={Link} to="/tasks" size="sm" c="indigo">查看全部 →</Text></Group>
        <QueryState loading={tasks.isPending} error={tasks.error} empty={tasks.data?.content.length === 0}>
          <Stack gap="sm">{tasks.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text fw={600}>{item.title}</Text><Text size="sm" c="dimmed">截止 {formatDate(item.dueAt)}</Text></div>{item.overdue && <Badge color="red" variant="light">已逾期</Badge>}</Group>)}</Stack>
        </QueryState>
      </Card>
    </SimpleGrid>
  </Stack>;
}

function StatCard({ label, value, color = 'indigo' }: { label: string; value: number; color?: string }) {
  return <Card withBorder radius="lg" p="lg"><Text size="sm" c="dimmed">{label}</Text><Text size="xl" fw={800} c={color} mt="xs">{value}</Text></Card>;
}
