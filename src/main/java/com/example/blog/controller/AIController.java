package com.example.blog.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final ChatClient chatClient;

    public AIController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/analyze-log")
    public ResponseEntity<?> analyzeLog(@RequestBody Map<String, String> request) {
        try {
            String logContent = request.get("logContent");
            if (logContent == null || logContent.isEmpty()) {
                // 替换 Map.of() 为 HashMap
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("success", false);
                errorMap.put("message", "日志内容不能为空");
                return ResponseEntity.badRequest().body(errorMap);
            }

            String aiResponse = chatClient.prompt()
                    .system("你是一名专业的日志分析工程师，擅长分析 Spring Boot/MySQL 日志。" +
                            "请完成以下任务：" +
                            "1. 识别日志类型（正常日志/异常日志）；" +
                            "2. 若有异常，提取异常原因和解决方案；" +
                            "3. 用简洁明了的中文总结核心信息（不超过 3 行）。")
                    .user("请分析以下日志：\n" + logContent)
                    .call()
                    .content();
            /**
             * 1.启动「提示词（Prompt）构建器」。
             * 核心概念：Prompt 是大模型的「输入指令集」，包含「系统指令、用户输入、历史对话」等，大模型会根据 Prompt 理解需求并生成响应。
             * 这里是链式调用的开始，后续所有配置（角色、任务、数据）都基于这个构建器。
             */
            /**
             * 2..system("...")给大模型「定义身份 + 设定任务规则」，让大模型明确自己的角色和工作边界。
             *
             * 3.. 用户输入：.user("...")
             *向大模型传递「具体要处理的数据」（即前端传入的日志内容 logContent）。
             * 关键细节：
             * 前缀 “请分析以下日志：\n” 是自然语言引导，让大模型清楚后续内容是需要分析的目标；
             * \n 是换行符，让日志内容和引导语分开，大模型更容易识别数据边界（避免把引导语当成日志的一部分）；
             * logContent 是前端通过请求体传入的日志字符串（比如 Spring Boot 报错日志、MySQL 慢查询日志）。
             *
             *
             * 4. 调用大模型：.call()触发大模型调用，将前面构建好的 Prompt（系统指令 + 用户输入）发送给配置好的大模型（如 GPT-3.5、通义千问）。
             * 底层逻辑：Spring AI 会自动完成以下工作（无需手动处理）：
             * 将 Prompt 转换成大模型支持的请求格式（如 OpenAI 的 JSON 格式）；
             * 发起 HTTP 请求到大模型的 API 接口（如 OpenAI 的 https://api.openai.com/v1/chat/completions）；
             * 处理身份验证（如携带 API Key）、超时控制等；
             * 接收大模型的响应数据。
             *
             * 5.获取结果.content()：从大模型的响应中提取「核心文本内容」，返回字符串格式。
             * 说明：大模型的响应通常包含很多元数据（如响应 ID、生成时间、token 数量等），content() 方法会直接过滤这些元数据，只返回大模型生成的「分析结果文本」（比如前面示例中的 “1. 异常日志；2. 异常原因：空指针异常...”）。
             */


            // 替换 Map.of() 为 HashMap
            Map<String, Object> successMap = new HashMap<>();
            successMap.put("success", true);
            successMap.put("result", aiResponse);
            return ResponseEntity.ok(successMap);
        } catch (Exception e) {
            // 替换 Map.of() 为 HashMap
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", "AI 分析失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }
    @PostMapping("/chat") // 新接口路径：/api/ai/chat
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> request) {
        try {
            // 1. 获取前端传递的「对话历史」和「新消息」
            List<Map<String, String>> chatHistory = (List<Map<String, String>>) request.get("chatHistory");
            String newMessage = (String) request.get("newMessage");

            // 2. 参数校验（新消息不能为空）
            if (newMessage == null || newMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "消息内容不能为空"
                ));
            }

            // 3. 构建多轮对话链（System 提示词 + 历史对话 + 新消息）
            List<Message> messages = new ArrayList<>();

            // ① 定义 AI 为通用聊天助手（核心：聊天相关提示词）
            messages.add(new SystemMessage(
                    "你是友好、专业的通用聊天助手，擅长解答各种问题,你的名字叫杨玉泉，是java开发工程师。" +
                            "要求：1. 中文回复，语气亲切自然，首次见面可以介绍一下自己；2. 回答准确简洁，不啰嗦；3. 支持多轮对话，记住上下文。"
            ));

            // ② 加入历史对话（实现上下文关联）
            if (chatHistory != null && !chatHistory.isEmpty()) {
                for (Map<String, String> msg : chatHistory) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    switch (role) {
                        case "user" -> messages.add(new UserMessage(content)); // 用户消息
                        case "assistant" -> messages.add(new AssistantMessage(content)); // AI 消息
                        default -> {} // 忽略无效角色
                    }
                }
            }

            // ③ 加入用户新发送的消息
            messages.add(new UserMessage(newMessage));

            // 4. 调用 AI 生成聊天回复
            String aiResponse = chatClient.prompt()
                    .messages(messages) // 传入完整对话链
                    .call()
                    .content();

            // 5. 返回结果（JDK 17 用 Map.of() 简化代码）
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "aiResponse", aiResponse // 前端接收 AI 回复
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "聊天失败：" + e.getMessage()
            ));
        }
    }
    @PostMapping("/generate-log-title")
    public ResponseEntity<?> generateLogTitle(@RequestBody Map<String, String> request) {
        try {
            String logContent = request.get("logContent");
            if (logContent == null || logContent.isEmpty()) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("success", false);
                errorMap.put("message", "日志内容不能为空");
                return ResponseEntity.badRequest().body(errorMap);
            }

            String aiTitle = chatClient.prompt()
                    .system("你是日志标题生成专家，根据日志内容生成简洁、准确的中文标题（不超过 15 字）。")
                    .user("日志内容：\n" + logContent)
                    .call()
                    .content();

            Map<String, Object> successMap = new HashMap<>();
            successMap.put("success", true);
            successMap.put("title", aiTitle);
            return ResponseEntity.ok(successMap);
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", "生成标题失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }
}