import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of vertices (V): ");
        int V = scanner.nextInt();

        while (V < 0) {
            System.out.print("Enter a valid integer: ");
            V = scanner.nextInt();
        }

        System.out.print("Enter edge probability (p between 0 and 1): ");
        double p = scanner.nextDouble();

        while (p < 0 || p > 1) {
            System.out.print("Enter a valid probability (0 to 1): ");
            p = scanner.nextDouble();
        }

        Graph g = GraphGenerator.generateRandomGraph(V, p);

        long start1 = System.nanoTime();
        boolean exact = HamiltonianBacktracking.hasHamiltonianPath(g);
        long end1 = System.nanoTime();

        System.out.println("Backtracking: " + exact +
                " Time: " + (end1 - start1));

        long start2 = System.nanoTime();
        boolean greedy = HamiltonianGreedy.hasHamiltonianPath(g);
        long end2 = System.nanoTime();

        System.out.println("Greedy: " + greedy +
                " Time: " + (end2 - start2));

        scanner.close();
    }
}