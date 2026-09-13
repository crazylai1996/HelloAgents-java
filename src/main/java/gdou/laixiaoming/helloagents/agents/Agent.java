package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.context.MessageHistoryManager;
import gdou.laixiaoming.helloagents.context.MessageCompressionPolicy;
import gdou.laixiaoming.helloagents.context.MessageCompressionStrategies;
import gdou.laixiaoming.helloagents.core.Llm;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.List;
import java.util.Objects;

/**
 * Agent抽象基类
 * <p>
 * 所有Agent的父类，提供通用的消息历史管理功能。
 */
public abstract class Agent {

    protected final String name;
    protected final Llm llm;
    protected final String systemPrompt;
    protected final MessageHistoryManager history;

    protected Agent(String name, Llm llm, String systemPrompt) {
        this(name, llm, systemPrompt, new MessageHistoryManager());
    }

    protected Agent(String name, Llm llm, String systemPrompt, int maxHistoryMessages) {
        this(name, llm, systemPrompt, createHistoryManager(llm, maxHistoryMessages));
    }

    protected Agent(String name, Llm llm, String systemPrompt, MessageHistoryManager history) {
        this.name = name;
        this.llm = llm;
        this.systemPrompt = systemPrompt;
        this.history = Objects.requireNonNull(history, "消息历史管理器不能为null");
    }

    /**
     * 运行Agent
     *
     * @param input 用户输入
     * @return Agent的回复
     */
    public abstract String run(String input);

    /**
     * 添加消息到历史记录
     */
    protected void addMessage(Message message) {
        history.add(message);
        if (history.size() > history.maxMessages()) {
            history.compress();
        }
    }

    private static MessageHistoryManager createHistoryManager(Llm llm, int maxMessages) {
        Objects.requireNonNull(llm, "启用摘要压缩需要LLM实例");
        return new MessageHistoryManager(maxMessages, MessageCompressionPolicy.auto(
                MessageCompressionStrategies.summary(messages -> generateSummary(llm, messages))));
    }

    private static String generateSummary(Llm llm, List<Message> messages) {
        var history = new StringBuilder();
        for (Message message : messages) {
            history.append("[")
                    .append(message.getRole())
                    .append("] ")
                    .append(message.getContent())
                    .append('\n');
        }
        String summary = llm.invoke(List.of(
                MessageFactory.system("你是对话历史摘要器。请提炼事实、用户偏好、已完成事项和未解决问题，保留后续对话所需信息。只输出摘要，不要添加额外说明。"),
                MessageFactory.user(history.toString())
        ));
        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("LLM未生成有效的历史摘要");
        }
        return summary;
    }

    /**
     * 获取历史记录的只读副本
     */
    public List<Message> getHistory() {
        return history.snapshot();
    }

    /**
     * 清空历史记录
     */
    public void clearHistory() {
        history.clear();
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "%s(name=%s)".formatted(getClass().getSimpleName(), name);
    }
}
