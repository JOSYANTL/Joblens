import { Badge, Button, Group } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import { notificationApi } from './api';

export function NotificationButton() {
  const unread = useQuery({
    queryKey: ['notification-unread-count'],
    queryFn: notificationApi.unreadCount,
    refetchInterval: 30_000,
  });

  return <Button component={Link} to="/notifications" variant="subtle" size="sm">
    <Group gap={6}>通知{Boolean(unread.data?.count) && <Badge size="sm" circle color="red">{unread.data!.count}</Badge>}</Group>
  </Button>;
}

