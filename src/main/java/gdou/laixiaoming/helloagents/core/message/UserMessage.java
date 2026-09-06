package gdou.laixiaoming.helloagents.core.message;

/**
 * 用户消息
 */
public final class UserMessage extends BaseMessage {

    public UserMessage(String content) {
        super(content);
    }

    @Override
    public MessageRole getRole() {
        return MessageRole.USER;
    }
}
