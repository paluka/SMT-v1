/*
 * Simple Multitouch Library Copyright 2011 Erik Paluka, Christopher Collins -
 * University of Ontario Institute of Technology Mark Hancock - University of
 * Waterloo
 * 
 * Parts of this library are based on: TUIOZones http://jlyst.com/tz/
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 3 as published by the
 * Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package vialab.simpleMultiTouch;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Vector;

import org.jdesktop.animation.timing.Animator;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import vialab.simpleMultiTouch.events.DragEvent;
import vialab.simpleMultiTouch.events.HSwipeEvent;
import vialab.simpleMultiTouch.events.PinchEvent;
import vialab.simpleMultiTouch.events.RotateEvent;
import vialab.simpleMultiTouch.events.TapAndHoldEvent;
import vialab.simpleMultiTouch.events.TapEvent;
import vialab.simpleMultiTouch.events.VSwipeEvent;
import TUIO.TuioCursor;

/**
 * This is the main zone class which RectZone and ImageZone extend. It holds the
 * zone's coordinates, size, matrices, friction, etc.. It was done with help
 * from the tuioZones library.
 * <P>
 * 
 * University of Ontario Institute of Technology. Summer Research Assistant with
 * Dr. Christopher Collins (Summer 2011) collaborating with Dr. Mark Hancock.
 * <P>
 * 
 * @author Erik Paluka
 * @date Summer, 2011
 * @version 1.0
 */

public class Zone extends ZoneAnimator {
	/** Processing PApplet */
	static PApplet applet = TouchClient.parent;

	/** The Touch Client zone list */
	static Vector<Zone> zoneList = TouchClient.zoneList;

	/** The Touch Client zone picker */
	private static ZonePicker picker = TouchClient.picker;


	/** The zone's child zone list */
	protected Vector<Zone> childList = new Vector<Zone>();

	/** The zone's transformation matrix */
	protected PMatrix3D matrix = new PMatrix3D();

	/** The zone's inverse transformation matrix */
	protected PMatrix3D inverse = new PMatrix3D();

	protected long lastTouchDown = -1, lastTouchUp = -1,  hSwipeCursor = -1, vSwipeCursor = -1, 
			timeTouchUp = 0, timeTouchDown = 0, lastTapTime = 0;

	protected int currentTouches = 0, borderColour = 0, borderWeight = 2;

	protected boolean draggable = false, xDraggable = false, yDraggable = false, pinchable = false,
			throwable = false, hSwipeable = false, vSwipeable = false, rotatable = false,
			active = true, xPinchable = false, yPinchable = false, pinching = false,
			rotating = false, drawBorder = true, changed = true, RNTing = false,
			RNTable = false, xyPinchable = false, xyPinching = false, tappable = false,
			tapAndHoldable = false, toggle = false, tapDown = true, tapUp = false;

	protected boolean draggableHandled = false, xDraggableHandled = false,
			yDraggableHandled = false, pinchableHandled = false, throwableHandled = false,
			hSwipeableHandled = false, vSwipeableHandled = false, rotatableHandled = false,
			activeHandled = true, xPinchableHandled = false, yPinchableHandled = false,
			pinchingHandled = false, rotatingHandled = false, drawBorderHandled = true,
			changedHandled = true, RNTingHandled = false,
			RNTableHandled = false, xyPinchableHandled = false, xyPinchingHandled = false,
			tappableHandled = false, tapAndHoldableHandled = false, poly = false, childZones = false,
			groupZones = false;


	/** List of current Touches (TUIO Cursors) on the zone */
	protected Vector<Touch> touchList = new Vector<Touch>();

	protected Vector<TuioCursor> tuioCursorList = new Vector<TuioCursor>();

	/** Name of zone group */
	protected String group = null;

	protected float releaseTime = 0, vx, vy, scl = 1.0f, sclX = 1.0f, sclY = 1.0f, sclLow = 0.3f,
			sclHigh = 5.0f, sclSens = 1.0f, lastXMovement = 0, lastYMovement = 0, lastSclDist = 0,
			lastSclXDist = 0, lastSclYDist = 0, frictionX = 5000, frictionY = 5000, hswipeDist = 0, 
			vswipeDist = 0;

	protected float angle, x, y, height, width;
	protected float xThresh = 0;
	protected float yThresh = 0;

	// localX and localY are the x and y coordinates that were checked against this zone (zone.contains(x, y))
	protected float localX;

	protected float localY;

	protected float lastLocalX;

	protected float lastLocalY;

	protected float lastGlobalX;

	protected float lastGlobalY;

	protected float translateAreaRadius;

	protected float[] coordinates;

	protected boolean propogateTouchEvents = true;

	private ArrayList<Long> currentTouchIDs = new ArrayList<Long>(5);

	public Animator zoneAnimator;
	
	public Color backgroundColour = new Color(55, 55, 55);


	/**
	 * Zone constructor
	 */
	public Zone() {
		this.timingZone = this;
	}

	public String toString(){
		String s = "Zone: x position->" + getXTimesMatrix() + " y position->" + getYTimesMatrix() + " width->" + getWidth() + " height->" + getHeight();
		return s;
	}
	
	public void setThresholdTouch(float x, float y){
		this.xThresh = x;
		this.yThresh = y;
	}

	public void setColour(Color backgroundColour){
		this.backgroundColour = backgroundColour;
	}
	
	public Color getColour(){
		return this.backgroundColour;
	}
	public void setAnimator(Animator anim){
		zoneAnimator = anim;
	}

	public Animator getAnimator(){
		return zoneAnimator;
	}

	public void setX(float x){
		this.x = x;
	}

	public void setY(float y){
		this.y = y;
	}

	public void setXY(float x, float y){
		this.x = x;
		this.y = y;
	}

	public void setWidth(float width){
		this.width = width;
	}

	public void setHeight(float height){
		this.height = height;
	}


	public float getLocalX(){
		return localX;
	}

	public float getLocalY(){
		return localY;
	}

	public void setPoly(boolean flag){
		this.poly = flag;
	}

	public boolean getPoly(){
		return this.poly;
	}

	public float[] getCoordinates(){
		return this.coordinates;
	}

	public void setCoordinates(float[] coordinates){
		this.coordinates = coordinates;
	}

	/**
	 * Returns the list of children for this zone
	 * 
	 * @return list of children zones
	 */
	public Vector<Zone> getChildren() {
		return childList;
	}

	/**
	 *  Gets the zone's transformation matrix
	 * @return PMatrix3D - zone's transformation matrix
	 */

	public PMatrix3D getMatrix(){
		return matrix;
	}

	/**
	 *  Sets the zone's transformation matrix
	 * 
	 * @param m - PMatrix3D
	 */
	public void setMatrix(PMatrix3D m){
		matrix = m;
	}

	/**
	 * Reset the transformation matrix
	 */
	public void resetMatrix() {
		this.matrix.reset();
		this.changed = true;
	}

	/**
	 *  Set the minimum distance threshold for the
	 *  horizontal swipe
	 * @param distance
	 */
	public void setHSwipeDist(float xDistance){
		hswipeDist = xDistance;
	}

	/**
	 *  Get the minimum distance threshold for the
	 *  horizontal swipe
	 */
	public float getHSwipeDist(){
		return hswipeDist;
	}

	/**
	 *  Set the minimum distance threshold for the
	 *  vertical swipe
	 * @param distance
	 */
	public void setVSwipeDist(float yDistance){
		vswipeDist = yDistance;
	}

	/**
	 *  Get the minimum distance threshold for the
	 *  vertical swipe
	 */
	public float getVSwipeDist(){
		return vswipeDist;
	}

	/**
	 * Returns the number of touches on the zone
	 * 
	 * @return currentTouches int
	 */
	public int getTouchCount() {
		return currentTouches;
	}

	/**
	 * Returns a list of the current touches on the zone.
	 * 
	 * @return touchList Vector<Touch>
	 */
	public Vector<Touch> getTouches() {
		return touchList;
	}

	/**
	 * Returns a list of the zone's children
	 * 
	 * @return Vector<Zone> childList
	 */
	public Vector<Zone> getChildZones() {
		return childList;
	}

	/**
	 * Sets the friction for the x and y directions. Default frictions are set
	 * to 5000 for both.
	 * 
	 * @param xF
	 *            float - Friction in the x direction
	 * @param yF
	 *            float - Friction in the y direction
	 */
	public void setFriction(float xF, float yF) {
		this.frictionX = xF;
		this.frictionY = yF;
	}

	/**
	 * Sets the flag 'changed' to the value of the boolean given as a parameter.
	 * If a zone's matrix has been changed, the flag should be set true.
	 * 
	 * @param changed
	 *            boolean - flag
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}


	/**
	 * Sets the zone to be able to respond to touch events. Gesture names
	 * include 'DRAG' 'XDRAG' 'YDRAG' 'RNT' 'TAP' 'TapAndHold' 'PINCH' 'XYPINCH'
	 * 'XPINCH' 'YPINCH' 'THROW' 'ROTATE' 'HSWIPE 'VSWIPE'. Gestures enabled
	 * with this method are handled by default.
	 * 
	 * @param gestureName
	 *            String - Gesture Name
	 * @param enabled
	 *            whether to turn the gesture on or off
	 */
	public void setGestureEnabled(String gestureName, boolean enabled) {
		setGestureEnabled(gestureName, enabled, true);
	}

	/**
	 * Sets the zone to be able to respond to touch events. Gesture names
	 * include 'DRAG' 'XDRAG' 'YDRAG' 'RNT' 'TAP' 'TapAndHold' 'PINCH' 'XYPINCH'
	 * 'XPINCH' 'YPINCH' 'THROW' 'ROTATE' 'HSWIPE 'VSWIPE'
	 * 
	 * @param gestureName
	 *            String - Gesture Name
	 * @param enabled
	 *            whether to turn the gesture on or off
	 * @param handled
	 *            true of processing of this gesture should stop after this zone
	 */
	public void setGestureEnabled(String gestureName, boolean enabled, boolean handled) {
		if (gestureName.equalsIgnoreCase("DRAG")) {
			draggable = enabled;
			draggableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("XDRAG")) {
			xDraggable = enabled;
			xDraggableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("YDRAG")) {
			yDraggable = enabled;
			yDraggableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("HSWIPE")) {
			hSwipeable = enabled;
			hSwipeableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("VSWIPE")) {
			vSwipeable = enabled;
			vSwipeableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("RNT")) {
			RNTable = enabled;
			RNTableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("TAP")) {
			tappable = enabled;
			tappableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("TAPANDHOLD")) {
			tapAndHoldable = enabled;
			tapAndHoldableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("PINCH")) {
			pinchable = enabled;
			pinchableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("XYPINCH")) {
			xyPinchable = enabled;
			xyPinchableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("XPINCH")) {
			xPinchable = enabled;
			xPinchableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("YPINCH")) {
			yPinchable = enabled;
			yPinchableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("THROW")) {
			throwable = enabled;
			throwableHandled = handled;
		}
		else if (gestureName.equalsIgnoreCase("ROTATE")) {
			rotatable = enabled;
			rotatableHandled = handled;
		}
		else {
			PApplet.println("Error: Gesture name spelt incorrectly.\n"
					+ "Available gesture names include 'DRAG' 'XDRAG' 'YDRAG' "
					+ "'RNT' 'Tap' 'TapAndHold' 'PINCH' 'XPINCH''YPINCH' "
					+ "'HSWIPE 'VSWIPE' 'THROW' 'ROTATE'");
		}
	}

	/**
	 * Set zone scale limits.
	 * 
	 * @param low
	 *            float - the lower limit of scale gesture (<=1), default is set
	 *            to 0.3
	 * @param high
	 *            float - the upper limit of scale gesture (>=1), default is set
	 *            to 5.0
	 */
	public void setScaleLimits(float low, float high) {
		sclLow = low;
		sclHigh = high;
	}

	/**
	 * Set zone scale sensitivity.
	 * 
	 * @param val
	 *            float - the sensitivity of scale gesture (default=1.0)
	 */
	public void setScaleSensitivity(float val) {
		sclSens = val;
	}

	/**
	 * Sets the radius of the area that will trigger only a translate event in
	 * the RNT gesture. Default is 50.
	 * 
	 * @param radius
	 *            float - the radius of the area
	 */
	public void setTranslateAreaRadius(float radius) {
		this.translateAreaRadius = radius;
	}

	/**
	 * Changes the coordinates and size of the zone.
	 * 
	 * @param xIn
	 *            int - The zone's new x-coordinate.
	 * @param yIn
	 *            int - The zone's new y-coordinate.
	 * @param wIn
	 *            int - The zone's new width.
	 * @param hIn
	 *            int - The zone's new height.
	 */
	public void setXYWH(int xIn, int yIn, int wIn, int hIn) {
		x = xIn;
		y = yIn;
		width = wIn;
		height = hIn;
	}

	/**
	 * Attach a zone to another zone. The attachment is a child/parent
	 * relationship. Default gesture actions will alter the child if the parent
	 * is manipulated, but not the other way around.
	 * 
	 * @param parentZone
	 *            Zone - the parent zone.
	 */

	public void attachTo(Zone parentZone) {
		if (parentZone != this) {
			parentZone.childZones = true;
			parentZone.addChild(this);
		}
	}

	/**
	 * Internal Method, attaches a child zone to this zone
	 * 
	 * @param child
	 *            Zone - Child zone for attachment
	 */
	protected void addChild(Zone child) {
		this.childList.add(child);
	}

	/**
	 * Assign a zone to a group. A new group can be created here or an existing
	 * group can be used.
	 * 
	 * @param group
	 *            String - the name of the group. Can be a new group or
	 *            existing.
	 */

	public void assignToGroup(String group) {
		groupZones = true;
		this.group = group;
	}

	protected void setLastHSwipeCursor(long sID){
		hSwipeCursor = sID;
	}

	protected void setLastVSwipeCursor(long sID){
		vSwipeCursor = sID;
	}

	protected long getLastHSwipeCursor(){
		return hSwipeCursor;
	}

	protected long getLastVSwipeCursor(){
		return vSwipeCursor;
	}

	/**
	 * Adds this zone to the zone list. When a student creates a zone, they must
	 * add it to this list.
	 * 
	 */
	public void add() {
		synchronized(zoneList){
			zoneList.add(this);
		}

		/*synchronized(picker){
			picker.add(this);
		}*/
	}



	/**
	 * Removes this zone from the zone list.
	 *  
	 */
	public void removeZone() {
		synchronized(zoneList){
			zoneList.remove(this);
		}

		/*synchronized(picker){
			picker.remove(this);
		}*/
	}

	/**
	 * Pull this zone to the top layer.
	 */
	public void pullToTop(){
		synchronized (zoneList) {
			int i = zoneList.indexOf(this);
			if (i > 0) {
				zoneList.remove(i);
				zoneList.add(this);
			}
		}

		/*synchronized (picker.zoneList) {
			int i = picker.zoneList.indexOf(this);
			if (i > 0) {
				picker.zoneList.remove(i);
				picker.zoneList.add(this);
			}
		}*/
	}
	/**
	 * Push this zone to the bottom layer.
	 */
	public void pushToBottom(){

		synchronized (zoneList) {
			int i = zoneList.indexOf(this);
			if (i > 0) {
				zoneList.remove(i);
				zoneList.add(0, this);
			}
		}


		/*synchronized (picker.zoneList) {
			int i = picker.zoneList.indexOf(this);
			if (i > 0) {
				picker.zoneList.remove(i);
				picker.zoneList.add(0, this);
			}
		}*/
	}

	/**
	 * Activate a zone so that it responds to touch. A zone is active by
	 * default.
	 * 
	 * @param active
	 *            boolean to set active state. true for set active, false for
	 *            set not active
	 */
	public void setActive(boolean active) {
		this.active = active;
		reset();
	}

	/**
	 * Activates the children of a zone so that it responds to touch. A zone is
	 * active by default.
	 * 
	 * @param active
	 *            boolean to set active state. true for set active, false for
	 *            set not active
	 */
	public void setChildrenActive(boolean active) {
		for (Zone zone : childList) {
			if (zone != null) {
				zone.active = active;
				zone.reset();
			}
		}
	}

	/**
	 * Get the zone x-coordinate. Upper left corner for rectangle.
	 * 
	 * @return x int representing the upper left x-coordinate of the zone.
	 */
	public float getX() {
		return this.x;
	}

	public float getXTimesMatrix(){
		return (x * this.matrix.m00 + y * this.matrix.m01 + 0 * this.matrix.m02 + this.matrix.m03);//(this.x + this.matrix.m03);

	}

	/**
	 * Get the zone y-coordinate. Upper left corner for rectangle.
	 * 
	 * @return y int representing the upper left y-coordinate of the zone.
	 */
	public float getY() {
		return this.y;
	}

	public float getYTimesMatrix(){
		return (x * this.matrix.m10 + y * this.matrix.m11 + 0 * this.matrix.m12 + this.matrix.m13);//(this.y + this.matrix.m13);

	}

	/**
	 * Get the zone's original width.
	 * 
	 * @return width int representing the width of the zone.
	 */
	public float getWidth() {
		return this.width;
	}

	/**
	 * Get the zone's original height.
	 * 
	 * @return height int representing the height of the zone.
	 * 
	 */
	public float getHeight() {
		return this.height;
	}


	/**
	 * Get the zone's scale factor.
	 * 
	 * @return scl float representing the scale factor of the zone.
	 */
	public float getScale() {
		return this.scl;
	}

	/**
	 * Get the zone's scale X factor.
	 * 
	 * @return sclX float representing the scale factor of the zone.
	 */
	public float getXScale() {
		return this.sclX;
	}

	/**
	 * Get the zone's scale Y factor.
	 * 
	 * @return sclY float representing the scale factor of the zone.
	 */
	public float getYScale() {
		return this.sclY;
	}

	/**
	 * Get an active 1-finger gesture's x-axis translation.
	 * 
	 * @return x float representing the x-axis translation
	 * 
	 */
	public float getSwipeXtranslation() {
		int x = 0;
		if (this.getNumIds() > 0) {
			long j = this.getId(0);
			x = TouchClient.tuioClient.getTuioCursor(j).getPath().lastElement()
					.getScreenX(applet.width)
					- TouchClient.tuioClient.getTuioCursor(j).getPath().firstElement()
					.getScreenX(applet.width);
		}
		return x;
	}

	/**
	 * Get an active 1-finger gesture's y-axis translation.
	 * 
	 * @return y float representing the y-axis translation
	 * 
	 */
	public float getSwipeYtranslation() {
		int y = 0;
		if (this.getNumIds() > 0) {
			long j = this.getId(0);
			y = TouchClient.tuioClient.getTuioCursor(j).getPath().lastElement()
					.getScreenY(applet.height)
					- TouchClient.tuioClient.getTuioCursor(j).getPath().firstElement()
					.getScreenY(applet.height);
		}
		return y;
	}

	/**
	 * Determine if the zone is being touched.
	 * 
	 * @return boolean True if zone is being touched
	 */
	public boolean isPressed() {
		if (this.getNumIds() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Determine the toggle state of the zone. If a zone has been touched an odd
	 * number of times, then its toggle state is true. Otherwise it is false.
	 * 
	 * @return boolean True if toggle is on.
	 */
	public boolean isToggleOn() {
		return toggle;
	}

	/**
	 * Determine if zone is active.
	 * 
	 * @return boolean True if active
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Sets the flag 'drawBorder' to the boolean value. If false, then the
	 * border of the zone will not be drawn.
	 * 
	 * @param drawBorder
	 *            Boolean
	 */
	public void setDrawBorder(boolean drawBorder) {
		this.drawBorder = drawBorder;

	}

	public void setPropogateTouchEvents(boolean propogateTouchEvents) {
		this.propogateTouchEvents = propogateTouchEvents;
	}

	/**
	 * Sets the zone border colour
	 * @param colour
	 */
	public void setBorderColour(int colour){
		borderColour = colour;
	}

	/**
	 * Sets the border stroke weight when drawing the border
	 * @param weight
	 */
	public void setBorderWeight(int weight){
		borderWeight = weight;
	}
	/**
	 * Pushes the current Matrix. Applies the zone's matrix. Draws the border of
	 * the zone if flag is 'drawBorder' is set to true
	 */
	public void preDraw() {
		applet.applyMatrix(this.matrix);

		if (drawBorder && !poly){
			//applet.noFill();
			applet.stroke(borderColour);
			applet.strokeWeight(borderWeight);
			//applet.rect(this.getX()-borderWeight/2, this.getY()-borderWeight/2, this.width, this.height);
			applet.line(this.getX(), this.getY(), this.getX(), this.height + this.getY());
			applet.line(this.getX() + this.width, this.getY(), this.getX() + this.width, this.height + this.getY());
			applet.line(this.getX()-borderWeight/2, this.getY(), this.getX() + this.width + borderWeight/2, this.getY());
			applet.line(this.getX() - borderWeight/2, this.height + this.getY(), this.getX() + this.width + borderWeight/2, this.height + this.getY());


			//applet.stroke(0);
			//applet.rect(this.getX()+1, this.getY()+1, this.width-3, this.height-2);
			applet.strokeWeight(1);
		}
	}

	/**
	 * The very last thing that gets called when you call the drawZone method.
	 */

	public void postDraw(){

	}

	/**
	 * Gets overrided by the zones that extend this class. The student may also
	 * override this method in his/her Processing sketch.
	 */
	public void drawZone() {
	}

	/**
	 * Resets the zone.
	 * 
	 * @param zone
	 *            Zone
	 */
	public void reset() {
		releaseTime = applet.millis();
		currentTouchIDs.clear();

		//localX = 0;
		//localY = 0;
		//lastLocalX = 0;
		//lastLocalY = 0;
		touchList.clear();
		tuioCursorList.clear();
		currentTouches = 0;

		pinching = false;
		rotating = false;
		RNTing = false;
		angle = 0;
		lastGlobalX = 0;
		lastGlobalY = 0;
		lastXMovement = 0;
		lastYMovement = 0;
		lastSclDist = 1;
		lastSclXDist = 1;
		lastSclYDist = 1;
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
		//if (this.changed) {
			this.inverse.reset();
			this.inverse.apply(this.matrix);
			this.inverse.invert();
			this.changed = false;
		//}
		PVector world = new PVector();
		PVector mouse = new PVector(x, y);
		this.inverse.mult(mouse, world);

		if(this.poly){
			float minX = applet.getWidth();
			float minY = applet.getHeight();
			float maxX = -1;
			float maxY = -1;
			int i2 = 0;
			Vector<Float> vertx = new Vector<Float>();
			Vector<Float> verty = new Vector<Float>();
			for(float f: coordinates){
				if(i2 % 2 == 0){
					if(f < minX){
						minX = f;
					}
					if(f > maxX){
						maxX = f;
					}
					vertx.add(f);
				} else {
					if(f < minY){
						minY = f;
					}
					if(f > maxY){
						maxY = f;
					}
					verty.add(f);
				}
				i2++;
			}

			if (world.x < minX || world.x > maxX || world.y < minY || world.y > maxY) {
				return false;
			} else {

				int nvert = (coordinates.length)/2;
				//point in polygon test from
				// http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
				int i, j;
				boolean c = false;
				for (i = 0, j = nvert-1; i < nvert; j = i++) {
					if ( ((verty.get(i)>world.y) != (verty.get(j)>world.y)) &&
							(world.x < (vertx.get(j)-vertx.get(i)) * (world.y-verty.get(i)) / (verty.get(j)-verty.get(i)) + vertx.get(i)) ){
						if(c){
							c = false;
						} else {
							c = true;
						}
					}
				}
				return c;


			}

		} else if ((world.x > (this.getX() - xThresh)) && (world.x < (this.getX() + this.width + xThresh))
				&& (world.y > (this.getY() - yThresh)) && (world.y < (this.getY() + this.height + yThresh))) {
			this.localX = world.x;
			this.localY = world.y;
			return true;
		}
		return false;

	}

	/**
	 * The method which the user of this framework will override. By default,
	 * the touch does propagate to all the zones under the touch. When
	 * overriding this function in the zone's instantiation, return true if you
	 * do not want the touch to continue to propagate.
	 * 
	 * @param t
	 *            Touch - the "finger"
	 * @return true if the touch was handled
	 */
	public boolean addTouch(Touch t) {
		return !propogateTouchEvents;
	}

	/**
	 * The method which the user of this framework will override. By default,
	 * the touch does propagate to all the zones under the touch. When
	 * overriding this function in the zone's instantiation, return true if you
	 * do not want the touch to continue to propagate.
	 * 
	 * @param t
	 *            Touch - the "finger"
	 * @return true if the touch was handled
	 */
	public boolean updateTouch(Touch t) {
		return !propogateTouchEvents;
	}

	/**
	 * The method which the user of this framework will override. By default,
	 * the touch does propagate to all the zones under the touch. When
	 * overriding this function in the zone's instantiation, return true if you
	 * do not want the touch to continue to propagate.
	 * 
	 * @param t
	 *            Touch - the "finger"
	 * @return true if the touch was handled
	 */
	public boolean removeTouch(Touch t) {
		return !propogateTouchEvents;
	}

	/**
	 * Allows the student to scale the zone in their Processing sketch.
	 * 
	 * @param sx
	 *            float - Scale X Amount
	 * @param sy
	 *            float - Scale Y Amount
	 */
	public void scale(float sx, float sy) {
		this.matrix.scale(sx, sy);
		this.changed = true;
	}

	/**
	 * Allows the student to rotate the zone in their Processing sketch.
	 * 
	 * @param angle
	 *            float - Rotate angle in radians
	 */
	public void rotate(float angle) {
		this.matrix.translate(this.getX() + this.getWidth()/2, this.getY() + this.getHeight()/2);
		this.matrix.rotate(angle);
		this.matrix.translate(-(this.getX() + this.getWidth()/2), -(this.getY() + this.getHeight()/2));
		this.changed = true;
	}

	/**
	 * Allows the student to translate the zone in their Processing sketch.
	 * 
	 * @param x
	 *            float - the amount of translation in the x-direction
	 * @param y
	 *            float - the amount of translation in the y-direction
	 */
	public void translate(float x, float y) {
		this.matrix.translate(x, y);
		this.changed = true;
	}

	/**
	 * Translates the zone, its group, and its children if it is set to
	 * draggable, xDraggable or yDraggable
	 * 
	 * @param e
	 *            DragEvent - The DragEvent which encapsulates the event's
	 *            information
	 */
	public void dragEvent(DragEvent e) {
		// TUIOCursor.getXSpeed() and getYSpeed() is multiplied by the width and
		// height since the
		// speed from the TUIO client is normalized (between 0 and 1)
		float vx = (this.vx + TouchClient.tuioClient.getTuioCursor(e.getCursors()[0]).getXSpeed()
				* applet.width) / 2;
		float vy = (this.vy + TouchClient.tuioClient.getTuioCursor(e.getCursors()[0]).getYSpeed()
				* applet.height) / 2;
		if (this.draggable || this.RNTable) {
			// TODO: need to handle pulling to top better
			this.pullToTop();

			this.vx = vx;
			this.vy = vy;
			this.matrix.translate(e.getXDistance(), e.getYDistance());
			this.changed = true;
			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.dragEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.draggable || zone.RNTable) {
							zone.vx = (zone.vx + TouchClient.tuioClient
									.getTuioCursor(e.getCursors()[0]).getXSpeed() * applet.width) / 2;
							zone.vy = (zone.vy + TouchClient.tuioClient
									.getTuioCursor(e.getCursors()[0]).getYSpeed() * applet.height) / 2;
							zone.matrix.translate(e.getXDistance(), e.getYDistance());
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(draggableHandled || RNTableHandled);
		}
		else {
			if (this.xDraggable) {
				this.vx = vx;
				this.matrix.translate(e.getXDistance(), 0);
				this.changed = true;
				// Change Children
				if(childZones){
					for (Zone zone : childList) {
						zone.dragEvent(e);
					}
				}
				// Change Group
				if(groupZones){
					for (Zone zone : zoneList) {
						if (zone != this && zone.group != null
								&& zone.group.equalsIgnoreCase(this.group)
								&& !this.childList.contains(zone)) {
							if (zone.xDraggable) {
								zone.vx = (zone.vx + TouchClient.tuioClient.getTuioCursor(
										e.getCursors()[0]).getXSpeed()
										* applet.width) / 2;
								zone.matrix.translate(e.getXDistance(), 0);
								zone.changed = true;
							}
						}
					}
				}
				e.setHandled(xDraggableHandled);
			}

			if (this.yDraggable) {
				this.vy = vy;
				this.matrix.translate(0, e.getYDistance());
				this.changed = true;
				// Change Children
				if(childZones){
					for (Zone zone : childList) {
						zone.dragEvent(e);
					}
				}
				// Change Group
				if(groupZones){
					for (Zone zone : zoneList) {
						if (zone != this && zone.group != null
								&& zone.group.equalsIgnoreCase(this.group)
								&& !this.childList.contains(zone)) {
							if (zone.yDraggable) {
								zone.vy = (zone.vy + TouchClient.tuioClient.getTuioCursor(
										e.getCursors()[0]).getYSpeed()
										* applet.height) / 2;
								zone.matrix.translate(0, e.getYDistance());
								zone.changed = true;
							}
						}
					}
				}
				e.setHandled(yDraggableHandled);
			}
		}
	}

	/**
	 * Scales the zone if it is set to pinchable, xPinchable or yPinchable
	 * 
	 * @param e
	 *            PinchEvent - The PinchEvent which encapsulates the event's
	 *            information
	 */
	public void pinchEvent(PinchEvent e) {
		//		this.matrix.translate(this.localX, this.localY);

		if (this.xyPinchable && this.currentTouches == 4) {
			this.matrix.translate(e.getXPofScale(), e.getYPofScale());
			this.matrix.scale(e.getXScaleAmount(), e.getYScaleAmount());
			this.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
			this.changed = true;
			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.pinchEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.xyPinchable && zone.currentTouches == 4) {
							zone.matrix.translate(e.getXPofScale(), e.getYPofScale());
							zone.matrix.scale(e.getXScaleAmount(), e.getYScaleAmount());
							zone.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(xyPinchableHandled);
		}
		else if (this.pinchable) {
			this.matrix.translate(e.getXPofScale(), e.getYPofScale());
			this.matrix.scale(e.getScaleAmount());
			this.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
			this.changed = true;
			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.pinchEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.pinchable) {
							zone.matrix.translate(e.getXPofScale(), e.getYPofScale());
							zone.matrix.scale(e.getScaleAmount());
							zone.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
	
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(pinchableHandled);
		}
		else {
			if (this.xPinchable) {
				this.matrix.translate(e.getXPofScale(), e.getYPofScale());
				this.matrix.scale(e.getXScaleAmount(), 1);
				this.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
				this.changed = true;
				// Change Children
				if(childZones){
					for (Zone zone : childList) {
						zone.pinchEvent(e);
					}
				}
				// Change Group
				if(groupZones){
					for (Zone zone : zoneList) {
						if (zone != this && zone.group != null
								&& zone.group.equalsIgnoreCase(this.group)
								&& !this.childList.contains(zone)) {
							if (zone.xPinchable) {
								zone.matrix.translate(e.getXPofScale(), e.getYPofScale());
								zone.matrix.scale(e.getXScaleAmount(), 1);
								zone.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
								zone.changed = true;
							}
						}
					}
				}
				e.setHandled(xPinchableHandled);
			}
			if (this.yPinchable) {
				this.matrix.translate(e.getXPofScale(), e.getYPofScale());
				this.matrix.scale(1, e.getYScaleAmount());
				this.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
				this.changed = true;

				// Change Children
				if(childZones){
					for (Zone zone : childList) {
						zone.pinchEvent(e);
					}
				}
				// Change Group
				if(groupZones){
					for (Zone zone : zoneList) {
						if (zone != this && zone.group != null
								&& zone.group.equalsIgnoreCase(this.group)
								&& !this.childList.contains(zone)) {
							if (zone.yPinchable) {
								zone.matrix.translate(e.getXPofScale(), e.getYPofScale());
								zone.matrix.scale(1, e.getYScaleAmount());
								zone.matrix.translate(-e.getXPofScale(), -e.getYPofScale());
								zone.changed = true;
							}
						}
					}
				}
				e.setHandled(yPinchableHandled);
			}
		}
		//		this.matrix.translate(-this.localX, -this.localY);

	}

	/**
	 * Rotates the zone if it is set to rotatable
	 * 
	 * @param e
	 *            RotateEvent - The RotateEvent which encapsulates the event's
	 *            information
	 */
	public void rotateEvent(RotateEvent e) {
		if (rotatable) {
			TuioCursor cursor = TouchClient.getTouch(e.getCursors()[0]);
			this.matrix.translate(e.getXPofRotation(), e.getYPofRotation());
			this.matrix.rotate(e.getAngle());
			this.matrix.translate(-e.getXPofRotation(), -e.getYPofRotation());
			this.changed = true;

			if (rotating) {
				TuioCursor cursor2 = TouchClient.getTouch(e.getCursors()[1]);
				this.angle = cursor.getAngle(cursor2);
			}
			else if (RNTing) {
				lastGlobalX = e.getXPofRotation();
				lastGlobalY = e.getYPofRotation();
				this.angle = GestureHandler.getAngleABC(new Point.Float(localX, localY),
						new Point.Float(x + width / 2, y + height / 2), new Point.Float(
								lastGlobalX, lastGlobalY));

			}

			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.rotateEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.rotatable) {
							zone.matrix.translate(e.getXPofRotation(), e.getYPofRotation());
							zone.matrix.rotate(e.getAngle());
							zone.matrix.translate(-e.getXPofRotation(), -e.getYPofRotation());
							zone.changed = true;
	
							if (zone.rotating) {
								TuioCursor cursor2 = TouchClient.getTouch(e.getCursors()[1]);
								zone.angle = cursor.getAngle(cursor2);
							}
							else if (zone.RNTing) {
								zone.lastGlobalX = e.getXPofRotation();
								zone.lastGlobalY = e.getYPofRotation();
								zone.angle = GestureHandler.getAngleABC(
										new Point.Float(localX, localY), new Point.Float(x + width / 2,
												y + height / 2), new Point.Float(lastGlobalX,
														lastGlobalY));
	
							}
						}
					}
				}
			}
			e.setHandled(rotatableHandled);
		}
	}

	/**
	 * Horizontal Swipe Event's default action is translating the zone in the
	 * x-direction by half of the horizontal swipe's distance.
	 * 
	 * @param e
	 *            HSwipeEvent - The Horizontal swipe event
	 */
	public void hSwipeEvent(HSwipeEvent e) {
		if (this.hSwipeable) {
			this.vx = (this.vx + TouchClient.getTouch(e.getCursors()[0]).getXSpeed() * applet.width) / 2;
			this.matrix.translate(getSwipeXtranslation() / 2, 0);
			this.changed = true;

			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.hSwipeEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.hSwipeable) {
							zone.vx = (zone.vx + TouchClient.getTouch(e.getCursors()[0]).getXSpeed()
									* applet.width) / 2;
							zone.matrix.translate(getSwipeXtranslation() / 2, 0);
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(hSwipeableHandled);
		}
	}

	/**
	 * Vertical Swipe Event's default action is translating the zone in the
	 * y-direction by half of the vertical swipe's distance.
	 * 
	 * @param e
	 *            VSwipeEvent - The Vertical swipe event
	 */
	public void vSwipeEvent(VSwipeEvent e) {
		if (this.vSwipeable) {
			this.vy = (this.vy + TouchClient.getTouch(e.getCursors()[0]).getYSpeed()
					* applet.height) / 2;
			this.matrix.translate(0, getSwipeYtranslation() / 2);
			this.changed = true;

			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.vSwipeEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.vSwipeable) {
							zone.vy = (zone.vy + TouchClient.getTouch(e.getCursors()[0]).getYSpeed()
									* applet.height) / 2;
							zone.matrix.translate(0, getSwipeYtranslation() / 2);
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(vSwipeableHandled);
		}
	}

	/**
	 * Tap Event's default action is printing the line
	 * "You (double) tapped a zone".
	 * 
	 * @param e
	 *            TapEvent - The tap event
	 */
	public void tapEvent(TapEvent e) {
		if (tappable) {
			if(TouchClient.debugMode) {
				if (e.getNumTaps() == 2) {
					PApplet.println("You double tapped a zone!");
				}
				else {
					PApplet.println("You tapped a zone!");
				}
			}

			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.tapEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.tappable) {
							;
						}
					}
				}
			}
			e.setHandled(tappableHandled);
		}
	}

	/**
	 * Tap and Hold Event's default action is rotating the zone 90 degrees.
	 * 
	 * @param e
	 *            TapEvent - The tap event
	 */
	public void tapAndHoldEvent(TapAndHoldEvent e) {
		if (this.tapAndHoldable) {
			TuioCursor tcur = TouchClient.getTouch(e.getCursors()[0]);
			int x = tcur.getScreenX(applet.width);
			int y = tcur.getScreenY(applet.height);
			this.matrix.translate(x, y);
			this.matrix.rotate((float) (3.14 / 2));
			this.matrix.translate(-x, -y);
			this.changed = true;

			// Change Children
			if(childZones){
				for (Zone zone : childList) {
					zone.tapAndHoldEvent(e);
				}
			}
			// Change Group
			if(groupZones){
				for (Zone zone : zoneList) {
					if (zone != this && zone.group != null && zone.group.equalsIgnoreCase(this.group)
							&& !this.childList.contains(zone)) {
						if (zone.tapAndHoldable) {
							zone.matrix.translate(x, y);
							zone.matrix.rotate((float) (3.14 / 2));
							zone.matrix.translate(-x, -y);
							zone.changed = true;
						}
					}
				}
			}
			e.setHandled(tapAndHoldableHandled);
		}
	}

	public boolean addId(Long e) {
		return currentTouchIDs.add(e);
	}

	public boolean removeId(Long e) {
		return currentTouchIDs.remove(e);
	}

	public Long removeId(int index) {
		try {
			return currentTouchIDs.remove(index);
		}
		catch (IndexOutOfBoundsException e) {
			return -999L;
		}
	}

	public Long getId(int index) {
		try {
			return currentTouchIDs.get(index);
		}
		catch (IndexOutOfBoundsException e) {
			return -999L;
		}
	}

	public int getNumIds() {
		return currentTouchIDs.size();
	}

	public boolean containsId(Long id) {
		return currentTouchIDs.contains(id);
	}

	/*public void uponAddingTCursor(TuioCursor tcur, Touch touch, int x, int y){
		if(getNumIds() == 0){
			lastLocalX = x;//zone.localX;
			lastLocalY = y;//zone.localY;
			localX = x;
			localY = y;
		}

		addId(tcur.getSessionID());

		touchList.add(touch);
		tuioCursorList.add(tcur);
		currentTouches++;
	}

	public void uponRemovingTCursor(TuioCursor tcur, Touch touch){
		removeId(tcur.getSessionID());

		touchList.remove(touch);
		tuioCursorList.remove(tcur);
		currentTouches--;
	}*/
}
