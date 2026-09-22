import { expect, test } from '@playwright/test';

function localDateTimeAfter(hours: number): string {
  const date = new Date(Date.now() + hours * 60 * 60 * 1000);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}

test('authenticates users and keeps the application workflow private', async ({ page }) => {
  test.setTimeout(90_000);
  const unique = Date.now();
  const company = `Joblens E2E ${unique}`;
  const taskTitle = `Follow up ${unique}`;
  const firstEmail = `owner-${unique}@example.com`;
  const secondEmail = `other-${unique}@example.com`;
  const password = 'Secure-password-123';

    await page.goto('/register');
    await page.getByRole('textbox', { name: '昵称' }).fill('E2E Owner');
    await page.getByRole('textbox', { name: '邮箱' }).fill(firstEmail);
    await page.getByRole('textbox', { name: '密码', exact: true }).fill(password);
    await page.getByRole('textbox', { name: '确认密码' }).fill(password);
    await page.getByRole('button', { name: '创建账户' }).click();
    await expect(page).toHaveURL('/');

    await page.goto('/applications/new');
    await page.getByRole('textbox', { name: /公司/ }).fill(company);
    await page.getByRole('textbox', { name: /岗位/ }).fill('Backend Engineer');
    await page.getByRole('textbox', { name: /职位描述/ }).fill('End-to-end workflow verification');
    await page.getByRole('button', { name: '保存申请' }).click();

    await expect(page).toHaveURL(/\/applications\/\d+$/);
    const applicationId = Number(page.url().match(/\/applications\/(\d+)$/)?.[1]);
    expect(applicationId).toBeGreaterThan(0);
    await expect(page.getByRole('heading', { name: company })).toBeVisible();

    await page.locator('input[type="file"]').setInputFiles({
      name: 'joblens-resume.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('%PDF-1.7\nJoblens E2E resume'),
    });
    await page.getByRole('button', { name: '上传' }).click();
    await expect(page.getByText('joblens-resume.pdf')).toBeVisible();
    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('link', { name: '下载' }).click();
    expect((await downloadPromise).suggestedFilename()).toBe('joblens-resume.pdf');

    await page.getByRole('link', { name: '预约面试' }).click();
    await page.getByLabel(/开始时间/).fill(localDateTimeAfter(2));
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

    await page.goto('/notifications');
    await expect(page.getByRole('heading', { name: '通知中心' })).toBeVisible();
    await expect(page.getByText('面试即将开始', { exact: true })).toBeVisible({ timeout: 15_000 });
    await page.getByRole('button', { name: '查看' }).click();
    await expect(page).toHaveURL(new RegExp(`/applications/${applicationId}/interviews/\\d+$`));

    await page.getByRole('button', { name: '退出' }).click();
    await expect(page).toHaveURL('/login');
    await page.getByRole('link', { name: '创建账户' }).click();
    await page.getByRole('textbox', { name: '昵称' }).fill('Other User');
    await page.getByRole('textbox', { name: '邮箱' }).fill(secondEmail);
    await page.getByRole('textbox', { name: '密码', exact: true }).fill(password);
    await page.getByRole('textbox', { name: '确认密码' }).fill(password);
    await page.getByRole('button', { name: '创建账户' }).click();
    await expect(page).toHaveURL('/');
    await page.goto('/applications');
    await expect(page.getByText(company)).toHaveCount(0);

    await page.getByRole('button', { name: '退出' }).click();
    await page.getByRole('textbox', { name: '邮箱' }).fill(firstEmail);
    await page.getByRole('textbox', { name: '密码', exact: true }).fill(password);
    await page.getByRole('button', { name: '登录' }).click();
    await expect(page).toHaveURL('/');
    await page.goto('/applications');
    await expect(page.getByText(company)).toBeVisible();

    await page.goto(`/applications/${applicationId}`);
    await page.getByRole('button', { name: '删除申请' }).click();
    await page.getByRole('button', { name: '确认删除' }).click();
    await expect(page).toHaveURL('/applications');
});
