package gdou.laixiaoming.helloagents.core.exceptions;

/**
 * Agent相关异常
 */
public class AgentException extends HelloAgentsException {

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
