package gdou.laixiaoming.helloagents.context;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 内置消息压缩策略。
 */
public final class MessageCompressionStrategies {

    private MessageCompressionStrategies() {
    }

    /**
     * 只保留最近的消息，适合更关注当前对话的场景。
     */
    public static MessageCompressionStrategy slidingWindow() {
        return (messages, maxMessages) -> {
            if (messages.size() <= maxMessages) {
                return List.copyOf(messages);
            }
            return List.copyOf(messages.subList(messages.size() - maxMessages, messages.size()));
        };
    }

    /**
     * 将较早消息合并为一条摘要，并保留最近消息。
     */
    public static MessageCompressionStrategy summary(Function<List<Message>, String> summaryGenerator) {
        Objects.requireNonNull(summaryGenerator, "摘要生成器不能为null");
        return (messages, maxMessages) -> {
            if (messages.size() <= maxMessages) {
                return List.copyOf(messages);
            }
            if (maxMessages == 1) {
                return List.of(MessageFactory.system(generateSummary(summaryGenerator, messages)));
            }

            int recentCount = maxMessages - 1;
            List<Message> compressed = new ArrayList<>(maxMessages);
            compressed.add(MessageFactory.system(generateSummary(
                    summaryGenerator, messages.subList(0, messages.size() - recentCount))));
            compressed.addAll(messages.subList(messages.size() - recentCount, messages.size()));
            return List.copyOf(compressed);
        };
    }

    private static String generateSummary(Function<List<Message>, String> summaryGenerator,
                                          List<Message> messages) {
        String summary = summaryGenerator.apply(List.copyOf(messages));
        if (summary == null || summary.isBlank()) {
            throw new IllegalStateException("摘要生成器未生成有效的历史摘要");
        }
        return summary;
    }
}