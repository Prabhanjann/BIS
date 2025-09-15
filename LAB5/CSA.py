import numpy as np
import random

# Generate a random tour
def generate_random_tour(n):
    return random.sample(range(n), n)

# Calculate the total distance of a tour using the distance matrix
def evaluate_tour(tour, D):
    distance = 0
    for i in range(len(tour) - 1):
        distance += D[tour[i]][tour[i + 1]]
    distance += D[tour[-1]][tour[0]]  # Return to the start point
    return distance

# Apply Levy flight (mutation) - Simple swap mutation here
def levy_flight(tour):
    new_tour = tour[:]
    i, j = random.sample(range(len(tour)), 2)
    new_tour[i], new_tour[j] = new_tour[j], new_tour[i]  # Swap two cities
    return new_tour

# Main Cuckoo Search Algorithm for TSP
def csa_tsp(D, N, Pa, MaxT):
    n = len(D)  # Number of cities
    # Step 1: Initialization
    nests = [generate_random_tour(n) for _ in range(N)]
    fitness = [evaluate_tour(nest, D) for nest in nests]
    
    # Find the best nest
    best_nest_idx = np.argmin(fitness)
    best_nest = nests[best_nest_idx]
    best_fitness = fitness[best_nest_idx]
    
    # Step 2: Main loop
    for t in range(MaxT):
        # Generate new solutions using Levy flight
        for i in range(N):
            new_nest = levy_flight(nests[i])
            new_fitness = evaluate_tour(new_nest, D)
            
            # Fitness Evaluation & Replacement
            j = random.randint(0, N - 1)
            if new_fitness < fitness[j]:
                nests[j] = new_nest
                fitness[j] = new_fitness
        
        # Abandon worst nests based on Pa
        num_worst = int(N * Pa)
        worst_nests_idx = np.argsort(fitness)[-num_worst:]
        
        for idx in worst_nests_idx:
            nests[idx] = generate_random_tour(n)
            fitness[idx] = evaluate_tour(nests[idx], D)
        
        # Update BestNest
        best_nest_idx = np.argmin(fitness)
        best_nest = nests[best_nest_idx]
        best_fitness = fitness[best_nest_idx]
        
        print(f"Iteration {t + 1}: Best fitness = {best_fitness}")
    
    return best_nest, best_fitness

# Example distance matrix for 5 cities
D = np.array([
    [0, 10, 15, 20, 25],
    [10, 0, 35, 25, 30],
    [15, 35, 0, 30, 5],
    [20, 25, 30, 0, 15],
    [25, 30, 5, 15, 0]
])

# Parameters
N = 4  # Number of nests
Pa = 0.25  # Discovery probability
MaxT = 3  # Maximum number of iterations

# Run the algorithm
best_tour, best_distance = csa_tsp(D, N, Pa, MaxT)
print(f"Best Tour: {best_tour}")
print(f"Best Distance: {best_distance}")
