package gdou.laixiaoming.helloagents.core;

/**
 * LLM工厂
 * <p>
 * 根据配置创建对应的LLM实例。
 * 当前所有Provider统一使用OpenAiLlm实现（OpenAI兼容模式）。
 */
public final class LlmFactory {

    private LlmFactory() {
    }

    /**
     * 根据配置创建LLM实例
     *
     * @param config LLM配置
     * @return LLM实例
     */
    public static Llm create(LlmConfig config) {
        return switch (config.provider()) {
            // 当前所有Provider都使用OpenAI兼容实现
            case OPENAI_COMPATIBLE, ANTHROPIC, GEMINI -> new OpenAiLlm(config);
        };
    }

    /**
     * 从环境变量加载配置并创建LLM实例
     *
     * @return LLM实例
     */
    public static Llm createFromEnv() {
        return create(LlmConfig.fromEnv());
    }
}
