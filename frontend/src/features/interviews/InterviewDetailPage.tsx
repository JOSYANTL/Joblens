import { useState } from 'react';
import { Alert, Badge, Button, Card, Divider, Group, Modal, Stack, Text, Title } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import type { InterviewFeedback, InterviewStatus } from '../../shared/api/types';
import { interviewApi } from './api';
import { FeedbackForm } from './FeedbackForm';
import { interviewStatusColors, interviewStatusLabels, interviewTypeLabels } from './status';

export function InterviewDetailPage() {
  const params = useParams();
  const applicationId = Number(params.applicationId);
  const interviewId = Number(params.interviewId);
  const validIds = [applicationId, interviewId].every((id) => Number.isSafeInteger(id) && id > 0);
  const queryClient = useQueryClient();
  const [cancelOpened, { open: openCancel, close: closeCancel }] = useDisclosure(false);
  const [notice, setNotice] = useState<string | null>(null);
  const interview = useQuery({ queryKey: ['interview', applicationId, interviewId], queryFn: () => interviewApi.get(applicationId, interviewId), enabled: validIds });

  async function refreshCollections() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['interviews'] }),
      queryClient.invalidateQueries({ queryKey: ['upcoming-interviews'] }),
    ]);
  }

  const changeStatus = useMutation({
    mutationFn: (status: InterviewStatus) => interviewApi.changeStatus(applicationId, interviewId, status, interview.data!.version),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['interview', applicationId, interviewId], saved);
      closeCancel();
      setNotice('面试状态已更新。');
      await refreshCollections();
    },
  });
  const saveFeedback = useMutation({
    mutationFn: (feedback: InterviewFeedback) => interviewApi.saveFeedback(applicationId, interviewId, feedback, interview.data!.version),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['interview', applicationId, interviewId], saved);
      setNotice('面试反馈已保存。');
      await refreshCollections();
    },
  });

  async function reloadLatest() {
    const result = await interview.refetch();
    if (result.isSuccess) {
      changeStatus.reset();
      saveFeedback.reset();
      closeCancel();
      setNotice('已加载最新版本，请核对后重试。');
    }
  }

  if (!validIds) return <Alert color="red" title="无效的面试编号">请从面试列表进入详情页。</Alert>;

  const item = interview.data;
  const hasStarted = item ? new Date(item.startsAt).getTime() <= Date.now() : false;
  const statusConflict = changeStatus.error instanceof ApiError && changeStatus.error.status === 409;
  const feedbackConflict = saveFeedback.error instanceof ApiError && saveFeedback.error.status === 409;

  return <Stack gap="lg">
    <Group justify="space-between" align="end">
      <div><Text component={Link} to="/interviews" size="sm" c="indigo">← 返回面试列表</Text><Title order={1} mt="sm">面试详情</Title></div>
      {item?.status === 'SCHEDULED' && <Button component={Link} to={`/applications/${applicationId}/interviews/${interviewId}/edit`} variant="light">编辑安排</Button>}
    </Group>
    {notice && <Alert color="green">{notice}</Alert>}
    <QueryState loading={interview.isPending} error={interview.error} empty={false}>
      {item && <>
        <Card withBorder radius="lg" p="lg">
          <Group justify="space-between" align="start"><div><Text c="dimmed" size="sm">申请 #{item.applicationId} · 面试 #{item.id}</Text><Title order={2}>第 {item.round} 轮 · {interviewTypeLabels[item.type]}</Title><Text component={Link} to={`/applications/${item.applicationId}`} size="sm" c="indigo">查看职位申请 →</Text></div><Badge color={interviewStatusColors[item.status]} variant="light" size="lg">{interviewStatusLabels[item.status]}</Badge></Group>
          <Divider my="lg" />
          <Stack gap="xs"><Text><strong>开始：</strong>{formatDate(item.startsAt)}</Text><Text><strong>结束：</strong>{formatDate(item.endsAt)}（{item.durationMinutes} 分钟）</Text>{item.contact && <Text><strong>联系人：</strong>{item.contact}</Text>}{item.location && <Text><strong>地点：</strong>{item.location}</Text>}{item.meetingUrl && <Text><strong>会议链接：</strong><a href={item.meetingUrl} target="_blank" rel="noreferrer">打开会议链接</a></Text>}</Stack>
        </Card>
        {item.status === 'SCHEDULED' && <Card withBorder radius="lg" p="lg">
          <Title order={3} mb="xs">面试进展</Title>
          <Text size="sm" c="dimmed" mb="lg">面试开始后可标记完成；取消后不能再修改。</Text>
          {changeStatus.isError && <Alert color="red" title="状态更新失败" mb="md">{changeStatus.error.message}{statusConflict && <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={interview.isFetching}>加载最新数据</Button>}</Alert>}
          <Group><Button color="green" disabled={!hasStarted || changeStatus.isPending} loading={changeStatus.isPending} onClick={() => changeStatus.mutate('COMPLETED')}>标记完成</Button><Button color="red" variant="light" disabled={changeStatus.isPending} onClick={openCancel}>取消面试</Button></Group>
          {!hasStarted && <Text size="sm" c="dimmed" mt="sm">面试开始时间尚未到，暂不能标记完成。</Text>}
        </Card>}
        {item.status === 'COMPLETED' && <Card withBorder radius="lg" p="lg">
          <Title order={3} mb="xs">面试反馈</Title>
          <Text size="sm" c="dimmed" mb="lg">记录问题、总结和下一步，可再次编辑。</Text>
          <FeedbackForm key={`${item.id}-${item.version}`} initialValues={{ questions: item.feedback?.questions ?? '', summary: item.feedback?.summary ?? '', nextSteps: item.feedback?.nextSteps ?? '' }} pending={saveFeedback.isPending} error={saveFeedback.error} errorAction={feedbackConflict ? <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={interview.isFetching}>加载最新版本</Button> : undefined} onSubmit={(feedback) => { setNotice(null); saveFeedback.mutate(feedback); }} />
        </Card>}
      </>}
    </QueryState>
    <Modal opened={cancelOpened} onClose={closeCancel} title="确认取消面试" centered>
      <Stack gap="md"><Text>确定取消这场面试吗？取消后无法改期或标记完成。</Text>{changeStatus.isError && <Alert color="red">{changeStatus.error.message}{statusConflict && <Button variant="subtle" size="compact-sm" onClick={reloadLatest}>加载最新数据</Button>}</Alert>}<Group justify="flex-end"><Button variant="default" onClick={closeCancel}>返回</Button><Button color="red" loading={changeStatus.isPending} onClick={() => changeStatus.mutate('CANCELLED')}>确认取消</Button></Group></Stack>
    </Modal>
  </Stack>;
}
