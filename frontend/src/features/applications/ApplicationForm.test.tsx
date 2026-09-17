import { MantineProvider } from '@mantine/core';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router';
import { describe, expect, it, vi } from 'vitest';
import { ApplicationForm } from './ApplicationForm';

describe('ApplicationForm', () => {
  it('validates required fields and submits trimmed values', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<MantineProvider env="test"><MemoryRouter><ApplicationForm initialValues={{ company: '', position: '', description: '' }} submitLabel="保存申请" cancelTo="/applications" pending={false} error={null} onSubmit={onSubmit} /></MemoryRouter></MantineProvider>);

    await user.click(screen.getByRole('button', { name: '保存申请' }));
    expect(screen.getByText('请输入公司名称')).toBeInTheDocument();
    expect(screen.getByText('请输入岗位名称')).toBeInTheDocument();
    expect(screen.getByText('请输入职位描述')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();

    await user.type(screen.getByRole('textbox', { name: /公司/ }), '  Example Company  ');
    await user.type(screen.getByRole('textbox', { name: /岗位/ }), '  Backend Engineer  ');
    await user.type(screen.getByRole('textbox', { name: /职位描述/ }), '  Java and PostgreSQL  ');
    await user.click(screen.getByRole('button', { name: '保存申请' }));

    expect(onSubmit).toHaveBeenCalledWith({ company: 'Example Company', position: 'Backend Engineer', description: 'Java and PostgreSQL' });
  });
});
