import { useState } from 'react';
import { Alert, Button, Card, Group, Stack, Text, Textarea, Title } from '@mantine/core';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ApplicationNote } from '../../shared/api/types';
import { QueryState } from '../../shared/components/QueryState';
import { formatDate } from '../../shared/format';
import { activityApi } from './api';

export function ApplicationNotesCard({ applicationId, enabled }: { applicationId: number; enabled: boolean }) {
  const queryClient = useQueryClient();
  const [content, setContent] = useState('');
  const notes = useQuery({
    queryKey: ['application-notes', applicationId],
    queryFn: () => activityApi.notes(applicationId),
    enabled,
  });
  const refresh = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['application-notes', applicationId] }),
      queryClient.invalidateQueries({ queryKey: ['application-activities', applicationId] }),
    ]);
  };
  const create = useMutation({
    mutationFn: () => activityApi.createNote(applicationId, content.trim()),
    onSuccess: async () => { setContent(''); await refresh(); },
  });
  const update = useMutation({
    mutationFn: ({ note, value }: { note: ApplicationNote; value: string }) =>
      activityApi.updateNote(applicationId, note.id, value.trim(), note.version),
    onSuccess: refresh,
  });
  const remove = useMutation({
    mutationFn: (note: ApplicationNote) => activityApi.deleteNote(applicationId, note.id, note.version),
    onSuccess: refresh,
  });

  return <Card withBorder radius="lg" p="lg">
    <Title order={3}>备注</Title>
    <Text c="dimmed" size="sm" mt={4} mb="md">记录招聘联系人、准备事项或其他只对你可见的信息。</Text>
    <Textarea label="添加备注" placeholder="例如：招聘经理希望下周二上午沟通" value={content}
      onChange={(event) => setContent(event.currentTarget.value)} maxLength={5000} autosize minRows={2} />
    <Group justify="space-between" mt="xs" mb="lg">
      <Text size="xs" c="dimmed">{content.length}/5000</Text>
      <Button size="sm" onClick={() => create.mutate()} loading={create.isPending}
        disabled={!content.trim()}>保存备注</Button>
    </Group>
    {create.isError && <Alert color="red" mb="md">{create.error.message}</Alert>}
    <QueryState loading={notes.isPending} error={notes.error} empty={notes.data?.length === 0} emptyText="还没有备注。">
      <Stack gap="sm">{notes.data?.map((note) => <NoteItem key={note.id} note={note}
        busy={update.isPending || remove.isPending}
        onSave={(value) => update.mutate({ note, value })}
        onDelete={() => remove.mutate(note)} />)}</Stack>
    </QueryState>
    {(update.isError || remove.isError) && <Alert color="red" mt="md">
      {(update.error ?? remove.error)?.message}
    </Alert>}
  </Card>;
}

function NoteItem({ note, busy, onSave, onDelete }: {
  note: ApplicationNote; busy: boolean; onSave: (value: string) => void; onDelete: () => void;
}) {
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState(note.content);
  return <div className="list-row">
    {editing ? <Stack gap="xs">
      <Textarea aria-label="编辑备注" value={value} onChange={(event) => setValue(event.currentTarget.value)}
        maxLength={5000} autosize minRows={2} />
      <Group justify="flex-end"><Button size="xs" variant="default" onClick={() => { setValue(note.content); setEditing(false); }}>取消</Button>
        <Button size="xs" loading={busy} disabled={!value.trim()} onClick={() => { onSave(value); setEditing(false); }}>保存</Button></Group>
    </Stack> : <>
      <Text style={{ whiteSpace: 'pre-wrap' }}>{note.content}</Text>
      <Group justify="space-between" mt="xs"><Text size="xs" c="dimmed">更新于 {formatDate(note.updatedAt)}</Text>
        <Group gap="xs"><Button size="compact-xs" variant="subtle" onClick={() => setEditing(true)}>编辑</Button>
          <Button size="compact-xs" variant="subtle" color="red" loading={busy} onClick={onDelete}>删除</Button></Group></Group>
    </>}
  </div>;
}
