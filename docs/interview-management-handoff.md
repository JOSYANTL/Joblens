# JOBL-15 面试安排与反馈管理

分支：`JOBL-15-implement-interview-scheduling-and-feedback-management`

## 已实现范围与验收

- 为已有申请安排多轮面试：轮次、类型、开始时间、时长、联系人、会议链接、地点。
- 查看详情、修改安排和改期；取消后保留记录。
- SCHEDULED 可以完成或取消；COMPLETED 和 CANCELLED 为终态。
- 开始时间未到不能完成；完成之后可填写、修改面试问题、表现总结和下一步行动。
- 重复设置相同状态不改变完成时间；反馈编辑不改变完成时间。
- 按申请、状态、开始时间范围分页查询；提供未来七天已安排面试的查询接口。
- 每轮面试独立版本，防止旧数据覆盖；跨申请访问返回 404。
- 时间输入支持偏移量，持久化使用带时区时间，响应使用 UTC。
- V6 建表、约束、索引和级联清理；领域层不引入 JPA。

接口和可复制请求见 [README](../backend/README.md#interview-scheduling-and-feedback)。

## 自动化验证

本轮新增 16 个测试；完整套件 71 个通过、0 失败、0 跳过，其中真实 PostgreSQL
集成测试 7 个。运行于 backend 目录：

```bash
TESTCONTAINERS_RYUK_DISABLED=true ./mvnw -B -ntp test
```

Ryuk 仅在本次验证命令中临时禁用，沿用本机拉取辅助镜像的规避方式；未修改项目配置。
正常 Docker 环境可直接使用 `./mvnw test`。

## 业务边界

面试状态与职位申请状态独立，完成某轮面试不会自动产生 Offer。
支持录入历史面试；同一申请允许同轮次多场面试。
当前不检查日程重叠、不发送提醒、不连接外部日历。
反馈的下一步行动是文字记录，不自动创建跟进任务。
完成和取消后的记录不能重新打开；可以新建面试记录。

## 建议验收流程

1. 创建职位申请，然后安排面试，确认状态 SCHEDULED、version 为 0。
2. 修改面试时间，确认版本增加；用旧版本再次修改应返回 409。
3. 查看未来七天列表和指定时间范围列表。
4. 对已开始的面试标记完成，再填写反馈；申请状态应保持不变。
5. 取消另一场面试，确认仍能查询，但不能改期或填写反馈。
6. 使用另一申请 id 访问该面试，应返回 404。

建议 commit message：

```text
feat(JOBL-15): implement interview scheduling and feedback management

- add interview lifecycle, rescheduling and feedback rules
- expose versioned APIs and paginated calendar queries
- add Flyway V6 migration and PostgreSQL integration coverage
- document API usage and acceptance criteria
```
