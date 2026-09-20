import { useState, type FormEvent, type ReactNode } from 'react';
import { Alert, Button, Group, Stack, Textarea, TextInput } from '@mantine/core';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/client';

export type ApplicationFormValues = { company: string; position: string; description: string };
type FormErrors = Partial<Record<keyof ApplicationFormValues, string>>;

export function validateApplication(values: ApplicationFormValues): FormErrors {
  const errors: FormErrors = {};
  if (!values.company.trim()) errors.company = '请输入公司名称';
  else if (values.company.trim().length > 150) errors.company = '公司名称不能超过 150 个字符';
  if (!values.position.trim()) errors.position = '请输入岗位名称';
  else if (values.position.trim().length > 150) errors.position = '岗位名称不能超过 150 个字符';
  if (!values.description.trim()) errors.description = '请输入职位描述';
  return errors;
}

export function ApplicationForm({ initialValues, submitLabel, cancelTo, pending, error, errorAction, onSubmit }: {
  initialValues: ApplicationFormValues;
  submitLabel: string;
  cancelTo: string;
  pending: boolean;
  error: Error | null;
  errorAction?: ReactNode;
  onSubmit: (values: ApplicationFormValues) => void;
}) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const serverErrors = error instanceof ApiError ? error.errors : {};

  function change(field: keyof ApplicationFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateApplication(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;
    onSubmit({
      company: values.company.trim(),
      position: values.position.trim(),
      description: values.description.trim(),
    });
  }

  return <form onSubmit={submit} noValidate>
    <Stack gap="md">
      {error && <Alert color="red" title="保存失败">{error.message}{errorAction}</Alert>}
      <TextInput label="公司" placeholder="例如 Example Company" required value={values.company} maxLength={150} error={errors.company ?? serverErrors.company} onChange={(event) => change('company', event.currentTarget.value)} />
      <TextInput label="岗位" placeholder="例如 Backend Engineer" required value={values.position} maxLength={150} error={errors.position ?? serverErrors.position} onChange={(event) => change('position', event.currentTarget.value)} />
      <Textarea label="职位描述" placeholder="记录岗位要求、链接或其他关键信息" required minRows={6} autosize value={values.description} error={errors.description ?? serverErrors.description} onChange={(event) => change('description', event.currentTarget.value)} />
      <Group justify="flex-end"><Button component={Link} to={cancelTo} variant="default">取消</Button><Button type="submit" loading={pending}>{submitLabel}</Button></Group>
    </Stack>
  </form>;
}
