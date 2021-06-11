package graphics;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Main runnable and window
 * 
 * @author Alex
 */
public class APBioSimulation {
	
	// if true, updates the canvas after each step
	// if false, runs steps as fast as possible while updating the canvas at a given framerate
	public static final boolean REAL_TIME = false;
	
	public static final int WIDTH = 500, HEIGHT = 500;
	
	JFrame frame;
	SimulationPanel simPanel;
	
	/**
	 * constructor
	 */
	public APBioSimulation() {
		this.frame = new JFrame("AP Bio Experiment");
		this.simPanel = new SimulationPanel();
	}
	
	/**
	 * Make the window and uuuuuuhhhhhhh
	 */
	void createWindow() {
		// jframe config
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.add(simPanel);
		
		frame.pack();
		frame.setVisible(true);
		
		simPanel.setupCanvas();
	}
	
	// main that runs things
	public static void main(String[] args) {
		// Look and feel thingy
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {	// the exceptions are many
			e.printStackTrace();
		}
		
		// let's get started
		new APBioSimulation().createWindow();
	}
}
