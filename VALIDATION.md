# Java SDK 验证记录

验证日期：2026-11-05。此记录针对独立 SDK 仓库。

- `./mvnw -B -ntp clean verify dependency:build-classpath "-Dmdep.includeScope=runtime" "-Dmdep.outputFile=target/classpath.txt"`：208 项 Java 测试全部通过，无失败、错误或跳过。本机使用 JDK 21，编译目标为 Java 17。
- `python -B scripts/sync_qq_sdk_catalog.py --check`：100 个 REST 接口、49 种事件的已提交清单与生成代码一致。
- `python -B -m unittest discover -s tests`：9 项检查通过，涵盖独立 POM、运行依赖、发布坐标、JitPack 配置与文档链接。
- `python -B scripts/verify_release.py`：验证二进制类均为 Java 17 字节码，官方快照随包分发，发布 POM 不依赖原项目父 POM，源码/Javadoc JAR 完整，两个示例仅依赖 SDK 产物及其传递依赖即可编译。
- GitHub Actions 配置在 JDK 17 和 21 上重复上述验证；具体远程执行结果以 Actions 页面为准。

SDK 测试使用本地 HTTP 服务、虚拟网关和签名事件验证请求与协议行为，不连接真实 QQ 账号。账号内邀或额外权限接口尚未做真实账号全量联调。

提取来源：qqbot-official 提交 `2146302`，提取时主仓库提交 `435d818`。运行时实现与原 SDK 模块保持一致，此次变更集中于独立构建、验证和发布。

0.1.0 的 JitPack 首次构建因服务默认 Maven 版本过旧而失败；0.1.1 改为使用仓库内 Maven Wrapper 固定 Maven 3.9.11。未移动旧版本标签。
