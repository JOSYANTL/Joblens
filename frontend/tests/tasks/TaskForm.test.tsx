import { MantineProvider } from '@mantine/core';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { toIsoInstant } from '../../src/features/interviews/time';
import { emptyTaskForm, TaskForm, validateTask } from '../../src/features/tasks/TaskForm';

describe('TaskForm', () => {
  it('validates title, notes and due date', () => {
    expect(validateTask({ title: ' ', notes: 'x'.repeat(10001), dueAt: '2026-02-30T14:30' })).toMatchObject({
      title: expect.any(String), notes: expect.any(String), dueAt: expect.any(String),
    });
    expect(validateTask({ title: 'Send email', notes: '', dueAt: '2026-09-20T14:30' })).toEqual({});
  });

  it('submits an ISO instant and trimmed text', () => {
    const onSubmit = vi.fn();
    render(<MantineProvider env="test"><MemoryRouter><TaskForm initialValues={{ ...emptyTaskForm, dueAt: '2026-09-20T14:30' }} submitLabel="创建任务" cancelTo="/applications/7" pending={false} error={null} onSubmit={onSubmit} /></MemoryRouter></MantineProvider>);

    fireEvent.change(screen.getByRole('textbox', { name: '任务标题' }), { target: { value: '  Follow up  ' } });
    fireEvent.change(screen.getByRole('textbox', { name: '备注' }), { target: { value: '  Email HR  ' } });
    fireEvent.click(screen.getByRole('button', { name: '创建任务' }));

    expect(onSubmit).toHaveBeenCalledWith({ title: 'Follow up', notes: 'Email HR', dueAt: toIsoInstant('2026-09-20T14:30') });
  });
});
