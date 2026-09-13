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
 * 历史压缩由上层Agent决定何时触发。</p>
 */
public final class MessageHistoryManager {

    private final Deque<Message> messages = new ArrayDeque<>();
    private final int maxMessages;
    private final MessageCompressionStrategy messageCompressionStrategy;

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
        this(maxMessages, MessageCompressionStrategies.slidingWindow());
    }

    /**
     * @param maxMessages 保留的最大消息数量，必须大于0
     * @param messageCompressionStrategy 超限时选择压缩方式的策略
     */
    public MessageHistoryManager(int maxMessages, MessageCompressionStrategy messageCompressionStrategy) {
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("消息历史容量必须大于0");
        }
        this.maxMessages = maxMessages;
        this.messageCompressionStrategy = Objects.requireNonNull(messageCompressionStrategy, "压缩策略不能为null");
    }

    /**
     * 添加一条消息。
     */
    public synchronized void add(Message message) {
        messages.addLast(Objects.requireNonNull(message, "消息不能为null"));
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

    /**
     * 根据当前策略主动压缩历史。历史未超过容量时不会改变内容。
     */
    public synchronized void compress() {
        if (messages.size() <= maxMessages) {
            return;
        }
        compressToLimit();
    }

    public int maxMessages() {
        return maxMessages;
    }

    private void compressToLimit() {
        while (messages.size() > maxMessages) {
            List<Message> snapshot = List.copyOf(messages);
            MessageCompressionStrategy strategy = Objects.requireNonNull(
                    messageCompressionStrategy,
                    "压缩策略不能为null");
            List<Message> compressed = strategy.compress(snapshot, maxMessages);
            validateCompressedMessages(compressed);
            if (compressed.size() >= snapshot.size()) {
                throw new IllegalStateException("压缩策略未减少消息数量，无法完成压缩");
            }
            messages.clear();
            messages.addAll(compressed);
        }
    }

    private void validateCompressedMessages(List<Message> compressed) {
        if (compressed == null || compressed.size() > maxMessages) {
            throw new IllegalStateException("压缩策略必须返回不超过容量限制的消息列表");
        }
        if (compressed.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("压缩策略不能返回null消息");
        }
    }
}