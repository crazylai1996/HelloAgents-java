package gdou.laixiaoming.helloagents.core.message;

/**
 * AI 消息
 */
public final class AssistantMessage extends BaseMessage {

    public AssistantMessage(String content) {
        super(content);
    }

    @Override
    public MessageRole getRole() {
        return MessageRole.ASSISTANT;
    }
}
