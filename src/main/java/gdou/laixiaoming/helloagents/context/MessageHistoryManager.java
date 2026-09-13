package gdou.laixiaoming.helloagents.context;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import gdou.laixiaoming.helloagents.core.message.Message;

/**
 * 管理Agent的消息历史。
 *
 * <p>管理器始终通过快照暴露消息，避免调用方直接修改内部历史。设置最大容量后，
 * 新消息会从历史开头淘汰最早的消息。</p>
 */
public final class MessageHistoryManager {

    private final Deque<Message> messages = new ArrayDeque<>();
    private final int maxMessages;

    /**
     * 创建一个不限制消息数量的历史管理器。
     */
    public MessageHistoryManager() {
        this(Integer.MAX_VALUE);
    }

    /**
     * @param maxMessages 保留的最大消息数量，必须大于0
     */
    public MessageHistoryManager(int maxMessages) {
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("消息历史容量必须大于0");
        }
        this.maxMessages = maxMessages;
    }

    /**
     * 添加一条消息，必要时淘汰最早的消息。
     */
    public synchronized void add(Message message) {
        messages.addLast(Objects.requireNonNull(message, "消息不能为null"));
        trimToLimit();
    }

    /**
     * 按顺序添加多条消息。
     */
    public synchronized void addAll(Collection<? extends Message> messages) {
        Objects.requireNonNull(messages, "消息集合不能为null");
        for (Message message : messages) {
            add(message);
        }
    }

    /**
     * 获取当前历史的只读快照。
     */
    public synchronized List<Message> snapshot() {
        return List.copyOf(messages);
    }

    public synchronized int size() {
        return messages.size();
    }

    public synchronized boolean isEmpty() {
        return messages.isEmpty();
    }

    public synchronized void clear() {
        messages.clear();
    }

    public int maxMessages() {
        return maxMessages;
    }

    private void trimToLimit() {
        while (messages.size() > maxMessages) {
            messages.removeFirst();
        }
    }
}