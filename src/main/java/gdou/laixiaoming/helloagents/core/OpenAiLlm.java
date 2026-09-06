package gdou.laixiaoming.helloagents.core;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import gdou.laixiaoming.helloagents.core.exceptions.LlmException;
import gdou.laixiaoming.helloagents.core.message.BaseMessage;
import gdou.laixiaoming.helloagents.core.message.Message;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * 基于OpenAI Java SDK的LLM实现
 * <p>
 * 支持所有OpenAI兼容的API服务（OpenAI、DeepSeek、通义千问、Ollama等）。
 * 设计理念：一个实现覆盖所有OpenAI兼容Provider。
 */
public class OpenAiLlm implements Llm {

    private final OpenAIClient client;
    private final String model;
    private final ProviderType provider;
    private final double temperature;
    private final Integer maxTokens;

    public OpenAiLlm(LlmConfig config) {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new LlmException("API密钥不能为空，请检查配置或环境变量");
        }
        if (config.baseUrl() == null || config.baseUrl().isBlank()) {
            throw new LlmException("服务地址不能为空，请检查配置或环境变量");
        }

        this.client = OpenAIOkHttpClient.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .timeout(Duration.ofSeconds(config.timeout()))
                .build();
        this.model = config.model();
        this.provider = config.provider();
        this.temperature = config.temperature();
        this.maxTokens = config.maxTokens();
    }

    @Override
    public String invoke(List<Message> messages) {
        try {
            var params = buildParams(messages);
            ChatCompletion completion = client.chat().completions().create(params);
            return completion.choices().getFirst().message().content().orElse("");
        } catch (Exception e) {
            throw new LlmException("LLM调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Stream<String> think(List<Message> messages) {
        try {
            var params = buildParams(messages);
            StreamResponse<com.openai.models.chat.completions.ChatCompletionChunk> streamResponse =
                    client.chat().completions().createStreaming(params);

            return streamResponse.stream()
                    .flatMap(chunk -> chunk.choices().stream())
                    .flatMap(choice -> choice.delta().content().stream())
                    .filter(content -> !content.isEmpty())
                    .onClose(streamResponse::close);
        } catch (Exception e) {
            throw new LlmException("LLM流式调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public ProviderType provider() {
        return provider;
    }

    /**
     * 构建ChatCompletion请求参数
     */
    private ChatCompletionCreateParams buildParams(List<Message> messages) {
        var builder = ChatCompletionCreateParams.builder();

        for (Message msg : messages) {
            builder.addMessage(buildMessageParam(msg));
        }

        builder.model(ChatModel.of(model));
        builder.temperature(temperature);

        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }

    /**
     * 将ChatMessage转换为SDK的消息参数
     */
    private static com.openai.models.chat.completions.ChatCompletionMessageParam buildMessageParam(Message msg) {
        return switch (msg.getRole()) {
            case SYSTEM -> com.openai.models.chat.completions.ChatCompletionMessageParam.ofSystem(
                    com.openai.models.chat.completions.ChatCompletionSystemMessageParam.builder()
                            .content(msg.getContent())
                            .build()
            );
            case USER -> com.openai.models.chat.completions.ChatCompletionMessageParam.ofUser(
                    com.openai.models.chat.completions.ChatCompletionUserMessageParam.builder()
                            .content(msg.getContent())
                            .build()
            );
            case ASSISTANT -> com.openai.models.chat.completions.ChatCompletionMessageParam.ofAssistant(
                    com.openai.models.chat.completions.ChatCompletionAssistantMessageParam.builder()
                            .content(msg.getContent())
                            .build()
            );
            case TOOL -> com.openai.models.chat.completions.ChatCompletionMessageParam.ofTool(
                    com.openai.models.chat.completions.ChatCompletionToolMessageParam.builder()
                            .content(msg.getContent())
                            .toolCallId("tool")
                            .build()
            );
        };
    }
}
