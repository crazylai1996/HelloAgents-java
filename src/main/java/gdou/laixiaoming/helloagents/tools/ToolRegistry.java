package gdou.laixiaoming.helloagents.tools;

import java.util.*;
import java.util.function.Function;

/**
 * 工具注册表
 * <p>
 * 提供工具的注册、管理和执行功能。
 * 支持两种方式注册工具：
 * 1. Tool对象注册（推荐）
 * 2. 函数直接注册（简便）
 */
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    /**
     * 注册Tool对象
     */
    public void registerTool(Tool tool) {
        tools.put(tool.name(), tool);
    }

    /**
     * 直接注册函数作为工具（简便方式）
     *
     * @param name        工具名称
     * @param description 工具描述
     * @param func        工具函数
     */
    public void registerFunction(String name, String description, Function<String, String> func) {
        tools.put(name, new FunctionTool(name, description, func));
    }

    /**
     * 注销工具
     */
    public void unregister(String name) {
        tools.remove(name);
    }

    /**
     * 获取工具
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }

    /**
     * 执行工具
     *
     * @param name  工具名称
     * @param input 输入参数
     * @return 执行结果
     */
    public String executeTool(String name, String input) {
        Tool tool = tools.get(name);
        if (tool == null) {
            return "错误：未找到名为 '%s' 的工具。".formatted(name);
        }
        try {
            return tool.run(input);
        } catch (Exception e) {
            return "错误：执行工具 '%s' 时发生异常: %s".formatted(name, e.getMessage());
        }
    }

    /**
     * 获取所有可用工具的描述字符串（用于构建提示词）
     */
    public String getToolsDescription() {
        if (tools.isEmpty()) {
            return "暂无可用工具";
        }
        var sb = new StringBuilder();
        for (var tool : tools.values()) {
            sb.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    /**
     * 列出所有工具名称
     */
    public List<String> listTools() {
        return List.copyOf(tools.keySet());
    }

    /**
     * 获取所有工具对象
     */
    public Collection<Tool> getAllTools() {
        return List.copyOf(tools.values());
    }

    /**
     * 清空所有工具
     */
    public void clear() {
        tools.clear();
    }

    /**
     * 将函数包装为Tool的适配器
     */
    private record FunctionTool(String name, String description,
                                Function<String, String> func) implements Tool {
        @Override
        public String run(String input) {
            return func.apply(input);
        }
    }
}
