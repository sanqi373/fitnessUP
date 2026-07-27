# UP 健身 (FitnessUP)

一个功能完整的健身 App，提供训练计划、在线课程、社区交流、饮食推荐等功能。

## 技术栈

### 前端
| 技术 | 版本 |
|------|------|
| Vue | 3.2.47 |
| Vite | 2.9.16 |
| Vue Router | 4.1.6 |
| Pinia | 2.0.36 |
| Axios | 1.3.6 |
| Vant UI | 4.3.2 |

### 后端

项目包含两套后端实现，可根据需要选用：

| | Node.js 版（简易） | Spring Boot 版（完整） |
|---|---|---|
| 框架 | Express 4.18 | Spring Boot 3.4.11 |
| 语言 | JavaScript | Java 25 |
| 数据库 | MySQL | MySQL + Redis |
| 认证 | 明文密码 | Spring Security + JWT |
| ORM | 原生 SQL | Spring Data JPA |
| 端口 | 3001 | 8080 |
| 启动命令 | `npm start` | `mvn spring-boot:run` |
| 定位 | 快速原型 / Mock | 生产级完整后端 |

---

## 项目结构

```
fitnessUP/
├── index.html                 # 前端 HTML 入口
├── package.json               # 前端依赖 & 脚本
├── vite.config.cjs            # Vite 配置（含 API 代理）
├── dist/                      # 前端构建产物
├── public/                    # 前端静态资源
├── src/                       # 前端源代码
│   ├── main.js                # Vue 应用入口
│   ├── App.vue                # 根组件
│   ├── router/index.js        # 路由配置
│   ├── stores/user.js         # Pinia 状态管理
│   ├── api/                   # API 接口层
│   │   ├── request.js         # Axios 封装（拦截器）
│   │   ├── user.js            # 用户接口
│   │   ├── course.js          # 课程接口
│   │   └── train.js           # 训练接口
│   ├── components/            # 公共组件（TabBar 等）
│   ├── views/                 # 页面视图
│   │   ├── home/              # 首页
│   │   ├── train/             # 训练计划
│   │   ├── course/            # 在线课程
│   │   ├── community/         # 社区动态
│   │   ├── diet/              # 饮食推荐
│   │   ├── live/              # 直播
│   │   ├── shop/              # 商城
│   │   ├── mine/              # 个人中心
│   │   ├── login/             # 登录
│   │   └── register/          # 注册
│   ├── mock/                  # Mock 数据
│   ├── styles/                # 全局样式
│   └── utils/                 # 工具函数
└── server/                    # 后端代码
    ├── index.js               # Node.js Express 入口
    ├── package.json            # Node.js 依赖
    ├── pom.xml                # Spring Boot Maven 配置
    └── src/main/
        ├── java/com/fitnessup/
        │   ├── FitnessUpApplication.java   # Spring Boot 入口
        │   ├── common/ApiResult.java        # 统一响应体
        │   ├── config/
        │   │   ├── RedisConfig.java         # Redis 序列化配置
        │   │   └── WebConfig.java           # CORS 等 Web 配置
        │   ├── security/
        │   │   ├── SecurityConfig.java      # Spring Security 配置
        │   │   ├── JwtAuthFilter.java       # JWT 认证过滤器
        │   │   └── JwtUtil.java             # JWT 工具类
        │   ├── controller/                  # 控制器层
        │   ├── service/                     # 业务逻辑层
        │   ├── repository/                  # JPA 数据访问层
        │   └── entity/                      # 实体类
        └── resources/
            └── application.yml              # Spring Boot 配置
```

---

## 快速启动

### 前置条件

- **Node.js** >= 16（前端 + Node.js 后端）
- **JDK 25**（Spring Boot 后端）
- **Maven** >= 3.8（Spring Boot 后端）
- **MySQL**（推荐 8.0+）
- **Redis**（仅 Spring Boot 版需要）

### 1. 创建数据库

连接 MySQL 并执行：

```sql
CREATE DATABASE IF NOT EXISTS fitness_up
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

Spring Boot 版启动后会自动通过 JPA `ddl-auto: update` 创建表结构。Node.js 版需手动建表：

```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 启动后端

#### 方式 A：Spring Boot（推荐，功能完整）

```bash
cd server
mvn spring-boot:run
```

服务运行在 `http://localhost:8080`。

**配置说明**（`server/src/main/resources/application.yml`）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| MySQL 地址 | `localhost:3306` | 数据库 `fitness_up` |
| MySQL 用户 | `root` / `fitness123` | 按需修改 |
| Redis 地址 | `localhost:6379` | 无密码 |
| JPA DDL | `update` | 自动建表/更新表结构 |
| 日志级别 | `debug` | `com.fitnessup` 包下 |

#### 方式 B：Node.js Express（简易原型）

```bash
cd server
npm install
npm start
```

服务运行在 `http://localhost:3001`。

**配置说明**（`server/index.js` 内硬编码）：

| 配置项 | 值 |
|--------|-----|
| MySQL 端口 | `3307` |
| MySQL 用户 | `root` / `MsDic373204.` |
| 数据库 | `fitness_up` |

> Node.js 版大部分接口返回 mock 数据，仅注册/登录接口实际连接 MySQL。

### 3. 启动前端

```bash
# 在项目根目录（fitnessUP/）
npm install
npm run dev
```

开发服务器运行在 `http://localhost:3000`。

---

## 前后端对接说明

### 当前对接方式

前端 `src/api/request.js` 中 Axios 的 `baseURL` 设为 `http://localhost:3001`，即**直连 Node.js 后端**。

`vite.config.cjs` 中配置了代理规则：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // 代理到 Spring Boot
    changeOrigin: true
  }
}
```

### 如何切换后端

**切换到 Spring Boot（端口 8080）**：

1. 确保 Spring Boot 已在 8080 端口启动
2. 修改 `src/api/request.js`：
   ```js
   baseURL: 'http://localhost:8080',
   ```
   或改为相对路径利用 Vite 代理：
   ```js
   baseURL: '/api',
   ```

**切换到 Node.js Express（端口 3001）**：

```js
baseURL: 'http://localhost:3001',
```

### 接口对照

| 模块 | Spring Boot Controller | Node.js 路由 |
|------|----------------------|-------------|
| 用户认证 | `AuthController` (`/api/auth/**`) | `/auth/login`, `/auth/register` |
| 用户信息 | `UserController` (`/api/user/**`) | `/user/info`, `/user/stats` |
| 训练计划 | `PlanController` (`/api/plans/**`) | `/train/plans` |
| 课程 | `CourseController` (`/api/courses/**`) | `/courses` |
| 社区 | `CommunityController` (`/api/community/**`) | `/community/posts` |
| 饮食 | `DietController` (`/api/diet/**`) | `/diet/recommend` |
| 成就 | `AchievementController` (`/api/achievements/**`) | `/user/achievements` |

> Spring Boot 版接口统一以 `/api` 为前缀，Node.js 版无前缀。

---

## 构建部署

```bash
# 前端构建
npm run build

# Spring Boot 打包
cd server
mvn clean package -DskipTests
java -jar target/fitness-up-server-1.0.0-SNAPSHOT.jar
```

构建产物在 `dist/` 目录，可直接部署到 Nginx 等静态服务器。
