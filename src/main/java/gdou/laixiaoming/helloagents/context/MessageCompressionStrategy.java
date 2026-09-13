package gdou.laixiaoming.helloagents.context;

import gdou.laixiaoming.helloagents.core.message.Message;

import java.util.List;

/**
 * 消息历史压缩策略。
 */
@FunctionalInterface
public interface MessageCompressionStrategy {

    /**
     * 将历史压缩到指定上限以内。
     *
     * @param messages 当前历史快照
     * @param maxMessages 压缩后的最大消息数
     * @return 压缩后的消息列表
     */
    List<Message> compress(List<Message> messages, int maxMessages);
}