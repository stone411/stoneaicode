# Stone AI Code

Stone AI Code 是一个基于大模型的 **AI 应用生成平台**。用户用自然语言描述需求，平台自动判断应该生成哪一类应用，调用 AI 通过「工具调用」把代码逐个文件写入磁盘，并实时把生成过程流式推送到前端；生成完成后可以预览、对话修改、一键部署，也可以下载完整源码。

后端基于 Spring Boot 3 + LangChain4j 实现，生成物支持三种形态：

| 生成类型 | 说明 |
| --- | --- |
| `HTML` | 单文件 HTML 页面，适合简单的展示页、小工具 |
| `MULTI_FILE` | HTML + CSS + JS 多文件网站，适合稍复杂的静态站点 |
| `VUE_PROJECT` | 完整 Vue 3 工程，适合管理系统等需要路由、组件、构建的项目 |

## 项目结构

```
src/main/java/com/stone/aicode
├── AiCodeApplication.java     启动类
├── ai/                        AI 能力
│   ├── AiCodeGeneratorService(Factory)      代码生成 AI 服务与工厂（按 appId/类型缓存实例）
│   ├── AiCodeGenTypeRoutingService(Factory) 生成类型智能路由
│   ├── guardrail/            输入护轨
│   ├── model/                HTML/多文件生成结果、图片资源模型
│   └── tools/                工具定义与 ToolManager（文件读写改删、目录浏览、图片搜索、插画、Logo、架构图）
├── core/                      代码生成核心链路
│   ├── AiCodeGeneratorFacade  生成入口：调用 AI + 解析 + 保存 + 流式处理
│   ├── parser/                HTML / 多文件代码解析器
│   ├── saver/                 代码落盘模板
│   ├── handler/               简单文本流 / JSON 消息流处理器
│   └── builder/               Vue 项目构建
├── controller/                REST 接口（应用、对话历史、用户、静态资源、健康检查）
├── service/                   业务服务与实现
├── aop/ annotation/           鉴权切面与注解
├── ratelimter/                基于 Redisson 的限流注解、切面与配置
├── manager/                   本地文件存储等外部服务封装
├── mapper/ model/             数据访问与实体、DTO、VO、枚举
└── config/ common/ constant/ exception/ utils/
```