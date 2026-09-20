import { useState } from 'react';
import { Alert, Button, Card, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { QueryState } from '../../shared/components/QueryState';
import { applicationApi } from './api';
import { ApplicationForm, type ApplicationFormValues } from './ApplicationForm';

export function EditApplicationPage() {
  const { id: rawId } = useParams();
  const id = Number(rawId);
  const validId = Number.isSafeInteger(id) && id > 0;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [reloaded, setReloaded] = useState(false);
  const application = useQuery({ queryKey: ['application', id], queryFn: () => applicationApi.get(id), enabled: validId });
  const update = useMutation({
    mutationFn: (values: ApplicationFormValues) => applicationApi.update(id, { ...values, version: application.data!.version }),
    onSuccess: async (saved) => {
      queryClient.setQueryData(['application', id], saved);
      await queryClient.invalidateQueries({ queryKey: ['applications'] });
      navigate(`/applications/${id}`);
    },
  });

  async function reloadLatest() {
    const result = await application.refetch();
    if (result.isSuccess) {
      update.reset();
      setReloaded(true);
    }
  }

  if (!validId) return <Alert color="red" title="无效的申请编号">请从职位申请列表进入编辑页。</Alert>;

  const conflict = update.error instanceof ApiError && update.error.status === 409;

  return <Stack gap="lg" maw={720}>
    <div><Text component={Link} to={`/applications/${id}`} size="sm" c="indigo">← 返回申请详情</Text><Title order={1} mt="sm">编辑申请</Title><Text c="dimmed">修改公司、岗位或职位描述。</Text></div>
    <QueryState loading={application.isPending} error={application.error} empty={false}>
      {application.data && <Card withBorder radius="lg" p="lg">
        {reloaded && <Alert color="blue" mb="md">已加载最新版本，请核对内容后重新提交。</Alert>}
        <ApplicationForm
          key={`${id}-${application.data.version}`}
          initialValues={{ company: application.data.company, position: application.data.position, description: application.data.description }}
          submitLabel="保存修改"
          cancelTo={`/applications/${id}`}
          pending={update.isPending}
          error={update.error}
          errorAction={conflict ? <Button variant="subtle" size="compact-sm" onClick={reloadLatest} loading={application.isFetching}>加载最新版本</Button> : undefined}
          onSubmit={(values) => { setReloaded(false); update.mutate(values); }}
        />
      </Card>}
    </QueryState>
  </Stack>;
}
