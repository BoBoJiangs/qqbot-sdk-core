# 更新记录

## 0.1.1

- 修复 JitPack 默认 Maven 版本过旧导致的构建失败，增加 Maven Wrapper 并固定 Maven 3.9.11。
- CI 和发布使用相同 Wrapper，在 JDK 17/21 上验证。

## 0.1.0

- 从 qqbot-official 提取独立 Java 17 SDK，保留 `top.sshh.qqbot.sdk` 包名。
- 覆盖 2026-11-05 官方文档快照中的 100 个 REST 接口与 49 种事件。
- 提供 Token、WebSocket 网关、Webhook 验签、富媒体与流式消息支持。
- 独立 Maven 构建、源码和 Javadoc 产物、Java 17/21 CI 与 JitPack 发布配置。

来源：qqbot-official 提交 `2146302`，提取时主仓库提交 `435d818`。
