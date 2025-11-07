# --- 1. Get User Input for Simulation Parameters ---
def get_user_input():
    """Prompts the user for grid size, time steps, and max load."""
    print("--- Pure Python CA Load Balancer Setup ---")
    
    while True:
        try:
            grid_size = int(input("Enter the grid size (N for an NxN grid, e.g., 5): "))
            if grid_size < 3:
                print("Grid size must be 3 or greater for the neighborhood rule to work.")
                continue
            break
        except ValueError:
            print("Invalid input. Please enter an integer.")

    while True:
        try:
            time_steps = int(input("Enter the number of time steps (e.g., 4): "))
            if time_steps < 1:
                print("Time steps must be 1 or greater.")
                continue
            break
        except ValueError:
            print("Invalid input. Please enter an integer.")

    while True:
        try:
            max_load = int(input("Enter the maximum server load (MAX_LOAD, e.g., 10): "))
            if max_load < 1:
                print("Max load must be 1 or greater.")
                continue
            break
        except ValueError:
            print("Invalid input. Please enter an integer.")

    # Fixed parameter for load balancing strength
    LOAD_TRANSFER_RATE = 0.2
    
    return grid_size, time_steps, max_load, LOAD_TRANSFER_RATE

# --- 2. Grid Initialization (Simulating randomness without 'random' module) ---
def initialize_grid(GRID_SIZE, MAX_LOAD):
    """Creates a grid (list of lists) with high/low initial loads."""
    # This manually creates a state that is intentionally unbalanced (hotspots)
    grid = []
    initial_loads = [6, 14, 5, 1, 12, 11, 3, 0, 8, 13] * (GRID_SIZE * GRID_SIZE // 10 + 1)
    
    k = 0
    for i in range(GRID_SIZE):
        row = []
        for j in range(GRID_SIZE):
            # Use loads sequentially from the list
            load = initial_loads[k] if k < len(initial_loads) else (MAX_LOAD // 2)
            row.append(float(load))
            k += 1
        grid.append(row)
    return grid

# --- 3. CA Transition Rule (The Load Balancing Logic) ---
def update_load(current_grid, GRID_SIZE, MAX_LOAD, LOAD_TRANSFER_RATE):
    """Applies the load balancing rule (pulling excess load from neighbors)."""
    
    # 1. Create a deep copy of the grid to hold the new state (crucial for CA parallelism)
    new_grid = [row[:] for row in current_grid]
    
    # 2. Iterate over every cell (excluding the borders)
    for i in range(1, GRID_SIZE - 1):
        for j in range(1, GRID_SIZE - 1):
            
            current_cell_load = current_grid[i][j]
            load_change = 0
            
            # Von Neumann neighborhood coordinates (relative)
            neighbor_coords = [
                (i - 1, j), (i + 1, j), (i, j - 1), (i, j + 1)
            ]
            
            # Local Rule: Pull excess load from overloaded neighbors
            for ni, nj in neighbor_coords:
                neighbor_load = current_grid[ni][nj]
                
                # Check if the neighbor is 'overloaded'
                if neighbor_load > MAX_LOAD:
                    excess_load = neighbor_load - MAX_LOAD
                    transfer_amount = excess_load * LOAD_TRANSFER_RATE
                    
                    # Determine how much load the current cell can take
                    available_capacity = MAX_LOAD - current_cell_load
                    
                    # Ensure the transfer doesn't exceed what's available or what the cell can take
                    transfer_in = min(transfer_amount, available_capacity)

                    # Accumulate the change
                    if transfer_in > 0:
                        load_change += transfer_in
            
            # 3. Apply the calculated change to the new grid state
            new_grid[i][j] = max(0.0, current_cell_load + load_change)
            
    return new_grid

# --- 4. Visualization and Stats Function ---
def print_visual_grid(grid, MAX_LOAD, step_num, GRID_SIZE):
    """Prints the grid using 'X' for overloaded cells and rounded numbers otherwise."""
    print(f"\n--- Visual Grid at Time Step {step_num} ('X' = Overloaded > {MAX_LOAD}) ---")
    
    overloaded_count = 0
    total_load = 0.0
    
    # Print header line
    print("+" + "----" * GRID_SIZE + "+")

    for row in grid:
        row_output = []
        for value in row:
            total_load += value
            
            if value > MAX_LOAD:
                row_output.append(' X ')  # Clearly mark overloaded cells
                overloaded_count += 1
            else:
                # For non-overloaded cells, show the rounded, integer load value
                # Using round() and int() for cleaner output
                row_output.append(f'{int(round(value)):<3}') 
        print("|" + "|".join(row_output) + "|")

    # Print footer line
    print("+" + "----" * GRID_SIZE + "+")
    
    average_load = total_load / (GRID_SIZE * GRID_SIZE)
    print(f"Total Load: {total_load:.2f} | Average Load: {average_load:.2f} | Overloaded Cells: {overloaded_count}")
    print("=" * 60)

# --- 5. Main Simulation Execution ---
if __name__ == "__main__":
    GRID_SIZE, TIME_STEPS, MAX_LOAD, LOAD_TRANSFER_RATE = get_user_input()

    # Initialize the grid (list of lists)
    current_grid = initialize_grid(GRID_SIZE, MAX_LOAD)
    
    # Print Initial State
    print_visual_grid(current_grid, MAX_LOAD, 0, GRID_SIZE)

    # Run the simulation
    for t in range(1, TIME_STEPS + 1):
        current_grid = update_load(current_grid, GRID_SIZE, MAX_LOAD, LOAD_TRANSFER_RATE)
        print_visual_grid(current_grid, MAX_LOAD, t, GRID_SIZE)
