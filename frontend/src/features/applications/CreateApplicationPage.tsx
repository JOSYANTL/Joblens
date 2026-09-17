import { useState, type FormEvent } from 'react';
import { Alert, Button, Card, Group, Stack, Text, Textarea, TextInput, Title } from '@mantine/core';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router';
import { ApiError } from '../../shared/api/client';
import { applicationApi } from './api';

type FormValues = { company: string; position: string; description: string };
type FormErrors = Partial<Record<keyof FormValues, string>>;

function validate(values: FormValues): FormErrors {
  const errors: FormErrors = {};
  if (!values.company.trim()) errors.company = '请输入公司名称';
  else if (values.company.trim().length > 150) errors.company = '公司名称不能超过 150 个字符';
  if (!values.position.trim()) errors.position = '请输入岗位名称';
  else if (values.position.trim().length > 150) errors.position = '岗位名称不能超过 150 个字符';
  if (!values.description.trim()) errors.description = '请输入职位描述';
  return errors;
}

export function CreateApplicationPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [values, setValues] = useState<FormValues>({ company: '', position: '', description: '' });
  const [errors, setErrors] = useState<FormErrors>({});
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

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validate(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;
    create.mutate({
      company: values.company.trim(),
      position: values.position.trim(),
      description: values.description.trim(),
    });
  }

  const serverErrors = create.error instanceof ApiError ? create.error.errors : {};

  return <Stack gap="lg" maw={720}>
    <div><Text component={Link} to="/applications" size="sm" c="indigo">← 返回职位申请</Text><Title order={1} mt="sm">新增申请</Title><Text c="dimmed">先保存职位信息，之后再更新投递进展。</Text></div>
    <Card withBorder radius="lg" p="lg">
      <form onSubmit={submit} noValidate>
        <Stack gap="md">
          {create.isError && <Alert color="red" title="保存失败">{create.error.message}</Alert>}
          <TextInput label="公司" placeholder="例如 Example Company" required value={values.company} maxLength={150} error={errors.company ?? serverErrors.company} onChange={(event) => setValues({ ...values, company: event.currentTarget.value })} />
          <TextInput label="岗位" placeholder="例如 Backend Engineer" required value={values.position} maxLength={150} error={errors.position ?? serverErrors.position} onChange={(event) => setValues({ ...values, position: event.currentTarget.value })} />
          <Textarea label="职位描述" placeholder="记录岗位要求、链接或其他关键信息" required minRows={6} autosize value={values.description} error={errors.description ?? serverErrors.description} onChange={(event) => setValues({ ...values, description: event.currentTarget.value })} />
          <Group justify="flex-end"><Button component={Link} to="/applications" variant="default">取消</Button><Button type="submit" loading={create.isPending}>保存申请</Button></Group>
        </Stack>
      </form>
    </Card>
  </Stack>;
}
