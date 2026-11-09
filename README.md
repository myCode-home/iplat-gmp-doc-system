# iplat-gmp-doc-system

GMP 文件管理系统（前端 + 本地演示后端）。

前端来自作者实习期间参与的项目，是一个面向制药质量体系的文档管理系统，覆盖文件库、记录库、模板管理、文档创建、变更/修订、文件失效、借阅、打印分发、培训等业务；为了让项目可以独立运行，仓库内同时提供了基于 RuoYi-Vue-Plus 的本地演示后端。

> 说明：公司真实业务后端不在本仓库中。`iplat-backend` 里的 DMS 接口是**演示实现**，返回内存中的模拟数据，用于展示页面和交互；新增/修改/删除不会持久化。

## 项目结构

```
iplat-gmp-doc-system/
├── iplat-front/      # Vue 3 + TypeScript + Element Plus 前端
└── iplat-backend/    # RuoYi-Vue-Plus v5.4.1 + DMS 演示接口
```

## 功能模块

- 文件管理
  - 文件库：生效 / 未生效 / 失效
  - 记录库：生效 / 历史
  - 模板管理：模板库、编制、审批、追踪
  - 文档创建：编制、审批、追踪
  - 记录变更、记录修订
  - 文档失效、记录失效
  - 文件借阅
  - 批量修订、批量打印、成册打印、打印分发
  - 培训：确认、生效、追踪、人员配置
  - 操作单、文件系统选项
- 设备管理（菜单与页面，演示数据为空）
- 系统管理、租户管理、系统监控、系统工具、工作流（RuoYi 原生能力）

## 技术栈

**前端**

- Vue 3.5、TypeScript 5.8、Vite 6
- Element Plus 2.9、vxe-table、UnoCSS
- Pinia、Vue Router、vue-i18n
- PageOffice、ECharts、warm-flow

**后端**

- RuoYi-Vue-Plus v5.4.1（Spring Boot 3.4、Sa-Token、MyBatis-Plus）
- MySQL 8+、Redis
- 新增 DMS/EQMS 演示控制器、动态页面配置接口和演示菜单 SQL

## 快速开始

### 1. 环境

- Node.js >= 18.18
- JDK 17、Maven 3.8+
- MySQL 8+（默认 `root/root`）
- Redis（脚本会自动在 `16379` 启动本地实例）

### 2. 初始化数据库

```bash
MYSQL_PWD=root mysql -uroot -e \
  "CREATE DATABASE IF NOT EXISTS \`ry-vue\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/ry_vue_5.X.sql

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/ry_workflow.sql

# 演示用 DMS 菜单，可重复执行
MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/iplat_mock_menus.sql
```

如果本机 MySQL 密码不是 `root`，请同步修改
`iplat-backend/ruoyi-admin/src/main/resources/application-dev.yml`。

### 3. 启动后端

```bash
cd iplat-backend

# 首次运行需要打包（JDK 17）
mvn -pl ruoyi-admin -am package -DskipTests

# 启动（会顺带拉起本地 Redis）
./start-local.sh
```

后端地址：<http://127.0.0.1:18080>

验证：

```bash
curl http://127.0.0.1:18080/auth/code
```

### 4. 启动前端

```bash
cd iplat-front
npm install
npm run dev
```

前端地址：<http://localhost:8081>

### 5. 登录

```
用户名：admin
密码：admin123
租户：000000
```

本地已关闭验证码和接口加密，直接登录即可。

## 本地化改动

为了让开源环境可以直接运行，做了以下调整：

- 前端代理指向 `127.0.0.1:18080`
- 前端端口从 `80` 改为 `8081`
- 开发环境关闭接口加密（`VITE_APP_ENCRYPT=false`）
- 后端端口从 `8080` 改为 `18080`
- 后端关闭验证码、接口加密、SnailJob 和监控客户端
- Redis 使用独立端口 `16379`，不影响本机已有 Redis

详细说明见 [iplat-front/本地运行说明.md](iplat-front/本地运行说明.md)。

## 已知限制

- 业务接口为演示数据，重启后重置
- PageOffice 在线编辑、PDF 预览、文件上传/下载、电子签名依赖真实文件服务，本地不可用
- 业务表单弹窗依赖公司后端的动态表单配置，本地多为空表单；列表页展示正常
- 首页部分入口来自旧项目 mock，指向不存在的路由

## 说明

- 前端基于 [RuoYi-Vue-Plus / plus-ui](https://gitee.com/JavaLionLi/plus-ui) 二次开发
- 后端基于 [RuoYi-Vue-Plus](https://github.com/dromara/RuoYi-Vue-Plus) v5.4.1
- 本项目仅用于学习与技术交流，演示数据均为虚构
