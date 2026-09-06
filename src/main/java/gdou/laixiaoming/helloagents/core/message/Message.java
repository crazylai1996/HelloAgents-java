package gdou.laixiaoming.helloagents.core.message;

/**
 * 消息
 */
public interface Message {
    MessageRole getRole();
    String getContent();
}
