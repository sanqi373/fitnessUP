# UP 健身（fitnessUP）

移动端 H5 健身 App，前后端分离架构。前端 Vue 3 + Vite + Vant；后端提供两套可独立运行的实现：**Spring Boot 3.4 完整版**（JPA + MySQL + Redis + JWT）与 **Node.js + Express 轻量版**（内置静态数据，便于联调）；前端另内置 axios Mock 层，可零后端预览全部页面。

功能覆盖：首页概览、训练计划、精品课程、饮食推荐、社区动态、商城、个人中心、登录注册。

## 技术栈

| 端 | 技术 |
| --- | --- |
| 前端 | Vue 3.2 / Vite 2.9 / Vue Router 4 / Pinia 2 / Vant 4.3 / Axios |
| 后端 A `server/src` | Spring Boot 3.4.11（JDK 17+）、Spring Data JPA、MySQL 8、Redis、Spring Security + JWT（BCrypt） |
| 后端 B `server/index.js` | Express 4.18 + mysql2 + cors（端口 3001） |

## 目录结构

```
src/            前端源码：api/ 接口封装、mock/ 模拟数据、views/ 页面、stores/ 状态、router/ 路由
server/
  index.js      Node 轻量服务
  pom.xml       Maven 配置
  src/main/java/com/fitnessup/   Spring Boot：controller / service / repository / entity / security
  src/main/resources/application.yml
vite.config.cjs、package.json、index.html、public/icons/
```

## 快速开始

环境：Node ≥ 16；使用 Spring Boot 版另需 JDK 17+、Maven、MySQL 8、Redis。

```bash
# 前端（http://localhost:3000）
npm install && npm run dev

# 后端 A：Spring Boot（http://localhost:8080）
cd server && mvn spring-boot:run

# 后端 B：Node 轻量版（http://localhost:3001）
cd server && npm install && npm start
```

- Spring Boot 版启动前需创建数据库 `fitness_up`（数据表由 JPA 自动生成）；Node 版默认连接 `localhost:3307`，仅注册/登录真实读写 MySQL。
- 仅看页面时，取消 `src/main.js` 中 `setupMock(request)` 的注释即可，无需启动任何后端。

**前后端地址对应**：`src/api/request.js` 的 `baseURL` 为 `http://localhost:3001`（Node 版直连）；改用 Spring Boot 版需改为 `/api`（经 Vite 代理到 8080），否则请求 404。

## 接口一览

统一响应体为 `{ code, message, data }`。

| 模块 | 主要接口 | Node 3001 | Spring Boot 8080 |
| --- | --- | --- | --- |
| 认证 | POST `/auth/register`、POST `/auth/login` | ✅ | ✅ |
| 用户 | GET/PUT `/user/info`、GET `/user/achievements` | ✅ | ✅ |
| 用户统计 | GET `/user/stats` | ✅ | ❌ 未实现 |
| 训练 | GET `/train/plans`、GET `/train/plans/{id}` | ✅ | ✅ |
| 训练扩展 | POST `/train/start`、POST `/train/finish`、GET `/train/history` | ✅ | ❌ 未实现 |
| 课程 | GET `/courses`、GET `/courses/{id}`、GET `/courses/{id}/comments` | ✅ | 课程 ✅ / 评论 ❌ |
| 社区 | GET `/community/posts` | ✅ | ✅（另支持 POST 发布动态） |
| 饮食 | GET `/diet/recommend` | ✅ | ✅ |

鉴权：Spring Boot 版除 `/api/auth/**` 外均需 `Authorization: Bearer <token>`（JWT 有效期 7 天）；Node 版不校验 Token。

## 项目现状

**已完成**：工程化搭建与主题样式体系、登录注册全链路（表单校验 → 接口 → Token 持久化）、训练计划与课程的列表/详情接口对接、社区/饮食/商城/个人中心页面骨架、两套后端实现、前端 Mock 体系。

**待办**：

- 训练：开始训练流程（计时与结算）、训练历史页，以及"我的课程/训练历史/身体数据"路由（当前未定义）。
- 社区：动态详情、发布动态、点赞与评论接口化（现为本地 Mock 与本地状态）。
- 其他：课程评论接入与"立即加入"报名逻辑；饮食改为调用 `/diet/recommend`；商城搜索生效与商品详情页；直播模块（`/live`）从零开发。
- 后端：Spring Boot 补齐上表标注 ❌ 的接口。

## 注意事项

- 部分页面（首页社区动态、社区列表、饮食、个人中心成就、商城）直接引用 `src/mock` 静态数据，未走 `src/api`，勿按"已对接接口"理解。
- Node 版写操作均为静态返回、不校验 Token，且密码明文存储；`server/index.js`、`application.yml`、`JwtUtil` 中的数据库密码与 JWT 密钥均为硬编码，需迁移至环境变量。
- Node 版用户表为 `users`，Spring Boot 实体表为 `user`，共用同一数据库时表名不一致。
- Vite 2.9 版本较旧，与 Node 18+ 存在兼容边界，建议升级。
