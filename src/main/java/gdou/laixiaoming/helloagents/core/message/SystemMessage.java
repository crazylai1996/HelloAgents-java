package gdou.laixiaoming.helloagents.core.message;

/**
 * 系统消息
 */
public final class SystemMessage extends BaseMessage {

    public SystemMessage(String content) {
        super(content);
    }

    @Override
    public MessageRole getRole() {
        return MessageRole.SYSTEM;
    }
}
