package genetics;

import java.awt.Graphics2D;

/**
 * Something to base genetic algorithm experiments off of
 * 
 * @author Alex
 */
public abstract class GeneticsBase {
	
	/**
	 * Create initial population
	 */
	public abstract void initPopulation();
	
	/**
	 * Run a full generation
	 */
	public void runGeneration() {
		generateFitnessParallel();
		select();
		cross();
		mutate();
	}
	
	/**
	 * Generate the fitness values of each individual
	 */
	public abstract void generateFitness();
	
	/**
	 * Generates fitness in parallel
	 * Implementation is optional, calls generateFitness() by default
	 */
	public void generateFitnessParallel() {
		generateFitness();
	}
	
	/**
	 * Select which individuals reproduce
	 */
	public abstract void select();
	
	/**
	 * Cross over pairs
	 */
	public abstract void cross();
	
	/**
	 * Mutate individuals
	 */
	public abstract void mutate();
	
	/**
	 * Draw to the canvas
	 * 
	 * @param g
	 */
	public abstract void draw(Graphics2D g);
}
