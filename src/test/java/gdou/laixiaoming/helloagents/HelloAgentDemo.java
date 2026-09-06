package gdou.laixiaoming.helloagents;

import gdou.laixiaoming.helloagents.agents.*;
import gdou.laixiaoming.helloagents.core.*;
import gdou.laixiaoming.helloagents.tools.ToolRegistry;

import java.util.Scanner;

/**
 * HelloAgents Agent 示例
 * <p>
 * 演示四种Agent范式的使用。
 * 运行前请确保配置了 .env 文件（LLM_API_KEY、LLM_BASE_URL、LLM_MODEL_ID）。
 */
public class HelloAgentDemo {

    public static void main(String[] args) {
        // 创建LLM实例
        Llm llm = LlmFactory.createFromEnv();
        System.out.println("✅ LLM已加载，模型: " + llm.model());

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n========== HelloAgents Demo ==========");
            System.out.println("1. SimpleAgent    - 简单对话");
            System.out.println("2. ReActAgent     - 推理+工具调用");
            System.out.println("3. ReflectionAgent - 自我反思与优化");
            System.out.println("4. PlanAndSolveAgent - 规划+逐步执行");
            System.out.println("0. 退出");
            System.out.print("请选择Agent类型 (0-4): ");

            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                System.out.println("再见！");
                break;
            }

            System.out.print("请输入你的问题: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("输入不能为空，请重试。");
                continue;
            }

            switch (choice) {
                case "1" -> runSimpleAgent(llm, input);
                case "2" -> runReActAgent(llm, input);
                case "3" -> runReflectionAgent(llm, input);
                case "4" -> runPlanAndSolveAgent(llm, input);
                default -> System.out.println("无效选择，请重试。");
            }
        }

        scanner.close();
    }

    /**
     * SimpleAgent - 简单对话
     */
    private static void runSimpleAgent(Llm llm, String input) {
        SimpleAgent agent = new SimpleAgent("小助手", llm, "你是一个友好的AI助手，回答简洁明了。");
        String result = agent.run(input);
        System.out.println("\n📝 回复: " + result);
    }

    /**
     * ReActAgent - 推理+工具调用
     */
    private static void runReActAgent(Llm llm, String input) {
        // 创建工具注册表并注册计算器工具
        ToolRegistry registry = new ToolRegistry();

        // 也可以注册自定义函数工具
        registry.registerFunction("text_length", "计算文本的字符数。输入文本，返回字符数。",
                text -> String.valueOf(text.length()));

        ReActAgent agent = new ReActAgent("研究助手", llm, registry, 5);
        String result = agent.run(input);
        System.out.println("\n📝 最终结果: " + result);
    }

    /**
     * ReflectionAgent - 自我反思与优化
     */
    private static void runReflectionAgent(Llm llm, String input) {
        ReflectionAgent agent = new ReflectionAgent("代码专家", llm, 2);
        String result = agent.run(input);
        System.out.println("\n📝 最终结果: " + result);
    }

    /**
     * PlanAndSolveAgent - 规划+逐步执行
     */
    private static void runPlanAndSolveAgent(Llm llm, String input) {
        PlanAndSolveAgent agent = new PlanAndSolveAgent("问题解决专家", llm);
        String result = agent.run(input);
        System.out.println("\n📝 最终结果: " + result);
    }
}
