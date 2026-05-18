import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    private static final int[] SIZES =
            {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 100};

    private static final double[] PROBABILITIES =
            {0.25, 0.4, 0.85};

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

            writer.println("p,n,algorithm,runs,successes,failures,average_seconds");

            for (double p : PROBABILITIES) {
                runProbabilityExperiment(p, writer);
            }

            System.out.println("\nResults saved to " + OUTPUT_FILE);

        } catch (IOException e) {
            System.out.println("Error writing CSV file.");
            e.printStackTrace();
        }
    }

    private static void runProbabilityExperiment(double p, PrintWriter writer) {

        System.out.println("\n=====================================");
        System.out.println("Testing edge probability p = " + p);
        System.out.println("Each run uses one random graph only");
        System.out.println("No graph is regenerated if backtracking fails");
        System.out.println("=====================================");

        for (int n : SIZES) {
            runSizeExperiment(n, p, writer);
        }
    }

    private static void runSizeExperiment(int n, double p, PrintWriter writer) {

        long totalBacktrackingTime = 0;
        long totalGreedyTime = 0;

        int backtrackingSuccesses = 0;
        int greedySuccesses = 0;

        System.out.println("\nVertices n = " + n);

        for (int run = 1; run <= RUNS; run++) {

            System.out.println("\nRun " + run + ":");

            Graph g = GraphGenerator.generateRandomGraph(n, p);

            long startBacktracking = System.nanoTime();
            boolean backtrackingResult =
                    HamiltonianBacktracking.hasHamiltonianPath(g);
            long endBacktracking = System.nanoTime();

            long backtrackingTime = endBacktracking - startBacktracking;
            totalBacktrackingTime += backtrackingTime;

            if (backtrackingResult) {
                backtrackingSuccesses++;
            }

            printRunResult("Backtracking", backtrackingResult, backtrackingTime);

            long startGreedy = System.nanoTime();
            boolean greedyResult =
                    HamiltonianGreedy.hasHamiltonianPath(g);
            long endGreedy = System.nanoTime();

            long greedyTime = endGreedy - startGreedy;
            totalGreedyTime += greedyTime;

            if (greedyResult) {
                greedySuccesses++;
            }

            printRunResult("Greedy", greedyResult, greedyTime);
        }

        long averageBacktrackingNs = totalBacktrackingTime / RUNS;
        long averageGreedyNs = totalGreedyTime / RUNS;

        int backtrackingFailures = RUNS - backtrackingSuccesses;
        int greedyFailures = RUNS - greedySuccesses;

        writer.println(p + "," + n + ",Backtracking,"
                + RUNS + ","
                + backtrackingSuccesses + ","
                + backtrackingFailures + ","
                + nanoToSeconds(averageBacktrackingNs));

        writer.println(p + "," + n + ",Greedy,"
                + RUNS + ","
                + greedySuccesses + ","
                + greedyFailures + ","
                + nanoToSeconds(averageGreedyNs));

        System.out.println("\nAverage Results for n = " + n);
        System.out.println("Probability p = " + p);

        System.out.println("Backtracking successes: "
                + backtrackingSuccesses + "/" + RUNS);

        System.out.println("Greedy successes: "
                + greedySuccesses + "/" + RUNS);

        printAverageResult("Backtracking", averageBacktrackingNs);
        printAverageResult("Greedy", averageGreedyNs);
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
