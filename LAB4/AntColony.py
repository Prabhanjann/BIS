import math
import random

class City:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def distance_to(self, city):
        dx = self.x - city.x
        dy = self.y - city.y
        return math.sqrt(dx * dx + dy * dy)

class Ant:
    def __init__(self, num_cities):
        self.tour = []
        self.visited = [False] * num_cities
        self.tour_length = 0.0

    def visit_city(self, city, distance, last_city):
        self.tour.append(city)
        self.visited[city] = True
        if last_city != -1:
            self.tour_length += distance[last_city][city]

    def complete_tour(self, distance):
        self.tour_length += distance[self.tour[-1]][self.tour[0]]

    def reset(self, num_cities):
        self.tour.clear()
        self.visited = [False] * num_cities
        self.tour_length = 0.0

class AntColonyTSP:
    def __init__(self, cities):
        self.num_cities = len(cities)
        self.num_ants = 30
        self.max_iterations = 100
        self.alpha = 1.0
        self.beta = 5.0
        self.evaporation = 0.5
        self.Q = 100

        self.cities = cities
        self.distance = [[0.0] * self.num_cities for _ in range(self.num_cities)]
        self.pheromone = [[1.0] * self.num_cities for _ in range(self.num_cities)]
        self.best_tour = []
        self.best_length = float('inf')

        self.init_distance_matrix()

    def init_distance_matrix(self):
        for i in range(self.num_cities):
            for j in range(self.num_cities):
                self.distance[i][j] = self.cities[i].distance_to(self.cities[j])

    def run(self):
        ants = [Ant(self.num_cities) for _ in range(self.num_ants)]

        for _ in range(self.max_iterations):
            for ant in ants:
                ant.reset(self.num_cities)
                start_city = random.randint(0, self.num_cities - 1)
                ant.visit_city(start_city, self.distance, -1)

                for _ in range(1, self.num_cities):
                    current_city = ant.tour[-1]
                    next_city = self.select_next_city(ant, current_city)
                    ant.visit_city(next_city, self.distance, current_city)

                ant.complete_tour(self.distance)

                if ant.tour_length < self.best_length:
                    self.best_length = ant.tour_length
                    self.best_tour = list(ant.tour)

            self.update_pheromones(ants)

        self.print_best_tour()

    def select_next_city(self, ant, current_city):
        probabilities = [0.0] * self.num_cities
        total = 0.0

        for i in range(self.num_cities):
            if not ant.visited[i]:
                tau = self.pheromone[current_city][i] ** self.alpha
                eta = (1.0 / self.distance[current_city][i]) ** self.beta
                probabilities[i] = tau * eta
                total += probabilities[i]

        r = random.random() * total
        cumulative = 0.0

        for i in range(self.num_cities):
            if not ant.visited[i]:
                cumulative += probabilities[i]
                if cumulative >= r:
                    return i

        # Fallback
        for i in range(self.num_cities):
            if not ant.visited[i]:
                return i

        return 0

    def update_pheromones(self, ants):
        # Evaporate
        for i in range(self.num_cities):
            for j in range(self.num_cities):
                self.pheromone[i][j] *= (1 - self.evaporation)

        # Deposit
        for ant in ants:
            contribution = self.Q / ant.tour_length
            for i in range(len(ant.tour) - 1):
                from_city = ant.tour[i]
                to_city = ant.tour[i + 1]
                self.pheromone[from_city][to_city] += contribution
                self.pheromone[to_city][from_city] += contribution

            # Complete the cycle
            last = ant.tour[-1]
            first = ant.tour[0]
            self.pheromone[last][first] += contribution
            self.pheromone[first][last] += contribution

    def print_best_tour(self):
        print("Best tour length:", self.best_length)
        print("Best tour:", ' '.join(map(str, self.best_tour + [self.best_tour[0]])))

# Example usage
if __name__ == "__main__":
    cities = [
        City(60, 200),
        City(180, 200),
        City(80, 180),
        City(140, 180),
        City(20, 160),
        City(100, 160),
        City(200, 160),
        City(140, 140),
        City(40, 120),
        City(100, 120)
    ]

    aco = AntColonyTSP(cities)
    aco.run()
