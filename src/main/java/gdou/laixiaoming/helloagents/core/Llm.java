package gdou.laixiaoming.helloagents.core;

import gdou.laixiaoming.helloagents.core.message.Message;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * LLM统一接口
 * <p>
 * 提供非流式和流式两种调用方式。
 */
public interface Llm {

    /**
     * 非流式调用LLM，返回完整响应
     *
     * @param messages 消息列表
     * @return 模型回复文本
     */
    String invoke(List<Message> messages);

    /**
     * 流式调用LLM，返回文本片段流
     * <p>
     * 调用方需确保在使用完毕后关闭返回的Stream（推荐使用try-with-resources），
     * 以释放底层连接资源。
     *
     * @param messages 消息列表
     * @return 流式文本片段
     */
    Stream<String> think(List<Message> messages);

    /**
     * 流式调用LLM，通过回调逐片输出
     *
     * @param messages 消息列表
     * @param onChunk  每收到一个文本片段时的回调
     */
    default void stream(List<Message> messages, Consumer<String> onChunk) {
        try (Stream<String> stream = think(messages)) {
            stream.forEach(onChunk);
        }
    }

    /**
     * 获取当前使用的模型名称
     */
    String model();

    /**
     * 获取Provider类型
     */
    ProviderType provider();
}
