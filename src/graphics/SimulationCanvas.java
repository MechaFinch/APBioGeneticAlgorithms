package graphics;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;

import genetics.GeneticsBase;

/**
 * For all your graphics needs
 * 
 * @author Alex
 */
@SuppressWarnings("serial")
public class SimulationCanvas extends Canvas {
	
	GeneticsBase sim;
	
	/**
	 * constructor
	 */
	public SimulationCanvas(GeneticsBase sim) {
		this.sim = sim;
	}
	
	/**
	 * Draws whatever we need to draw, probably a bunch of points
	 */
	@Override
	public void paint(Graphics g1d) {
		super.paint(g1d); // we probably need this
		
		// just slapp that into the second dimension
		Graphics2D g = (Graphics2D) g1d;
		
		sim.draw(g);
		
		// actually show
		try {	//This may give a npe if the main thread is too slow
			getBufferStrategy().show();
		} catch(NullPointerException e) {
			System.out.println("Paint called too early");
		}
	}
}
