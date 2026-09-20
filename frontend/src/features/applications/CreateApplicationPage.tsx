import { Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router';
import { applicationApi } from './api';
import { ApplicationForm } from './ApplicationForm';

export function CreateApplicationPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const create = useMutation({
    mutationFn: applicationApi.create,
    onSuccess: async (application) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['applications'] }),
        queryClient.invalidateQueries({ queryKey: ['application-statistics'] }),
      ]);
      navigate(`/applications/${application.id}`);
    },
  });

  return <Stack gap="lg" maw={720}>
    <div><Text component={Link} to="/applications" size="sm" c="indigo">← 返回职位申请</Text><Title order={1} mt="sm">新增申请</Title><Text c="dimmed">先保存职位信息，之后再更新投递进展。</Text></div>
    <Card withBorder radius="lg" p="lg">
      <ApplicationForm initialValues={{ company: '', position: '', description: '' }} submitLabel="保存申请" cancelTo="/applications" pending={create.isPending} error={create.error} onSubmit={create.mutate} />
    </Card>
  </Stack>;
}
