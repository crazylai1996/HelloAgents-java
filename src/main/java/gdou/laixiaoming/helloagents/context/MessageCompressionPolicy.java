package gdou.laixiaoming.helloagents.context;

import gdou.laixiaoming.helloagents.core.message.Message;

import java.util.List;
import java.lang.Math;

/**
 * 根据当前历史选择压缩方式。
 */
@FunctionalInterface
public interface MessageCompressionPolicy {

    MessageCompressionStrategy select(List<Message> messages, int maxMessages);

    /**
     * 根据历史规模动态选择策略：历史较长时保留摘要，否则使用滑动窗口。
     */
    static MessageCompressionPolicy auto(MessageCompressionStrategy summaryStrategy) {
        MessageCompressionStrategy slidingWindow = MessageCompressionStrategies.slidingWindow();
        MessageCompressionStrategy summary = summaryStrategy;
        return (messages, maxMessages) -> messages.size() > Math.min(Integer.MAX_VALUE, maxMessages * 2L)
                ? summary
                : slidingWindow;
    }
}