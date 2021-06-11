package graphics;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import genetics.*;

/**
 * For all your jpanel needs
 * this is also where most management happens, because convenience
 * 
 * @author Alex
 */
@SuppressWarnings("serial")
public class SimulationPanel extends JPanel implements ActionListener {
	
	GeneticsBase simulation;
	
	SimulationCanvas canvas;
	
	JButton toggleButton,
			stepButton,
			resetButton;
	
	boolean simulationRunning;
	
	/**
	 * constructor
	 * also sets up components
	 */
	public SimulationPanel() {
		/*
		 * To change which simulation you use, uncomment the one you want by removing the "//"
		 * before its line, and add a "//" to the one that was there before.
		 * 
		 * For the Optimizers, the first number is the number of individuals and the lists in
		 * brackets control the targets. For the LinearOptimizer, the first list is the x-coordinate
		 * of each line on the bottom of the screen and the second list is the x-coordinate on the
		 * top of the screen. For the PointOptimizer, the first list is the x-coordinates of the
		 * points and the second list is the y-coordinates. X and Y values range from 0-500
		 * 
		 * For Salesman, the first number is the number of cities, the second the number of 
		 * individuals, the third the number of "elite" individuals per generation, which get a free
		 * pass for having the best fitness, the fourth the probability an elite individual is
		 * crossed rather than left alone, and the fifth the opacity of each line 
		 */
		
		//simulation = new LinearOptimizer(400, new int[] {0, 100, 400, 500}, new int[] {100, 0, 500, 400});
		//simulation = new PointOptimizer(1000, new int[] {150, 350, 250}, new int[] {300, 300, 200});
		simulation = new Salesman(35, 60, 5, 0.5, 0.2);
		
		simulationRunning = false;
		
		// layout stuff to have column of controls on the left and sim on the right
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		toggleButton = new JButton("Start"); // have a start button so it doesn't run before the window's up
		toggleButton.addActionListener(this);
		toggleButton.setActionCommand("toggle");
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(toggleButton, gbc);
		
		stepButton = new JButton("Step"); // step forward a generation
		stepButton.addActionListener(this);
		stepButton.setActionCommand("step");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.NORTH;
		add(stepButton, gbc);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.setActionCommand("reset");
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(resetButton, gbc);
		
		canvas = new SimulationCanvas(simulation);
		canvas.setSize(500, 500);
		canvas.setMaximumSize(canvas.getSize());
		canvas.setBackground(Color.white);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 3;
		add(canvas, gbc);
	}
	
	/**
	 * we can't set up the canvas immediately so finish that here
	 */
	public void setupCanvas() {		
		canvas.createBufferStrategy(2);
		
		// async canvas updater thread for running faster than real time
		if(!APBioSimulation.REAL_TIME) {
			new Thread(() -> {
				long lastTime = System.currentTimeMillis(),
					 frameRate = 15;
				
				while(true) {
					// step
					if(simulationRunning) {
						for(int i = 0; i < 1000; i++) {
							simulation.runGeneration();
						}
					}
					
					// draw
					canvas.paint(canvas.getBufferStrategy().getDrawGraphics());
					
					// wait
					try {
						long newTime = System.currentTimeMillis(),
							 delta = (lastTime + (1000 / frameRate)) - newTime;
						lastTime = newTime;
						
						if(delta > 0) {
							Thread.sleep(delta);
						}
					} catch (InterruptedException e) {
						System.out.println("canvas updater interrupted");
					}
				}
			}).start();
		}
	}
	
	/**
	 * action listener
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand().split(" ")[0]) {
			case "toggle":				
				if(simulationRunning) {
					toggleButton.setText("Start");
				} else {
					toggleButton.setText("Stop");
				}
				
				simulationRunning = !simulationRunning;
				break;
			
			case "step":
				for(int i = 0; i < 1; i++) {
					simulation.runGeneration();
				}
				
				if(APBioSimulation.REAL_TIME) {
					canvas.paint(canvas.getBufferStrategy().getDrawGraphics());
				}
				break;
			
			case "reset":
				simulation.initPopulation();
				break;
			
			default:
				System.out.println("invalid action command: " + e.getActionCommand());
		}
	}
}
