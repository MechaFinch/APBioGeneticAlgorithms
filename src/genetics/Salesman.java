package genetics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import graphics.APBioSimulation;

/**
 * tries to solve the traveling salesman problem
 */
public class Salesman extends GeneticsBase {
	
	/*
	 * PARAMETERS
	 * Constants you can change
	 */
	// Opacity of each line in the general population. Integer value from 0-255 
	final byte POPULATION_OPACITY = 10;
	
	// Probability of a mutation
	final double MUTATION_PROBABILITY = 0.1;
	
	
	/*
	 * CODE
	 * Change at your own risk
	 */
	
	int[][] cities;
	
	int numSolutions,
		numCities,
		elites;
	
	double eliteProb,
		   mutProb;
	
	ArrayList<SalesmanSolution> solutions,
								selectedSolutions;
	
	Random rand = new Random();
	
	/**
	 * Constructor
	 * 
	 * @param numCities number of cities
	 * @param numSolutions number of solutions
	 * @param elites number of elites
	 * @param eliteProb probability of elite cross
	 * @param mutProb probability of mutation
	 */
	public Salesman(int numCities, int numSolutions, int elites, double eliteProb, double mutProb) {
		cities = new int[numCities][2];
		solutions = new ArrayList<>(numSolutions);
		selectedSolutions = new ArrayList<>(numSolutions);
		
		this.numCities = numCities;
		this.numSolutions = numSolutions;
		this.elites = elites;
		this.eliteProb = eliteProb;
		this.mutProb = mutProb;
		
		generateCities();
		initPopulation();
		generateFitness();
	}
	
	public void generateCities() {
		// Generate cities
		for(int i = 0; i < cities.length; i++) {
			cities[i][0] = rand.nextInt(APBioSimulation.WIDTH);
			cities[i][1] = rand.nextInt(APBioSimulation.HEIGHT);
		}
	}

	@Override
	public void initPopulation() {
		generateCities();
		
		solutions.clear();
		
		for(int i = 0; i < numSolutions; i++) {
			solutions.add(new SalesmanSolution(cities.length));
		}
	}

	@Override
	public void generateFitness() {
		// Walk the solution to find the length
		for(int i = 0; i < solutions.size(); i++) {
			SalesmanSolution sol = solutions.get(i);
			int[] path = sol.path;
			sol.fitness = 0;
			
			for(int j = 1; j < numCities; j++) {
				sol.fitness += dist(cities[path[j]][0], cities[path[j]][1],
									cities[path[j - 1]][0], cities[path[j - 1]][1]);
			}
		}
	}
	
	void parallelGenerateFitness(int start, int end) {
		// Walk the solution to find the length
		for(int i = start; i < end; i++) {
			SalesmanSolution sol = solutions.get(i);
			int[] path = sol.path;
			sol.fitness = 0;
			
			for(int j = 1; j < numCities; j++) {
				sol.fitness += dist(cities[path[j]][0], cities[path[j]][1],
									cities[path[j - 1]][0], cities[path[j - 1]][1]);
			}
		}
	}
	
	/**
	 * Returns the distance between two points
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
	}

	@Override
	public void select() {
		// elitist tournament selection
		Collections.sort(solutions);
		
		selectedSolutions.clear();
		
		// Copy elites
		for(int i = 0; i < elites; i++) {
			selectedSolutions.add(solutions.get(i));
		}
		
		// Tournament selected randos
		int tournSize = 8;
		for(int i = elites; i < solutions.size(); i++) {
			ArrayList<SalesmanSolution> candidates = new ArrayList<>(tournSize);
			
			for(int j = 0; j < tournSize; j++) {
				candidates.add(solutions.get(rand.nextInt(solutions.size())));
			}
			
			// take the best
			selectedSolutions.add(Collections.min(candidates));
		}
	}

	@Override
	public void cross() {
		ArrayList<SalesmanSolution> newSolutions = new ArrayList<>();
		
		// OX1 ordered crossover
		// Copy a random segment from one, then copy missing items in the order they appear
		// on the second, starting from the end of the random segment
		for(int i = 0; i < numSolutions; i++) {
			
			// always breed for normal and sometimes breed for elites
			if((i >= elites || rand.nextDouble() < eliteProb) && i != 0) {
				int[] p1 = selectedSolutions.get(i).path,
					  p2 = selectedSolutions.get(i - 1).path;
				
				ArrayList<Integer> newPath = new ArrayList<>(numCities);
				
				// initialize with -1
				for(int j = 0; j < numCities; j++) newPath.add(-1);
				
				int start = rand.nextInt(numCities),
					end = rand.nextInt(numCities);
				
				if(start > end) {
					int t = start;
					start = end;
					end = t;
				}
				
				// copy from first
				for(int j = start; j < end; j++) {
					newPath.set(j, p1[j]);
				}
				
				// copy from second
				// j is source index k is dest index
				for(int j = end, k = end; newPath.contains(-1); j++) {
					if(j >= numCities) j = 0;
					
					if(!newPath.contains(p2[j])) {
						newPath.set(k++, p2[j]);
						
						if(k >= numCities) k = 0;
					}
				}
				
				newSolutions.add(new SalesmanSolution(newPath));
			} else {
				newSolutions.add(selectedSolutions.get(i));
			}
		}
		
		solutions = newSolutions;
	}
	
	void parallelCross(int start, int end) {
		ArrayList<SalesmanSolution> newSolutions = new ArrayList<>();
		
		// OX1 ordered crossover
		// Copy a random segment from one, then copy missing items in the order they appear
		// on the second, starting from the end of the random segment
		for(int i = start; i < end; i++) {
			
			// always breed for normal and sometimes breed for elites
			if((i >= elites || rand.nextDouble() < eliteProb) && i != 0) {
				int[] p1 = selectedSolutions.get(i).path,
					  p2 = selectedSolutions.get(i - 1).path;
				
				ArrayList<Integer> newPath = new ArrayList<>(numCities);
				
				// initialize with -1
				for(int j = 0; j < numCities; j++) newPath.add(-1);
				
				int s = rand.nextInt(numCities),
					e = rand.nextInt(numCities);
				
				if(s > e) {
					int t = s;
					s = e;
					e = t;
				}
				
				// copy from first
				for(int j = s; j < e; j++) {
					newPath.set(j, p1[j]);
				}
				
				// copy from second
				// j is source index k is dest index
				for(int j = e, k = e; newPath.contains(-1); j++) {
					if(j >= numCities) j = 0;
					
					if(!newPath.contains(p2[j])) {
						newPath.set(k++, p2[j]);
						
						if(k >= numCities) k = 0;
					}
				}
				
				newSolutions.add(new SalesmanSolution(newPath));
			} else {
				newSolutions.add(selectedSolutions.get(i));
			}
		}
		
		// copy new into segment of old
		for(int i = 0; i < newSolutions.size(); i++) {
			solutions.set(start + i, newSolutions.get(i));
		}
	}

	@Override
	public void mutate() {
		// Swap mutation
		for(int i = 0; i < solutions.size(); i++) {
			if(rand.nextDouble() < MUTATION_PROBABILITY) {
				int n = rand.nextInt(3) + 1;
				
				for(int j = 0; j < n; j++) {
					int[] p = solutions.get(i).path;
					
					int a = rand.nextInt(numCities),
						b = rand.nextInt(numCities),
						c = p[a];
					
					p[a] = p[b];
					p[b] = c;
				}
			}
		}
	}
	
	/**
	 *  Version of mutate for parallelization
	 *  
	 * @param solutions
	 */
	void parallelMutate(int start, int end) {
		// Swap mutation
		for(int i = start; i < end; i++) {
			if(rand.nextDouble() < MUTATION_PROBABILITY) {
				int n = rand.nextInt(3) + 1;
				
				for(int j = 0; j < n; j++) {
					int[] p = solutions.get(i).path;
					
					int a = rand.nextInt(numCities),
						b = rand.nextInt(numCities),
						c = p[a];
					
					p[a] = p[b];
					p[b] = c;
				}
			}
		}
	}
	
	@Override
	public void runGeneration() {
		select();
		cross();
		mutate();
		generateFitness(); // we need fitness for our draw function so do that after a gen instead of before
	}
	
	public void runGenerationParalel() {
		select();
		
		// run the other 3 in parallel
		
	}

	@Override
	public void draw(Graphics2D g) {
		g.setStroke(new BasicStroke(2f));
		
		// draw all solutions with low opacity
		// bitmask stuff reduces alpha
		g.setColor(new Color((Color.blue.getRGB() & 0x00FF_FFFF) | (POPULATION_OPACITY << 24), true));
		for(int i = 0; i < solutions.size(); i++) {
			int[] path = solutions.get(i).path;
			
			for(int j = 1; j < path.length; j++) {
				g.drawLine(cities[path[j]][0], cities[path[j]][1],
						   cities[path[j - 1]][0], cities[path[j - 1]][1]);
			}
		}
		
		// draw best solution
		/*
		g.setColor(Color.green);
		int[] path = solutions.get(0).path;
		
		for(int i = 1; i < path.length; i++) {
			g.drawLine(cities[path[i]][0], cities[path[i]][1],
					   cities[path[i - 1]][0], cities[path[i - 1]][1]);
		}
		*/
		
		// draw cities
		g.setColor(Color.blue);
		for(int i = 0; i < cities.length; i++) {
			g.fillRect(cities[i][0] - 4, cities[i][1] - 4, 8, 8);
		}
		
		// display best fitness
		g.drawString(String.format("%.2f", solutions.get(0).fitness), 0, 500);
	}

}
