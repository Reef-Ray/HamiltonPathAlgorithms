package HamiltonianPath;

import java.util.Random;

public class GraphGenerator {

    public static Graph generateRandomGraph(int V) {
        return generateRandomGraph(V, 0.5, System.nanoTime());
    }

    public static Graph generateRandomGraph(int V, double edgeProbability) {
        return generateRandomGraph(V, edgeProbability, System.nanoTime());
    }

    public static Graph generateRandomGraph(int V, double edgeProbability, long seed) {

        Graph g = new Graph(V);
        Random rand = new Random(seed);

        for (int i = 0; i < V; i++) {
            for (int j = i + 1; j < V; j++) {
                if (rand.nextDouble() < edgeProbability) {
                    g.addEdge(i, j);
                }
            }
        }

        return g;
    }

    public static Graph generateCompleteGraph(int V) {

        Graph g = new Graph(V);

        for (int i = 0; i < V; i++) {
            for (int j = i + 1; j < V; j++) {
                g.addEdge(i, j);
            }
        }

        return g;
    }
}
