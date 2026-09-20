import { useState } from 'react';
import { Badge, Button, Card, Group, Pagination, Select, Stack, Table, Text, TextInput, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import { applicationApi } from './api';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import type { ApplicationStatus } from '../../shared/api/types';
import { statusColors, statusLabels } from './status';

export function ApplicationsPage() {
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<ApplicationStatus | undefined>();
  const [page, setPage] = useState(1);
  const query = useQuery({
    queryKey: ['applications', { page, keyword, status }],
    queryFn: () => applicationApi.list({ page: page - 1, keyword: keyword.trim(), status }),
  });

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="end">
        <div><Text c="indigo" fw={700} size="sm">APPLICATIONS</Text><Title order={1}>职位申请</Title><Text c="dimmed">集中查看所有投递记录与进展。</Text></div>
        <Button component={Link} to="/applications/new">新增申请</Button>
      </Group>
      <Card withBorder radius="lg" p="lg">
        <Group mb="lg" align="end">
          <TextInput label="搜索" placeholder="公司或岗位关键词" value={keyword} onChange={(event) => { setKeyword(event.currentTarget.value); setPage(1); }} style={{ flex: 1, minWidth: 200 }} />
          <Select label="状态" placeholder="全部状态" clearable data={Object.entries(statusLabels).map(([value, label]) => ({ value, label }))} value={status ?? null} onChange={(value) => { setStatus((value || undefined) as ApplicationStatus | undefined); setPage(1); }} w={160} />
        </Group>
        <QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
          <Table.ScrollContainer minWidth={650}>
            <Table striped highlightOnHover verticalSpacing="md">
              <Table.Thead><Table.Tr><Table.Th>公司</Table.Th><Table.Th>岗位</Table.Th><Table.Th>状态</Table.Th><Table.Th>最近更新</Table.Th><Table.Th>操作</Table.Th></Table.Tr></Table.Thead>
              <Table.Tbody>{query.data?.content.map((item) => <Table.Tr key={item.id}><Table.Td fw={600}>{item.company}</Table.Td><Table.Td>{item.position}</Table.Td><Table.Td><Badge color={statusColors[item.status]} variant="light">{statusLabels[item.status]}</Badge></Table.Td><Table.Td>{formatDate(item.updatedAt)}</Table.Td><Table.Td><Button component={Link} to={`/applications/${item.id}`} variant="subtle" size="xs">查看详情</Button></Table.Td></Table.Tr>)}</Table.Tbody>
            </Table>
          </Table.ScrollContainer>
        </QueryState>
        {query.data && query.data.totalPages > 1 && <Group justify="space-between" mt="lg"><Text size="sm" c="dimmed">共 {query.data.totalElements} 条</Text><Pagination total={query.data.totalPages} value={page} onChange={setPage} /></Group>}
      </Card>
    </Stack>
  );
}
