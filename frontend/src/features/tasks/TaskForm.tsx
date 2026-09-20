import { useState, type FormEvent, type ReactNode } from 'react';
import { Alert, Button, Group, Stack, Textarea, TextInput } from '@mantine/core';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/client';
import type { FollowUpTaskDetails } from '../../shared/api/types';
import { toIsoInstant } from '../interviews/time';

export type TaskFormValues = { title: string; notes: string; dueAt: string };
export const emptyTaskForm: TaskFormValues = { title: '', notes: '', dueAt: '' };
type FormErrors = Partial<Record<keyof TaskFormValues, string>>;

export function validateTask(values: TaskFormValues): FormErrors {
  const errors: FormErrors = {};
  if (!values.title.trim() || values.title.length > 200) errors.title = '标题需要 1–200 个字符';
  if (values.notes.length > 10000) errors.notes = '备注不能超过 10000 个字符';
  if (!toIsoInstant(values.dueAt)) errors.dueAt = '请选择有效的截止时间';
  return errors;
}

export function TaskForm({ initialValues, submitLabel, cancelTo, pending, error, errorAction, onSubmit }: {
  initialValues: TaskFormValues;
  submitLabel: string;
  cancelTo: string;
  pending: boolean;
  error: Error | null;
  errorAction?: ReactNode;
  onSubmit: (details: FollowUpTaskDetails) => void;
}) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const serverErrors = error instanceof ApiError ? error.errors : {};

  function change(field: keyof TaskFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateTask(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    onSubmit({ title: values.title.trim(), notes: values.notes.trim(), dueAt: toIsoInstant(values.dueAt)! });
  }

  return <form onSubmit={submit} noValidate><Stack gap="md">
    {error && <Alert color="red" title="保存失败">{error.message}{errorAction}</Alert>}
    <TextInput label="任务标题" required maxLength={200} value={values.title} error={errors.title ?? serverErrors.title} onChange={(event) => change('title', event.currentTarget.value)} />
    <TextInput label="截止时间" type="datetime-local" required value={values.dueAt} error={errors.dueAt ?? serverErrors.dueAt} onChange={(event) => change('dueAt', event.currentTarget.value)} />
    <Textarea label="备注" minRows={4} maxLength={10000} value={values.notes} error={errors.notes ?? serverErrors.notes} onChange={(event) => change('notes', event.currentTarget.value)} />
    <Group justify="flex-end"><Button component={Link} to={cancelTo} variant="default">取消</Button><Button type="submit" loading={pending}>{submitLabel}</Button></Group>
  </Stack></form>;
}
