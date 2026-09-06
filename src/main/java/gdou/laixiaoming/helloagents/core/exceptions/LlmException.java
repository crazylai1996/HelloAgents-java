package gdou.laixiaoming.helloagents.core.exceptions;

/**
 * LLM相关异常
 */
public class LlmException extends HelloAgentsException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
