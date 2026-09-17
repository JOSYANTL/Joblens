import { Alert, Button, Group, Modal, Stack, Text } from '@mantine/core';

export function DeleteApplicationDialog({ opened, company, position, pending, error, onClose, onConfirm }: {
  opened: boolean;
  company: string;
  position: string;
  pending: boolean;
  error: Error | null;
  onClose: () => void;
  onConfirm: () => void;
}) {
  return <Modal opened={opened} onClose={onClose} title="确认删除申请" centered>
    <Stack gap="md">
      <Text>确定删除“{company} · {position}”吗？删除后无法恢复。</Text>
      {error && <Alert color="red" title="删除失败">{error.message}</Alert>}
      <Group justify="flex-end"><Button variant="default" onClick={onClose} disabled={pending}>取消</Button><Button color="red" onClick={onConfirm} loading={pending}>确认删除</Button></Group>
    </Stack>
  </Modal>;
}
