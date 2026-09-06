package gdou.laixiaoming.helloagents.core.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public abstract sealed class BaseMessage implements Message
        permits SystemMessage, UserMessage, AssistantMessage, ToolMessage {

    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间戳
     */
    private long timestamp;

    public BaseMessage(String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 消息类型
     *
     * @return
     */
    public abstract MessageRole getRole();

}
