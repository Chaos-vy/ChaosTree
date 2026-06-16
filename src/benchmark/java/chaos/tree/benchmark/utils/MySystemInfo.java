package chaos.tree.benchmark.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Provides static utility methods to inspect, extract, and print low-level
 * operating system distribution metrics and bare-metal hardware topologies.
 * <p>
 * This utility uses a combination of native JVM management beans and shell fallbacks
 * to parse virtual files under the Linux kernel environment.
 * </p>
 */
public final class MySystemInfo {
    private MySystemInfo() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Executes a hardware and environmental diagnostics sweep, printing the system
     * signature directly to standard output.
     * <p>
     * <b>Extracted Metadata Parameters:</b>
     * <ul>
     * <li>Linux Distribution (via /etc/os-release mapping)</li>
     * <li>Kernel Version and System Target Architecture</li>
     * <li>Active JVM Heap Bounds and Thread Stack configurations</li>
     * <li>Physical RAM footprint hardware characteristics (Clock Speed and Type)</li>
     * </ul>
     * </p>
     */
    public static void printSignature() {
        System.out.println("=".repeat(45));
        System.out.println("        ENVIRONMENT & HARDWARE SIGNATURE         ");
        System.out.println("=".repeat(45));

        String distro = "Unknown Linux";
        try {
            File osRelease = new File("/etc/os-release");
            if (osRelease.exists()) {
                Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "grep '^PRETTY_NAME=' /etc/os-release | cut -d'=' -f2 | tr -d '\"'"});
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null) distro = line.trim();
                }
            }
        } catch (Exception e) {
            distro = System.getProperty("os.name") + " (Fallback Namespace)";
        }
        System.out.println("OS Distribution : " + distro);
        System.out.println("Kernel Version  : " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");

        long maxMemory = Runtime.getRuntime().maxMemory();
        long allocatedMemory = Runtime.getRuntime().totalMemory();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        System.out.println("\n[JVM Runtime Bounds]");
        System.out.println("Max Allowed Heap (-Xmx) : " + (maxMemory == Long.MAX_VALUE ? "Unlimited Allocation" : String.format("%,d MB", maxMemory / (1024 * 1024))));
        System.out.println("Current Allocated Heap  : " + String.format("%,d MB", allocatedMemory / (1024 * 1024)));
        System.out.println("JVM Engine Arguments    : " + runtimeMxBean.getInputArguments());

        System.out.println("\n[Physical Hardware Memory]");
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", "pkexec dmidecode --type memory | grep -E 'Speed:|Type:|Size:' | grep -v 'No Module' | tr -d '\\t'"});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                boolean physicalFound = false;
                while ((line = reader.readLine()) != null) {
                    System.out.println(" Hardware RAM Component -> " + line.trim());
                    physicalFound = true;
                }
                if (!physicalFound) {
                    Process p2 = Runtime.getRuntime().exec(new String[]{"sh", "-c", "grep MemTotal /proc/meminfo"});
                    try (BufferedReader reader2 = new BufferedReader(new InputStreamReader(p2.getInputStream()))) {
                        String mem = reader2.readLine();
                        if (mem != null)
                            System.out.println(" Hardware RAM Component -> Total System Capacity: " + mem.trim());
                    }
                    System.out.println(" [Notice: Run with root/pkexec execution wrappers to unlock raw type and clock frequencies]");
                }
            }
        } catch (Exception e) {
            System.out.println(" Hardware RAM Component -> Access Restricted / Pipeline Refused");
        }
        System.out.println("\n[Execution Context Metadata]");
        System.out.println("Available CPU Cores     : " + Runtime.getRuntime().availableProcessors());
        System.out.println("Java Vendor / Version   : " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        System.out.println("=".repeat(45)+"\n");
    }
}
