import { MantineProvider } from '@mantine/core';
import { fireEvent, render, screen } from '@testing-library/react';
import { useState } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { DeleteApplicationDialog } from '../../src/features/applications/DeleteApplicationDialog';

vi.mock('@mantine/core', async (importOriginal) => {
  const original = await importOriginal<typeof import('@mantine/core')>();
  return {
    ...original,
    Modal: ({ opened, title, children }: { opened: boolean; title: string; children: React.ReactNode }) =>
      opened ? <div role="dialog" aria-label={title}><h2>{title}</h2>{children}</div> : null,
  };
});

describe('DeleteApplicationDialog', () => {
  it('does not delete on cancel and requires explicit confirmation', () => {
    const onConfirm = vi.fn();
    function Harness() {
      const [opened, setOpened] = useState(false);
      return <MantineProvider env="test"><button onClick={() => setOpened(true)}>打开删除确认</button><DeleteApplicationDialog opened={opened} company="Example Company" position="Engineer" pending={false} error={null} onClose={() => setOpened(false)} onConfirm={onConfirm} /></MantineProvider>;
    }

    render(<Harness />);
    expect(onConfirm).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: '打开删除确认' }));
    expect(screen.getByText('确定删除“Example Company · Engineer”吗？删除后无法恢复。')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '取消' }));
    expect(onConfirm).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '打开删除确认' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });
});
