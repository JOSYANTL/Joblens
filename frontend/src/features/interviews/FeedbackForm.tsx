import { useState, type FormEvent, type ReactNode } from 'react';
import { Alert, Button, Stack, Textarea } from '@mantine/core';
import type { InterviewFeedback } from '../../shared/api/types';

export function FeedbackForm({ initialValues, pending, error, errorAction, onSubmit }: {
  initialValues: InterviewFeedback;
  pending: boolean;
  error: Error | null;
  errorAction?: ReactNode;
  onSubmit: (feedback: InterviewFeedback) => void;
}) {
  const [values, setValues] = useState(initialValues);
  const [validationError, setValidationError] = useState<string | null>(null);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (Object.values(values).some((value) => value.length > 10_000)) {
      setValidationError('每个反馈字段不能超过 10000 个字符');
      return;
    }
    setValidationError(null);
    onSubmit({
      questions: values.questions.trim(), summary: values.summary.trim(), nextSteps: values.nextSteps.trim(),
    });
  }

  return <form onSubmit={submit} noValidate>
    <Stack gap="md">
      {error && <Alert color="red" title="保存反馈失败">{error.message}{errorAction}</Alert>}
      {validationError && <Alert color="red">{validationError}</Alert>}
      <Textarea label="面试问题" minRows={3} autosize maxLength={10_000} value={values.questions} onChange={(event) => setValues({ ...values, questions: event.currentTarget.value })} />
      <Textarea label="总结" minRows={3} autosize maxLength={10_000} value={values.summary} onChange={(event) => setValues({ ...values, summary: event.currentTarget.value })} />
      <Textarea label="下一步" minRows={3} autosize maxLength={10_000} value={values.nextSteps} onChange={(event) => setValues({ ...values, nextSteps: event.currentTarget.value })} />
      <Button type="submit" loading={pending} style={{ alignSelf: 'flex-end' }}>保存反馈</Button>
    </Stack>
  </form>;
}
