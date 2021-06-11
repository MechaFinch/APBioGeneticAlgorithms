package genetics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import graphics.APBioSimulation;

/**
 * Tries to move points to the right side of a line
 * 
 * @author Alex
 */
public class PointOptimizer extends GeneticsBase {
	
	/*
	 * PARAMETERS
	 * Constants you can change
	 */
	// Fitness per target point is the minimum of (1000 / distance) and this value. Any value 1000
	// or above removes the maximum, and values below 1000 create a deadzone around the point of 
	// equal fitness
	final double MAX_FITNESS = 1000;
	
	// If true, selection takes place like in LinearOptimizer. If false, points look for the fittest
	// individual within the SELECTION_DISTANCE
	final boolean SELECTION = false;
	
	// Square of the distance a point needs to be within to be considered
	final double SELECTION_DISTANCE = 100;
	
	// Mutations add a value taken from a bell curve with this standard deviation, divided by the
	// number of elapsed generations divided by the diminishing factor
	final double MUTATION_STD_DEVIATION = 3;
	
	// Controls how quickly the standard deviation of mutations is reduced. Higher = slower
	final double DIMINISHING_FACTOR = 12;
	
	// Controls whether mutations have reduced standard deviation over time
	final boolean DIMINISHING_MUTATIONS = true;
	
	/*
	 * CODE
	 * Change at your own risk
	 */
	
	double[][] points,
			   selectedPoints;
	
	double[] fitness;
	
	double minFitness, maxFitness;
	
	int[] xs, ys;
	
	int width, height, numGenerations;
	
	Random rand = new Random();
	
	/**
	 * constructor
	 * 
	 * @param numPoints Number of points per generation
	 * @param xLow X position of the lower point of the line
	 * @param xHigh X position of the upper point of the line
	 */
	public PointOptimizer(int numPoints, int[] xs, int[] ys) {
		this.xs = xs;
		this.ys = ys;
		
		minFitness = 0;
		maxFitness = 0;
		
		width = APBioSimulation.WIDTH;
		height = APBioSimulation.HEIGHT;
		
		points = new double[numPoints][2];
		initPopulation();
		
		selectedPoints = new double[numPoints / 2][2];
		fitness = new double[numPoints];
		
		numGenerations = 0;
	}
	
	@Override
	public void initPopulation() {
		for(int i = 0; i < points.length; i++) {
			points[i][0] = (float) (rand.nextFloat() * width);
			points[i][1] = (float) (rand.nextFloat() * height);
		}
		
		// find min/max possible fitness
		minFitness = fitness(0, 0);
		maxFitness = minFitness;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double f = fitness(x, y);
				
				if(f > maxFitness) maxFitness = f;
				else if(f < minFitness) minFitness = f;
			}
		}
		
		numGenerations = 0;
	}

	@Override
	public void generateFitness() {
		for(int i = 0; i < points.length; i++) {
			fitness[i] = fitness(points[i][0], points[i][1]);
		}
	}
	
	/**
	 * Determines the fitness of an individual
	 * 
	 * @param x
	 * @param y
	 */
	public double fitness(double x, double y) {
		double fitness = 0,
			   ax = 0,
			   ay = 0;
			
		// fitness is the sum of distances to points
		for(int j = 0; j < xs.length; j++) {
			ax = xs[j];
			ay = ys[j];
			
			// 
			double d = Math.sqrt(((ax - x) * (ax - x)) + ((ay - y) * (ay - y)));
			fitness += Math.min(MAX_FITNESS, 1000 / Math.sqrt(d));
		}
		
		return fitness;
	}

	@Override
	public void select() {
		if(SELECTION) {
			// select parents by placing them in adjacent pairs
			// we'll use 4 way tournament selection here
			for(int i = 0; i < selectedPoints.length; i++) {
				ArrayList<Double> fitnesses = new ArrayList<>(); // for collections
				ArrayList<Integer> indicies = new ArrayList<>(); // we need to know where they come from
				
				// grab 8 at random
				for(int j = 0; j < 8; j++) {
					int ind = rand.nextInt(fitness.length);
					fitnesses.add(fitness[ind]);
					indicies.add(ind);
				}
				
				// take the best
				int ind = indicies.get(fitnesses.indexOf(Collections.max(fitnesses)));
				selectedPoints[i][0] = points[ind][0];
				selectedPoints[i][1] = points[ind][1];
			}
		}
	}

	@Override
	public void cross() {
		if(SELECTION) {
			// replace half the points with crossed children
			//int side = (int) Math.round(rand.nextDouble()) * selectedPoints.length;
			
			for(int i = 0; i < selectedPoints.length; i++) {
				for(int j = 0; j < 2; j++) {
					// select other parent
					// try random others until one within range is found or too many tries attempted
					int other = 0;
					
					for(int n = 0; n < 16; n++) {
						other = rand.nextInt(selectedPoints.length);
						double a = selectedPoints[i][0] - selectedPoints[other][0],
							   b = selectedPoints[i][1] - selectedPoints[other][1];
						
						if((a * a) + (b * b) < 100) break;
					}
					
					// loop over genes
					for(int k = 0; k < 2; k++) {
						// average cross
						// offspring are 75/25 for one and 75/25 for the other
						if(rand.nextFloat() > 0.3) {
							points[(2 * i) + j][k] = selectedPoints[i][k];
						} else {
							points[(2 * i) + j][k] = (selectedPoints[i][k] * 0.75) + (selectedPoints[other][k] * 0.25);
						}
					}
				}
			}
		}
		
		
		else {
			// Every individual tries to find the fittest individual near it
			for(int i = 0; i < points.length; i++) {
				// Find best of 8 candidates
				double[] candidate = new double[2];
				double cf = 0;
				for(int j = 0; j < 8; j++) {
					// Try a few times
					for(int k = 0; k < 32; k++) {
						int ind = rand.nextInt(points.length);
						double a = points[ind][0] - points[i][0],
							   b = points[ind][1] - points[i][1];
						
						// check if in range or out of tries
						if(((a * a) + (b * b) < SELECTION_DISTANCE || k == 7) && cf < fitness[ind]) {
							// assign
							candidate[0] = points[ind][0];
							candidate[1] = points[ind][1];
							cf = fitness[ind];
						}
					}
				}
				
				// cross with found individual
				// loop over genes
				for(int k = 0; k < 2; k++) {
					// average cross
					// offspring are 75/25 for one and 75/25 for the other
					if(rand.nextFloat() > 0.7) {
						points[i][k] = points[i][k];
					} else {
						points[i][k] = (candidate[k] * 0.75) + (candidate[k] * 0.25);
					}
				}
			}
		}
	}
	
	@Override
	public void mutate() {
		// if we mutate, add a value in [-5, 5]
		for(int i = 0; i < points.length; i++) {
			/*
			for(int j = 0; j < 2; j++) {
				if(rand.nextFloat() > 0.9) {
					points[i][j] += rand.nextGaussian() * 10;
				}
			}
			*/
			
			if(rand.nextFloat() > 0.9) {
				if(DIMINISHING_MUTATIONS) {
					// let's try reducing learning rate over time
					points[i][0] += rand.nextGaussian() * MUTATION_STD_DEVIATION / Math.max(1, (double)(numGenerations) / DIMINISHING_FACTOR);
					points[i][1] += rand.nextGaussian() * MUTATION_STD_DEVIATION / Math.max(1, (double)(numGenerations) / DIMINISHING_FACTOR);
				} else {
					// let's try reducing learning rate over time
					points[i][0] += rand.nextGaussian() * MUTATION_STD_DEVIATION;
					points[i][1] += rand.nextGaussian() * MUTATION_STD_DEVIATION;
				}
			}
		}
		
		// cleanup - make sure we stay in bounds
		for(int i = 0; i < points.length; i++) {
			if(points[i][0] < 0) points[i][0] = 0;
			if(points[i][0] > width) points[i][0] = width;
			
			if(points[i][1] < 0) points[i][1] = 0;
			if(points[i][1] > height) points[i][1] = height;
		}
		
		numGenerations++;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setStroke(new BasicStroke(2f));
		
		// draw heatmap of fitness in red
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double fitness = fitness(x, y);
				
				double v = ((fitness - minFitness) / (maxFitness - minFitness));
				int col = Math.max(0, Math.min(255, (int) (255 * (1 - (v * 1.5)))));
				//if(x == 50) System.out.println(fitness + ": " + col);
				
				g.setColor(new Color(255, col, col));
				g.drawRect(x, y, 1, 1);
			}
		}
		
		// target points
		/*
		g.setColor(Color.blue);
		for(int i = 0; i < xs.length; i++) {
			g.fillRect(xs[i] - 5, ys[i] - 5, 10, 10);
		}
		*/
		
		// draw points
		g.setColor(Color.black);
		for(int i = 0; i < points.length; i++) {
			g.fillRect((int) points[i][0] - 2, (int) points[i][1] - 2, 4, 4);
		}
		
		System.out.println("frame drawn");
	}
}
