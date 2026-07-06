# 💻 技术类知识

> 开发、运维、工具等技术相关的问题与解答整理

---

## OpenClaw

### 微信插件扫码后连接不上怎么办？

**问题**：微信扫码成功但连接不上，日志只到 `Starting to poll QR code status...`
**日期**：2026-05-18
**解答**：
- 不要用 `npx -y @tencent-weixin/openclaw-weixin-cli@latest install` 反复安装，每次 install 会改 config 导致 gateway 自动重启，打断扫码轮询
- 正确方式：插件装好后用 `openclaw channels login --channel openclaw-weixin` 单独触发扫码登录
- 扫码成功后重启 gateway：`openclaw gateway restart`

### QQBot插件报 "plugin not found" 怎么办？

**问题**：启动时报 `plugins.entries.openclaw-qqbot: plugin not found`
**日期**：2026-05-18
**解答**：
1. 插件源码在但没编译：检查 `~/.openclaw/extensions/qqbot/dist/` 是否存在
2. 不存在则 `cd ~/.openclaw/extensions/qqbot && npm run build`
3. 配置中 `plugins.entries` 的 key 要和 `openclaw.plugin.json` 中的 `id` 一致（qqbot 的 id 是 `qqbot`，不是 `openclaw-qqbot`）
4. 删除配置中不匹配的幽灵条目

### 百度云服务器连不上GitHub怎么办？

**问题**：`git push` 到 GitHub 时 SSL 超时
**日期**：2026-05-17（已解决 2026-05-18）
**解答**：
- 百度云服务器访问 github.com 受网络波动影响，有时 SSL 超时
- 等网络恢复后重试即可（2026-05-18 网络恢复，推送成功）
- 长期方案：配代理 / 换 Gitee 镜像
- 仓库地址：https://github.com/jorden2014/openClawMemory.git

---

*持续更新中...*
