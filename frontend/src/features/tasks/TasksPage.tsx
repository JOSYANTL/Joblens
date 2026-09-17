import { Badge, Card, Group, Pagination, Stack, Table, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { getJson } from '../../shared/api/client';
import type { FollowUpTask, Page } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';

export function TasksPage() {
  const [page, setPage] = useState(1);
  const query = useQuery({ queryKey: ['tasks', page], queryFn: () => getJson<Page<FollowUpTask>>('/api/tasks', { page: page - 1, size: 10 }) });
  return <Stack gap="lg">
    <div><Text c="indigo" fw={700} size="sm">FOLLOW UPS</Text><Title order={1}>跟进任务</Title><Text c="dimmed">掌握需要跟进的事项和截止日期。</Text></div>
    <Card withBorder radius="lg" p="lg"><QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
      <Table.ScrollContainer minWidth={520}><Table striped highlightOnHover verticalSpacing="md"><Table.Thead><Table.Tr><Table.Th>任务</Table.Th><Table.Th>申请</Table.Th><Table.Th>截止时间</Table.Th><Table.Th>状态</Table.Th></Table.Tr></Table.Thead><Table.Tbody>{query.data?.content.map((item) => <Table.Tr key={item.id}><Table.Td fw={600}>{item.title}</Table.Td><Table.Td>#{item.applicationId}</Table.Td><Table.Td>{formatDate(item.dueAt)}</Table.Td><Table.Td><Badge color={item.overdue ? 'red' : item.status === 'DONE' ? 'green' : 'blue'} variant="light">{item.overdue ? '已逾期' : item.status}</Badge></Table.Td></Table.Tr>)}</Table.Tbody></Table></Table.ScrollContainer>
    </QueryState>{query.data && query.data.totalPages > 1 && <Group justify="flex-end" mt="lg"><Pagination total={query.data.totalPages} value={page} onChange={setPage} /></Group>}</Card>
  </Stack>;
}
