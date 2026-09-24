import { Badge, Box, Card, Group, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import { applicationApi } from '../applications/api';
import { statusColors, statusLabels } from '../applications/status';
import type { ApplicationStatus } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { interviewApi } from '../interviews/api';
import { interviewTypeLabels } from '../interviews/status';
import { taskApi } from '../tasks/api';
import { activityApi } from '../activity/api';
import { activityTypeLabels } from '../activity/labels';

export function DashboardPage() {
  const statistics = useQuery({ queryKey: ['application-statistics'], queryFn: applicationApi.statistics });
  const interviews = useQuery({ queryKey: ['upcoming-interviews'], queryFn: () => interviewApi.upcoming({ size: 3 }) });
  const tasks = useQuery({ queryKey: ['open-tasks'], queryFn: () => taskApi.list({ page: 0, status: 'TODO', size: 3 }) });
  const activities = useQuery({ queryKey: ['recent-activities'], queryFn: () => activityApi.recent(8) });

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
          <Stack gap="sm">{interviews.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text component={Link} to={`/applications/${item.applicationId}/interviews/${item.id}`} fw={600} c="indigo">第 {item.round} 轮 · 申请 #{item.applicationId}</Text><Text size="sm" c="dimmed">{formatDate(item.startsAt)}</Text></div><Badge variant="light">{interviewTypeLabels[item.type]}</Badge></Group>)}</Stack>
        </QueryState>
      </Card>
      <Card withBorder radius="lg" p="lg">
        <Group justify="space-between" mb="md"><Title order={3}>待办跟进</Title><Text component={Link} to="/tasks" size="sm" c="indigo">查看全部 →</Text></Group>
        <QueryState loading={tasks.isPending} error={tasks.error} empty={tasks.data?.content.length === 0}>
          <Stack gap="sm">{tasks.data?.content.map((item) => <Group key={item.id} justify="space-between" className="list-row"><div><Text component={Link} to={`/applications/${item.applicationId}/tasks/${item.id}`} fw={600} c="indigo">{item.title}</Text><Text size="sm" c="dimmed">截止 {formatDate(item.dueAt)}</Text></div>{item.overdue && <Badge color="red" variant="light">已逾期</Badge>}</Group>)}</Stack>
        </QueryState>
      </Card>
    </SimpleGrid>
    <Card withBorder radius="lg" p="lg">
      <Group justify="space-between" mb="md">
        <div><Title order={3}>最近活动</Title><Text c="dimmed" size="sm" mt={4}>集中查看所有职位申请的最新变化。</Text></div>
      </Group>
      <QueryState loading={activities.isPending} error={activities.error}
        empty={activities.data?.length === 0} emptyText="还没有活动记录。">
        <Stack gap={0}>{activities.data?.map((item) => <Group key={item.id} gap="md" wrap="nowrap"
          align="flex-start" className="list-row">
          <Box className="activity-dot" mt={7} />
          <Box style={{ flex: 1 }}>
            <Group gap="xs">
              <Badge variant="light">{activityTypeLabels[item.type]}</Badge>
              <Text component={Link} to={`/applications/${item.applicationId}`} size="sm" c="indigo" fw={600}>
                申请 #{item.applicationId}
              </Text>
            </Group>
            <Text mt={6}>{item.summary}</Text>
          </Box>
          <Text size="xs" c="dimmed" ta="right">{formatDate(item.occurredAt)}</Text>
        </Group>)}</Stack>
      </QueryState>
    </Card>
  </Stack>;
}

function StatCard({ label, value, color = 'indigo' }: { label: string; value: number; color?: string }) {
  return <Card withBorder radius="lg" p="lg"><Text size="sm" c="dimmed">{label}</Text><Text size="xl" fw={800} c={color} mt="xs">{value}</Text></Card>;
}
