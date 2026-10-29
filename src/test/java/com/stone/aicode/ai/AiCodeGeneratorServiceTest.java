package com.stone.aicode.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        String result = collect(aiCodeGeneratorService.generateHtmlCodeStream("做个程序员的博客，不超过 20 行"));
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        String result = collect(aiCodeGeneratorService.generateMultiFileCodeStream("做个程序员的留言板"));
        Assertions.assertNotNull(result);
    }

    @Test
    void testChatMemory() {
        String result = collect(aiCodeGeneratorService.generateHtmlCodeStream("做个程序员的工具网站，总代码量不超过 20 行"));
        Assertions.assertNotNull(result);
        result = collect(aiCodeGeneratorService.generateHtmlCodeStream("不要生成网站，告诉我你刚刚做了什么？"));
        Assertions.assertNotNull(result);
        result = collect(aiCodeGeneratorService.generateHtmlCodeStream("做个程序员的工具网站，总代码量不超过 20 行"));
        Assertions.assertNotNull(result);
        result = collect(aiCodeGeneratorService.generateHtmlCodeStream("不要生成网站，告诉我你刚刚做了什么？"));
        Assertions.assertNotNull(result);
    }

    /**
     * 阻塞收集流式结果
     */
    private String collect(Flux<String> flux) {
        return flux.collectList().block().stream().collect(Collectors.joining());
    }
}
