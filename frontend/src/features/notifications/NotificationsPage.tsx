import { Alert, Badge, Button, Card, Group, Pagination, Stack, Switch, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import type { NotificationType, UserNotification } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { notificationApi } from './api';

const labels: Record<NotificationType, string> = {
  INTERVIEW_UPCOMING: '面试提醒',
  TASK_DUE_SOON: '即将到期',
  TASK_OVERDUE: '任务逾期',
};

export function NotificationsPage() {
  const [page, setPage] = useState(0);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const query = useQuery({
    queryKey: ['notifications', page, unreadOnly],
    queryFn: () => notificationApi.list({ page, unreadOnly }),
    refetchInterval: 5_000,
  });
  const refresh = () => {
    void queryClient.invalidateQueries({ queryKey: ['notifications'] });
    void queryClient.invalidateQueries({ queryKey: ['notification-unread-count'] });
  };
  const read = useMutation({ mutationFn: notificationApi.markRead, onSuccess: refresh });
  const readAll = useMutation({ mutationFn: notificationApi.markAllRead, onSuccess: refresh });
  const remove = useMutation({ mutationFn: notificationApi.remove, onSuccess: refresh });
  const error = read.error ?? readAll.error ?? remove.error;

  async function open(notification: UserNotification) {
    if (!notification.readAt) await read.mutateAsync(notification.id);
    navigate(notification.targetUrl);
  }

  return <Stack gap="lg">
    <Group justify="space-between" align="end">
      <div><Text c="indigo" fw={700} size="sm">NOTIFICATIONS</Text><Title order={1}>通知中心</Title><Text c="dimmed">面试和跟进任务的重要时间会显示在这里。</Text></div>
      <Button variant="light" onClick={() => readAll.mutate()} loading={readAll.isPending}>全部标为已读</Button>
    </Group>
    <Switch label="只看未读" checked={unreadOnly} onChange={(event) => { setUnreadOnly(event.currentTarget.checked); setPage(0); }} />
    {error && <Alert color="red" title="操作失败">{error.message}</Alert>}
    <QueryState loading={query.isPending} error={query.error} empty={query.data?.content.length === 0}>
      <Stack gap="sm">
        {query.data?.content.map((item) => <Card key={item.id} withBorder radius="lg" p="lg" bg={item.readAt ? undefined : 'indigo.0'}>
          <Group justify="space-between" align="flex-start" wrap="nowrap">
            <div><Group gap="xs"><Badge variant="light" color={item.type === 'TASK_OVERDUE' ? 'red' : 'indigo'}>{labels[item.type]}</Badge>{!item.readAt && <Badge color="red" size="xs">未读</Badge>}</Group><Text fw={700} mt="xs">{item.title}</Text><Text size="sm">{item.message}</Text><Text c="dimmed" size="xs" mt="xs">相关时间：{formatDate(item.eventAt)}</Text></div>
            <Group gap="xs"><Button size="xs" variant="light" onClick={() => void open(item)}>查看</Button><Button size="xs" color="red" variant="subtle" onClick={() => remove.mutate(item.id)}>删除</Button></Group>
          </Group>
        </Card>)}
        {(query.data?.totalPages ?? 0) > 1 && <Pagination value={page + 1} total={query.data?.totalPages ?? 1} onChange={(value) => setPage(value - 1)} />}
      </Stack>
    </QueryState>
  </Stack>;
}
