# OA Application

## 项目概述

本项目是一个现代化的办公自动化(OA)系统，旨在提高组织的工作效率和协作能力。系统采用前后端分离架构，具有良好的用户体验和可扩展性。

## 技术栈

### 后端
- Java (Spring Boot框架)
- JPA/Hibernate
- RESTful API
- JWT认证
- Maven构建工具
- MySQL数据库

### 前端
- Vue.js组件化框架
- HTML5/CSS3
- JavaScript
- 响应式设计

## 项目结构

```
oaapp/
├── spring-oaserver/        # 后端代码
│   ├── src/main/           # 源代码
│   │   ├── java/           # Java源文件
│   │   └── resources/      # 配置文件
│   └── src/test/           # 测试代码
│       ├── java/           # Java测试文件
│       └── resources/      # 测试配置
└── frontend/               # 前端代码
    ├── src/                # 源代码
    │   ├── components/     # Vue组件
    │   ├── views/          # 页面视图
    │   └── assets/         # 静态资源
    └── public/             # 公共文件
```

## 功能特性

- 用户管理与权限控制
- 待办事项管理
- 审批流程
- 日历与日程管理
- 文档管理
- 消息通知

## 安装指南

### 前提条件
- Java 17或更高版本
- Node.js 14或更高版本
- Maven 3.6或更高版本
- MySQL 8.0或更高版本

### 后端设置
1. 克隆仓库
   ```bash
   git clone https://your-repository-url/oaapp.git
   cd oaapp/spring-oaserver
   ```

2. 配置数据库
   - 在`src/main/resources/application.properties`中更新数据库连接

3. 构建并运行后端
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
4. 后端将在`http://localhost:8080`启动

### 前端设置
1. 进入前端目录
   ```bash
   cd ../frontend
   ```

2. 安装依赖
   ```bash
   npm install
   ```

3. 运行开发服务器
   ```bash
   npm run serve
   ```

4. 前端将在`http://localhost:8081`启动

## 测试

项目包含全面的测试套件，包括单元测试和集成测试。

### 运行测试
```bash
# 后端测试
cd spring-oaserver
mvn test

# 前端测试
cd frontend
npm run test
```

## 构建部署

### 后端
```bash
cd spring-oaserver
mvn clean package
```
生成的JAR文件位于`target/`目录。

### 前端
```bash
cd frontend
npm run build
```
构建文件位于`dist/`目录，可部署到任何静态文件服务器。

## 开发指南

- 遵循项目现有的代码风格和架构模式
- 新功能开发前先编写测试用例
- 提交代码前运行所有测试，确保通过
- 使用有意义的提交信息

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 许可证

[指定您的许可证] 