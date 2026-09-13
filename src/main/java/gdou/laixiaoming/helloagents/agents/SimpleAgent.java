package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.core.Llm;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单对话Agent
 * <p>
 * 最基础的Agent，直接将用户输入和系统提示词发送给LLM，获取回复。
 * 支持多轮对话（自动维护对话历史）。
 */
public class SimpleAgent extends Agent {

    public SimpleAgent(String name, Llm llm, String systemPrompt) {
        super(name, llm, systemPrompt);
    }

    public SimpleAgent(String name, Llm llm) {
        this(name, llm, "你是一个有用的AI助手。");
    }

    @Override
    public String run(String input) {
        // 记录用户输入
        addMessage(MessageFactory.user(input));

        // 构建消息列表（system prompt + 历史）
        List<Message> messages = buildMessages();

        // 调用LLM
        String response = llm.invoke(messages);

        // 记录助手回复
        addMessage(MessageFactory.assistant(response));

        return response;
    }

    /**
     * 流式运行
     *
     * @param input   用户输入
     * @param onChunk 流式回调
     */
    public void streamRun(String input, java.util.function.Consumer<String> onChunk) {
        addMessage(MessageFactory.user(input));

        List<Message> messages = buildMessages();

        // 收集完整回复
        var fullResponse = new StringBuilder();
        llm.stream(messages, chunk -> {
            fullResponse.append(chunk);
            onChunk.accept(chunk);
        });

        addMessage(MessageFactory.assistant(fullResponse.toString()));
    }

    private List<Message> buildMessages() {
        var messages = new ArrayList<Message>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(MessageFactory.system(systemPrompt));
        }
        messages.addAll(history.snapshot());
        return messages;
    }
}
