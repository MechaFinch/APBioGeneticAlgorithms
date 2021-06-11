package genetics;

import java.util.ArrayList;
import java.util.Random;

public class SalesmanSolution implements Comparable<SalesmanSolution> {
	
	public int[] path;
	
	public double fitness;
	
	/**
	 * Random solution
	 */
	public SalesmanSolution(int numCities) {
		path = new int[numCities];
		Random r = new Random();
		
		// numbers 0 to n-1 to rancomly take from
		ArrayList<Integer> pool = new ArrayList<>();
		for(int i = 0; i < path.length; i++) pool.add(i);
		
		for(int i = 0; i < path.length; i++) {
			path[i] = pool.remove(r.nextInt(pool.size()));
		}
		
		fitness = 0;
	}
	
	/**
	 * Copy
	 */
	public SalesmanSolution(SalesmanSolution other) {
		this.path = other.path;
		this.fitness = other.fitness;
	}
	
	/**
	 * Set Path
	 */
	public SalesmanSolution(int[] path) {
		this.path = path;
		fitness = 0;
	}
	
	public SalesmanSolution(ArrayList<Integer> alPath) {
		this.path = alPath.stream().mapToInt(a -> a).toArray();
		fitness = 0;
	}

	@Override
	public int compareTo(SalesmanSolution o) {
		double diff = fitness - o.fitness;
		if(diff > 0) return 1;
		else if(diff == 0) return 0;
		else return -1;
	}
}
