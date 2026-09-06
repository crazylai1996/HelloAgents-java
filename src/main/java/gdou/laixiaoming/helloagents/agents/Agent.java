package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.core.Llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agent抽象基类
 * <p>
 * 所有Agent的父类，提供通用的消息历史管理功能。
 */
public abstract class Agent {

    protected final String name;
    protected final Llm llm;
    protected final String systemPrompt;
    protected final List<Message> history = new ArrayList<>();

    protected Agent(String name, Llm llm, String systemPrompt) {
        this.name = name;
        this.llm = llm;
        this.systemPrompt = systemPrompt;
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
        return Collections.unmodifiableList(new ArrayList<>(history));
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
