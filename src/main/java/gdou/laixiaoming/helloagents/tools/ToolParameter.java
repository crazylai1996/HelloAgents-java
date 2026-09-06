package gdou.laixiaoming.helloagents.tools;

/**
 * 工具参数定义
 *
 * @param name        参数名
 * @param type        参数类型（string, integer, number, boolean, array, object）
 * @param description 参数描述
 * @param required    是否必需
 */
public record ToolParameter(
        String name,
        String type,
        String description,
        boolean required) {

    public ToolParameter(String name, String type, String description) {
        this(name, type, description, true);
    }
}
