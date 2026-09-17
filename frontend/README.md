# Joblens 前端

技术栈：React、TypeScript、Vite、Mantine、React Router、TanStack Query。

## 本地启动

需要 Node.js 22.12+（或 20.19+）。先启动仓库根目录的 PostgreSQL 和 `backend` Spring Boot 服务，再运行：

```bash
cd frontend
npm install
npm run dev
```

打开终端显示的本地地址（通常是 `http://localhost:5173`）。Vite 开发服务器会把 `/api` 和 `/actuator` 代理到 `http://localhost:8080`，因此本地无需单独配置跨域。

## 目录

- `src/app`：应用布局、路由与主题
- `src/features`：按业务能力划分的页面与接口
- `src/shared`：通用 API、类型、组件及格式化工具

目前提供总览、职位申请、面试安排和跟进任务页面。职位申请支持创建、查看详情与按后端允许的路径更新状态；面试和跟进任务暂时只读。生产部署时需让同源 `/api` 请求转发到后端。
