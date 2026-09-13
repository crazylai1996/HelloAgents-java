package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.context.MessageHistoryManager;
import gdou.laixiaoming.helloagents.core.Llm;

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
