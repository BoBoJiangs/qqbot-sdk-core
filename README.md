# QQ Bot Java SDK

基于 QQ 机器人开放平台的 Java 17 SDK，由本项目维护，非腾讯官方 SDK。核心库不依赖 Spring，参考官方 Node.js、Python 和 Go SDK 的协议实现。参考 commit 和验签样例差异记录在 [sdk-references.json](spec/sdk-references.json)。

当前官方文档快照日期为 **2026-11-05**：**100 个 REST 接口、49 种事件**。完整入口、Java 方法及官方文档链接见 [接口覆盖清单](API_COVERAGE.md)。包含鉴权、群聊/单聊/频道/频道私信、消息和富媒体、流式回复、素材管理、指令面板、群管理与审批、频道身份组/权限/禁言/公告/精华/日程/论坛/音频等接口。

所有 REST 接口都有命名调用方法。请求体与响应体使用普通 Java Map/JSON 数据，完整保留官方字段；常用消息额外提供 `Message`、`MessageTarget`、`MessageEvent`。需要内邀或额外权限的接口也已封装，能否成功调用取决于 QQ 账号权限。

## 构建与依赖

要求 JDK 17+。仓库内 Maven Wrapper 固定使用 Maven 3.9.11；Windows 下将 `./mvnw` 替换为 `.\mvnw.cmd`。发布仓库为 [JitPack](https://jitpack.io/#BoBoJiangs/qqbot-sdk-core)，在使用方的 `pom.xml` 中添加：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
        <snapshots><enabled>false</enabled></snapshots>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.BoBoJiangs</groupId>
    <artifactId>qqbot-sdk-core</artifactId>
    <version>0.1.1</version>
</dependency>
</dependencies>
```

Java 包名仍为 `top.sshh.qqbot.sdk`。SDK 运行依赖仅为 JDK、fastjson2 和 SLF4J API，应用按需选择日志实现。此仓库独立构建，无需下载机器人业务项目；源码来自 [qqbot-official](https://github.com/BoBoJiangs/qqbot-official)。

从源码安装到本地 Maven 仓库：`./mvnw -B -ntp clean install`。产物为 `target/qqbot-sdk-core-0.1.1.jar`，同时生成 `-sources.jar` 和 `-javadoc.jar`。

## 收发消息

```java
import top.sshh.qqbot.sdk.*;

QQBotConfig config = QQBotConfig.builder()
    .appId(System.getenv("OFFICIAL_APP_ID"))
    .appSecret(System.getenv("OFFICIAL_CLIENT_SECRET"))
    .build();

try (QQBot bot = QQBot.builder(config).build()) {
    bot.onError(error -> System.err.println(error.getClass().getSimpleName()));
    bot.onGroupMessage(event -> bot.reply(event, Message.text("收到：" + event.content())));
    bot.on(EventType.INTERACTION_CREATE, event ->
        bot.api().answerInteraction((String) event.data().get("id"), 0));
    Runtime.getRuntime().addShutdownHook(new Thread(bot::close));
    bot.start();
    bot.awaitTermination();
}
```

完整可编译程序见 [StandaloneBot.java](examples/StandaloneBot.java)。构建对象不会连接 QQ；`start()` 才启动 WebSocket，REST 调用按需取得 Token。相同客户端重复 `start()` 不会创建第二条连接；`close()` 幂等，关闭后需新建客户端。SDK 创建的定时器和事件执行器由 SDK 关闭，调用方注入的执行器仍由调用方管理。

核心默认使用当前文档地址 `https://api.bot.qq.com`。`QQBotConfig.apiBase`、`tokenEndpoint` 可以分别覆盖，支持已有地址或沙箱环境。凭据来自配置对象，SDK 不自行读取环境变量。`intents(...)` 设置事件订阅，`shard(id, count)` 配置当前分片；多分片分别创建客户端。

## 调用全部接口

```java
// 查询群成员，GET 参数通过 query 传入。
ApiResponse members = bot.openApi().getV2GroupsGroupOpenidMembers(
    "group-openid", ApiRequest.empty().query(java.util.Map.of("cursor", "")));

// 创建频道身份组，支持官方文档的全部请求字段。
ApiResponse role = bot.openApi().postGuildsGuildIdRoles(
    "guild-id", ApiRequest.json(java.util.Map.of("name", "运营")));

// 也可通过枚举调用；新发布且尚未同步的接口可使用 api().request(method, path, request)。
ApiResponse me = bot.openApi().call(ApiEndpoint.GET_USERS_ME,
    java.util.Map.of(), ApiRequest.empty());
```

路径参数单独传递并编码；query、JSON、multipart、原始字节与文件请求体均可组合。响应包含 HTTP 状态、`traceId`、原始 JSON 和 `object()`。201/202 返回的审核/异步受理用 `pending()` 区分，平台受理不代表用户已经收到消息。

SDK 用 `QQApiException` 返回机器可判断的错误类型、HTTP 状态、平台业务码、TraceID 和 Retry-After。鉴权失败最多刷新后重试一次，同一消息保持相同 `msg_seq`。限频错误交由业务按 Retry-After/平台规则安排；写请求网络失败不会自动重发，`mayHaveSucceeded()` 为 true 时，调用结果未知。

`api().send(target, message, replyTo)` 自动维护群聊/单聊回复序号。回复窗口与条数上限可配置，计数按场景、目标和消息隔离；满额后不会删除计数并重新从 1 开始。低层 REST 调用完全按照传入字段发送，调用方自行提供回复序号。

## 媒体与流式消息

URL 上传、预上传、分片完成及素材管理均在接口清单中。使用 `ApiRequest.multipart(fields, files)` 上传文件时，对象/数组字段序列化为 JSON，文件通过 JDK BodyPublisher 流式读取。`ApiRequest.file/bytes` 支持二进制请求体。预上传返回的签名上传地址可交给 `bot.mediaUploader().upload(...)`，该步骤不会自动携带 QQ Token，调用方按上传响应提供请求头和后续完成参数。

```java
C2CStream stream = bot.stream("user-openid", "incoming-message-id");
stream.append("第一段");
stream.append("第二段");
stream.complete("结束");
```

流式会话维护平台返回的消息 ID 和分片 index；`replace(...)` 的正文必须满足平台对已发送前缀的约束。遇到发送结果未知会关闭当前会话，避免继续发送到未知状态。高级上传编排和各接口分页由调用方使用相应接口控制。

## Webhook 与事件

```java
WebhookHandler handler = bot.webhook();
WebhookHandler.Response result = handler.handle(
    timestampHeader, signatureHeader, originalRequestBodyBytes);
// 将 result.status() 与 result.body() 原样写回 HTTP 响应。
```

Webhook 与 WebSocket 使用同一组事件监听器。Webhook 无需调用 `bot.start()`，由现有 HTTP 服务转交请求；接口接入示例见 [WebhookServer.java](examples/WebhookServer.java)。支持 Ed25519 验签、回调地址挑战、时间窗口和接收 ACK；验签必须使用原始请求体字节。平台首次配置地址的 op13 挑战可以不带签名，只返回挑战签名，不进入业务。业务事件必须验签，默认拒绝相差超过五分钟的时间戳，以及超过 8 MiB 的回调正文。

默认使用单线程、有容量上限的事件队列，业务回调不会阻塞网关心跳。队列满时 Webhook 返回 503，WebSocket 保留旧序号并重连请求补发。SDK 对成功处理的重复事件做五分钟、有容量上限的内存去重。

`onEvent` 接收全部已知及未知事件；`on(EventType, ...)` 按事件类型订阅；`BotEvent.dataJson()`/`data()` 保留所有业务字段。用户回调异常通过 `onError` 报告，SDK 不自动重执行业务。网关序号在事件被队列接受后推进，Webhook 也在接受入队后 ACK：这是内存接收语义，不提供持久化投递或跨重启的 exactly-once 保证。重要业务应在回调中自行持久化与幂等处理。

## 验证与维护

```shell
python -B scripts/sync_qq_sdk_catalog.py --check
python -B -m unittest discover -s tests
./mvnw -B -ntp clean verify dependency:build-classpath "-Dmdep.includeScope=runtime" "-Dmdep.outputFile=target/classpath.txt"
python -B scripts/verify_release.py
```

SDK 测试覆盖 100 个命名方法的本机 HTTP 请求、49 种事件、Token 并发刷新、回复序号、鉴权重试、未知发送结果、流式会话、心跳/恢复/关闭状态、Webhook 验签与事件背压。测试使用本地服务和虚拟网关，不调用真实 QQ 账号，也不把离线覆盖声称为所有权限的线上联调。

更新公开文档快照时，显式联网执行 `python scripts/sync_qq_sdk_catalog.py --refresh`，审查生成差异、运行 SDK 测试，再提交。普通构建不联网抓取文档。快照同时打入 SDK JAR 的 `META-INF/qqbot/`，方便追踪覆盖版本。

本次构建、回归与环境限制详见 [验证记录](VALIDATION.md)。

## 发布

发布流程与远程验收命令见 [发布说明](PUBLISHING.md)，版本变更见 [更新记录](CHANGELOG.md)。JitPack 的 Maven 坐标使用 Git 标签作为版本；已发布标签不移动。
