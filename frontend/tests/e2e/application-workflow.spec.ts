import { expect, test } from '@playwright/test';

function localDateTimeAfter(hours: number): string {
  const date = new Date(Date.now() + hours * 60 * 60 * 1000);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}

test('creates an application, schedules an interview, and completes a follow-up task', async ({ page, request }) => {
  const unique = Date.now();
  const company = `Joblens E2E ${unique}`;
  const taskTitle = `Follow up ${unique}`;
  let applicationId: number | undefined;

  try {
    await page.goto('/applications/new');
    await page.getByRole('textbox', { name: /公司/ }).fill(company);
    await page.getByRole('textbox', { name: /岗位/ }).fill('Backend Engineer');
    await page.getByRole('textbox', { name: /职位描述/ }).fill('End-to-end workflow verification');
    await page.getByRole('button', { name: '保存申请' }).click();

    await expect(page).toHaveURL(/\/applications\/\d+$/);
    applicationId = Number(page.url().match(/\/applications\/(\d+)$/)?.[1]);
    expect(applicationId).toBeGreaterThan(0);
    await expect(page.getByRole('heading', { name: company })).toBeVisible();

    await page.getByRole('link', { name: '预约面试' }).click();
    await page.getByLabel(/开始时间/).fill(localDateTimeAfter(24));
    await page.getByRole('button', { name: '保存面试' }).click();
    await expect(page).toHaveURL(new RegExp(`/applications/${applicationId}/interviews/\\d+$`));
    await expect(page.getByRole('heading', { name: '面试详情' })).toBeVisible();
    await expect(page.getByRole('heading', { name: '第 1 轮 · 视频' })).toBeVisible();

    await page.getByRole('link', { name: '查看职位申请' }).click();
    await page.getByRole('link', { name: '新建任务' }).click();
    await page.getByRole('textbox', { name: /任务标题/ }).fill(taskTitle);
    await page.getByLabel(/截止时间/).fill(localDateTimeAfter(48));
    await page.getByRole('textbox', { name: '备注' }).fill('Send a thank-you email');
    await page.getByRole('button', { name: '创建任务' }).click();

    await expect(page).toHaveURL(new RegExp(`/applications/${applicationId}/tasks/\\d+$`));
    await expect(page.getByRole('heading', { name: taskTitle })).toBeVisible();
    await page.getByRole('button', { name: '标记完成' }).click();
    await expect(page.getByText('任务状态已更新。')).toBeVisible();
    await expect(page.getByText('已完成', { exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: '重新打开' })).toBeVisible();
  } finally {
    if (applicationId) {
      const response = await request.delete(`/api/applications/${applicationId}`);
      expect(response.ok()).toBeTruthy();
    }
  }
});
