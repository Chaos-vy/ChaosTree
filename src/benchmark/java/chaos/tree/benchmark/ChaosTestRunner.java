package chaos.tree.benchmark;

import chaos.tree.benchmark.annotations.Chaos;
import chaos.tree.benchmark.utils.TreeFamilyChaos;

import java.lang.reflect.Method;

/**
 * The central command execution entry point for the Chaos Engine.
 * Uses reflection to discover and isolate heavy-duty structural destruction targets.
 */
public class ChaosTestRunner {

    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("          CHAOS ENGINE HARNESS ACTIVATED         ");
        System.out.println("=".repeat(50));

        try {
            Class<?> targetClass = TreeFamilyChaos.class;
            Object instance = targetClass.getDeclaredConstructor().newInstance();

            boolean testExecuted = false;
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Chaos.class)) {
                    Chaos chaosMetadata = method.getAnnotation(Chaos.class);

                    System.out.println("[Target Isolated] : " + method.getName());
                    System.out.println("[Description]     : " + chaosMetadata.description());
                    long configuredCap = chaosMetadata.initialNodeCap();
                    method.invoke(instance, configuredCap);

                    testExecuted = true;
                    System.out.println("=".repeat(50));
                    System.out.println("          CHAOS TEST SEQUENCE COMPLETED          ");
                    System.out.println("=".repeat(50));
                }
            }

            if (!testExecuted) {
                System.err.println("[Error] No methods marked with @Chaos were detected inside " + targetClass.getSimpleName());
            }

        } catch (Exception e) {
            System.err.println("\n[FATAL] The Orchestrator runner collapsed prematurely due to a configuration failure:");
            e.printStackTrace();
        }
    }
}