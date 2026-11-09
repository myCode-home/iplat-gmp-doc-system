## 平台简介

> 本地运行（含配套后端、数据库初始化、登录账号）请见 [本地运行说明.md](./本地运行说明.md)。

- 本仓库为前端技术栈 [Vue3](https://v3.cn.vuejs.org) + [TS](https://www.typescriptlang.org/) + [Element Plus](https://element-plus.org/zh-CN) + [Vite](https://cn.vitejs.dev) 版本。
- 成员项目: 基于 vben5(ant-design-vue) 的前端项目 [ruoyi-plus-vben5](https://gitee.com/dapppp/ruoyi-plus-vben5)
- 成员项目: 基于soybean 的前端项目 [ruoyi-plus-soybean](https://gitee.com/xlsea/ruoyi-plus-soybean)

## 配套后端代码仓库地址

| 介绍         | 项目名              | 项目地址                                                                                                                                                                           |
|------------|:-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 🔥 分布式集群框架 | RuoYi-Vue-Plus   | - [Gitee](https://gitee.com/dromara/RuoYi-Vue-Plus)<br> - [GitHub](https://github.com/dromara/RuoYi-Vue-Plus)<br> - [GitCode](https://gitcode.com/dromara/RuoYi-Vue-Plus)      |
| 🔥 微服务框架   | RuoYi-Cloud-Plus | - [Gitee](https://gitee.com/dromara/RuoYi-Cloud-Plus)<br>- [GitHub](https://github.com/dromara/RuoYi-Cloud-Plus)<br> - [GitCode](https://gitcode.com/dromara/RuoYi-Cloud-Plus) |

## 分支说明

- ts分支(稳定发布主分支 生产可用)
- dev分支(开发分支 开发过程中使用)

## 前端运行

```bash
# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev

# 构建生产环境
npm run build:prod

# 前端访问地址 http://localhost:80
```
