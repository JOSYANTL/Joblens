import { useState, type FormEvent, type ReactNode } from 'react';
import { Alert, Button, Group, NativeSelect, Stack, TextInput } from '@mantine/core';
import { Link } from 'react-router';
import { ApiError } from '../../shared/api/client';
import type { InterviewDetails, InterviewType } from '../../shared/api/types';
import { interviewTypeLabels } from './status';
import { toIsoInstant } from './time';

export type InterviewFormValues = {
  round: string;
  type: InterviewType;
  startsAt: string;
  durationMinutes: string;
  contact: string;
  meetingUrl: string;
  location: string;
};

type FormErrors = Partial<Record<keyof InterviewFormValues, string>>;

export const emptyInterviewForm: InterviewFormValues = {
  round: '1', type: 'VIDEO', startsAt: '', durationMinutes: '60',
  contact: '', meetingUrl: '', location: '',
};

export function validateInterview(values: InterviewFormValues): FormErrors {
  const errors: FormErrors = {};
  const round = Number(values.round);
  const duration = Number(values.durationMinutes);
  if (!Number.isInteger(round) || round < 1 || round > 100) errors.round = '轮次必须是 1–100 的整数';
  if (!Number.isInteger(duration) || duration < 1 || duration > 480) errors.durationMinutes = '时长必须是 1–480 分钟';
  if (!toIsoInstant(values.startsAt)) errors.startsAt = '请选择有效的开始时间';
  if (values.contact.length > 200) errors.contact = '联系人不能超过 200 个字符';
  if (values.location.length > 500) errors.location = '地点不能超过 500 个字符';
  if (values.meetingUrl.length > 2000) errors.meetingUrl = '会议链接不能超过 2000 个字符';
  if (values.meetingUrl.trim()) {
    try {
      const url = new URL(values.meetingUrl.trim());
      if (!['http:', 'https:'].includes(url.protocol) || !url.hostname || url.username || url.password) {
        errors.meetingUrl = '请输入不含账号密码的 HTTP(S) 链接';
      }
    } catch {
      errors.meetingUrl = '请输入有效的 HTTP(S) 链接';
    }
  }
  return errors;
}

export function InterviewForm({ initialValues, submitLabel, cancelTo, pending, error, errorAction, onSubmit }: {
  initialValues: InterviewFormValues;
  submitLabel: string;
  cancelTo: string;
  pending: boolean;
  error: Error | null;
  errorAction?: ReactNode;
  onSubmit: (details: InterviewDetails) => void;
}) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const serverErrors = error instanceof ApiError ? error.errors : {};

  function change(field: keyof InterviewFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateInterview(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;
    onSubmit({
      round: Number(values.round),
      type: values.type,
      startsAt: toIsoInstant(values.startsAt)!,
      durationMinutes: Number(values.durationMinutes),
      contact: values.contact.trim(),
      meetingUrl: values.meetingUrl.trim(),
      location: values.location.trim(),
    });
  }

  return <form onSubmit={submit} noValidate>
    <Stack gap="md">
      {error && <Alert color="red" title="保存失败">{error.message}{errorAction}</Alert>}
      <Group grow align="start">
        <TextInput label="轮次" type="number" min={1} max={100} required value={values.round} error={errors.round ?? serverErrors.round} onChange={(event) => change('round', event.currentTarget.value)} />
        <NativeSelect label="面试方式" required data={Object.entries(interviewTypeLabels).map(([value, label]) => ({ value, label }))} value={values.type} onChange={(event) => change('type', event.currentTarget.value)} />
      </Group>
      <Group grow align="start">
        <TextInput label="开始时间" type="datetime-local" required value={values.startsAt} error={errors.startsAt ?? serverErrors.startsAt} onChange={(event) => change('startsAt', event.currentTarget.value)} />
        <TextInput label="时长（分钟）" type="number" min={1} max={480} required value={values.durationMinutes} error={errors.durationMinutes ?? serverErrors.durationMinutes} onChange={(event) => change('durationMinutes', event.currentTarget.value)} />
      </Group>
      <TextInput label="联系人" placeholder="面试官或 HR" maxLength={200} value={values.contact} error={errors.contact ?? serverErrors.contact} onChange={(event) => change('contact', event.currentTarget.value)} />
      <TextInput label="会议链接" placeholder="https://…" maxLength={2000} value={values.meetingUrl} error={errors.meetingUrl ?? serverErrors.meetingUrl} onChange={(event) => change('meetingUrl', event.currentTarget.value)} />
      <TextInput label="地点" placeholder="办公室地址或其他地点" maxLength={500} value={values.location} error={errors.location ?? serverErrors.location} onChange={(event) => change('location', event.currentTarget.value)} />
      <Group justify="flex-end"><Button component={Link} to={cancelTo} variant="default">取消</Button><Button type="submit" loading={pending}>{submitLabel}</Button></Group>
    </Stack>
  </form>;
}
