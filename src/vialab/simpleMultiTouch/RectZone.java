/*
  Simple Multitouch Library
  Copyright 2011
  Erik Paluka, Christopher Collins - University of Ontario Institute of Technology
  Mark Hancock - University of Waterloo

  Parts of this library are based on:
  TUIOZones http://jlyst.com/tz/

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public
  License Version 3 as published by the Free Software Foundation.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */
package vialab.simpleMultiTouch;

import java.awt.Color;

/** 
 * This is the basic rectangular zone class.<P>
 * 
 * University of Ontario Institute of Technology.
 * Summer Research Assistant with Dr. Christopher Collins (Summer 2011) collaborating with Dr. Mark Hancock.<P>
 * 
 * @author  Erik Paluka 
 * @date  Summer, 2011
 * @version 1.0
 */

public class RectZone extends Zone{
	boolean rounded = false;
	boolean fill = false;
	boolean stroke = false;
	boolean shadow = false;
	Color strokeColour, shadowColour;
	float r;
	float strokeWeight = 1;
	
	float prevX = 0;
	float prevY = 0;
	float xShadow = 5;
	float yShadow = 5;
	float wShadow = 0;
	float hShadow = 0;
	
	
	
	/**
	 * RectZone constructor, creates a rectangular zone by calling "createRectZone()"
	 * 
	 * @param x int - X-coordinate of the upper left corner of the zone
	 * @param y int - Y-coordinate of the upper left corner of the zone
	 * @param width int - Width of the zone
	 * @param height int - Height of the zone
	 */
	public RectZone(float x, float y, float width, float height){
		super();
		createRectZone(x, y, width, height);
	}

	public RectZone(float x, float y, float width, float height, float r){
		super();
		rounded = true;
		this.r = r;
		createRectZone(x, y, width, height);
	}

	/**
	 * Sets the x, y, width, height, and translate area radius (for RNT gesture) of the new rectangular zone.
	 * 
	 * @param xIn int - X-coordinate of the upper left corner of the zone
	 * @param yIn int - Y-coordinate of the upper left corner of the zone
	 * @param wIn int - Width of the zone
	 * @param hIn int - Height of the zone
	 */
	public void createRectZone(float xIn, float yIn, float wIn, float hIn){
		shadowColour = new Color(155, 155, 155);
		strokeColour = new Color(0, 0, 0);
		x = xIn;					// upper left corner x
		y = yIn;					// upper left corner y
		width = wIn;		// width
		height = hIn;	// height
		vx = vy = 0;				// velocities
		translateAreaRadius =  (float) (Math.sqrt(wIn*wIn + hIn*hIn) / 5); // Set the translate area radius for RNT
	}
	



	public void setShadow(boolean flag){
		shadow = flag;
	}
	public void setShadowColour(int r, int g, int b){
		shadowColour = new Color(r, g, b);
	}
	
	public void setShadowColour(int r, int g, int b, int a){
		shadowColour = new Color(r, g, b, a);
	}


	public void setShadowColour(Color c){
		shadowColour = c;
	}


	public Color getShadowColour(){
		return shadowColour;
	}
	public void setColour(int r, int g, int b){
		fill = true;
		super.setColour(new Color(r, g, b));
	}
	
	

	public void setColour(Color c){
		fill = true;
		super.setColour(c);
	}


	public void setFill(boolean fill){
		this.fill = fill;
	}

	public void setStroke(boolean stroke){
		this.stroke = stroke;
	}

	public void setStrokeColour(int r, int g, int b){
		this.strokeColour = new Color(r, g, b);
	}
	
	public void setStrokeColour(int r, int g, int b, int a){
		this.strokeColour = new Color(r, g, b, a);
	}

	public void setStrokeWeight(float strokeWeight){
		this.strokeWeight = strokeWeight;
	}

	public void setShadowX(int xShadow){
		this.xShadow = xShadow;
	}

	public void setShadowY(int yShadow){
		this.yShadow = yShadow;
	}
	
	public void setShadowW(int wShadow){
		this.wShadow = wShadow;
	}

	public void setShadowH(int hShadow){
		this.hShadow = hShadow;
	}

	public void drawZone(){
		super.drawZone();
		
		if(shadow){
			TouchClient.parent.stroke(shadowColour.getRed(), shadowColour.getGreen(), shadowColour.getBlue(), shadowColour.getAlpha());
			TouchClient.parent.strokeWeight(1);
			applet.fill(shadowColour.getRed(), shadowColour.getGreen(), shadowColour.getBlue());
			if(rounded){
				roundedRect(x + xShadow, y + yShadow, width + wShadow, height + hShadow, r);
			} else {
				applet.rect(x + xShadow, y + yShadow, width + wShadow, height + hShadow);
			}
		}


		if(stroke){
			TouchClient.parent.stroke(strokeColour.getRed(), strokeColour.getGreen(), strokeColour.getBlue(), strokeColour.getAlpha());
			TouchClient.parent.strokeWeight(strokeWeight);

		} else {
			TouchClient.parent.noStroke();
		}



		if(rounded){

			/*TouchClient.parent.fill(colour.getRed(), colour.getGreen(), colour.getBlue());
			TouchClient.parent.noStroke();
			TouchClient.parent.rectMode(PConstants.CORNER);
			TouchClient.parent.ellipseMode(PConstants.CENTER);
			TouchClient.parent.rect(x2, y2, width2, height2);
			TouchClient.parent.arc(x2, y2, r, r, PApplet.radians((float) 180.0), PApplet.radians((float) 270.0));
			TouchClient.parent.arc(ax, y2, r,r, PApplet.radians((float) 270.0), PApplet.radians((float) 360.0));
			TouchClient.parent.arc(x2, ay, r,r, PApplet.radians((float) 90.0), PApplet.radians((float) 180.0));
			TouchClient.parent.arc(ax, ay, r,r, PApplet.radians((float) 0.0), PApplet.radians((float) 90.0));
			TouchClient.parent.rect(x2, y2-hr, width2, hr);
			TouchClient.parent.rect(x2-hr, y2, hr, height2);
			TouchClient.parent.rect(x2, y2+height2, width2, hr);
			TouchClient.parent.rect(x2+width2,y2,hr, height2);*/
			if(fill){
				TouchClient.parent.fill(getColour().getRed(), getColour().getGreen(), getColour().getBlue(), getColour().getAlpha());
			}
			roundedRect(x, y, width, height, r);

		} else if (fill){
			TouchClient.parent.noStroke();
			TouchClient.parent.fill(getColour().getRed(), getColour().getGreen(), getColour().getBlue(), getColour().getAlpha());
			TouchClient.parent.rect(getX(), getY(), getWidth(), getHeight());

		}

		//TODO
		// GRADIENTs!!!!
		//setGradient(getX(),getY(), getWidth(), getHeight()/2, new Color(0,0,0), new Color(22, 22, 22), "Y_AXIS");
		//setGradient(getX(), getY() + getHeight()/2, getWidth(), getHeight(), new Color(22,22,22), new Color(44, 44, 44), "Y_AXIS");


	}


	//////////////////////////////////////////////
	///// From WebSite - http://quasipartikel.at/2010/01/07/quadratic-bezier-curves-for-processingjs/
	///// Last Accessed on May 22, 2012
	///// Quadratic bezier curves for Processing.js
	public void quadraticBezierVertex(float f, float g, float h, float i) {
		prevX = (float) f;
		prevY = (float) g;
		float cp1x = (float) (prevX + 2.0/3.0*(f - prevX));
		float cp1y = (float) (prevY + 2.0/3.0*(g - prevY));
		float cp2x = (float) (cp1x + (h - prevX)/3.0);
		float cp2y = (float) (cp1y + (i - prevY)/3.0);


		// finally call cubic Bezier curve function
		TouchClient.parent.bezierVertex(cp1x, cp1y, cp2x, cp2y, h, i);

	}


	//////////////////////////////////////////////
	///// From WebSite - http://quasipartikel.at/2010/01/07/quadratic-bezier-curves-for-processingjs/
	///// Last Accessed on May 22, 2012
	///// Quadratic bezier curves for Processing.js
	public void roundedRect(float f, float g, float h, float i, float r2) {  

		TouchClient.parent.beginShape();
		TouchClient.parent.vertex(f+r2, g);
		TouchClient.parent.vertex(f+h-r2, g);
		quadraticBezierVertex(f+h, g, f+h, g+r2);
		TouchClient.parent.vertex(f+h, g+i-r2);
		quadraticBezierVertex(f+h, g+i, f+h-r2, g+i);
		TouchClient.parent.vertex(f+r2, g+i);
		quadraticBezierVertex(f, g+i, f, g+i-r2);
		TouchClient.parent.vertex(f, g+r2);
		quadraticBezierVertex(f, g, f+r2, g);
		TouchClient.parent.endShape();
	}



}
