import java.util.Random;

public class GraphGenerator {
    public static Graph generateRandomGraph(int V) {
        Graph g = new Graph(V);
        Random rand = new Random();

        for (int i = 0; i < V; i++) {
            for (int j = i + 1; j < V; j++) {
                if (rand.nextDouble() < 0.5) {
                    g.addEdge(i, j);
                }
            }
        }

        return g;
    }
}