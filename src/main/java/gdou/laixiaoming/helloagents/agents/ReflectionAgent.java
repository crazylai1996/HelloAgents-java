package gdou.laixiaoming.helloagents.agents;

import gdou.laixiaoming.helloagents.core.Llm;
import gdou.laixiaoming.helloagents.core.message.MessageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reflection Agent - 自我反思与迭代优化的智能体
 * <p>
 * 能够：
 * 1. 执行初始任务
 * 2. 对结果进行自我反思
 * 3. 根据反思结果进行优化
 * 4. 迭代改进直到满意
 * <p>
 * 特别适合代码生成、文档写作、分析报告等需要迭代优化的任务。
 */
public class ReflectionAgent extends Agent {

    private static final Map<String, String> DEFAULT_PROMPTS = Map.of(
            "initial", """
                    请根据以下要求完成任务：
                    
                    任务: %s
                    
                    请提供一个完整、准确的回答。""",

            "reflect", """
                    请仔细审查以下回答，并找出可能的问题或改进空间：
                    
                    # 原始任务:
                    %s
                    
                    # 当前回答:
                    %s
                    
                    请分析这个回答的质量，指出不足之处，并提出具体的改进建议。
                    如果回答已经很好，请回答"无需改进"。""",

            "refine", """
                    请根据反馈意见改进你的回答：
                    
                    # 原始任务:
                    %s
                    
                    # 上一轮回答:
                    %s
                    
                    # 反馈意见:
                    %s
                    
                    请提供一个改进后的回答。"""
    );

    private final int maxIterations;
    private final Map<String, String> prompts;

    public ReflectionAgent(String name, Llm llm, int maxIterations) {
        super(name, llm, null);
        this.maxIterations = maxIterations;
        this.prompts = DEFAULT_PROMPTS;
    }

    public ReflectionAgent(String name, Llm llm) {
        this(name, llm, 3);
    }

    @Override
    public String run(String input) {
        System.out.printf("%n🤖 %s 开始处理任务: %s%n", name, input);

        List<Record> records = new ArrayList<>();

        // 1. 初始执行
        System.out.println("\n--- 正在进行初始尝试 ---");
        String initialPrompt = prompts.get("initial").formatted(input);
        String initialResult = invokeLlm(initialPrompt);
        records.add(new Record("execution", initialResult));

        // 2. 迭代循环：反思与优化
        for (int i = 0; i < maxIterations; i++) {
            System.out.printf("%n--- 第 %d/%d 轮迭代 ---%n", i + 1, maxIterations);

            String lastResult = getLastExecution(records);

            // a. 反思
            System.out.println("\n-> 正在进行反思...");
            String reflectPrompt = prompts.get("reflect").formatted(input, lastResult);
            String feedback = invokeLlm(reflectPrompt);
            records.add(new Record("reflection", feedback));

            // b. 检查是否需要停止
            if (feedback.contains("无需改进") || feedback.toLowerCase().contains("no need for improvement")) {
                System.out.println("\n✅ 反思认为结果已无需改进，任务完成。");
                break;
            }

            // c. 优化
            System.out.println("\n-> 正在进行优化...");
            String refinePrompt = prompts.get("refine").formatted(input, lastResult, feedback);
            String refinedResult = invokeLlm(refinePrompt);
            records.add(new Record("execution", refinedResult));
        }

        String finalResult = getLastExecution(records);
        System.out.printf("%n--- 任务完成 ---%n最终结果:%n%s%n", finalResult);

        addMessage(MessageFactory.user(input));
        addMessage(MessageFactory.assistant(finalResult));
        return finalResult;
    }

    private String invokeLlm(String prompt) {
        return llm.invoke(List.of(MessageFactory.user(prompt)));
    }

    private static String getLastExecution(List<Record> records) {
        for (int i = records.size() - 1; i >= 0; i--) {
            if ("execution".equals(records.get(i).type)) {
                return records.get(i).content;
            }
        }
        return "";
    }

    private record Record(String type, String content) {
    }
}
