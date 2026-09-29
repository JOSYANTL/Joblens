import { useState } from 'react';
import { Badge, Card, Group, Pagination, Select, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import type { ApplicationActivityType } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { activityApi } from './api';
import { activityTypeLabels } from './labels';

const filterOptions = Object.entries(activityTypeLabels).map(([value, label]) => ({ value, label }));

export function ApplicationActivityCard({ applicationId, enabled }: { applicationId: number; enabled: boolean }) {
  const [page, setPage] = useState(1);
  const [type, setType] = useState<ApplicationActivityType | null>(null);
  const activities = useQuery({
    queryKey: ['application-activities', applicationId, { page: page - 1, type }],
    queryFn: () => activityApi.list(applicationId, { page: page - 1, size: 10, type: type ?? undefined }),
    enabled,
  });
  return <Card withBorder radius="lg" p="lg">
    <Group justify="space-between" align="end" mb="md">
      <div><Title order={3}>活动时间线</Title><Text c="dimmed" size="sm" mt={4}>按时间查看这份申请发生过的关键操作。</Text></div>
      <Select aria-label="活动类型" placeholder="全部活动" clearable searchable data={filterOptions} value={type}
        onChange={(value) => { setType(value as ApplicationActivityType | null); setPage(1); }} w={200} />
    </Group>
    <QueryState loading={activities.isPending} error={activities.error}
      empty={activities.data?.content.length === 0} emptyText="没有符合条件的活动。">
      <Stack gap="sm">{activities.data?.content.map((item) => <Group key={item.id} justify="space-between"
        align="start" className="list-row">
        <div><Badge variant="light">{activityTypeLabels[item.type]}</Badge><Text mt="xs">{item.summary}</Text></div>
        <Text size="xs" c="dimmed">{formatDate(item.occurredAt)}</Text>
      </Group>)}</Stack>
    </QueryState>
    {activities.data && activities.data.totalPages > 1 && <Pagination mt="lg" value={page}
      total={activities.data.totalPages} onChange={setPage} />}
  </Card>;
}
