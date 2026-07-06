# 🏠 生活类知识

> 日常生活相关的问题与解答整理

---

## QQ/微信/社交

### QQ上收不到机器人提醒怎么办？

**问题**：定时任务执行成功但QQ上收不到提醒
**日期**：2026-05-18
**解答**：
1. 检查QQBot插件是否安装且编译完成（`dist/` 目录是否存在）
2. 检查 `openclaw.json` 配置中 `channels.qqbot` 是否有 `appId` 和 `clientSecret`
3. 如缺 `dist/` 目录，到插件目录执行 `npm run build`
4. 清理配置中的幽灵条目（如不存在的 `openclaw-qqbot`）
5. 重启 gateway：`openclaw gateway restart`

---

*持续更新中...*
