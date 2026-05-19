package HamiltonianPath;

import java.util.ArrayList;
import java.util.List;

public class HamiltonianGreedy {

    // 5 minutes = 300,000,000,000 nanoseconds
    private static final long TIME_LIMIT_NS = 600_000_000_000L;

    private static boolean timedOut = false;

    public static boolean hasHamiltonianPath(Graph g) {
        return findHamiltonianPath(g) != null;
    }

    public static boolean wasTimedOut() {
        return timedOut;
    }

    public static List<Integer> findHamiltonianPath(Graph g) {

        timedOut = false;

        int n = g.size();
        long startTime = System.nanoTime();

        for (int start = 0; start < n; start++) {

            if (shouldStop(startTime)) {
                timedOut = true;
                return null;
            }

            boolean[] visited = new boolean[n];
            List<Integer> path = new ArrayList<>();

            int current = start;

            while (true) {

                if (shouldStop(startTime)) {
                    timedOut = true;
                    return null;
                }

                visited[current] = true;
                path.add(current);

                if (path.size() == n) {
                    return path;
                }

                int next = -1;
                int minDegree = Integer.MAX_VALUE;

                for (int neighbor : g.getNeighbors(current)) {

                    if (shouldStop(startTime)) {
                        timedOut = true;
                        return null;
                    }

                    if (!visited[neighbor]) {

                        int degree = g.getNeighbors(neighbor).size();

                        if (degree < minDegree) {
                            minDegree = degree;
                            next = neighbor;
                        }
                    }
                }

                if (next == -1) {
                    break;
                }

                current = next;
            }
        }

        return null;
    }

    private static boolean shouldStop(long startTime) {

        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        return System.nanoTime() - startTime > TIME_LIMIT_NS;
    }
}
