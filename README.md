# iplate-quchong

GMP 文件管理系统（前端 + 完整后端实现）。

前端来自作者实习期间参与的项目，是一个面向制药质量体系的文档管理系统；本仓库同时包含为其实现的**完整后端**（基于 RuoYi-Vue-Plus v5.4.1 扩展的 `ruoyi-dms` 模块）：真实数据库表、真实 warm-flow 审批流、真实 MinIO 文件存储，覆盖文件库、记录库、模板、文档编制、变更/修订、失效、借阅、打印分发、培训等全部 16 个业务模块。

> 说明：公司真实业务后端不可得，本仓库的后端是依据前端接口规格**从零实现**的参考实现，业务规则按前端代码推导（状态机、审批联动、生效入库等）；页面列配置等元数据由数据库表提供。

## 项目结构

```
iplate-quchong/
├── iplat-front/      # Vue 3 + TypeScript + Element Plus 前端
└── iplat-backend/    # RuoYi-Vue-Plus v5.4.1 + ruoyi-dms 完整业务后端
    └── ruoyi-modules/ruoyi-dms/   # DMS 业务模块（新增）
```

## 后端实现内容

- **44 张业务表**（`script/sql/iplat_dms.sql`）：文件库、记录库、模板库/编制/快照、文档编制及替代/关联/快照、记录编制、变更/修订、失效单、借阅、操作单、批量修订、打印申请/明细/批量打印、成册打印（申请/明细/打印件/待组册任务）、培训、文件类型及属性、附件关联、浏览历史等
- **约 40 个 REST 控制器**（`org.dromara.dms.controller`），覆盖前端全部业务接口，包括 `editList/approvalList/list` 三态列表、逗号分隔批量删除、`judge/revise` 等特殊契约
- **warm-flow 审批流集成**：通用审批流 `dms_common_approval`（申请人→审批），`submitAndFlowStart` 发起流程，审批/驳回/撤回/终止事件回写业务状态；新增 `/workflow/instance/waitingTaskIdByInstanceId/{instanceId}` 接口
- **审批生效联动**（`DmsFlowEventHandler`）：模板编制审批通过→自动写入模板库并生成快照；文档编制审批通过→自动写入文件库（未生效）、生成替代/关联关系与快照；记录编制审批通过→自动写入记录库；失效单通过→文件/记录失效；打印申请通过→生成打印明细；成册申请通过→生成打印件；借阅通过→写入借阅库；变更单通过→生成修订编制任务
- **文件存储**：官方 OSS 模块 + 本地 MinIO，业务附件关联表 `dms_oss_biz`，支持上传关联、按业务查询、批量打包下载
- **页面动态配置**：`sys_page_config` 系列表 + 启动时自动导入 69 个页面配置（表格/搜索/表单列），SuperTable/SuperSearch/SuperForm 数据全部来自数据库
- **培训闭环**：培训确认/无需培训/完成培训/文件生效（联动文件库 `useState`）
- **PDF 预览**：内置极简 PDF 生成器（占位内容，无第三方依赖）
- **电子签名**：签名定义按 URL 清单返回，签名值为演示令牌
- **种子数据**（`script/sql/iplat_dms_seed.sql`）：字典、系统参数、审批流定义、各模块演示数据

## 快速开始

### 1. 环境

- Node.js >= 18.18
- JDK 17、Maven 3.8+
- MySQL 8+（默认 `root/root`）
- MinIO（`brew install minio`，或 Docker）
- Redis（启动脚本自动在 16379 端口拉起独立实例）

### 2. 初始化数据库

```bash
MYSQL_PWD=root mysql -uroot -e \
  "CREATE DATABASE IF NOT EXISTS \`ry-vue\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/ry_vue_5.X.sql

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/ry_workflow.sql

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/iplat_dms.sql            # DMS 业务表

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/iplat_dms_seed.sql       # 字典/参数/审批流/演示数据（幂等）

MYSQL_PWD=root mysql -uroot --default-character-set=utf8mb4 ry-vue \
  < iplat-backend/script/sql/iplat_dms_menus.sql      # DMS 菜单
```

如果本机 MySQL 密码不是 `root`，请同步修改
`iplat-backend/ruoyi-admin/src/main/resources/application-dev.yml`。

### 3. 启动后端

```bash
cd iplat-backend

# 首次运行需要打包
mvn -pl ruoyi-admin -am package -DskipTests

# 启动（自动拉起 Redis 16379 与 MinIO 9000）
./start-local.sh
```

后端地址：<http://127.0.0.1:18080>

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

## 端到端流程（已验证）

文件库/记录库/模板库列表 → 文档编制新增 → 提交审批（warm-flow 发起）→
审批同意（审批事件回写）→ 文件库自动入库（未生效）→ 培训生效 → 文件库生效；
打印申请 → 审批 → 打印明细状态机（打印/发放/领用/交回/关闭/销毁）；
附件上传 → MinIO 存储 → 按业务查询/打包下载。

## 本地化改动

- 前端代理指向 `127.0.0.1:18080`，端口 8081，开发环境关闭接口加密
- 后端端口 18080，关闭验证码/接口加密/SnailJob/监控客户端
- Redis 独立端口 16379；`start-local.sh` 自动拉起 Redis + MinIO
- `ruoyi-common-oss/OssClient`：去掉 `chunkedEncodingEnabled(false)`（aws sdk 2.28 异步客户端在该配置下不携带 `x-amz-content-sha256` 头，会被 MinIO 拒绝）
- 启动脚本设置 `AWS_CONFIG_FILE=/dev/null`，规避本机 `~/.aws/config` 格式问题

详细说明见 [iplat-front/本地运行说明.md](iplat-front/本地运行说明.md)。

## 已知限制

- PDF 预览为占位生成物（无真实文档渲染引擎）；PageOffice 在线编辑不可用
- 电子签名为演示令牌；动态表单规则引擎（ruleList）字段保留但规则不执行
- 设备管理（EQMS）为最小 CRUD
- 业务规则按前端代码推导，可能与公司原实现存在差异

## 说明

- 前端基于 [RuoYi-Vue-Plus / plus-ui](https://gitee.com/JavaLionLi/plus-ui) 二次开发
- 后端基于 [RuoYi-Vue-Plus](https://github.com/dromara/RuoYi-Vue-Plus) v5.4.1，新增 `ruoyi-dms` 模块
- 本项目仅用于学习与技术交流，演示数据均为虚构
