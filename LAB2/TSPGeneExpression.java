import java.util.*;

public class TSPGeneExpression {

    static Random random = new Random();

    // Define cities with (x, y) coordinates
    static double[][] cities = {
        {0, 0}, {3, 14}, {12, 5}, {7, 18}, {20, 3},
        {15, 25}, {25, 10}, {30, 20}, {22, 30}, {35, 5}
    };

    // Calculate Euclidean distance
    public static double distance(int c1, int c2) {
        double dx = cities[c1][0] - cities[c2][0];
        double dy = cities[c1][1] - cities[c2][1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Calculate total tour distance
    public static double tourDistance(List<Integer> route) {
        double dist = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            dist += distance(route.get(i), route.get(i + 1));
        }
        dist += distance(route.get(route.size() - 1), route.get(0));
        return dist;
    }

    // Fitness = inverse of distance
    public static double fitness(List<Integer> route) {
        return 1.0 / tourDistance(route);
    }

    // Initialize population with random gene sequences
    public static List<List<Integer>> initializePopulation(int popSize, int numCities) {
        List<List<Integer>> population = new ArrayList<>();
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < numCities; i++) base.add(i);

        for (int i = 0; i < popSize; i++) {
            List<Integer> individual = new ArrayList<>(base);
            Collections.shuffle(individual, random);
            population.add(individual);
        }
        return population;
    }

    // Evaluate fitness for population
    public static List<Double> evaluateFitness(List<List<Integer>> population) {
        List<Double> scores = new ArrayList<>();
        for (List<Integer> route : population) {
            scores.add(fitness(route));
        }
        return scores;
    }

    // Select top individuals
    public static List<List<Integer>> selectParents(List<List<Integer>> population, List<Double> scores, int numParents) {
        List<List<Integer>> parents = new ArrayList<>();
        for (int i = 0; i < numParents; i++) {
            int bestIndex = scores.indexOf(Collections.max(scores));
            parents.add(population.get(bestIndex));
            scores.set(bestIndex, -1.0); // prevent reselection
        }
        return parents;
    }

    // Crossover: ordered crossover
    public static List<Integer> crossover(List<Integer> p1, List<Integer> p2) {
        int size = p1.size();
        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));
        int start = random.nextInt(size);
        int end = random.nextInt(size);
        if (start > end) { int temp = start; start = end; end = temp; }

        for (int i = start; i <= end; i++) child.set(i, p1.get(i));

        int pos = (end + 1) % size;
        for (int i = 0; i < size; i++) {
            int city = p2.get((end + 1 + i) % size);
            if (!child.contains(city)) {
                child.set(pos, city);
                pos = (pos + 1) % size;
            }
        }
        return child;
    }

    // Mutate: swap two cities
    public static List<Integer> mutate(List<Integer> route, double rate) {
        List<Integer> mutated = new ArrayList<>(route);
        if (random.nextDouble() < rate) {
            int i = random.nextInt(mutated.size());
            int j = random.nextInt(mutated.size());
            Collections.swap(mutated, i, j);
        }
        return mutated;
    }

    // Main GEA loop
    public static List<Integer> geneExpressionAlgorithm(int popSize, int numGenerations, int numParents, double mutationRate) {
        int numCities = cities.length;
        List<List<Integer>> population = initializePopulation(popSize, numCities);
        List<Integer> bestRoute = null;
        double bestFitness = 0;

        for (int gen = 0; gen < numGenerations; gen++) {
            List<Double> scores = evaluateFitness(population);
            List<List<Integer>> parents = selectParents(population, new ArrayList<>(scores), numParents);

            List<List<Integer>> offspring = new ArrayList<>();
            while (offspring.size() < popSize - numParents) {
                List<Integer> p1 = parents.get(random.nextInt(parents.size()));
                List<Integer> p2 = parents.get(random.nextInt(parents.size()));
                List<Integer> child = crossover(p1, p2);
                offspring.add(mutate(child, mutationRate));
            }

            population.clear();
            population.addAll(parents);
            population.addAll(offspring);

            scores = evaluateFitness(population);
            double currentBest = Collections.max(scores);
            if (currentBest > bestFitness) {
                bestFitness = currentBest;
                bestRoute = population.get(scores.indexOf(currentBest));
            }

            System.out.printf("Generation %d: Best Distance = %.4f%n", gen + 1, 1 / bestFitness);
        }

        return bestRoute;
    }

    public static void printRoute(List<Integer> route) {
        System.out.println("Best Route:");
        for (int city : route) System.out.print(city + " -> ");
        System.out.println(route.get(0));
        System.out.printf("Total Distance: %.4f%n", tourDistance(route));
    }

    public static void main(String[] args) {
        List<Integer> best = geneExpressionAlgorithm(50, 100, 10, 0.2);
        printRoute(best);
    }
}