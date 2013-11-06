package vialab.simpleMultiTouch;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class GarbageZone extends RectZone{
	TouchClient client;
	PApplet applet;
	boolean image = false;
	PImage img;

	public GarbageZone(TouchClient client, float x, float y, float width, float height) {
		super(x, y, width, height);
		this.client = client;

	}

	public GarbageZone(TouchClient client, float x, float y, float width, float height, PImage img) {
		super(x, y, width, height);
		this.client = client;
		this.applet = client.getParent();
		this.img = img;
		image = true;

	}

	public void drawZone(){
		super.drawZone();
		if(image){
			applet.image(img, getX(), getY(), getWidth(), getHeight());
		}
		synchronized(zoneList){
			for(int i = 0; i < zoneList.size(); i++){
				Zone zone = zoneList.get(i);
				if(zone != null && zone.active){
					if(zone != this){			
						if (zone.changed) {
							zone.inverse.reset();
							zone.inverse.apply(zone.matrix);
							zone.inverse.invert();
							zone.changed = false;
						}
						PVector world = new PVector();
						PVector mouse = new PVector(this.getX() + this.getWidth()/2, this.getY() + this.getHeight()/2);
						zone.inverse.mult(mouse, world);

						if ((world.x > zone.getX()) && (world.x < zone.getX() + zone.width)
								&& (world.y > zone.getY()) && (world.y < zone.getY() + zone.height)) {
							
							zone.setActive(false);
							gbTrigger(zone);

						}

					}
				}
			}
		}

	}

	public void gbTrigger(Zone z){

	}
}
