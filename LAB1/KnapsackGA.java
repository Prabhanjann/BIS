import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class KnapsackGA {

    private static Random random = new Random();

    // Define the Knapsack problem: item weights, values, and maximum capacity
    static int[] weights = {2, 3, 4, 5, 9}; // Weights of items
    static int[] values = {3, 4, 5, 6, 10}; // Values of items
    static int maxWeight = 10; // Maximum weight capacity of the knapsack

    // Initialize population with binary representation of inclusion/exclusion of items
    public static List<List<Integer>> initializePopulation(int size, int numItems) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<Integer> individual = new ArrayList<>();
            for (int j = 0; j < numItems; j++) {
                individual.add(random.nextInt(2)); // 0 or 1 (exclude or include item)
            }
            population.add(individual);
        }
        return population;
    }

    // Fitness function: sum of values of selected items, penalized if weight exceeds maxWeight
    public static int fitness(List<Integer> individual) {
        int totalValue = 0;
        int totalWeight = 0;
        for (int i = 0; i < individual.size(); i++) {
            if (individual.get(i) == 1) {
                totalValue += values[i];
                totalWeight += weights[i];
            }
        }
        // Penalize if total weight exceeds the capacity
        if (totalWeight > maxWeight) {
            return 0;
        }
        return totalValue;
    }

    // Calculate the fitness for each individual in the population
    public static List<Integer> calculateFitness(List<List<Integer>> population) {
        List<Integer> fitnessScores = new ArrayList<>();
        for (List<Integer> individual : population) {
            fitnessScores.add(fitness(individual));
        }
        return fitnessScores;
    }

    // Select mating pool based on fitness using roulette wheel selection
    public static List<List<Integer>> selectMatingPool(List<List<Integer>> population, List<Integer> fitnessScores, int numParents) {
        int totalFitness = 0;
        for (int score : fitnessScores) {
            totalFitness += score;
        }

        List<List<Integer>> parents = new ArrayList<>();
        if (totalFitness == 0) {
            // Select random individuals if all fitness scores are zero
            Collections.shuffle(population, random);
            parents.addAll(population.subList(0, numParents));
        } else {
            // Calculate selection probabilities
            List<Double> selectionProbs = new ArrayList<>();
            for (int score : fitnessScores) {
                selectionProbs.add(score / (double) totalFitness);
            }

            // Select parents based on weighted probabilities
            for (int i = 0; i < numParents; i++) {
                double r = random.nextDouble();
                double cumulative = 0.0;
                for (int j = 0; j < population.size(); j++) {
                    cumulative += selectionProbs.get(j);
                    if (r <= cumulative) {
                        parents.add(population.get(j));
                        break;
                    }
                }
            }
        }

        return parents;
    }

    // Crossover operation (single-point crossover)
    public static List<List<Integer>> crossover(List<List<Integer>> parents, int offspringSize) {
        List<List<Integer>> offspring = new ArrayList<>();

        for (int i = 0; i < offspringSize; i++) {
            List<Integer> parent1 = parents.get(random.nextInt(parents.size()));
            List<Integer> parent2 = parents.get(random.nextInt(parents.size()));

            int crossoverPoint = random.nextInt(parent1.size());  // Crossover point
            List<Integer> child = new ArrayList<>();

            // Create child by combining parts from both parents
            for (int j = 0; j < parent1.size(); j++) {
                if (j < crossoverPoint) {
                    child.add(parent1.get(j));
                } else {
                    child.add(parent2.get(j));
                }
            }
            offspring.add(child);
        }
        return offspring;
    }

    // Mutation operation (randomly flip bits in the binary string)
    public static List<List<Integer>> mutate(List<List<Integer>> population, double mutationRate) {
        List<List<Integer>> mutatedPop = new ArrayList<>();

        for (List<Integer> individual : population) {
            List<Integer> mutatedIndividual = new ArrayList<>(individual);
            for (int i = 0; i < individual.size(); i++) {
                if (random.nextDouble() < mutationRate) {
                    mutatedIndividual.set(i, 1 - mutatedIndividual.get(i)); // Flip bit
                }
            }
            mutatedPop.add(mutatedIndividual);
        }

        return mutatedPop;
    }

    // Check if population has converged (no change in best solution for multiple generations)
    public static boolean isConverged(List<List<Integer>> population, List<List<Integer>> prevPopulation) {
        return population.equals(prevPopulation);
    }

    public static List<Integer> geneticAlgorithm(int popSize, int maxGenerations, int numParents, double mutationRate, int patience) {
        List<List<Integer>> population = initializePopulation(popSize, weights.length);
        List<Integer> fitnessScores = calculateFitness(population);
        List<Integer> bestIndividual = population.get(fitnessScores.indexOf(Collections.max(fitnessScores)));
        int bestFitness = fitness(bestIndividual);
        int unchangedGenerations = 0;

        for (int gen = 0; gen < maxGenerations; gen++) {
            List<List<Integer>> parents = selectMatingPool(population, fitnessScores, numParents);
            List<List<Integer>> offspring = crossover(parents, popSize - numParents);
            offspring = mutate(offspring, mutationRate);

            List<List<Integer>> prevPopulation = new ArrayList<>(population);
            population.clear();
            population.addAll(parents);
            population.addAll(offspring);

            fitnessScores = calculateFitness(population);
            List<Integer> currentBestIndividual = population.get(fitnessScores.indexOf(Collections.max(fitnessScores)));
            int currentBestFitness = fitness(currentBestIndividual);

            if (currentBestFitness > bestFitness) {
                bestIndividual = currentBestIndividual;
                bestFitness = currentBestFitness;
                unchangedGenerations = 0;
            } else if (isConverged(population, prevPopulation)) {
                unchangedGenerations++;
            } else {
                unchangedGenerations = 0;
            }

            System.out.printf("Generation %d: Best fitness=%d, Items=%s%n", gen + 1, bestFitness, bestIndividual);

            if (unchangedGenerations >= patience) {
                System.out.printf("Converged after %d generations.%n", gen + 1);
                break;
            }
        }

        return bestIndividual;
    }

    public static void main(String[] args) {
        List<Integer> result = geneticAlgorithm(10, 100, 4, 0.1, 10);
        System.out.printf("Optimal solution found: Items selected: %s, Fitness=%d%n", result, fitness(result));
    }
}
