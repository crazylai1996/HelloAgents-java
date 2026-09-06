package gdou.laixiaoming.helloagents.core.message;

/**
 * 消息工厂
 */
public final class MessageFactory {

    public static SystemMessage system(String content) {
        return new SystemMessage(content);
    }

    public static UserMessage user(String content) {
        return new UserMessage(content);
    }

    public static AssistantMessage assistant(String content) {
        return new AssistantMessage(content);
    }

    public static ToolMessage tool(String toolCallId, String name, String content) {
        return new ToolMessage(toolCallId, name, content);
    }
}
