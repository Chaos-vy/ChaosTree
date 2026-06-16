package chaos.tree.benchmark.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * Provides static utility methods to inspect, extract, and print low-level
 * operating system distribution metrics and bare-metal hardware topologies.
 * <p>
 * This utility relies exclusively on core Java Management Extensions (JMX),
 * ensuring cross-platform stability across Linux, macOS, and Windows environments.
 * </p>
 */
public final class SystemInfo {
    private SystemInfo() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    public static void printSignature() {
        System.out.println("=".repeat(45));
        System.out.println("        ENVIRONMENT & HARDWARE SIGNATURE         ");
        System.out.println("=".repeat(45));
        OperatingSystemMXBean x = ManagementFactory.getOperatingSystemMXBean();
        System.out.println("OS Architecture : " + x.getName() + " (" + x.getArch() + ")");
        System.out.println("OS Version/Patch: " + x.getVersion());
        long maxMemory = Runtime.getRuntime().maxMemory();
        long allocatedMemory = Runtime.getRuntime().totalMemory();
        RuntimeMXBean runtimeMx = ManagementFactory.getRuntimeMXBean();

        System.out.println("\n[JVM Runtime Bounds]");
        System.out.println("Max Allowed Heap (-Xmx) : " + (maxMemory == Long.MAX_VALUE ? "Unlimited Allocation" : String.format("%,d MB", maxMemory / (1024 * 1024))));
        System.out.println("Current Allocated Heap  : " + String.format("%,d MB", allocatedMemory / (1024 * 1024)));
        System.out.println("JVM Engine Arguments    : " + runtimeMx.getInputArguments());
        System.out.println("\n[Physical Hardware Memory]");
        try {
            com.sun.management.OperatingSystemMXBean sunOsMx =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long totalPhysicalMemory = sunOsMx.getTotalMemorySize();
            System.out.println(" Total System Capacity  : " + String.format("%,d MB", totalPhysicalMemory / (1024 * 1024)));
        } catch (Exception e) {
            System.out.println(" Total System Capacity  : Access Restricted via Core MXBean");
        }
        System.out.println("\n[Execution Context Metadata]");
        System.out.println("Available CPU Cores     : " + x.getAvailableProcessors());
        System.out.println("Java Vendor / Version   : " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        System.out.println("=".repeat(45)+"\n");
    }
}