package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.message.Message;
import gdou.laixiaoming.helloagents.core.Llm;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;
import gdou.laixiaoming.helloagents.tools.ToolRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct (Reasoning and Acting) Agent
 * <p>
 * 结合推理和行动的智能体，能够：
 * 1. 分析问题并制定行动计划
 * 2. 调用外部工具获取信息
 * 3. 基于观察结果进行推理
 * 4. 迭代执行直到得出最终答案
 * <p>
 * 适合需要外部信息的任务。
 */
public class ReActAgent extends Agent {

    private static final String DEFAULT_REACT_PROMPT = """
            你是一个具备推理和行动能力的AI助手。你可以通过思考分析问题，然后调用合适的工具来获取信息，最终给出准确的答案。
            
            ## 可用工具
            %s
            
            ## 工作流程
            请严格按照以下格式进行回应，每次只能执行一个步骤：
            
            Thought: 分析问题，确定需要什么信息，制定研究策略。
            Action: 选择合适的工具获取信息，格式为：
            - `{tool_name}[{tool_input}]`：调用工具获取信息。
            - `Finish[研究结论]`：当你有足够信息得出结论时。
            
            ## 重要提醒
            1. 每次回应必须包含Thought和Action两部分
            2. 工具调用的格式必须严格遵循：工具名[参数]
            3. 只有当你确信有足够信息回答问题时，才使用Finish
            4. 如果工具返回的信息不够，继续使用其他工具或相同工具的不同参数
            
            ## 当前任务
            **Question:** %s
            
            ## 执行历史
            %s
            
            现在开始你的推理和行动：""";

    private static final Pattern THOUGHT_PATTERN = Pattern.compile("Thought:\\s*(.+)");
    private static final Pattern ACTION_PATTERN = Pattern.compile("Action:\\s*(.+)");
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile("(\\w+)\\[(.*)]");
    private static final Pattern FINISH_PATTERN = Pattern.compile("Finish\\[(.*)]");

    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final String promptTemplate;

    public ReActAgent(String name, Llm llm, ToolRegistry toolRegistry, int maxSteps) {
        super(name, llm, null);
        this.toolRegistry = toolRegistry != null ? toolRegistry : new ToolRegistry();
        this.maxSteps = maxSteps;
        this.promptTemplate = null;
    }

    public ReActAgent(String name, Llm llm, ToolRegistry toolRegistry) {
        this(name, llm, toolRegistry, 5);
    }

    public ReActAgent(String name, Llm llm) {
        this(name, llm, null, 5);
    }

    /**
     * 添加工具到工具注册表
     */
    public void addTool(gdou.laixiaoming.helloagents.tools.Tool tool) {
        toolRegistry.registerTool(tool);
    }

    @Override
    public String run(String input) {
        List<String> currentHistory = new ArrayList<>();

        System.out.printf("%n🤖 %s 开始处理问题: %s%n", name, input);

        for (int step = 1; step <= maxSteps; step++) {
            System.out.printf("%n--- 第 %d 步 ---%n", step);

            // 构建提示词
            String toolsDesc = toolRegistry.getToolsDescription();
            String historyStr = String.join("\n", currentHistory);
            String prompt = buildPrompt(toolsDesc, input, historyStr);

            // 调用LLM
            List<Message> messages = List.of(MessageFactory.user(prompt));
            String responseText = llm.invoke(messages);

            if (responseText == null || responseText.isBlank()) {
                System.out.println("❌ 错误：LLM未能返回有效响应。");
                break;
            }

            // 解析输出
            String thought = extractGroup(responseText, THOUGHT_PATTERN);
            String action = extractGroup(responseText, ACTION_PATTERN);

            if (thought != null) {
                System.out.printf("🤔 思考: %s%n", thought);
            }

            if (action == null) {
                System.out.println("⚠️ 警告：未能解析出有效的Action，流程终止。");
                break;
            }

            // 检查是否完成
            Matcher finishMatcher = FINISH_PATTERN.matcher(action);
            if (finishMatcher.find()) {
                String finalAnswer = finishMatcher.group(1).trim();
                System.out.printf("🎉 最终答案: %s%n", finalAnswer);

                addMessage(MessageFactory.user(input));
                addMessage(MessageFactory.assistant(finalAnswer));
                return finalAnswer;
            }

            // 解析工具调用
            Matcher toolMatcher = TOOL_CALL_PATTERN.matcher(action);
            if (!toolMatcher.find()) {
                currentHistory.add("Observation: 无效的Action格式，请检查。");
                continue;
            }

            String toolName = toolMatcher.group(1);
            String toolInput = toolMatcher.group(2);

            System.out.printf("🎬 行动: %s[%s]%n", toolName, toolInput);

            // 调用工具
            String observation = toolRegistry.executeTool(toolName, toolInput);
            System.out.printf("👀 观察: %s%n", observation);

            currentHistory.add("Action: " + action);
            currentHistory.add("Observation: " + observation);
        }

        System.out.println("⏰ 已达到最大步数，流程终止。");
        String fallback = "抱歉，我无法在限定步数内完成这个任务。";
        addMessage(MessageFactory.user(input));
        addMessage(MessageFactory.assistant(fallback));
        return fallback;
    }

    private String buildPrompt(String toolsDesc, String question, String historyStr) {
        if (promptTemplate != null) {
            return promptTemplate.formatted(toolsDesc, question, historyStr.isEmpty() ? "无" : historyStr);
        }
        return DEFAULT_REACT_PROMPT.formatted(toolsDesc, question, historyStr.isEmpty() ? "无" : historyStr);
    }

    private static String extractGroup(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}
