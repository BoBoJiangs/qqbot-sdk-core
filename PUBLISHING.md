# 发布说明

本库通过 JitPack 提供 Maven 依赖，不依赖原机器人项目的父 POM。JitPack 根据 Git 标签构建，使用方需要在 Maven 中添加 `https://jitpack.io` 仓库。此发布渠道不是 Maven Central。

## 发布新版本

1. 在 `pom.xml`、README 与更新记录中设置版本号，运行 README 中的完整验证命令。
2. 提交并推送 `main`；确认 GitHub Actions 的 Java 17/21 验证成功。
3. 创建并推送同版本标签，例如 `git tag -a 0.1.0 -m "Release 0.1.0"`、`git push origin 0.1.0`。已经发布的标签不移动，修复时发布新版本。
4. 请求对应 POM 触发 JitPack 构建：`https://jitpack.io/com/github/BoBoJiangs/qqbot-sdk-core/0.1.0/qqbot-sdk-core-0.1.0.pom`。
5. 检查 [构建状态](https://jitpack.io/api/builds/com.github.BoBoJiangs/qqbot-sdk-core/0.1.0) 与 [构建日志](https://jitpack.io/com/github/BoBoJiangs/qqbot-sdk-core/0.1.0/build.log)。首次构建需要等待。
6. 在新建 Maven 项目中加入 README 的仓库和依赖配置，以空本地仓库执行 `mvn -U verify "-Dmaven.repo.local=target/remote-repository"`。确认 SDK 来自 JitPack，传递依赖可解析，并编译一个使用 SDK 的类；同时检查源码与 Javadoc JAR 可下载。

`jitpack.yml` 固定 JDK 17，执行包含测试的 `mvn clean install`。二进制、源码与 Javadoc 会一起安装供 JitPack 收集；JAR 中包含官方文档快照。

## Maven Central

未来可另行增加 Maven Central 发布。需先完成 Sonatype Central Portal 账号及命名空间验证，确定开源许可证，配置发布 Token 和签名密钥，再添加发布配置。不得将密钥、Token 或含凭据的 Maven settings 提交到仓库。
