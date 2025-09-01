import java.util.Random;
import java.util.Arrays;

public class ParticleSwarmOptimization {

    static Random random = new Random();

    // Fitness function: sum of squares of the position vector
    static double fitnessFunction(double[] position) {
        double sum = 0.0;
        for (double x : position) {
            sum += x * x;
        }
        return sum;
    }

    static double[] copyArray(double[] source) {
        return Arrays.copyOf(source, source.length);
    }

    static class Particle {
        double[] position;
        double[] velocity;
        double[] pbestPosition;
        double pbestFitness;

        Particle(int dimensions) {
            position = new double[dimensions];
            velocity = new double[dimensions];
            pbestPosition = new double[dimensions];
            pbestFitness = Double.POSITIVE_INFINITY;
        }
    }

    public static void particleSwarmOptimization(int dimensions, int numParticles, int maxIterations) {
        double w = 0.5;
        double c1 = 0.8;
        double c2 = 0.9;

        Particle[] swarm = new Particle[numParticles];

        // Initialize swarm
        for (int i = 0; i < numParticles; i++) {
            Particle p = new Particle(dimensions);
            for (int d = 0; d < dimensions; d++) {
                p.position[d] = -10 + 20 * random.nextDouble();  // Uniform(-10, 10)
                p.velocity[d] = -1 + 2 * random.nextDouble();   // Uniform(-1, 1)
                p.pbestPosition[d] = p.position[d];
            }
            p.pbestFitness = fitnessFunction(p.position);
            swarm[i] = p;
        }

        double[] gbestPosition = new double[dimensions];
        double gbestFitness = Double.POSITIVE_INFINITY;

        // Initialize global best
        for (Particle p : swarm) {
            if (p.pbestFitness < gbestFitness) {
                gbestFitness = p.pbestFitness;
                gbestPosition = copyArray(p.pbestPosition);
            }
        }

        for (int iter = 0; iter < maxIterations; iter++) {
            // Update velocity and position
            for (Particle p : swarm) {
                for (int d = 0; d < dimensions; d++) {
                    double rand1 = random.nextDouble();
                    double rand2 = random.nextDouble();

                    double inertiaTerm = w * p.velocity[d];
                    double personalTerm = c1 * rand1 * (p.pbestPosition[d] - p.position[d]);
                    double socialTerm = c2 * rand2 * (gbestPosition[d] - p.position[d]);

                    p.velocity[d] = inertiaTerm + personalTerm + socialTerm;
                    p.position[d] += p.velocity[d];
                }
            }

            // Update personal best and global best based on new positions
            for (Particle p : swarm) {
                double fitness = fitnessFunction(p.position);
                if (fitness < p.pbestFitness) {
                    p.pbestFitness = fitness;
                    p.pbestPosition = copyArray(p.position);
                }
                if (fitness < gbestFitness) {
                    gbestFitness = fitness;
                    gbestPosition = copyArray(p.position);
                }
            }

            // Optional: print progress every some iterations
            if (iter % 10000 == 0) {
                System.out.printf("Iteration %d, Best fitness: %.6f%n", iter, gbestFitness);
            }
        }

        // Output results
        System.out.println("SOLUTION FOUND:");
        System.out.println("  Position: " + Arrays.toString(gbestPosition));
        System.out.println("  Fitness: " + gbestFitness);
    }

    public static void main(String[] args) {
        particleSwarmOptimization(2, 200, 50000);
    }
}
