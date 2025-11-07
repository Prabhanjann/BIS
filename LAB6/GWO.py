import numpy as np
# Objective Function: Total distance to nearest sensor
def sensor_cost(sensors, points):
    sensors = np.sort(sensors)  # ensure order
    total = 0
    for p in points:
        total += np.min(np.abs(sensors - p))  # distance to nearest sensor
    return total
def GWO_sensor(obj_func, n_wolves, max_iter, n_sensors, field_length):
    dim = n_sensors
    lb, ub = 0, field_length
    wolves = np.random.uniform(lb, ub, (n_wolves, dim))
    alpha = np.zeros(dim)
    beta = np.zeros(dim)
    delta = np.zeros(dim)
    alpha_score = beta_score = delta_score = float("inf")
    points = np.linspace(0, field_length, 21)  # points to cover
    
    for t in range(max_iter):
        a = 2 - 2*(t/max_iter)  # linearly decreasing from 2 → 0
        
        for i in range(n_wolves):
            wolves[i] = np.clip(wolves[i], lb, ub)
            fitness = obj_func(wolves[i], points)
            
            # Update hierarchy
            if fitness < alpha_score:
                alpha_score, alpha = fitness, wolves[i].copy()
            elif fitness < beta_score:
                beta_score, beta = fitness, wolves[i].copy()
            elif fitness < delta_score:
                delta_score, delta = fitness, wolves[i].copy()
        
        # Update positions
        for i in range(n_wolves):
            for j in range(dim):
                Xs = []
                for leader in [alpha, beta, delta]:
                    r1, r2 = np.random.rand(), np.random.rand()
                    A = 2*a*r1 - a
                    C = 2*r2
                    D = abs(C*leader[j] - wolves[i,j])
                    Xs.append(wolves[i,j] - A*D)
                wolves[i,j] = np.mean(Xs)
    
    return alpha_score, np.sort(alpha)
# User Input
if __name__ == "__main__":
    print("Sensor Placement Optimization using GWO")
    field_length = float(input("Enter the field length: "))
    n_sensors = int(input("Enter number of sensors to place: "))
    n_wolves = int(input("Enter number of wolves: "))
    max_iter = int(input("Enter number of iterations: "))
    
    best_cost, best_sensors = GWO_sensor(sensor_cost, n_wolves, max_iter, n_sensors, field_length)
    
    print("\nBest Total Distance:", best_cost)
    print("Sensor Positions:", best_sensors)

