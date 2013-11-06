package vialab.simpleMultiTouch;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import processing.core.PConstants;
import processing.core.PVector;

public class CircleZone extends Zone {
	Shape circle;
	Color colour = new Color(0, 0, 0);
	Color sColour = new Color(0, 0, 0);
	public boolean fill = true;
	public boolean stroke = true;
	
	public CircleZone(float x, float y, float width, float height){
		super();
		circle = new Ellipse2D.Float(x, y, width, height);
		createCircleZone(x, y, width, height);
	}
	

	public void createCircleZone(float xIn, float yIn, float wIn, float hIn){
		x = xIn;					// upper left corner x
		y = yIn;					// upper left corner y
		width = wIn;		// width
		height = hIn;	// height
		vx = vy = 0;				// velocities
		translateAreaRadius =  (float) (Math.sqrt(wIn*wIn + hIn*hIn) / 5); // Set the translate area radius for RNT
	}
	/**
	 * Tests to see if the x and y coordinates are in the zone. If the zone's
	 * matrix has been changed, reset its inverse matrix. This method is also
	 * used to place the x and y coordinate in the zone's matrix space and saves
	 * it to localX and localY.
	 * 
	 * 
	 * @param x
	 *            float - X-coordinate to test
	 * @param y
	 *            float - Y-coordinate to test
	 * @return boolean True if x and y is in the zone, false otherwise.
	 */
	public boolean contains(float x, float y) {
		if (this.changed) {
			this.inverse.reset();
			this.inverse.apply(this.matrix);
			this.inverse.invert();
			this.changed = false;
		}
		PVector world = new PVector();
		PVector mouse = new PVector(x, y);
		this.inverse.mult(mouse, world);

		//if ((world.x > this.getX()) && (world.x < this.getX() + this.width)
		//		&& (world.y > this.getY()) && (world.y < this.getY() + this.height)) {
		if(circle.contains(world.x, world.y)){
			this.localX = world.x;
			this.localY = world.y;
			return true;
		}
		return false;

	}
	
	public void setColour(int r, int g, int b){
		colour = new Color(r, g, b);
	}
	
	public void setColour(Color c){
		colour = c;
	}
	
	public Color getColour(){
		return colour;
	}
	public void setStrokeColour(int r, int g, int b){
		sColour = new Color(r, g, b);
	}
	
	public void setFill(boolean fill){
		this.fill = fill;
	}
	
	public void setStroke(boolean stroke){
		this.stroke = stroke;
	}
	
	public void drawZone(){
		
		if(stroke){
			TouchClient.parent.strokeWeight(1);
			TouchClient.parent.stroke(sColour.getRed(), sColour.getGreen(), sColour.getBlue());
		} else {
			TouchClient.parent.noStroke();
		}
		
		if(fill){
			TouchClient.parent.fill(colour.getRed(), colour.getGreen(), colour.getBlue());
		} else {
			TouchClient.parent.noFill();
		}
		TouchClient.parent.ellipseMode(PConstants.CORNER);
		TouchClient.parent.ellipse(getX(), getY(), getWidth(), getHeight());
		TouchClient.parent.ellipseMode(PConstants.CENTER);

	}


}
