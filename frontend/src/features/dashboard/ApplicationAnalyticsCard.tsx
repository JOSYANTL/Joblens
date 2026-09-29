import { Box, Card, Group, Progress, SimpleGrid, Stack, Text, Title } from '@mantine/core';
import { useQuery } from '@tanstack/react-query';
import { applicationApi } from '../applications/api';
import { QueryState } from '../../shared/components/QueryState';

function percent(numerator: number, denominator: number): number {
  return denominator === 0 ? 0 : Math.round((numerator / denominator) * 100);
}

export function ApplicationAnalyticsCard() {
  const analytics = useQuery({ queryKey: ['application-analytics'], queryFn: applicationApi.analytics });
  const weeks = analytics.data?.weeklyApplications ?? [];
  const conversion = analytics.data?.conversion;
  const maximum = Math.max(1, ...weeks.map((week) => week.count));
  const interviewRate = percent(conversion?.interviewed ?? 0, conversion?.applied ?? 0);
  const offerRate = percent(conversion?.offered ?? 0, conversion?.interviewed ?? 0);

  return <SimpleGrid cols={{ base: 1, lg: 2 }} spacing="lg">
    <Card withBorder radius="lg" p="lg">
      <Title order={3}>每周新增申请</Title>
      <Text c="dimmed" size="sm" mt={4} mb="lg">最近 12 周，按申请创建时间统计。</Text>
      <QueryState loading={analytics.isPending} error={analytics.error} empty={false}>
        <Box style={{ overflowX: 'auto' }}>
          <Group align="end" gap="xs" wrap="nowrap" miw={600} h={170} role="img"
            aria-label={`最近 12 周每周新增申请数量：${weeks.map((week) => `${week.weekStart} ${week.count} 个`).join('，')}`}>
            {weeks.map((week) => <Stack key={week.weekStart} align="center" gap={4} style={{ flex: 1 }}>
              <Text size="xs" fw={700}>{week.count}</Text>
              <Box bg={week.count ? 'indigo.5' : 'gray.3'} w="100%" maw={30} h={Math.max(4, week.count / maximum * 110)}
                style={{ borderRadius: '4px 4px 0 0' }} title={`${week.weekStart}：${week.count} 个申请`} />
              <Text size="xs" c="dimmed">{week.weekStart.slice(5).replace('-', '/')}</Text>
            </Stack>)}
          </Group>
        </Box>
      </QueryState>
    </Card>
    <Card withBorder radius="lg" p="lg">
      <Title order={3}>申请转化</Title>
      <Text c="dimmed" size="sm" mt={4} mb="lg">历史累计去重；曾进入下一阶段的申请仍计入该阶段。</Text>
      <QueryState loading={analytics.isPending} error={analytics.error} empty={false}>
        <SimpleGrid cols={3} mb="lg">
          <Stage label="已投递" count={conversion?.applied ?? 0} />
          <Stage label="进入面试" count={conversion?.interviewed ?? 0} />
          <Stage label="获得 Offer" count={conversion?.offered ?? 0} />
        </SimpleGrid>
        <Stack gap="md">
          <div>
            <Group justify="space-between" mb={4}><Text size="sm">投递 → 面试</Text><Text size="sm" fw={700}>{interviewRate}%</Text></Group>
            <Progress value={interviewRate} color="violet" aria-label="投递到面试转化率" />
          </div>
          <div>
            <Group justify="space-between" mb={4}><Text size="sm">面试 → Offer</Text><Text size="sm" fw={700}>{offerRate}%</Text></Group>
            <Progress value={offerRate} color="green" aria-label="面试到 Offer 转化率" />
          </div>
        </Stack>
      </QueryState>
    </Card>
  </SimpleGrid>;
}

function Stage({ label, count }: { label: string; count: number }) {
  return <div><Text size="sm" c="dimmed">{label}</Text><Text size="xl" fw={800}>{count}</Text></div>;
}
