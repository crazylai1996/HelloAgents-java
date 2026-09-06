package gdou.laixiaoming.helloagents.tools;

/**
 * 工具接口
 * <p>
 * 所有工具都必须实现此接口。
 * 工具是Agent与外部世界交互的桥梁。
 */
public interface Tool {

    /**
     * 工具名称
     */
    String name();

    /**
     * 工具描述（用于构建提示词）
     */
    String description();

    /**
     * 执行工具
     *
     * @param input 输入参数
     * @return 执行结果
     */
    String run(String input);
}
