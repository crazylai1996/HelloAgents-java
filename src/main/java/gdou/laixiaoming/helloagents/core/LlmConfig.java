package gdou.laixiaoming.helloagents.core;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * LLM配置
 * <p>
 * 设计理念：参数优先，环境变量兜底。
 * 支持从 .env 文件加载配置。
 *
 * @param provider    模型提供者类型
 * @param model       模型名称
 * @param apiKey      API密钥
 * @param baseUrl     服务地址
 * @param temperature 温度参数（0-2），默认0.7
 * @param maxTokens   最大token数，null表示不限制
 * @param timeout     超时时间（秒），默认60
 */
public record LlmConfig(
        ProviderType provider,
        String model,
        String apiKey,
        String baseUrl,
        double temperature,
        Integer maxTokens,
        int timeout
) {

    /**
     * 便捷构造：仅指定模型和API Key，使用OpenAI兼容模式
     */
    public static LlmConfig of(String model, String apiKey, String baseUrl) {
        return new LlmConfig(ProviderType.OPENAI_COMPATIBLE, model, apiKey, baseUrl, 0.7, null, 60);
    }

    /**
     * 从环境变量加载配置（支持 .env 文件）
     */
    public static LlmConfig fromEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String apiKey = resolve(dotenv, "LLM_API_KEY");
        String baseUrl = resolve(dotenv, "LLM_BASE_URL");
        String model = resolve(dotenv, "LLM_MODEL_ID");
        String timeoutStr = resolve(dotenv, "LLM_TIMEOUT");

        // 自动检测Provider
        ProviderType provider = detectProvider(apiKey, baseUrl);

        // 如果未指定model，根据provider获取默认模型
        if (model == null || model.isBlank()) {
            model = getDefaultModel(provider, baseUrl);
        }

        int timeout = timeoutStr != null ? Integer.parseInt(timeoutStr) : 60;

        return new LlmConfig(provider, model, apiKey, baseUrl, 0.7, null, timeout);
    }

    /**
     * 自动检测Provider类型
     * <p>
     * 检测逻辑：
     * 1. 根据API密钥格式判断
     * 2. 根据base_url判断
     * 3. 默认为OPENAI_COMPATIBLE
     */
    static ProviderType detectProvider(String apiKey, String baseUrl) {
        // 1. 根据API密钥格式判断
        if (apiKey != null) {
            String keyLower = apiKey.toLowerCase();
            if (apiKey.startsWith("ms-")) {
                return ProviderType.OPENAI_COMPATIBLE;
            }
            if ("ollama".equals(keyLower) || "vllm".equals(keyLower) || "local".equals(keyLower)) {
                return ProviderType.OPENAI_COMPATIBLE;
            }
        }

        // 2. 根据base_url判断
        if (baseUrl != null) {
            String urlLower = baseUrl.toLowerCase();
            if (urlLower.contains("anthropic.com")) {
                return ProviderType.ANTHROPIC;
            }
            if (urlLower.contains("generativelanguage.googleapis.com") || urlLower.contains("gemini")) {
                return ProviderType.GEMINI;
            }
            // 以下都是OpenAI兼容的服务
            return ProviderType.OPENAI_COMPATIBLE;
        }

        // 3. 默认
        return ProviderType.OPENAI_COMPATIBLE;
    }

    /**
     * 获取Provider对应的默认模型
     */
    private static String getDefaultModel(ProviderType provider, String baseUrl) {
        if (baseUrl != null) {
            String urlLower = baseUrl.toLowerCase();
            if (urlLower.contains("deepseek")) return "deepseek-chat";
            if (urlLower.contains("dashscope")) return "qwen-plus";
            if (urlLower.contains("modelscope")) return "Qwen/Qwen2.5-72B-Instruct";
            if (urlLower.contains("moonshot")) return "moonshot-v1-8k";
            if (urlLower.contains("bigmodel")) return "glm-4";
            if (urlLower.contains("11434") || urlLower.contains("ollama")) return "llama3.2";
        }

        return switch (provider) {
            case OPENAI_COMPATIBLE -> "gpt-4o-mini";
            case ANTHROPIC -> "claude-3-5-sonnet-20241022";
            case GEMINI -> "gemini-pro";
        };
    }

    /**
     * 从dotenv中解析环境变量，优先dotenv，其次System.getenv
     */
    private static String resolve(Dotenv dotenv, String key) {
        try {
            String value = dotenv.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        } catch (Exception ignored) {
            // dotenv中未找到
        }
        return System.getenv(key);
    }
}
