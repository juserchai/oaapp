# Spring OA Server

Spring Boot后端服务，提供办公自动化系统的API支持。

## 功能特性

- 用户认证与授权（基于JWT）
- 待办事项管理
- 审批流程管理
- 日历事件管理
- RESTful API设计
- 数据持久化（H2/MySQL）

## 技术栈

- Java 17
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- JWT认证
- Maven
- H2/MySQL数据库

## 项目结构

```
spring-oaserver/
├── src/
│   ├── main/
│   │   ├── java/com/example/oaserver/
│   │   │   ├── config/       # 配置类
│   │   │   ├── controller/   # REST控制器
│   │   │   ├── model/        # 数据模型
│   │   │   │   ├── entity/   # 实体类
│   │   │   │   └── enums/    # 枚举类型
│   │   │   ├── repository/   # 数据访问层
│   │   │   ├── security/     # 安全相关
│   │   │   └── service/      # 业务逻辑层
│   │   └── resources/        # 配置文件
│   └── test/                 # 测试代码
├── pom.xml                   # Maven配置
└── README.md                 # 项目文档
```

## 快速开始

### 前提条件

- JDK 17+
- Maven 3.6+

### 安装与运行

1. 克隆仓库
```bash
git clone <repository-url>
cd spring-oaserver
```

2. 编译项目
```bash
mvn clean install
```

3. 运行应用
```bash
mvn spring-boot:run
```

默认情况下，应用将在 `http://localhost:8080` 上运行。

### 测试

运行自动化测试：
```bash
mvn test
```

## API文档

主要API端点：

- 认证相关: `/api/auth/**`
- 用户管理: `/api/users/**`
- 待办事项: `/api/todos/**`
- 审批流程: `/api/approvals/**`
- 日历事件: `/api/calendar/**`

## 数据库配置

应用默认使用H2内存数据库，适用于开发和测试环境。
如需连接MySQL数据库，请在`application.properties`或`application.yml`中修改相关配置。

## 安全性说明

- 使用JWT进行身份验证和授权
- 密码加密存储
- HTTPS支持（生产环境）
- CORS配置

## 贡献指南

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request 