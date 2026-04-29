import java.util.*;

public class HamiltonianGreedy {
    public static boolean hasHamiltonianPath(Graph g) {
        return findHamiltonianPath(g) != null;
    }

    // Greedy attempt that returns the path if found, otherwise null
    public static List<Integer> findHamiltonianPath(Graph g) {
        int n = g.size();
        for (int start = 0; start < n; start++) {
            boolean[] visited = new boolean[n];
            List<Integer> path = new ArrayList<>();
            int current = start;
            while (true) {
                visited[current] = true;
                path.add(current);

                if (path.size() == n) {
                    return path;
                }

                int next = -1;
                int minDegree = Integer.MAX_VALUE;

                for (int neighbor : g.getNeighbors(current)) {
                    if (!visited[neighbor]) {
                        int degree = g.getNeighbors(neighbor).size();
                        if (degree < minDegree) {
                            minDegree = degree;
                            next = neighbor;
                        }
                    }
                }

                if (next == -1) {
                    break; // dead end for this start
                }

                current = next;
            }
        }
        return null;
    }
}