import { useState } from 'react';
import { Badge, Card, Group, Pagination, Select, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import type { ApplicationActivityType } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { activityApi } from './api';

const labels: Record<ApplicationActivityType, string> = {
  APPLICATION_CREATED: '创建申请', APPLICATION_UPDATED: '更新申请', APPLICATION_STATUS_CHANGED: '更新申请状态',
  INTERVIEW_SCHEDULED: '预约面试', INTERVIEW_RESCHEDULED: '调整面试', INTERVIEW_STATUS_CHANGED: '更新面试状态',
  INTERVIEW_FEEDBACK_UPDATED: '更新面试反馈', TASK_CREATED: '创建任务', TASK_UPDATED: '更新任务',
  TASK_STATUS_CHANGED: '更新任务状态', TASK_DELETED: '删除任务', DOCUMENT_UPLOADED: '上传文档',
  DOCUMENT_DELETED: '删除文档', NOTE_CREATED: '添加备注', NOTE_UPDATED: '更新备注', NOTE_DELETED: '删除备注',
};

const filterOptions = Object.entries(labels).map(([value, label]) => ({ value, label }));

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
        <div><Badge variant="light">{labels[item.type]}</Badge><Text mt="xs">{item.summary}</Text></div>
        <Text size="xs" c="dimmed">{formatDate(item.occurredAt)}</Text>
      </Group>)}</Stack>
    </QueryState>
    {activities.data && activities.data.totalPages > 1 && <Pagination mt="lg" value={page}
      total={activities.data.totalPages} onChange={setPage} />}
  </Card>;
}
