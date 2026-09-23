# Joblens

[![Backend CI](https://github.com/JOSYANTL/Joblens/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/JOSYANTL/Joblens/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/JOSYANTL/Joblens/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/JOSYANTL/Joblens/actions/workflows/frontend-ci.yml)

A full-stack workspace for tracking job applications, interviews, follow-up
tasks, and time-sensitive reminders.

一个用于管理职位申请、面试、跟进任务和时间提醒的全栈求职管理平台。

## Features / 功能

- **Account security / 账户安全** — Session-based authentication, CSRF
  protection, encoded passwords, and per-user data isolation / 基于 Session 的
  登录认证、CSRF 防护、密码加密以及用户数据隔离。
- **Application tracking / 申请追踪** — Create, search, edit, delete, and move
  applications through a controlled recruitment workflow / 创建、搜索、编辑、
  删除职位申请，并通过受控状态流转跟踪招聘进度。
- **Interview management / 面试管理** — Schedule and reschedule interviews,
  manage their status, and record feedback / 预约和调整面试、管理面试状态并记录
  面试反馈。
- **Follow-up tasks / 跟进任务** — Create deadlines, filter pending or overdue
  work, and complete, cancel, or reopen tasks / 创建带截止时间的任务，筛选待办或
  逾期任务，并支持完成、取消和重新打开。
- **Document management / 文档管理** — Upload, download, categorize, and delete
  private PDF or DOCX files for each application / 为每个职位申请上传、下载、分类和
  删除私有 PDF 或 DOCX 文档。
- **Activity timeline and notes / 活动时间线与备注** — Keep private notes and
  review a paginated, filterable audit trail of application, interview, task,
  document, and note changes / 保存私有备注，并通过可分页、可筛选的时间线查看申请、
  面试、任务、文档和备注变更。
- **Notification center / 通知中心** — Receive in-app reminders for upcoming
  interviews, tasks due within 24 hours, and overdue tasks / 接收即将开始的面试、
  24 小时内到期任务和逾期任务提醒。
- **Reliable updates / 可靠更新** — Optimistic locking prevents concurrent
  edits from silently overwriting each other / 使用乐观锁防止并发编辑互相覆盖。

## Architecture / 架构

The backend follows a bounded-context-first DDD structure. Domain code stays
independent of Spring and persistence concerns, while application services,
REST interfaces, and infrastructure adapters point inward.

后端采用以限界上下文为核心的 DDD 结构。领域层不依赖 Spring 或数据库实现，应用服务、
REST 接口和基础设施适配器均向内依赖。

```text
React + Mantine frontend
          │ REST + session + CSRF
          ▼
Spring Boot interfaces ──> application ──> domain
          ▲                    ▲
          └── infrastructure ──┘
                    │
              PostgreSQL 16
```

Backend bounded contexts / 后端限界上下文：

- `identity` — accounts and authentication / 账户与认证
- `activity` — application timeline and private notes / 申请时间线与私有备注
- `document` — application documents and replaceable storage adapters / 申请文档与可替换存储适配器
- `job` — applications, interviews, and follow-up tasks / 职位申请、面试与跟进任务
- `notification` — reminder generation and read-state management / 提醒生成与已读状态管理
- `shared` — small cross-context application ports / 少量跨上下文应用端口

## Tech stack / 技术栈

| Area / 范围 | Technologies / 技术 |
| --- | --- |
| Backend / 后端 | Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Maven |
| Database / 数据库 | PostgreSQL 16, Flyway |
| Frontend / 前端 | React 19, TypeScript, Vite, Mantine, React Router, TanStack Query |
| Testing / 测试 | JUnit, MockMvc, Testcontainers, Vitest, Testing Library, Playwright |
| Delivery / 交付 | Docker Compose, GitHub Actions |

## Project structure / 项目结构

```text
Joblens/
├── backend/                 # Spring Boot service / 后端服务
│   ├── docker/              # Local PostgreSQL / 本地数据库
│   └── src/
├── frontend/                # React application / 前端应用
│   ├── src/
│   └── tests/               # Unit, component, and E2E tests / 测试
├── docs/                    # Engineering notes / 工程文档
├── scripts/                 # CI verification helpers / CI 检查脚本
└── .github/workflows/       # Backend and frontend CI / 持续集成
```

## Quick start / 快速启动

### Prerequisites / 环境要求

- Java 21
- Node.js 22.12+, 24.x, or 26+
- Docker Desktop with Docker Compose

### 1. Clone and start PostgreSQL / 克隆并启动数据库

```bash
git clone https://github.com/JOSYANTL/Joblens.git
cd Joblens
docker compose -f backend/docker/docker-compose.yml up -d
```

The local database uses `joblens` as its database name, username, and default
development password. Production credentials must be supplied through the
environment and must not reuse these local values.

本地数据库名、用户名和默认开发密码均为 `joblens`。生产环境必须通过环境变量提供独立凭据，
不能复用本地开发配置。

### 2. Start the backend / 启动后端

```bash
cd backend
DB_PASSWORD=joblens ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Verify the service at `http://localhost:8080/actuator/health`. Flyway applies
all database migrations automatically during startup.

访问 `http://localhost:8080/actuator/health` 验证服务状态。启动时 Flyway 会自动执行
数据库迁移。

### 3. Start the frontend / 启动前端

Open another terminal / 打开另一个终端：

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`, register an account, and start tracking your
applications. The Vite development server proxies `/api` requests to the
backend automatically.

打开 `http://localhost:5173`，注册账户后即可开始记录职位申请。Vite 开发服务器会自动把
`/api` 请求代理到后端。

## Testing / 测试

Run backend tests, including PostgreSQL integration tests through
Testcontainers / 运行后端测试，包括基于 Testcontainers 的 PostgreSQL 集成测试：

```bash
cd backend
./mvnw test
```

Run frontend unit and component tests / 运行前端单元测试与组件测试：

```bash
cd frontend
npm test
npm run build
```

Run the complete Chromium workflow / 运行完整 Chromium 端到端流程：

```bash
cd frontend
npx playwright install --no-shell chromium
npm run test:e2e
```

GitHub Actions runs backend tests, frontend tests, the production build, and
the end-to-end workflow on pushes and pull requests.

GitHub Actions 会在推送和 Pull Request 时运行后端测试、前端测试、生产构建和端到端流程。

## API overview / API 概览

| Capability / 能力 | Main routes / 主要路由 |
| --- | --- |
| Authentication / 认证 | `/api/auth/register`, `/api/auth/login`, `/api/auth/me`, `/api/auth/logout` |
| Applications / 职位申请 | `/api/applications` |
| Interviews / 面试 | `/api/interviews`, `/api/applications/{id}/interviews` |
| Follow-up tasks / 跟进任务 | `/api/tasks`, `/api/applications/{id}/tasks` |
| Notifications / 通知 | `/api/notifications` |
| Documents / 文档 | `/api/applications/{id}/documents` |
| Activity and notes / 活动与备注 | `/api/applications/{id}/activities`, `/api/applications/{id}/notes` |
| Service health / 服务健康 | `/actuator/health` |

State-changing requests require the current session cookie and CSRF token.
Detailed backend and frontend instructions are available in
[`backend/README.md`](backend/README.md) and
[`frontend/README.md`](frontend/README.md).

所有修改数据的请求都需要当前 Session Cookie 和 CSRF Token。更多说明请查看
[`backend/README.md`](backend/README.md) 和 [`frontend/README.md`](frontend/README.md)。

## Roadmap / 后续计划

- S3 document-storage adapter / S3 文档存储适配器
- AI-assisted matching and application insights / AI 匹配分析与申请洞察
- Email or push notification delivery / 邮件或推送通知
- Production deployment and observability / 生产部署与可观测性
