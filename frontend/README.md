# Joblens Frontend / 前端

Tech stack / 技术栈：React、TypeScript、Vite、Mantine、React Router、TanStack Query。

## Local development / 本地启动

Requires Node.js 22.12+, 24.x, or 26+; odd-numbered Node.js releases are not
supported. Start PostgreSQL and the Spring Boot service in `backend` first,
then run:

需要 Node.js 22.12+、24.x 或 26+（不支持奇数版本的 Node.js）。先启动仓库根目录的
PostgreSQL 和 `backend` Spring Boot 服务，再运行：

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed in the terminal, usually `http://localhost:5173`.
The Vite development server proxies `/api` and `/actuator` to
`http://localhost:8080`, so no additional CORS configuration is required for
local development.

打开终端显示的本地地址（通常是 `http://localhost:5173`）。Vite 开发服务器会把
`/api` 和 `/actuator` 代理到 `http://localhost:8080`，因此本地无需单独配置跨域。

## Project structure / 目录结构

- `src/app`: application layout, routes, and theme / 应用布局、路由与主题
- `src/features`: feature-oriented pages and APIs / 按业务能力划分的页面与接口
- `src/shared`: shared API helpers, types, components, and formatters / 通用 API、类型、组件及格式化工具
- `tests`: frontend tests and test setup, kept separate from production code / 前端测试及其模拟环境，与业务代码分开

The application provides dashboard, job application, interview, follow-up task,
and notification-center pages. Job applications support creation, details,
editing, deletion, and workflow-based status changes. Interviews can be
scheduled, rescheduled, cancelled, completed, and supplemented with feedback.
Follow-up tasks support filtering, editing, completion, cancellation, reopening,
and deletion. Optimistic-lock conflicts can be resolved by reloading the latest
record. The notification center shows interviews and tasks due within 24 hours,
as well as overdue tasks, with unread filtering, mark-all-read, deletion, and
navigation to the related record. Production deployments must proxy same-origin
`/api` requests to the backend.

目前提供总览、职位申请、面试安排、跟进任务和通知中心页面。职位申请支持创建、查看详情、
编辑、删除与按后端允许的路径更新状态；编辑或状态更新遇到版本冲突时可重新加载最新数据。
面试可从申请详情预约，在面试列表查看未来 7 天安排，进入详情改期、取消或完成，完成后可记录
反馈。跟进任务可从申请详情创建，在任务列表按状态或逾期筛选，进入详情编辑、完成、取消、
重新打开或删除。任务编辑、状态更新与删除遇到版本冲突时也可重新加载最新数据。通知中心
显示 24 小时内的面试、即将到期和已逾期任务提醒，支持未读筛选、全部已读、删除和跳转到
相关记录。生产部署时需让同源 `/api` 请求转发到后端。

Run the frontend test suite / 运行前端测试：

```bash
cd frontend
npm test
```

## End-to-end testing / 端到端测试

End-to-end tests live in `tests/e2e` and use Playwright with Chromium. They
cover the complete flow: create an application → schedule an interview → create
and complete a follow-up task → receive and open a reminder. The test removes
the application and related data it creates.

端到端测试位于 `tests/e2e`，使用 Playwright 驱动 Chromium，覆盖“创建申请 → 预约面试 →
创建并完成跟进任务 → 接收并打开提醒”的完整流程。测试结束后会删除自己创建的申请及关联数据。

Before the first run, install Chromium and make sure PostgreSQL is running in
Docker and the current terminal uses Java 21.

首次运行先安装浏览器，并确保 Docker 中的 PostgreSQL 已启动、当前终端使用 Java 21：

```bash
cd frontend
npx playwright install --no-shell chromium
npm run test:e2e
```

Playwright automatically starts and stops Spring Boot and Vite, or reuses them
when they are already running locally. Run `npm run test:e2e:ui` for interactive
debugging.

Playwright 会自动启动并关闭 Spring Boot 与 Vite；如果本地服务已经运行则会复用。调试时
可以运行 `npm run test:e2e:ui`。

The Frontend CI workflow in GitHub Actions runs unit/component tests, the
production build, and Chromium end-to-end tests. It uploads the Playwright HTML
report when a run fails.

GitHub Actions 的 Frontend CI 会分别执行单元/组件测试、生产构建和 Chromium 端到端测试；
失败时会上传 Playwright HTML 报告。
