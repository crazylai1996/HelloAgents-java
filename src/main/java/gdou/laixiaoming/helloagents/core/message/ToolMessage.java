package gdou.laixiaoming.helloagents.core.message;

import lombok.Getter;

/**
 * 工具调用消息
 */
@Getter
public final class ToolMessage extends BaseMessage {


    private final String toolCallId;

    private final String name;


    public ToolMessage(String toolCallId, String name, String content) {
        super(content);
        this.toolCallId = toolCallId;
        this.name = name;
    }

    @Override
    public MessageRole getRole() {
        return MessageRole.TOOL;
    }
}
