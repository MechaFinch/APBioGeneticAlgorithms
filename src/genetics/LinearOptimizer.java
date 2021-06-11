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
public class LinearOptimizer extends GeneticsBase {
	
	/*
	 * PARAMETERS
	 * Constants you can change
	 */
	
	// The probability that a pair will be crossed rather than copied. Range 0-1
	final double CROSS_PROBABILITY = 0.1;
	
	// Mutations add a value taken from a bell curve with this standard deviation
	final double MUTATION_STD_DEVIATION = 5;
	
	
	/*
	 * CODE
	 * Change at your own risk
	 */
	
	double[][] points,
			   selectedPoints;
	
	double[] fitness;
	
	double minFitness, maxFitness;
	
	int[] xLow, xHigh;
	
	int width, height;
	
	Random rand = new Random();
	
	/**
	 * constructor
	 * 
	 * @param numPoints Number of points per generation
	 * @param xLow X position of the lower point of the line
	 * @param xHigh X position of the upper point of the line
	 */
	public LinearOptimizer(int numPoints, int[] xLow, int[] xHigh) {
		this.xLow = xLow;
		this.xHigh = xHigh;
		
		minFitness = 0;
		maxFitness = 0;
		
		width = APBioSimulation.WIDTH;
		height = APBioSimulation.HEIGHT;
		
		points = new double[numPoints][2];
		initPopulation();
		
		selectedPoints = new double[numPoints / 2][2];
		fitness = new double[numPoints];
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
			   ay = (double) height,
			   bx = 0,
			   by = 0;
			
		// fitness is the sum of distances to lines
		for(int j = 0; j < xLow.length; j++) {
			ax = xLow[j];
			bx = xHigh[j];
			
			// distance from a point to a line defined by two points from wikipedia
			fitness += Math.max(0, ((double) Math.abs(((bx - ax)*(ay - y) - (ax - x)*(by - ay)) / Math.sqrt(((bx - ax)*(bx - ax)) + ((by - ay)*(by - ay))))));
		}
		
		return fitness;
	}

	@Override
	public void select() {
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
			int ind = indicies.get(fitnesses.indexOf(Collections.min(fitnesses)));
			selectedPoints[i][0] = points[ind][0];
			selectedPoints[i][1] = points[ind][1];
		}
	}

	@Override
	public void cross() {
		// produce 4 children per pair to fill our population
		for(int i = 0; i < selectedPoints.length; i += 2) {
			// 2 pairs
			for(int j = 0; j < 2; j++) {
				// there's only 2 genes so uniform crossover
				// 50/50 whether we cross or not
				for(int k = 0; k < 2; k++) {
					if(rand.nextFloat() > CROSS_PROBABILITY) {
						points[2*(i + j)][k] = selectedPoints[i][k];
						points[2*(i + j) + 1][k] = selectedPoints[i + 1][k];
					} else {
						points[2*(i + j)][k] = selectedPoints[i + 1][k];
						points[2*(i + j) + 1][k] = selectedPoints[i][k];
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
				points[i][0] += rand.nextGaussian() * MUTATION_STD_DEVIATION;
				points[i][1] += rand.nextGaussian() * MUTATION_STD_DEVIATION;
			}
		}
		
		// cleanup - make sure we stay in bounds
		for(int i = 0; i < points.length; i++) {
			if(points[i][0] < 0) points[i][0] = 0;
			if(points[i][0] > width) points[i][0] = width;
			
			if(points[i][1] < 0) points[i][1] = 0;
			if(points[i][1] > height) points[i][1] = height;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setStroke(new BasicStroke(2f));
		
		// draw heatmap of fitness in red
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				double fitness = fitness(x, y);
				
				double v = 1 - ((fitness - minFitness) / (maxFitness - minFitness));
				int col = Math.max(0, Math.min(255, (int) (255 * (1 - (v * v * v)))));
				//if(x == 50) System.out.println(fitness + ": " + col);
				
				g.setColor(new Color(255, col, col));
				g.drawRect(x, y, 1, 1);
			}
		}
		
		g.setColor(Color.black);
		
		// dividing lines
		for(int i = 0; i < xLow.length; i++) {
			g.drawLine(xLow[i], height, xHigh[i], 0);
		}
		
		// draw points
		for(int i = 0; i < points.length; i++) {
			g.fillRect((int) points[i][0] - 3, (int) points[i][1] - 3, 5, 5);
		}
		
		System.out.println("frame drawn");
	}
}
