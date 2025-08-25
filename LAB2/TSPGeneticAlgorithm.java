import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TSPGeneticAlgorithm {

    private static Random random = new Random();

    // Coordinates of cities (x,y)
    static double[][] cities = {
        {0, 0},
        {1, 5},
        {5, 2},
        {6, 6},
        {8, 3},
        {7, 9}
    };

    // Calculate Euclidean distance between two cities
    public static double distance(int city1, int city2) {
        double dx = cities[city1][0] - cities[city2][0];
        double dy = cities[city1][1] - cities[city2][1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Calculate total tour distance for a given individual (order of cities)
    public static double tourDistance(List<Integer> individual) {
        double dist = 0;
        for (int i = 0; i < individual.size() - 1; i++) {
            dist += distance(individual.get(i), individual.get(i + 1));
        }
        // Return to start city to complete the tour
        dist += distance(individual.get(individual.size() - 1), individual.get(0));
        return dist;
    }

    // Fitness = inverse of tour distance (shorter tours are fitter)
    public static double fitness(List<Integer> individual) {
        return 1.0 / tourDistance(individual);
    }

    // Initialize population with random permutations of city indices
    public static List<List<Integer>> initializePopulation(int size, int numCities) {
        List<List<Integer>> population = new ArrayList<>();
        List<Integer> base = new ArrayList<>();
        for (int i = 0; i < numCities; i++) base.add(i);

        for (int i = 0; i < size; i++) {
            List<Integer> individual = new ArrayList<>(base);
            Collections.shuffle(individual, random);
            population.add(individual);
        }
        return population;
    }

    // Calculate fitness for entire population
    public static List<Double> calculateFitness(List<List<Integer>> population) {
        List<Double> fitnessScores = new ArrayList<>();
        for (List<Integer> individual : population) {
            fitnessScores.add(fitness(individual));
        }
        return fitnessScores;
    }

    // Roulette wheel selection for parents
    public static List<List<Integer>> selectMatingPool(List<List<Integer>> population, List<Double> fitnessScores, int numParents) {
        double totalFitness = 0;
        for (double f : fitnessScores) totalFitness += f;

        List<List<Integer>> parents = new ArrayList<>();
        for (int i = 0; i < numParents; i++) {
            double r = random.nextDouble() * totalFitness;
            double cumulative = 0;
            for (int j = 0; j < population.size(); j++) {
                cumulative += fitnessScores.get(j);
                if (r <= cumulative) {
                    parents.add(population.get(j));
                    break;
                }
            }
        }
        return parents;
    }

    // Ordered Crossover (OX) for permutations
    public static List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        int size = parent1.size();
        List<Integer> child = new ArrayList<>(Collections.nCopies(size, -1));

        // Random crossover segment
        int start = random.nextInt(size);
        int end = random.nextInt(size);

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        // Copy a slice from parent1
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }

        // Fill remaining positions with parent2 in order
        int currentPos = (end + 1) % size;
        for (int i = 0; i < size; i++) {
            int city = parent2.get((end + 1 + i) % size);
            if (!child.contains(city)) {
                child.set(currentPos, city);
                currentPos = (currentPos + 1) % size;
            }
        }

        return child;
    }

    // Generate offspring using crossover
    public static List<List<Integer>> crossover(List<List<Integer>> parents, int offspringSize) {
        List<List<Integer>> offspring = new ArrayList<>();
        for (int i = 0; i < offspringSize; i++) {
            List<Integer> parent1 = parents.get(random.nextInt(parents.size()));
            List<Integer> parent2 = parents.get(random.nextInt(parents.size()));
            offspring.add(crossover(parent1, parent2));
        }
        return offspring;
    }

    // Mutation by swapping two cities
    public static List<List<Integer>> mutate(List<List<Integer>> population, double mutationRate) {
        List<List<Integer>> mutatedPop = new ArrayList<>();
        for (List<Integer> individual : population) {
            List<Integer> mutated = new ArrayList<>(individual);
            if (random.nextDouble() < mutationRate) {
                int i = random.nextInt(mutated.size());
                int j = random.nextInt(mutated.size());
                Collections.swap(mutated, i, j);
            }
            mutatedPop.add(mutated);
        }
        return mutatedPop;
    }

    // Check if no improvement (by comparing best fitnesses)
    public static boolean isConverged(double currentBest, double prevBest, double threshold) {
        return Math.abs(currentBest - prevBest) < threshold;
    }

    public static List<Integer> geneticAlgorithm(int popSize, int maxGenerations, int numParents, double mutationRate, int patience) {
        int numCities = cities.length;
        List<List<Integer>> population = initializePopulation(popSize, numCities);
        List<Double> fitnessScores = calculateFitness(population);

        int unchangedGenerations = 0;
        double bestFitness = Collections.max(fitnessScores);
        List<Integer> bestIndividual = population.get(fitnessScores.indexOf(bestFitness));

        for (int gen = 0; gen < maxGenerations; gen++) {
            List<List<Integer>> parents = selectMatingPool(population, fitnessScores, numParents);
            List<List<Integer>> offspring = crossover(parents, popSize - numParents);
            offspring = mutate(offspring, mutationRate);

            // New population = parents + offspring
            population.clear();
            population.addAll(parents);
            population.addAll(offspring);

            fitnessScores = calculateFitness(population);
            double currentBestFitness = Collections.max(fitnessScores);
            List<Integer> currentBestIndividual = population.get(fitnessScores.indexOf(currentBestFitness));

            if (currentBestFitness > bestFitness) {
                bestFitness = currentBestFitness;
                bestIndividual = currentBestIndividual;
                unchangedGenerations = 0;
            } else {
                unchangedGenerations++;
            }

            System.out.printf("Generation %d: Best distance=%.4f%n", gen + 1, 1 / bestFitness);

            if (unchangedGenerations >= patience) {
                System.out.printf("Converged after %d generations.%n", gen + 1);
                break;
            }
        }
        return bestIndividual;
    }

    public static void printTour(List<Integer> tour) {
        System.out.println("Best tour found:");
        for (int city : tour) {
            System.out.print(city + " -> ");
        }
        System.out.println(tour.get(0)); // Return to start city

        System.out.printf("Total distance: %.4f%n", tourDistance(tour));
    }

    public static void main(String[] args) {
        List<Integer> bestTour = geneticAlgorithm(20, 500, 10, 0.2, 50);
        printTour(bestTour);
    }
}