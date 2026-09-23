import { Alert, Badge, Button, Card, FileInput, Group, Select, Stack, Text, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import type { ApplicationDocumentType } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { documentApi } from './api';

const labels: Record<ApplicationDocumentType, string> = {
  RESUME: '简历',
  JOB_DESCRIPTION: '职位描述',
  OTHER: '其他',
};

function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

export function validateDocumentFile(file: File | null) {
  if (!file) return '请选择要上传的文件。';
  if (file.size > 10 * 1024 * 1024) return '文件不能超过 10 MB。';
  if (!/\.(pdf|docx)$/i.test(file.name)) return '只支持 PDF 和 DOCX 文件。';
  return null;
}

export function ApplicationDocumentsCard({ applicationId, enabled }: { applicationId: number; enabled: boolean }) {
  const [type, setType] = useState<ApplicationDocumentType>('RESUME');
  const [file, setFile] = useState<File | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ['application-documents', applicationId],
    queryFn: () => documentApi.list(applicationId),
    enabled,
  });
  const upload = useMutation({
    mutationFn: () => documentApi.upload(applicationId, type, file!),
    onSuccess: async () => {
      setFile(null);
      setValidationError(null);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['application-documents', applicationId] }),
        queryClient.invalidateQueries({ queryKey: ['application-activities', applicationId] }),
      ]);
    },
  });
  const remove = useMutation({
    mutationFn: (documentId: number) => documentApi.remove(applicationId, documentId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['application-documents', applicationId] }),
        queryClient.invalidateQueries({ queryKey: ['application-activities', applicationId] }),
      ]);
    },
  });

  function submit() {
    const error = validateDocumentFile(file);
    if (error) return setValidationError(error);
    setValidationError(null);
    upload.mutate();
  }

  return <Card withBorder radius="lg" p="lg">
    <Title order={3} mb="xs">申请文档</Title>
    <Text c="dimmed" size="sm" mb="md">上传简历或职位描述。支持 PDF、DOCX，单个文件最大 10 MB。</Text>
    {(validationError || upload.error || remove.error) && <Alert color="red" title="文档操作失败" mb="md">{validationError ?? upload.error?.message ?? remove.error?.message}</Alert>}
    <Group align="end" mb="lg">
      <Select label="文档类型" value={type} onChange={(value) => setType(value as ApplicationDocumentType)} data={Object.entries(labels).map(([value, label]) => ({ value, label }))} w={160} />
      <FileInput label="选择文件" value={file} onChange={(value) => { setFile(value); setValidationError(null); }} accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" clearable flex={1} />
      <Button onClick={submit} loading={upload.isPending} disabled={!enabled}>上传</Button>
    </Group>
    <QueryState loading={query.isPending} error={query.error} empty={query.data?.length === 0}>
      <Stack gap="sm">{query.data?.map((document) => <Group key={document.id} justify="space-between" className="list-row" wrap="nowrap">
        <div><Group gap="xs"><Text fw={600}>{document.originalFileName}</Text><Badge variant="light">{labels[document.type]}</Badge></Group><Text size="xs" c="dimmed">{formatSize(document.sizeBytes)} · {formatDate(document.createdAt)}</Text></div>
        <Group gap="xs"><Button component="a" href={document.downloadUrl} variant="subtle" size="xs">下载</Button><Button color="red" variant="subtle" size="xs" loading={remove.isPending && remove.variables === document.id} onClick={() => remove.mutate(document.id)}>删除</Button></Group>
      </Group>)}</Stack>
    </QueryState>
  </Card>;
}
