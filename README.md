# UP 健身（fitnessUP）

Vue 3 + Vite + Vant 前端，Java 21 + Spring Boot 3.4 + JPA + MySQL + Redis + JWT 后端。

## 启动

```bash
# 前端 http://localhost:3000
npm install
npm run dev

# Spring Boot 后端 http://localhost:8080
cd server
mvn spring-boot:run
```

前端 API 使用 `/api`，Vite 会将请求代理到 Spring Boot 的 `8080` 端口。如需连接其他端口，可在启动 Vite 前设置 `FITNESS_API_TARGET`。数据库默认连接 `localhost:3307/fitness_up`，启动时会自动创建表并写入演示数据。

启动后端前，请设置环境变量 `FITNESS_DB_PASSWORD`，或在本机的 `server/config/application.properties` 中设置同名属性。Redis 地址可以通过 `REDIS_HOST` 和 `REDIS_PORT` 配置。

## 主要接口

- `POST /api/auth/register`、`POST /api/auth/login`
- `GET/PUT /api/user/info`
- `GET /api/user/stats`、`GET /api/user/achievements`
- `GET /api/train/plans`、`GET /api/train/plans/{id}`
- `POST /api/train/start`、`POST /api/train/finish`
- `GET /api/courses`、`GET /api/courses/{id}`、`GET /api/courses/{id}/comments`
- `GET/POST /api/community/posts`
- `GET /api/diet/recommend`、`GET /api/diet/recommend/{id}`
- `GET /api/user/plans/{training|diet}` 获取当前账号已添加的卡片
- `GET/PUT/DELETE /api/user/plans/{training|diet}/{id}` 查询、添加、移除当前账号的卡片
- `PUT /api/user/calorie-goal` 保存当前账号的每日目标热量

Web 端登录后可访问业务页面。除登录和注册外，业务接口均使用 `Authorization: Bearer <token>` 进行 JWT 鉴权。

商城和直播页面目前保留页面内演示数据。
