package gdou.laixiaoming.helloagents.core.exceptions;

/**
 * HelloAgents基础异常类
 */
public class HelloAgentsException extends RuntimeException {

    public HelloAgentsException(String message) {
        super(message);
    }

    public HelloAgentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
