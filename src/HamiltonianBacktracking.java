package HamiltonianPath;

import java.util.ArrayList;
import java.util.List;

public class HamiltonianBacktracking {

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
            int[] path = new int[n];

            if (dfsFindOne(g, start, visited, 1, path, startTime)) {

                List<Integer> result = new ArrayList<>();

                for (int i = 0; i < n; i++) {
                    result.add(path[i]);
                }

                return result;
            }
        }

        return null;
    }

    private static boolean dfsFindOne(
            Graph g,
            int current,
            boolean[] visited,
            int count,
            int[] path,
            long startTime) {

        if (shouldStop(startTime)) {
            timedOut = true;
            return false;
        }

        visited[current] = true;
        path[count - 1] = current;

        if (count == g.size()) {
            return true;
        }

        for (int neighbor : g.getNeighbors(current)) {

            if (shouldStop(startTime)) {
                timedOut = true;
                return false;
            }

            if (!visited[neighbor]) {
                if (dfsFindOne(g, neighbor, visited, count + 1, path, startTime)) {
                    return true;
                }
            }
        }

        visited[current] = false;
        return false;
    }

    public static long countAllHamiltonianPaths(Graph g) {

        timedOut = false;

        int n = g.size();
        long startTime = System.nanoTime();
        long totalPaths = 0;

        for (int start = 0; start < n; start++) {

            if (shouldStop(startTime)) {
                timedOut = true;
                return totalPaths;
            }

            boolean[] visited = new boolean[n];

            totalPaths += dfsCountAll(g, start, visited, 1, startTime);

            if (timedOut) {
                return totalPaths;
            }
        }

        return totalPaths;
    }

    private static long dfsCountAll(
            Graph g,
            int current,
            boolean[] visited,
            int count,
            long startTime) {

        if (shouldStop(startTime)) {
            timedOut = true;
            return 0;
        }

        visited[current] = true;

        if (count == g.size()) {
            visited[current] = false;
            return 1;
        }

        long paths = 0;

        for (int neighbor : g.getNeighbors(current)) {

            if (shouldStop(startTime)) {
                timedOut = true;
                visited[current] = false;
                return paths;
            }

            if (!visited[neighbor]) {
                paths += dfsCountAll(g, neighbor, visited, count + 1, startTime);

                if (timedOut) {
                    visited[current] = false;
                    return paths;
                }
            }
        }

        visited[current] = false;
        return paths;
    }

    private static boolean shouldStop(long startTime) {

        if (Thread.currentThread().isInterrupted()) {
            return true;
        }

        return System.nanoTime() - startTime > TIME_LIMIT_NS;
    }
}
