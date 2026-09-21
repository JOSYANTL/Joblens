# JOBL-7：后端 CI

当前范围是持续集成。AWS、自动部署、镜像发布及 CD 尚未配置。

工作流的 Action 版本依据官方文档：[checkout](https://github.com/actions/checkout)、
[setup-java](https://github.com/actions/setup-java)、[upload-artifact](https://github.com/actions/upload-artifact)。

## 触发与检查

- 推送任意分支，以及向 develop/master 创建或更新 PR 时运行。
- 工作流进入默认分支后，也可从 GitHub Actions 页面手动运行。
- GitHub 托管 Ubuntu runner 安装 Java 21，使用 Maven Wrapper 执行 clean verify。
- Docker 必须可用；Testcontainers 自动创建独立 PostgreSQL 16 数据库，执行 Flyway 和集成测试。
- 不依赖本地 PostgreSQL，也不需要 AWS 密钥或数据库 secrets。
- CI 保持 Ryuk 开启；没有沿用本地禁用 Ryuk 的规避配置。
- 检查 Surefire 报告：没有报告、存在失败或跳过、缺失 PostgreSQL 测试均使 CI 失败。
- 测试报告（失败时也尽量上传）和验证通过的 JAR 在 Actions artifacts 保留 7 天。
- JAR 构建与上传不代表已经部署。

工作流：`.github/workflows/backend-ci.yml`

## 本地复现

准备 Java 21、Python 3、运行中的 Docker，然后在仓库根目录执行：

```bash
cd backend
./mvnw --batch-mode --no-transfer-progress clean verify
cd ..
python3 scripts/check-ci-test-reports.py backend/target/surefire-reports
```

本地普通测试仍允许无 Docker 时跳过集成测试，但 CI 报告检查会拒绝这样的结果。
报告检查不固定测试数量，后续新增测试无需修改数字。

## 第一次推送后

1. 打开 GitHub → Actions → Backend CI，确认当前提交的运行结果。
2. 失败时查看 Build and run all tests 日志和 backend-test-reports 附件。
3. 第一次通过后，可在 develop/master 的分支规则中把
   `Java 21 build and PostgreSQL tests` 设为必需检查。

本地验证不能代替 GitHub runner 上的第一次实际运行；工作流需要推送后才生效。
本轮没有修改远端分支规则。

## 后续 CD

AWS 环境确定后，再补镜像构建/发布、OIDC 身份认证、部署及回滚。
部署目标、镜像仓库和运行时配置应按实际 AWS 环境决定。
