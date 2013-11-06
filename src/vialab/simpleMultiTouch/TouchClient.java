/*
 * Modified version of the TUIO processing library - part of the reacTIVision
 * project http://reactivision.sourceforge.net/
 * 
 * Copyright (c) 2005-2009 Martin Kaltenbrunner <mkalten@iua.upf.edu>
 * 
 * This version Copyright (c) 2011 Erik Paluka, Christopher Collins - University
 * of Ontario Institute of Technology Mark Hancock - University of Waterloo
 * contact: christopher.collins@uoit.ca
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import vialab.mouseToTUIO.MouseToTUIO;
import vialab.simpleMultiTouch.events.TapEvent;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioPoint;

/**
 * The TUIO Processing client.
 * 
 * University of Ontario Institute of Technology. Summer Research Assistant with
 * Dr. Christopher Collins (Summer 2011) collaborating with Dr. Mark Hancock.
 * <P>
 * 
 * @author Erik Paluka
 * @date Summer, 2011
 * @version 1.0
 */
public class TouchClient {
	/** Processing PApplet */
	static PApplet parent;
	/** Gesture Handler */
	static GestureHandler handler;

	/** Tuio Client that listens for Tuio Messages via port 3333 UDP */
	static TuioClient tuioClient;

	/** The main zone list */
	protected static Vector<Zone> zoneList = new Vector<Zone>();

	/** Flag for drawing touch points */
	static boolean drawTouchPoints = true;
	static HashMap<TuioCursor, int[]> colours = new HashMap<TuioCursor, int[]>();
	static int size;

	/** Flag for applying the zone's matrix to the touch input **/
	static boolean applyZoneMatrix = false;
	
	static boolean debugMode = false;

	public static ZonePicker picker = new ZonePicker();

	/**
	 * Default Constructor. Default port is 3333 for TUIO
	 * 
	 * @param parent
	 *            PApplet - The Processing PApplet
	 */
	public TouchClient(PApplet parent) {
		this(parent, 3333);
	}

	/**
	 * Constructor. Allows you to set the port to connect to.
	 * 
	 * @param parent
	 *            PApplet - The Processing PApplet
	 * @param port
	 *            int - The port to connect to.
	 */
	public TouchClient(PApplet parent, int port) {
		this(parent, port, true, false);
	}

	public TouchClient(PApplet parent, boolean emulateTouches) {
		this(parent, emulateTouches, false);
	}

	public TouchClient(PApplet parent, boolean emulateTouches, boolean fullscreen) {
		this(parent, 3333, emulateTouches, fullscreen);
	}

	public TouchClient(PApplet parent, int port, boolean emulateTouches, boolean fullscreen) {
		size = parent.getHeight()/20;
		
		parent.setLayout(new BorderLayout());

		if (emulateTouches) {
			parent.add(new MouseToTUIO(parent.getWidth(), parent.getHeight()));
		}

		parent.frame.removeNotify();
		if (fullscreen) {
			parent.frame.setUndecorated(true);
			parent.frame.setIgnoreRepaint(true);
			parent.frame.setExtendedState(Frame.MAXIMIZED_BOTH);

			GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice displayDevice = environment.getDefaultScreenDevice();

			DisplayMode mode = displayDevice.getDisplayMode();
			Rectangle fullScreenRect = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
			parent.frame.setBounds(fullScreenRect);
			parent.frame.setVisible(true);

			// the following is exclusive mode
			//	        displayDevice.setFullScreenWindow(parent.frame);
			//
			//			Rectangle fullScreenRect = parent.frame.getBounds();

			parent.frame.setBounds(fullScreenRect);
			parent.setBounds((fullScreenRect.width - parent.width) / 2,
					(fullScreenRect.height - parent.height) / 2, parent.width, parent.height);
		}
		parent.frame.addNotify();
		parent.frame.toFront();

		TouchClient.parent = parent;
		parent.registerDispose(this);
		parent.registerDraw(this);
		parent.registerPre(this);
		handler = new GestureHandler();

		tuioClient = new TuioClient(port);

		SimpleTuioListener listener = new SimpleTuioListener(handler, picker);
		tuioClient.addTuioListener(listener);
		//		tuioClient.addTuioListener(this);
		tuioClient.connect();
	}

	/**
	 * Set the debug mode on or off
	 * 
	 * @param debug
	 */
	public void setDebugMode(boolean debug){
		TouchClient.debugMode = debug;
	}
	/**
	 * Returns the list of zones.
	 * 
	 * @return zoneList
	 */
	public Vector<Zone> getZones() {
		return zoneList;
	}
	
	/**
	 * 	Sets the size of the touch points that get drawn. (int)
	 * @param size - int - touch points size for drawing
	 */
	public void setDrawTouchPointsSize(int size){
		TouchClient.size = size;
	}
	/**
	 * Sets the flag for drawing touch points in the PApplet. Draws the touch
	 * points if flag is set to true.
	 * 
	 * @param drawTouchPoints
	 *            boolean - flag
	 */
	public void setDrawTouchPoints(boolean drawTouchPoints) {
		TouchClient.drawTouchPoints = drawTouchPoints;
	}

	/**
	 * Draws the touch points in the PApplet if flag is set to true.
	 */
	public static void drawTouchPoints() {
		Vector<TuioCursor> curs = tuioClient.getTuioCursors();
		parent.strokeWeight(1);
		parent.noFill();
		
		if (curs.size() > 0) {
			for (int i = 0; i < curs.size(); i++) {
				
				//parent.stroke(255);
				if(colours.containsKey(curs.get(i))){
					parent.fill(colours.get(curs.get(i))[0], colours.get(curs.get(i))[1], colours.get(curs.get(i))[2]);
					parent.stroke(colours.get(curs.get(i))[0], colours.get(curs.get(i))[1], colours.get(curs.get(i))[2]);
				}
				parent.ellipse(curs.get(i).getScreenX(TouchClient.parent.width), curs.get(i)
						.getScreenY(parent.height), size, size);
				/*parent.stroke(0);
				parent.ellipse(curs.get(i).getScreenX(parent.width),
						curs.get(i).getScreenY(parent.height), 22, 22);*/
				Vector<TuioPoint> path = curs.get(i).getPath();
				if (path.size() > 1) {
					for (int j = 1; j < path.size(); j++) {

						parent.stroke(255);
						parent.line(path.get(j).getScreenX(parent.width) - 0.5f, path.get(j)
								.getScreenY(parent.height) - 0.5f,
								path.get(j - 1).getScreenX(parent.width) - 0.5f, path.get(j - 1)
								.getScreenY(parent.height) - 0.5f);
						parent.ellipse(path.get(j).getScreenX(parent.width), path.get(j)
								.getScreenY(parent.height), 5, 5);
						parent.stroke(0);
						parent.line(path.get(j).getScreenX(parent.width) + 0.5f, path.get(j)
								.getScreenY(parent.height) + 0.5f,
								path.get(j - 1).getScreenX(parent.width) + 0.5f, path.get(j - 1)
								.getScreenY(parent.height) + 0.5f);
						parent.ellipse(path.get(j).getScreenX(parent.width), path.get(j)
								.getScreenY(parent.height), 7, 7);
					}
				}
			}
		}
	}

	/**
	 * Adds a zone to the zone list. When a student creates a zone, they must
	 * add it to this list.
	 * 
	 * @param zone
	 *            Zone - The zone to add to the list.
	 */
	public void addZone(Zone zone) {
		synchronized(zoneList){
			zoneList.add(zone);
		}
		
		/*synchronized(picker){
			picker.add(zone);
		}*/
	}



	/**
	 * Removes a zone from the zone list.
	 * 
	 * @param zone
	 *            Zone - The zone to remove from the list.
	 */
	public void removeZone(Zone zone) {
		synchronized(zoneList){
			zoneList.remove(zone);
		}
		
		/*synchronized(picker){
			picker.remove(zone);
		}*/
	}

	/**
	 * Pull a zone to the top layer.
	 */
	public void pullToTop(Zone zone){
		synchronized (zoneList) {
			int i = zoneList.indexOf(zone);
			if (i > 0) {
				zoneList.remove(i);
				zoneList.add(zone);
			}
		}

		synchronized (picker.zoneList) {
			int i = picker.zoneList.indexOf(zone);
			if (i > 0) {
				picker.zoneList.remove(i);
				picker.zoneList.add(zone);
			}
		}
	}
	/**
	 * Push a zone to the bottom layer.
	 */
	public void pushToBottom(Zone zone){

		synchronized (zoneList) {
			int i = zoneList.indexOf(zone);
			if (i > 0) {
				zoneList.remove(i);
				zoneList.add(0, zone);
			}
		}


		synchronized (picker.zoneList) {
			int i = picker.zoneList.indexOf(zone);
			if (i > 0) {
				picker.zoneList.remove(i);
				picker.zoneList.add(0, zone);
			}
		}
	}

	/**
	 * Returns the TouchClient's PApplet
	 * 
	 * @return parent
	 * 				Parent - The TouchClient's PApplet
	 */
	public PApplet getParent(){
		return TouchClient.parent;
	}

	/**
	 * 	Apply the zone's matrix to the touch input
	 * @param flag - boolean
	 */
	public void applyZonesMatrix(boolean flag){
		TouchClient.applyZoneMatrix = flag;
	}



	/**
	 * Performs the drawing of the zones in order. Zone on top-most layer gets
	 * drawn last. Goes through the list on zones, pushes the current
	 * transformation matrix, applies the zone's matrix, draws the zone, pops
	 * the matrix, and when at the end of the list, it draws the touch points.
	 */
	public synchronized void draw() {
		synchronized (zoneList) {
			for (Zone zone : zoneList) {
				if (zone != null && zone.active) {
					TouchClient.parent.pushMatrix();
					zone.preDraw();
					zone.drawZone();
					zone.postDraw();
					TouchClient.parent.popMatrix();
				}
			}
		}

		if (drawTouchPoints) {
			drawTouchPoints();
		}
	}

	/**
	 * Returns a vector containing all the current TuioObjects.
	 * 
	 * @return Vector<TuioObject>
	 */
	public Vector<TuioObject> getTuioObjects() {
		return tuioClient.getTuioObjects();
	}

	/**
	 * Returns a vector containing all the current Touches(TuioCursors).
	 * 
	 * @return Vector<TuioCursor>
	 */
	public Touch[] getTouches() {
		Vector<TuioCursor> cursors = tuioClient.getTuioCursors();
		Touch[] touches = new Touch[cursors.size()];
		int i = 0;
		for (TuioCursor c : cursors) {
			touches[i++] = new Touch(c);
		}
		return touches;
	}

	/**
	 * Returns a the TuioObject associated with the session ID
	 * 
	 * @param s_id
	 *            long - Session ID of the TuioObject
	 * @return TuioObject
	 */
	public TuioObject getTuioObject(long s_id) {
		return tuioClient.getTuioObject(s_id);
	}

	/**
	 * Returns the Touch(TuioCursor) associated with the session ID
	 * 
	 * @param s_id
	 *            long - Session ID of the Touch(TuioCursor)
	 * @return TuioCursor
	 */
	public static Touch getTouch(long s_id) {
		return new Touch(tuioClient.getTuioCursor(s_id));
	}

	/**
	 * Returns the number of current Touches (TuioCursors)
	 * 
	 * @return number of current touches
	 */
	public int getTouchCount() {
		return tuioClient.getTuioCursors().size();
	}

	/**
	 * Manipulates the zone's position if it is throw-able after it has been
	 * released by a finger (cursor) Done before each call to draw. Uses the
	 * zone's x and y friction values.
	 */
	public void pre() {
		// TODO: I really don't think we should be doing anything with zones in TouchClient
		// This seems a lot more like event handling than drawing...
		/*for (int i = 0; i < zoneList.size(); i++) {
			Zone zone = zoneList.get(i);
			if (zone != null && zone.active) {
				if (zone.throwable && zone.getNumIds() > 0 && (zone.vx != 0 || zone.vy != 0)) {
					float vratio = zone.vy / zone.vx;
					if (zone.vx > parent.width) {
						zone.vx = parent.width;
						zone.vy = (int) (vratio * parent.width);
					}// governor
					if (zone.vy > parent.height) {
						zone.vy = parent.height;
						zone.vx = (int) (parent.width / vratio);
					}
					float ax = -zone.frictionX, ay = -zone.frictionY;// deceleration
					if (zone.vy == 0) {
						ay = 0;
					}
					else {
						float ratio = zone.vx / zone.vy;
						ay = -PApplet.sqrt(ax * ax / ((ratio * ratio) + 1)) * PApplet.abs(zone.vy)
								/ zone.vy;
						ax = ratio * ay;
					}

					float t = (parent.millis() - zone.releaseTime) / 10000f;
					float movementX = zone.vx * t + 0.5f * ax * PApplet.pow(t, 2);
					float movementY = zone.vy * t + 0.5f * ay * PApplet.pow(t, 2);
					// Get the movement in the zone's matrix space
					// Need to do it when zone has been rotated
					zone.contains(movementX - zone.lastXMovement, movementY - zone.lastYMovement);

					mTest.reset();
					mTest.apply(zone.matrix);
					PVector a = new PVector(zone.x, zone.y);
					PVector tA = new PVector();
					PVector b = new PVector(zone.x + zone.width, zone.y);
					PVector tB = new PVector();
					PVector c = new PVector(zone.x, zone.y + zone.height);
					PVector tC = new PVector();
					PVector d = new PVector(zone.x + zone.width, zone.y + zone.height);
					PVector tD = new PVector();
					mTest.mult(a, tA);
					mTest.mult(b, tB);
					mTest.mult(c, tC);
					mTest.mult(d, tD);

					if (PApplet.abs(ax * t) >= PApplet.abs(zone.vx)
							|| PApplet.abs(ay * t) >= PApplet.abs(zone.vy) || zone.border == true
							&& (tA.x < 0 || tA.x > TouchClient.parent.width) || tA.y < 0
							|| tA.y > TouchClient.parent.height || tB.x < 0
							|| tB.x > TouchClient.parent.width || tB.y < 0
							|| tB.y > TouchClient.parent.height || tC.x < 0
							|| tC.x > TouchClient.parent.width || tC.y < 0
							|| tC.y > TouchClient.parent.height || tD.x < 0
							|| tD.x > TouchClient.parent.width || tD.y < 0
							|| tD.y > TouchClient.parent.height) {

						// Finish the movement if friction multiplied by time is
						// bigger than the zone's velocity
						// Or if one of the corners of the zone goes off the
						// screen
						ax = zone.vx = 0;
						ay = zone.vy = 0;
						zone.lastXMovement = 0;
						zone.lastYMovement = 0;
						// Change children
						for (Zone z : zone.childList) {
							z.vx = 0;
							z.vy = 0;
							z.lastXMovement = 0;
							z.lastYMovement = 0;
						}
						// Change Group
						for (Zone z : zoneList) {
							if (z != zone && z.group != null
									&& z.group.equalsIgnoreCase(zone.group)) {
								z.vx = 0;
								z.vy = 0;
								z.lastXMovement = 0;
								z.lastYMovement = 0;
							}
						}

					}
					else {
						// Move the zone
						zone.matrix.translate(zone.localX - zone.lastLocalX, zone.localY
								- zone.lastLocalY);
						zone.changed = true;
						zone.lastXMovement = movementX;
						zone.lastYMovement = movementY;

						// Change children
						for (Zone z : zone.childList) {
							z.matrix.translate(zone.localX - zone.lastLocalX, zone.localY
									- zone.lastLocalY);
							z.changed = true;
							z.lastXMovement = movementX;
							z.lastYMovement = movementY;
						}
						// Change Group
						for (Zone z : zoneList) {
							if (z != zone && z.group != null
									&& z.group.equalsIgnoreCase(zone.group)) {
								z.matrix.translate(zone.localX - zone.lastLocalX, zone.localY
										- zone.lastLocalY);
								z.changed = true;
								z.lastXMovement = movementX;
								z.lastYMovement = movementY;
							}
						}
					}

				}
			}

		}*/
	}

	/**
	 * Disconnects the TuioClient when the PApplet is stopped. Shuts down any
	 * threads, disconnect from the net, unload memory, etc.
	 */
	public void dispose() {
		if (tuioClient.isConnected()) {
			tuioClient.disconnect();
		}
	}

	/**
	 * Runs a server that sends TUIO events using Windows 7 Touch events
	 * 
	 * @param touch2TuioExePath
	 *            String - the full name (including path) of the exe of
	 *            Touch2Tuio
	 * @see <a href='http://dm.tzi.de/touch2tuio/'>Touch2Tuio</a>
	 */
	public void runWinTouchTuioServer(String touch2TuioExePath) {
		final String tuioServerCommand = touch2TuioExePath + " " + parent.frame.getTitle();

		Thread serverThread = new Thread() {

			@Override
			public void run() {
				while (true) {
					try {
						Process tuioServer = Runtime.getRuntime().exec(tuioServerCommand);
						tuioServer.waitFor();
					}
					catch (Exception e) {
						System.err.println("TUIO Server stopped!");
					}
				}
			}
		};
		serverThread.start();
	}
}
