import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {

    private static Random random = new Random();

    public static List<Integer> initializePopulation(int size, int minX, int maxX) {
        List<Integer> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int individual = random.nextInt(maxX - minX + 1) + minX;
            population.add(individual);
        }
        return population;
    }

    public static int fitness(int x) {
        return x * x;
    }

    public static List<Integer> calculateFitness(List<Integer> population) {
        List<Integer> fitnessScores = new ArrayList<>();
        for (int individual : population) {
            fitnessScores.add(fitness(individual));
        }
        return fitnessScores;
    }

    public static List<Integer> selectMatingPool(List<Integer> population, List<Integer> fitnessScores, int numParents) {
        int totalFitness = 0;
        for (int score : fitnessScores) {
            totalFitness += score;
        }

        List<Integer> parents = new ArrayList<>();
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

    public static List<Integer> crossover(List<Integer> parents, int offspringSize) {
        List<Integer> offspring = new ArrayList<>();

        for (int i = 0; i < offspringSize; i++) {
            int parent1 = parents.get(random.nextInt(parents.size()));
            int parent2 = parents.get(random.nextInt(parents.size()));

            int crossoverPoint = random.nextInt(4) + 1;  // 1 to 4 inclusive
            int mask = (1 << crossoverPoint) - 1;

            int child = (parent1 & ~mask) | (parent2 & mask);
            offspring.add(child);
        }
        return offspring;
    }

    public static List<Integer> mutate(List<Integer> population, double mutationRate) {
        List<Integer> mutatedPop = new ArrayList<>();

        for (int individual : population) {
            if (random.nextDouble() < mutationRate) {
                int bitToFlip = 1 << random.nextInt(5); // bits 0 to 4
                individual ^= bitToFlip;
            }
            mutatedPop.add(individual);
        }

        return mutatedPop;
    }

    public static boolean isConverged(List<Integer> population, List<Integer> prevPopulation) {
        List<Integer> sortedPop = new ArrayList<>(population);
        List<Integer> sortedPrev = new ArrayList<>(prevPopulation);
        Collections.sort(sortedPop);
        Collections.sort(sortedPrev);
        return sortedPop.equals(sortedPrev);
    }

    public static int[] geneticAlgorithm(int popSize, int maxGenerations, int numParents, double mutationRate, int patience) {
        List<Integer> population = initializePopulation(popSize, 0, 31);
        int best = Collections.max(population, (a, b) -> Integer.compare(fitness(a), fitness(b)));
        int bestFitness = fitness(best);
        int unchangedGenerations = 0;

        for (int gen = 0; gen < maxGenerations; gen++) {
            List<Integer> fitnessScores = calculateFitness(population);
            List<Integer> parents = selectMatingPool(population, fitnessScores, numParents);
            List<Integer> offspring = crossover(parents, popSize - numParents);
            offspring = mutate(offspring, mutationRate);

            List<Integer> prevPopulation = population;
            population = new ArrayList<>();
            population.addAll(parents);
            population.addAll(offspring);

            int currentBest = Collections.max(population, (a, b) -> Integer.compare(fitness(a), fitness(b)));
            int currentBestFitness = fitness(currentBest);

            if (currentBestFitness > bestFitness) {
                best = currentBest;
                bestFitness = currentBestFitness;
                unchangedGenerations = 0;
            } else if (isConverged(population, prevPopulation)) {
                unchangedGenerations++;
            } else {
                unchangedGenerations = 0;
            }

            System.out.printf("Generation %d: Best x=%d, Fitness=%d%n", gen + 1, best, bestFitness);

            if (unchangedGenerations >= patience) {
                System.out.printf("Converged after %d generations.%n", gen + 1);
                break;
            }
        }

        return new int[]{best, bestFitness};
    }

    public static void main(String[] args) {
        int[] result = geneticAlgorithm(10, 100, 4, 0.1, 10);
        System.out.printf("Optimal solution found: x=%d, Fitness=%d%n", result[0], result[1]);
    }
}