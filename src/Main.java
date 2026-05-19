package HamiltonianPath;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final int[] BACKTRACKING_SIZES =
            {5, 6, 7, 8, 9, 10, 11, 12, 13};

    private static final int[] GREEDY_SIZES =
        {500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000};

//	private static final int[] BACKTRACKING_SIZES = {};
//	
//	private static final int[] GREEDY_SIZES = {4500};
	
    private static final int RUNS = 1;

    // 10 minutes = 600 seconds
    private static final long TIME_LIMIT_SECONDS = 600;

    private static final double GREEDY_EDGE_PROBABILITY = 0.35;

    private static final String OUTPUT_FILE = "hamiltonian_results.csv";

    private static boolean stopBacktrackingExperiments = false;
    private static boolean stopGreedyExperiments = false;

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

            writer.println("n,algorithm,graph_type,runs_completed,successes,failures,timeouts,average_seconds,paths_counted");

            System.out.println("\nTime limit per run: "
                    + TIME_LIMIT_SECONDS + " seconds");

            System.out.println("\nBacktracking and greedy have separate stop conditions.");
            System.out.println("If backtracking hits the timeout, greedy will still run.");

            System.out.println("\n=== BACKTRACKING COUNT-ALL EXPERIMENTS ===");
            System.out.println("Backtracking uses one complete graph per n.");
            System.out.println("This forces the algorithm to count all Hamiltonian paths.");

            for (int n : BACKTRACKING_SIZES) {

                if (stopBacktrackingExperiments) {
                    break;
                }

                Graph g = GraphGenerator.generateCompleteGraph(n);
                runBacktrackingExperiment(n, g, writer);
                writer.flush();
            }

            if (stopBacktrackingExperiments) {
                System.out.println("\nBacktracking stopped because a run hit the 10-minute limit.");
            }

            System.out.println("\n=== GREEDY EXPERIMENTS ===");
            System.out.println("Greedy uses one random graph per n.");
            System.out.println("Each n gets one graph, reused across all runs for that n.");

            for (int n : GREEDY_SIZES) {

                if (stopGreedyExperiments) {
                    break;
                }

                long seed = 1000L + n;
                Graph g = GraphGenerator.generateRandomGraph(n, GREEDY_EDGE_PROBABILITY, seed);

                runGreedyExperiment(n, g, writer);
                writer.flush();
            }

            if (stopGreedyExperiments) {
                System.out.println("\nGreedy stopped because a run hit the 10-minute limit.");
            }

            System.out.println("\nResults saved to " + OUTPUT_FILE);

        } catch (IOException e) {
            System.out.println("Error writing CSV file.");
            e.printStackTrace();
        }
    }

    private static void runBacktrackingExperiment(
            int n,
            Graph g,
            PrintWriter writer) {

        long totalTime = 0;
        long lastPathCount = 0;

        int successes = 0;
        int failures = 0;
        int timeouts = 0;
        int runsCompleted = 0;

        System.out.println("\nBacktracking Count All | Vertices n = " + n);
        System.out.println("Graph type: Complete graph");
        System.out.println("Expected complete graph paths: n!");

        for (int run = 1; run <= RUNS; run++) {

            if (stopBacktrackingExperiments) {
                break;
            }

            System.out.println("\nRun " + run + ":");

            TimedRunResult result = runWithTimeLimit(() -> {
                long pathCount = HamiltonianBacktracking.countAllHamiltonianPaths(g);
                boolean algorithmTimedOut = HamiltonianBacktracking.wasTimedOut();

                return new AlgorithmResult(pathCount > 0, algorithmTimedOut, pathCount);
            });

            runsCompleted++;
            totalTime += result.elapsedTimeNs;
            lastPathCount = result.pathsCounted;

            if (result.timedOut) {
                timeouts++;
                printTimeoutResult("Backtracking Count All", result.elapsedTimeNs);
                System.out.println("Paths counted before timeout: " + result.pathsCounted);
                stopBacktrackingExperiments = true;
            } else if (result.foundPath) {
                successes++;
                printRunResult("Backtracking Count All", true, result.elapsedTimeNs);
                System.out.println("Total Hamiltonian paths counted: " + result.pathsCounted);
            } else {
                failures++;
                printRunResult("Backtracking Count All", false, result.elapsedTimeNs);
                System.out.println("Total Hamiltonian paths counted: " + result.pathsCounted);
            }
        }

        if (runsCompleted == 0) {
            return;
        }

        long averageNs = totalTime / runsCompleted;

        writer.println(n + ",Backtracking Count All,Complete,"
                + runsCompleted + ","
                + successes + ","
                + failures + ","
                + timeouts + ","
                + nanoToSeconds(averageNs) + ","
                + lastPathCount);

        System.out.println("\nAverage Results for n = " + n);
        System.out.println("Backtracking runs completed: " + runsCompleted + "/" + RUNS);
        System.out.println("Backtracking successes: " + successes + "/" + runsCompleted);
        System.out.println("Backtracking failures: " + failures + "/" + runsCompleted);
        System.out.println("Backtracking timeouts: " + timeouts + "/" + runsCompleted);
        System.out.println("Last path count: " + lastPathCount);
        printAverageResult("Backtracking Count All", averageNs);
    }

    private static void runGreedyExperiment(
            int n,
            Graph g,
            PrintWriter writer) {

        long totalTime = 0;

        int successes = 0;
        int failures = 0;
        int timeouts = 0;
        int runsCompleted = 0;

        System.out.println("\nGreedy | Vertices n = " + n);
        System.out.println("Graph type: Random graph, p = " + GREEDY_EDGE_PROBABILITY);
        System.out.println("Same graph reused for every run at this n.");

        for (int run = 1; run <= RUNS; run++) {

            if (stopGreedyExperiments) {
                break;
            }

            System.out.println("\nRun " + run + ":");

            TimedRunResult result = runWithTimeLimit(() -> {
                boolean foundPath = HamiltonianGreedy.hasHamiltonianPath(g);
                boolean algorithmTimedOut = HamiltonianGreedy.wasTimedOut();

                return new AlgorithmResult(foundPath, algorithmTimedOut, 0);
            });

            runsCompleted++;
            totalTime += result.elapsedTimeNs;

            if (result.timedOut) {
                timeouts++;
                printTimeoutResult("Greedy", result.elapsedTimeNs);
                stopGreedyExperiments = true;
            } else if (result.foundPath) {
                successes++;
                printRunResult("Greedy", true, result.elapsedTimeNs);
            } else {
                failures++;
                printRunResult("Greedy", false, result.elapsedTimeNs);
            }
        }

        if (runsCompleted == 0) {
            return;
        }

        long averageNs = totalTime / runsCompleted;

        writer.println(n + ",Greedy,Random p=" + GREEDY_EDGE_PROBABILITY + ","
                + runsCompleted + ","
                + successes + ","
                + failures + ","
                + timeouts + ","
                + nanoToSeconds(averageNs) + ","
                + 0);

        System.out.println("\nAverage Results for n = " + n);
        System.out.println("Greedy runs completed: " + runsCompleted + "/" + RUNS);
        System.out.println("Greedy successes: " + successes + "/" + runsCompleted);
        System.out.println("Greedy failures: " + failures + "/" + runsCompleted);
        System.out.println("Greedy timeouts: " + timeouts + "/" + runsCompleted);
        printAverageResult("Greedy", averageNs);
    }

    private static TimedRunResult runWithTimeLimit(
            Callable<AlgorithmResult> task) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        long start = System.nanoTime();
        Future<AlgorithmResult> future = executor.submit(task);

        try {
            AlgorithmResult algorithmResult =
                    future.get(TIME_LIMIT_SECONDS, TimeUnit.SECONDS);

            long end = System.nanoTime();

            executor.shutdownNow();

            return new TimedRunResult(
                    algorithmResult.foundPath,
                    algorithmResult.timedOut,
                    end - start,
                    algorithmResult.pathsCounted);

        } catch (TimeoutException e) {
            long end = System.nanoTime();

            future.cancel(true);
            executor.shutdownNow();

            return new TimedRunResult(false, true, end - start, 0);

        } catch (InterruptedException e) {
            long end = System.nanoTime();

            future.cancel(true);
            executor.shutdownNow();
            Thread.currentThread().interrupt();

            return new TimedRunResult(false, true, end - start, 0);

        } catch (ExecutionException e) {
            long end = System.nanoTime();

            future.cancel(true);
            executor.shutdownNow();

            System.out.println("Algorithm threw an error:");
            e.printStackTrace();

            return new TimedRunResult(false, false, end - start, 0);
        }
    }

    private static void printRunResult(
            String algorithm,
            boolean result,
            long timeNs) {

        System.out.println(algorithm + ": " + result
                + " Time: " + formatMilliseconds(timeNs)
                + " ms (" + formatSeconds(timeNs) + " seconds)");
    }

    private static void printTimeoutResult(String algorithm, long timeNs) {
        System.out.println(algorithm + ": TIMEOUT"
                + " Time: " + formatMilliseconds(timeNs)
                + " ms (" + formatSeconds(timeNs) + " seconds)");
    }

    private static void printAverageResult(String algorithm, long averageNs) {
        System.out.println("Average " + algorithm + " Time: "
                + formatMilliseconds(averageNs)
                + " ms (" + formatSeconds(averageNs) + " seconds)");
    }

    private static class AlgorithmResult {

        private final boolean foundPath;
        private final boolean timedOut;
        private final long pathsCounted;

        private AlgorithmResult(
                boolean foundPath,
                boolean timedOut,
                long pathsCounted) {

            this.foundPath = foundPath;
            this.timedOut = timedOut;
            this.pathsCounted = pathsCounted;
        }
    }

    private static class TimedRunResult {

        private final boolean foundPath;
        private final boolean timedOut;
        private final long elapsedTimeNs;
        private final long pathsCounted;

        private TimedRunResult(
                boolean foundPath,
                boolean timedOut,
                long elapsedTimeNs,
                long pathsCounted) {

            this.foundPath = foundPath;
            this.timedOut = timedOut;
            this.elapsedTimeNs = elapsedTimeNs;
            this.pathsCounted = pathsCounted;
        }
    }
}
