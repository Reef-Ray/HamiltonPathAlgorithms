import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    // Backtracking is exponential/factorial — hits ~10 min around n=25-28
    private static final int[] BACKTRACKING_SIZES =
            {5, 8, 10, 12, 14, 16, 18, 20, 22, 25};

    // Greedy is polynomial — hits ~10 min in the tens-of-thousands range
    private static final int[] GREEDY_SIZES =
            {500, 1000, 2500, 5000, 10000, 25000, 50000, 100000, 250000, 500000};

    private static final int RUNS = 5;

    private static final String OUTPUT_FILE = "hamiltonian_results.csv";

    public static double nanoToSeconds(long nanoseconds) {
        return nanoseconds / 1_000_000_000.0;
    }

    public static double nanoToMilliseconds(long nanoseconds) {
        return nanoseconds / 1_000_000.0;
    }

    public static String formatSeconds(long nanoseconds) {
        return String.format("%.6f", nanoToSeconds(nanoseconds));
    }

    public static String formatMilliseconds(long nanoseconds) {
        return String.format("%.6f", nanoToMilliseconds(nanoseconds));
    }

    public static void main(String[] args) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE))) {

            writer.println("n,algorithm,runs,successes,failures,average_seconds");

            System.out.println("\n=== BACKTRACKING EXPERIMENTS ===");
            for (int n : BACKTRACKING_SIZES) {
                runBacktrackingExperiment(n, writer);
            }

            System.out.println("\n=== GREEDY EXPERIMENTS ===");
            for (int n : GREEDY_SIZES) {
                runGreedyExperiment(n, writer);
            }

            System.out.println("\nResults saved to " + OUTPUT_FILE);

        } catch (IOException e) {
            System.out.println("Error writing CSV file.");
            e.printStackTrace();
        }
    }

    private static void runBacktrackingExperiment(int n, PrintWriter writer) {

        long totalTime = 0;
        int successes = 0;

        System.out.println("\nBacktracking | Vertices n = " + n);

        for (int run = 1; run <= RUNS; run++) {

            System.out.println("\nRun " + run + ":");

            Graph g = GraphGenerator.generateRandomGraph(n);

            long start = System.nanoTime();
            boolean result = HamiltonianBacktracking.hasHamiltonianPath(g);
            long end = System.nanoTime();

            long elapsed = end - start;
            totalTime += elapsed;

            if (result) {
                successes++;
            }

            printRunResult("Backtracking", result, elapsed);
        }

        long averageNs = totalTime / RUNS;
        int failures = RUNS - successes;

        writer.println(n + ",Backtracking,"
                + RUNS + ","
                + successes + ","
                + failures + ","
                + nanoToSeconds(averageNs));

        System.out.println("\nAverage Results for n = " + n);
        System.out.println("Backtracking successes: " + successes + "/" + RUNS);
        printAverageResult("Backtracking", averageNs);
    }

    private static void runGreedyExperiment(int n, PrintWriter writer) {

        long totalTime = 0;
        int successes = 0;

        System.out.println("\nGreedy | Vertices n = " + n);

        for (int run = 1; run <= RUNS; run++) {

            System.out.println("\nRun " + run + ":");

            Graph g = GraphGenerator.generateRandomGraph(n);

            long start = System.nanoTime();
            boolean result = HamiltonianGreedy.hasHamiltonianPath(g);
            long end = System.nanoTime();

            long elapsed = end - start;
            totalTime += elapsed;

            if (result) {
                successes++;
            }

            printRunResult("Greedy", result, elapsed);
        }

        long averageNs = totalTime / RUNS;
        int failures = RUNS - successes;

        writer.println(n + ",Greedy,"
                + RUNS + ","
                + successes + ","
                + failures + ","
                + nanoToSeconds(averageNs));

        System.out.println("\nAverage Results for n = " + n);
        System.out.println("Greedy successes: " + successes + "/" + RUNS);
        printAverageResult("Greedy", averageNs);
    }

    private static void printRunResult(
            String algorithm,
            boolean result,
            long timeNs) {

        System.out.println(algorithm + ": " + result
                + " Time: " + formatMilliseconds(timeNs)
                + " ms (" + formatSeconds(timeNs) + " seconds)");
    }

    private static void printAverageResult(String algorithm, long averageNs) {
        System.out.println("Average " + algorithm + " Time: "
                + formatMilliseconds(averageNs)
                + " ms (" + formatSeconds(averageNs) + " seconds)");
    }
}
