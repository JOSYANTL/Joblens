import { Badge, Card, Group, Pagination, Stack, Table, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { getJson } from '../../shared/api/client';
import type { Interview, Page } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';

export function InterviewsPage() {
  const [page, setPage] = useState(1);
  const query = useQuery({ queryKey: ['interviews', page], queryFn: () => getJson<Page<Interview>>('/api/interviews', { page: page - 1, size: 10 }) });
  return <Stack gap="lg">
    <div><Text c="indigo" fw={700} size="sm">INTERVIEWS</Text><Title order={1}>面试安排</Title><Text c="dimmed">查看面试时间和当前状态。</Text></div>
    <Card withBorder radius="lg" p="lg"><QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
      <Table.ScrollContainer minWidth={560}><Table striped highlightOnHover verticalSpacing="md"><Table.Thead><Table.Tr><Table.Th>申请</Table.Th><Table.Th>轮次</Table.Th><Table.Th>方式</Table.Th><Table.Th>开始时间</Table.Th><Table.Th>状态</Table.Th></Table.Tr></Table.Thead><Table.Tbody>{query.data?.content.map((item) => <Table.Tr key={item.id}><Table.Td>#{item.applicationId}</Table.Td><Table.Td>第 {item.round} 轮</Table.Td><Table.Td>{item.type}</Table.Td><Table.Td>{formatDate(item.startsAt)}</Table.Td><Table.Td><Badge color={item.status === 'SCHEDULED' ? 'blue' : item.status === 'COMPLETED' ? 'green' : 'gray'} variant="light">{item.status}</Badge></Table.Td></Table.Tr>)}</Table.Tbody></Table></Table.ScrollContainer>
    </QueryState>{query.data && query.data.totalPages > 1 && <Group justify="flex-end" mt="lg"><Pagination total={query.data.totalPages} value={page} onChange={setPage} /></Group>}</Card>
  </Stack>;
}
