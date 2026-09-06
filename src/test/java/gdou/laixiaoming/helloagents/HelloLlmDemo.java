package gdou.laixiaoming.helloagents;

import gdou.laixiaoming.helloagents.core.*;
import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.List;

/**
 * HelloAgents LLM调用示例
 * <p>
 * 运行前请确保：
 * 1. 在项目根目录创建 .env 文件，配置 LLM_API_KEY 和 LLM_BASE_URL
 *    或通过代码直接传入参数
 * 2. .env 示例：
 *    LLM_MODEL_ID=deepseek-chat
 *    LLM_API_KEY=sk-xxx
 *    LLM_BASE_URL=https://api.deepseek.com
 */
public class HelloLlmDemo {

    public static void main(String[] args) {
        // 方式1：从环境变量/.env加载配置
        // Llm llm = LlmFactory.createFromEnv();

        // 方式2：直接指定配置
        LlmConfig config = LlmConfig.of(
                "deepseek-chat",
                System.getenv("LLM_API_KEY"),
                "https://api.deepseek.com"
        );
        Llm llm = LlmFactory.create(config);

        System.out.println("模型: " + llm.model());
        System.out.println("Provider: " + llm.provider());
        System.out.println("---");

        // 构建消息
        List<Message> messages = List.of(
                MessageFactory.system("你是一个友好的AI助手，回答简洁。"),
                MessageFactory.user("用一句话解释什么是Java 21的虚拟线程")
        );

        // 非流式调用
        System.out.println("【非流式调用】");
        String response = llm.invoke(messages);
        System.out.println("回复: " + response);
        System.out.println();

        // 流式调用（回调方式）
        System.out.println("【流式调用】");
        System.out.print("回复: ");
        llm.stream(messages, chunk -> System.out.print(chunk));
        System.out.println();
    }
}
