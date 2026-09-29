import { Alert, Center, Loader, Stack, Text } from '@mantine/core';

export function QueryState({ loading, error, empty, emptyText = '数据会在创建后显示在这里。', children }: {
  loading: boolean;
  error: Error | null;
  empty: boolean;
  emptyText?: string;
  children: React.ReactNode;
}) {
  if (loading) return <Center py="xl"><Loader size="sm" /></Center>;
  if (error) return <Alert color="red" title="加载失败">{error.message}</Alert>;
  if (empty) return <Center py="xl"><Stack gap={4} align="center"><Text fw={600}>暂无数据</Text><Text c="dimmed" size="sm">{emptyText}</Text></Stack></Center>;
  return children;
}
