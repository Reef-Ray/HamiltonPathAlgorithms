import java.util.*;

public class HamiltonianBacktracking {
    public static boolean hasHamiltonianPath(Graph g) {
        return findHamiltonianPath(g) != null;
    }

    // Returns a Hamiltonian path as a list of vertex indices, or null if none exists
    public static List<Integer> findHamiltonianPath(Graph g) {
        int n = g.size();
        for (int start = 0; start < n; start++) {
            boolean[] visited = new boolean[n];
            int[] path = new int[n];
            if (dfs(g, start, visited, 1, path)) {
                List<Integer> result = new ArrayList<>();
                for (int i = 0; i < n; i++) result.add(path[i]);
                return result;
            }
        }
        return null;
    }

    private static boolean dfs(Graph g, int current, boolean[] visited, int count, int[] path) {
        visited[current] = true;
        path[count - 1] = current;

        if (count == g.size()) {
            return true;
        }

        for (int neighbor : g.getNeighbors(current)) {
            if (!visited[neighbor]) {
                if (dfs(g, neighbor, visited, count + 1, path)) {
                    return true;
                }
            }
        }

        visited[current] = false; // backtrack
        return false;
    }
}