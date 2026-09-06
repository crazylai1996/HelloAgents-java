package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.Llm;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plan and Solve Agent - 分解规划与逐步执行的智能体
 * <p>
 * 能够：
 * 1. 将复杂问题分解为简单步骤
 * 2. 按照计划逐步执行
 * 3. 维护执行历史和上下文
 * 4. 得出最终答案
 * <p>
 * 特别适合多步骤推理、数学问题、复杂分析等任务。
 */
public class PlanAndSolveAgent extends Agent {

    private static final String DEFAULT_PLANNER_PROMPT = """
            你是一个顶级的AI规划专家。你的任务是将用户提出的复杂问题分解成一个由多个简单步骤组成的行动计划。
            请确保计划中的每个步骤都是一个独立的、可执行的子任务，并且严格按照逻辑顺序排列。
            你的输出必须是一个JSON数组，其中每个元素都是一个描述子任务的字符串。
            
            问题: %s
            
            请严格按照以下格式输出你的计划（不要输出其他内容）:
            ```json
            ["步骤1", "步骤2", "步骤3"]
            ```""";

    private static final String DEFAULT_EXECUTOR_PROMPT = """
            你是一位顶级的AI执行专家。你的任务是严格按照给定的计划，一步步地解决问题。
            你将收到原始问题、完整的计划、以及到目前为止已经完成的步骤和结果。
            请你专注于解决"当前步骤"，并仅输出该步骤的最终答案，不要输出任何额外的解释或对话。
            
            # 原始问题:
            %s
            
            # 完整计划:
            %s
            
            # 历史步骤与结果:
            %s
            
            # 当前步骤:
            %s
            
            请仅输出针对"当前步骤"的回答:""";

    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[\\s*\"(.+?)\"\\s*]", Pattern.DOTALL);

    private final String plannerPrompt;
    private final String executorPrompt;

    public PlanAndSolveAgent(String name, Llm llm) {
        super(name, llm, null);
        this.plannerPrompt = DEFAULT_PLANNER_PROMPT;
        this.executorPrompt = DEFAULT_EXECUTOR_PROMPT;
    }

    @Override
    public String run(String input) {
        System.out.printf("%n🤖 %s 开始处理问题: %s%n", name, input);

        // 1. 生成计划
        List<String> plan = generatePlan(input);
        if (plan.isEmpty()) {
            String fallback = "无法生成有效的行动计划，任务终止。";
            System.out.printf("%n--- 任务终止 ---%n%s%n", fallback);
            addMessage(MessageFactory.user(input));
            addMessage(MessageFactory.assistant(fallback));
            return fallback;
        }

        // 2. 执行计划
        String finalAnswer = executePlan(input, plan);
        System.out.printf("%n--- 任务完成 ---%n最终答案: %s%n", finalAnswer);

        addMessage(MessageFactory.user(input));
        addMessage(MessageFactory.assistant(finalAnswer));
        return finalAnswer;
    }

    /**
     * 调用LLM生成执行计划
     */
    private List<String> generatePlan(String question) {
        System.out.println("--- 正在生成计划 ---");

        String prompt = plannerPrompt.formatted(question);
        String response = llm.invoke(List.of(MessageFactory.user(prompt)));

        if (response == null || response.isBlank()) {
            System.out.println("❌ LLM未能返回有效计划。");
            return List.of();
        }

        System.out.printf("✅ 计划已生成:%n%s%n", response);
        return parsePlan(response);
    }

    /**
     * 按计划逐步执行
     */
    private String executePlan(String question, List<String> plan) {
        System.out.println("\n--- 正在执行计划 ---");

        StringBuilder history = new StringBuilder();
        String finalAnswer = "";
        String planStr = String.join("\n", plan);

        for (int i = 0; i < plan.size(); i++) {
            String step = plan.get(i);
            System.out.printf("%n-> 正在执行步骤 %d/%d: %s%n", i + 1, plan.size(), step);

            String prompt = executorPrompt.formatted(
                    question,
                    planStr,
                    history.isEmpty() ? "无" : history.toString(),
                    step
            );

            String response = llm.invoke(List.of(MessageFactory.user(prompt)));
            if (response == null) {
                response = "";
            }

            history.append("步骤 ").append(i + 1).append(": ").append(step)
                    .append("\n结果: ").append(response).append("\n\n");

            finalAnswer = response;
            System.out.printf("✅ 步骤 %d 已完成，结果: %s%n", i + 1, finalAnswer);
        }

        return finalAnswer;
    }

    /**
     * 从LLM响应中解析计划步骤
     * <p>
     * 支持JSON数组格式：["步骤1", "步骤2", ...]
     */
    static List<String> parsePlan(String response) {
        List<String> steps = new ArrayList<>();

        // 尝试提取JSON数组
        // 先找到 [ 和 ] 的位置
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start < 0 || end < 0 || end <= start) {
            System.out.println("❌ 无法从响应中解析计划。");
            return List.of();
        }

        String arrayContent = response.substring(start + 1, end);

        // 匹配引号内的内容
        Matcher matcher = JSON_ARRAY_PATTERN.matcher("[" + arrayContent + "]");
        while (matcher.find()) {
            String step = matcher.group(1).trim();
            if (!step.isEmpty()) {
                steps.add(step);
            }
        }

        // 如果JSON解析失败，尝试按行分割
        if (steps.isEmpty()) {
            for (String line : arrayContent.split("\n")) {
                String trimmed = line.trim().replaceAll("^\"|\"$", "").replaceAll(",$", "").trim();
                if (!trimmed.isEmpty()) {
                    steps.add(trimmed);
                }
            }
        }

        return steps;
    }
}
