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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import processing.core.PApplet;
import vialab.simpleMultiTouch.events.DragEvent;
import vialab.simpleMultiTouch.events.HSwipeEvent;
import vialab.simpleMultiTouch.events.PinchEvent;
import vialab.simpleMultiTouch.events.RotateEvent;
import vialab.simpleMultiTouch.events.TapAndHoldEvent;
import vialab.simpleMultiTouch.events.TapEvent;
import vialab.simpleMultiTouch.events.VSwipeEvent;
import TUIO.TuioCursor;
import TUIO.TuioPoint;
import TUIO.TuioTime;

/**
 * It handles the TuioCursor events, sees if a gesture has been performed, and
 * if so, it triggers the gesture's action.
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
public class GestureHandler {
	/** Processing PApplet */
	static PApplet parent = TouchClient.parent;

	/** The Touch Client zone list */
	static Vector<Zone> zoneList = TouchClient.zoneList;

	/** The gesture events that the student may override */
	protected Method dragEvent, pinchEvent, rotateEvent, vSwipeEvent, hSwipeEvent, tapEvent,
	tapAndHoldEvent;

	/**
	 * Gesture Handler constructor, gets the gesture and TUIO methods from the
	 * Processing sketch if the student has overridden them.
	 */
	public GestureHandler() {
		dragEvent = SMTUtilities.getPMethod(parent, "dragEvent", new Class[] { DragEvent.class });
		pinchEvent = SMTUtilities.getPMethod(parent, "pinchEvent", new Class[] { PinchEvent.class });
		tapEvent = SMTUtilities.getPMethod(parent, "tapEvent", new Class[] { TapEvent.class });
		tapAndHoldEvent = SMTUtilities.getPMethod(parent, "tapAndHoldEvent",
				new Class[] { TapAndHoldEvent.class });
		rotateEvent = SMTUtilities.getPMethod(parent, "rotateEvent",
				new Class[] { RotateEvent.class });
		hSwipeEvent = SMTUtilities.getPMethod(parent, "hSwipeEvent",
				new Class[] { HSwipeEvent.class });
		vSwipeEvent = SMTUtilities.getPMethod(parent, "vSwipeEvent",
				new Class[] { VSwipeEvent.class });
	}

	/**
	 * Tests if gestures, that require an addTuioCursor event, have been
	 * performed. If yes, it triggers the gesture's action.
	 * 
	 * @param zone
	 *            Zone - The zone on the top-most layer that contains the
	 *            coordinates of the TuioCursor.
	 * @param tcur
	 *            TuioCursor - The TuioCursor that has just moved on the screen.
	 */
	public void detectOnAdd(Zone zone, TuioCursor tcur, int xIn, int yIn) {
		long[] cursor = new long[1];
		cursor[0] = tcur.getSessionID();

		// ////////////////
		// Tap Gesture
		// ////////////////
		if (zone.tappable && zone.tapDown) {
			detectTouchDown(zone, xIn, yIn, cursor);
		}
	}

	/**
	 * Tests if gestures, that require a removeTuioCursor event, have been
	 * performed. If yes, it triggers the gesture's action.
	 * 
	 * @param zone
	 *            Zone - The zone on the top-most layer that contains the
	 *            coordinates of the TuioCursor.
	 * @param tcur
	 *            TuioCursor - The TuioCursor that has just moved on the screen.
	 */
	public void detectOnRemove(Zone zone, TuioCursor tcur, int xIn, int yIn) {
		long[] cursor = new long[1];
		cursor[0] = tcur.getSessionID();

		// ////////////////
		// Tap Gesture
		// ////////////////
		if (zone.tappable && zone.tapUp) {
			detectTouchUp(zone, xIn, yIn, cursor);
		}

	}

	/**
	 * Tests if gestures, that require an updateTuioCursor event, have been
	 * performed. If yes, it triggers the gesture's action.
	 * 
	 * @param zone
	 *            Zone - The zone on the top-most layer that contains the
	 *            coordinates of the TuioCursor.
	 * @param tcur
	 *            TuioCursor - The TuioCursor that has just moved on the screen.
	 * @param xIn
	 *            int - The x-coordinate of the TuioCursor.
	 * @param yIn
	 *            int - The y-coordinate of the TuioCursor
	 */
	void detectOnUpdate(Zone zone, TuioCursor tcur, int xIn, int yIn) {
		long[] cursor = new long[1];
		cursor[0] = tcur.getSessionID();

		// /////////////////////////////////////////////////////////////
		// Two-finger gesture -- Rotate and Pinch Gesture (Scalable)
		// ////////////////////////////////////////////////////////////
		if (zone.currentTouches >= 2) {
			zone.RNTing = false;
			for (TuioCursor tcur2 : zone.tuioCursorList) {
				if (tcur2 != null && tcur.getSessionID() != tcur2.getSessionID()) {

					long[] cursors = new long[2];
					cursors[0] = tcur.getSessionID();
					cursors[1] = tcur2.getSessionID();

					int[] iLast = new int[2];
					iLast[0] = tcur2.getPath().lastElement().getScreenX(TouchClient.parent.width);
					iLast[1] = tcur2.getPath().lastElement().getScreenY(TouchClient.parent.height);

					int midX = (xIn + iLast[0]) / 2;
					int midY = (yIn + iLast[1]) / 2; // Center of pinch and
					// rotate gesture

					zone.contains(midX, midY); // Get midX and midY in the
					// zone's matrix space
					// The values will be in the variables localX and localY

					// Reset the lastLocal values when first starting the
					// gestures.
					if (!zone.rotating && !zone.pinching) {
						zone.lastLocalX = zone.localX;
						zone.lastLocalY = zone.localY;
						zone.angle = 0;
					}
					// When the centre point between the fingers moves,
					// translate that amount
					if (zone.currentTouches == 2) {
						fireDragEvent(midX, midY, cursors, zone.localX - zone.lastLocalX,
								zone.localY - zone.lastLocalY, zone);
					}

					// ///////////////////
					// Pinching Gesture
					// //////////////////
					if (zone.pinchable || zone.xyPinchable || zone.xPinchable || zone.yPinchable) {
						detectPinch(zone, xIn, yIn, cursors);
					}
					// ////////////////
					// Rotate Gesture
					// ////////////////
					if (zone.rotatable) {
						detectRotate(zone, xIn, yIn, cursors);
					}
					return;
				}
			}
		}

		// ////////////////
		// RNT Gesture
		// Drag and X/Y Drag Gesture
		// ////////////////
		if (zone.RNTable) {
			detectRNT(zone, xIn, yIn, cursor);
		}
		else if (zone.draggable || zone.xDraggable || zone.yDraggable) {
			detectDrag(zone, xIn, yIn, cursor);
		}

		// ////////////////////////
		// Horizontal Swipe Gesture
		// /////////////////////////
		if (zone.hSwipeable) {
			detectHSwipe(zone, xIn, yIn, cursor);
		}
		// ///////////////////////////
		// Vertical Swipe Gesture
		// ///////////////////////////
		if (zone.vSwipeable) {
			detectVSwipe(zone, xIn, yIn, cursor);
		}
		// //////////////////////////
		// Tap and Hold Gesture
		// /////////////////////////
		if (zone.tapAndHoldable) {
			detectTapAndHold(zone, xIn, yIn, cursor);
		}
	}

	/**
	 * Detects a rotate gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the rotate occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursors
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 2 cursors in this case)
	 */
	private void detectRotate(Zone zone, int xIn, int yIn, long[] cursors) {
		TuioCursor tcur = TouchClient.tuioClient.getTuioCursor(cursors[1]);

		if(tcur != null){
			if (!zone.rotating) {
				zone.angle = TouchClient.tuioClient.getTuioCursor(cursors[0]).getAngle(
						TouchClient.tuioClient.getTuioCursor(cursors[1]));
				zone.rotating = true;
			}
			float rotAngle = TouchClient.tuioClient.getTuioCursor(cursors[0]).getAngle(
					TouchClient.tuioClient.getTuioCursor(cursors[1]))
					- zone.angle;

			// When you first start to rotate zone, it rotates 360 degrees for some
			// reason.
			// This gets rid of that bug
			if (Math.abs(rotAngle) > 3) {
				return;
			}

			// If the current rotation angle is less than the last rotation angle
			// for the zone, then
			// rotate the opposite way.
			if (rotAngle < zone.angle) {
				rotAngle = -rotAngle;
			}
			fireRotateEvent(xIn, yIn, cursors, (int) zone.localX, (int) zone.localY, rotAngle, zone);
		}
	}

	/**
	 * Detects a pinch gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the pinch occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursors
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 2 cursors in this case)
	 */
	private void detectPinch(Zone zone, int xIn, int yIn, long[] cursors) {
		TuioCursor tcur = TouchClient.tuioClient.getTuioCursor(cursors[1]);

		if(tcur != null){
			int[] iLast = new int[2];
			iLast[0] = tcur.getPath().lastElement().getScreenX(parent.width);
			iLast[1] = tcur.getPath().lastElement().getScreenY(parent.height);

			float sclXDist = (float) Math.sqrt((iLast[0] - xIn) * (iLast[0] - xIn));
			float sclYDist = (float) Math.sqrt((iLast[1] - yIn) * (iLast[1] - yIn));
			float sclDist = (float) Math.sqrt((iLast[0] - xIn) * (iLast[0] - xIn) + (iLast[1] - yIn)
					* (iLast[1] - yIn));

			// For XYPINCH
			if (zone.currentTouches == 4 && zone.xyPinchable) {
				int[] z = new int[8];
				int j = 0;
				cursors = new long[4];
				for (int i = 0; i < 4; i++) {
					cursors[i] = zone.tuioCursorList.get(i).getSessionID();
					z[j] = zone.tuioCursorList.get(i).getPath().lastElement()
							.getScreenX(TouchClient.parent.width);
					z[j + 1] = zone.tuioCursorList.get(i).getPath().lastElement()
							.getScreenY(TouchClient.parent.height);
					j += 2;
				}
				float dist1x = Math.abs(z[0] - z[2]);
				float dist2x = Math.abs(z[0] - z[4]);
				float dist3x = Math.abs(z[0] - z[6]);
				int x, y;

				if (dist1x >= dist2x && dist1x >= dist3x) {
					x = 2;
				}
				else if (dist2x >= dist1x && dist2x >= dist3x) {
					x = 4;
				}
				else {
					x = 6;
				}

				float dist1y = Math.abs(z[1] - z[3]);
				float dist2y = Math.abs(z[1] - z[5]);
				float dist3y = Math.abs(z[1] - z[7]);

				if (dist1y >= dist2y && dist1y >= dist3y) {
					y = 3;
				}
				else if (dist2y >= dist1y && dist2y >= dist3y) {
					y = 5;
				}
				else {
					y = 7;
				}

				sclXDist = (float) Math.sqrt((z[0] - z[x]) * (z[0] - z[x]));
				sclYDist = (float) Math.sqrt((z[1] - z[y]) * (z[1] - z[y]));
				sclDist = (float) Math.sqrt((z[0] - z[x]) * (z[0] - z[x]) + (z[1] - z[y])
						* (z[1] - z[y]));
				// The values will be in the variables localX and localY
				int midX = (xIn + iLast[0]) / 2;
				int midY = (yIn + iLast[1]) / 2; // Center of pinch and rotate
				// gesture
				zone.contains(midX, midY); // Get midX and midY in the zone's matrix
				// space

				// Reset the lastLocal values when first starting the gestures.
				if (!zone.xyPinching) {
					zone.lastLocalX = zone.localX;
					zone.lastLocalY = zone.localY;
					zone.lastSclDist = sclDist;
					zone.lastSclYDist = sclYDist;
					zone.lastSclXDist = sclXDist;
					zone.xyPinching = true;
				}
				// When the centre point between the fingers moves, translate that
				// amount
				fireDragEvent(midX, midY, cursors, zone.localX - zone.lastLocalX, zone.localY
						- zone.lastLocalY, zone);
			}
			if (!zone.pinching) {
				zone.lastSclDist = sclDist;
				zone.lastSclYDist = sclYDist;
				zone.lastSclXDist = sclXDist;
				zone.pinching = true;
			}

			float scl = 1.0f - zone.sclSens + zone.sclSens * (sclDist / zone.lastSclDist);// apply
			// sensitivity

			float sclX = 1.0f - zone.sclSens + zone.sclSens * (sclXDist / zone.lastSclXDist);// apply
			// sensitivity

			float sclY = 1.0f - zone.sclSens + zone.sclSens * (sclYDist / zone.lastSclYDist);// apply
			// sensitivity

			zone.lastSclDist = sclDist;
			zone.lastSclYDist = sclYDist;
			zone.lastSclXDist = sclXDist;

			// limit scale gesture
			if (scl < zone.sclLow) {
				scl = zone.sclLow;
			}
			if (scl > zone.sclHigh) {
				scl = zone.sclHigh;
			}
			if (sclX < zone.sclLow) {
				sclX = zone.sclLow;
			}
			if (sclX > zone.sclHigh) {
				sclX = zone.sclHigh;
			}
			if (sclY < zone.sclLow) {
				sclY = zone.sclLow;
			}
			if (sclY > zone.sclHigh) {
				sclY = zone.sclHigh;
			}
			firePinchEvent(xIn, yIn, cursors, scl, sclX, sclY, (int) zone.localX, (int) zone.localY, zone);
		}
	}

	/**
	 * Detects if a horizontal swipe has occurred over a zone and fires the
	 * corresponding event if it has occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the Horizontal swipe occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectHSwipe(Zone zone, int xIn, int yIn, long[] cursor) {
		TuioCursor t = TouchClient.tuioClient.getTuioCursor(cursor[0]);
		float xSpeed = t.getXSpeed();
		float thresholdDistX = (zone.getWidth() / 2);
		if(zone.getHSwipeDist() != 0){
			thresholdDistX = zone.getHSwipeDist();
		}
		//TODO

		float p1 = t.getPath().get(0).getScreenX(parent.getWidth());
		float p2 = t.getPath().lastElement().getScreenX(parent.getWidth());

		if(zone.getLastHSwipeCursor() != t.getSessionID() && Math.abs(p1 - p2) > thresholdDistX){
			if(xSpeed > 0){
				fireHSwipeEvent(xIn, yIn, cursor, zone, 1);
			} else if (xSpeed < 0){
				fireHSwipeEvent(xIn, yIn, cursor, zone, -1);
			}
		}

	}

	/**
	 * Detects if a vertical swipe has occurred over a zone and fires the
	 * corresponding event if it has occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the vertical swipe occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectVSwipe(Zone zone, int xIn, int yIn, long[] cursor) {
		TuioCursor t = TouchClient.tuioClient.getTuioCursor(cursor[0]);
		float ySpeed = t.getYSpeed();
		float thresholdDistY = (zone.getHeight() / 2);
		if(zone.getVSwipeDist() != 0){
			thresholdDistY = zone.getVSwipeDist();
		}
		//TODO

		float p1 = t.getPath().get(0).getScreenY(parent.getHeight());
		float p2 = t.getPath().lastElement().getScreenY(parent.getHeight());

		if(zone.getLastVSwipeCursor() != t.getSessionID() && Math.abs(p1 - p2) > thresholdDistY){
			if(ySpeed > 0){
				fireVSwipeEvent(xIn, yIn, cursor, zone, 1);
			} else if (ySpeed < 0){
				fireVSwipeEvent(xIn, yIn, cursor, zone, -1);
			}
		}
	}

	/**
	 * Detects an RNT gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that RNT occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectRNT(Zone zone, int xIn, int yIn, long[] cursor) {
		if (zone.getId(0) == TouchClient.tuioClient.getTuioCursor(cursor[0]).getSessionID()) {
			float xDist = zone.localX - zone.lastLocalX;
			float yDist = zone.localY - zone.lastLocalY;
			fireDragEvent(xIn, yIn, cursor, xDist, yDist, zone);
			Point2D positionInZone = new Point2D.Float(zone.localX, zone.localY);

			if (positionInZone.distance(zone.x + zone.width / 2, zone.y + zone.height / 2) >= zone.translateAreaRadius) {
				if (!zone.RNTing) {
					zone.lastGlobalX = zone.localX;
					zone.lastGlobalY = zone.localY;
					zone.angle = getAngleABC(new Point.Float(zone.localX, zone.localY),
							new Point.Float(zone.x + zone.width / 2, zone.y + zone.height / 2),
							new Point.Float(zone.lastGlobalX, zone.lastGlobalY));
					zone.RNTing = true;
				}
				float angle = getAngleABC(new Point.Float(zone.localX, zone.localY),
						new Point.Float(zone.x + zone.width / 2, zone.y + zone.height / 2),
						new Point.Float(zone.lastGlobalX, zone.lastGlobalY));
				fireRotateEvent(xIn, yIn, cursor, (int) zone.localX, (int) zone.localY, angle
						- zone.angle, zone);

			}
		}
	}

	/**
	 * Detects a drag gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the drag occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectDrag(Zone zone, int xIn, int yIn, long[] cursor) {
		if (zone.getId(0) == TouchClient.tuioClient.getTuioCursor(cursor[0]).getSessionID()) {
			float xDist = zone.localX - zone.lastLocalX;
			float yDist = zone.localY - zone.lastLocalY;
			fireDragEvent(xIn, yIn, cursor, xDist, yDist, zone);
		}
	}

	/**
	 * Detects a tap(s) gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the drag occurred in
	 *
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectTouchDown(Zone zone, int xIn, int yIn, long[] cursor) {


		if (zone.lastTouchDown != cursor[0]) {

			
				fireTapEvent(xIn, yIn, cursor, 1, zone);
				zone.lastTouchDown = cursor[0];
				

		}
	}

	/**
	 * Detects a tap gesture and fires the corresponding event if it has
	 * occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the drag occurred in
	 *
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectTouchUp(Zone zone, int xIn, int yIn, long[] cursor) {

		if (zone.lastTouchUp != cursor[0]) {

			fireTapEvent(xIn, yIn, cursor, 1, zone);
			
			zone.lastTouchUp = cursor[0];


		}
	}

	/**
	 * Detects a tap and hold gesture and fires the corresponding event if it
	 * has occurred.
	 * 
	 * @param zone
	 *            Zone - The zone that the drag occurred in
	 * @param xIn
	 *            int - The current x-coordinate of the TuioCursor
	 * @param yIn
	 *            int - The current y-coordinate of the TuioCursor
	 * @param cursor
	 *            long[] - An array holding the session IDs of the cursors
	 *            involved in the gesture (just 1 cursor in this case)
	 */
	private void detectTapAndHold(Zone zone, int xIn, int yIn, long[] cursor) {
		TuioCursor tcur = TouchClient.tuioClient.getTuioCursor(cursor[0]);
		Vector<TuioPoint> path = tcur.getPath();
		long time = tcur.getStartTime().getTotalMilliseconds();

		if (TuioTime.getSessionTime().getTotalMilliseconds() - time > 800
				&& (path.firstElement().getX() - path.lastElement().getX() < 0.05 || path
						.firstElement().getY() - path.lastElement().getY() < 0.05)) {
			fireTapAndHoldEvent(xIn, yIn, cursor, time, zone);
		}

	}

	/**
	 * Fires a pinch event. First fires the event in the Processing PApplet if
	 * the student has implemented the method. If the event has not been handled
	 * then it goes through the zone list until it has been handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param scl
	 *            float - The scale amount
	 * @param sclX
	 *            float - The X scale amount
	 * @param sclY
	 *            float - The Y scale amount
	 */
	public void firePinchEvent(int x, int y, long[] cursors, float scl, float sclX, float sclY, int xPofScale, int yPofScale, Zone zone) {
		PinchEvent e = new PinchEvent(x, y, cursors, scl, sclX, sclY, xPofScale, yPofScale);
		if (pinchEvent != null) {
			try {
				// fire event on PApplet
				pinchEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			//for (int i = zoneList.size() - 1; i >= 0; i--) {
			if (/*zoneList.get(i).contains(x, y)
						&& */(zone.pinchable || zone.xyPinchable
								|| zone.xPinchable || zone.yPinchable)) {
				zone.pinchEvent(e);
			}
			if (e.handled()) {
				return;
			}

			//}
		}
	}

	/**
	 * Fires a drag event. First fires the event in the Processing PApplet if
	 * the student has implemented the method. If the event has not been handled
	 * then it goes through the zone list until it has been handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param xDist
	 *            float - The movement in the x-direction
	 * @param yDist
	 *            float - The movement in the y-direction
	 */
	public void fireDragEvent(int x, int y, long[] cursors, float xDist, float yDist, Zone zone) {
		DragEvent e = new DragEvent(x, y, cursors, xDist, yDist);
		if (dragEvent != null) {
			try {
				// fire event on PApplet
				dragEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			// for (int i = zoneList.size()-1; i >= 0; i--) {
			if (/*
			 * zone.contains(x, y) &&
			 */(zone.draggable || zone.xDraggable || zone.yDraggable || zone.RNTable)) {
				zone.dragEvent(e);
			}
			if (e.handled()) {
				return;
			}

			// }
		}
	}

	/**
	 * Fires a rotate event. First fires the event in the Processing PApplet if
	 * the student has implemented the method. If the event has not been handled
	 * then it goes through the zone list until it has been handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param xPofRotation
	 *            int - the x-coordinate of the point of rotation
	 * @param yPofRotation
	 *            int - the y-coordinate of the point of rotation
	 * @param angle
	 *            float - The angle of rotation
	 */
	public void fireRotateEvent(int x, int y, long[] cursors, int xPofRotation, int yPofRotation,
			float angle, Zone zone) {
		RotateEvent e = new RotateEvent(x, y, cursors, xPofRotation, yPofRotation, angle);
		if (rotateEvent != null) {
			try {
				// fire event on PApplet
				rotateEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			//for (int i = zoneList.size() - 1; i >= 0; i--) {
			//Zone zone = zoneList.get(i);
			if (/*zone.contains(x, y) &&*/ (zone.rotatable || zone.RNTable)) {
				zone.rotateEvent(e);
			}
			if (e.handled()) {
				return;
			}

			//}
		}
	}

	/**
	 * Fires a horizontal swipe event. First fires the event in the Processing
	 * PApplet if the student has implemented the method. If the event has not
	 * been handled then it goes through the zone list until it has been
	 * handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param zone
	 * 			  Zone - The zone associated with the HSwipeEvent
	 * @param swipeType
	 * 			  int - The type of swipe -> -1 for left swipe, 1 for right swipe
	 */
	public void fireHSwipeEvent(int x, int y, long[] cursors, Zone zone, int swipeType) {
		TuioCursor t = TouchClient.tuioClient.getTuioCursor(cursors[0]);
		zone.setLastHSwipeCursor(t.getSessionID());

		HSwipeEvent e = new HSwipeEvent(x, y, cursors, swipeType);
		if (hSwipeEvent != null) {
			try {
				// fire event on PApplet
				hSwipeEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			//for (int i = zoneList.size() - 1; i >= 0; i--) {
			if (/*
			 * zoneList.get(i).contains(x, y) &&
			 */zone.hSwipeable) {
				if(TouchClient.debugMode) {
					System.out.println("Firing H Swipe: " + zone.toString());
				}

				zone.hSwipeEvent(e);
			}
			if (e.handled()) {
				return;
			}

			//}
		}
	}

	/**
	 * Fires a vertical swipe event. First fires the event in the Processing
	 * PApplet if the student has implemented the method. If the event has not
	 * been handled then it goes through the zone list until it has been
	 * handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param zone
	 * 			  Zone - The zone associated with the HSwipeEvent
	 * @param swipeType
	 * 			  int - The type of swipe -> -1 for down swipe, 1 for up swipe
	 */
	public void fireVSwipeEvent(int x, int y, long[] cursors, Zone zone, int swipeType) {
		TuioCursor t = TouchClient.tuioClient.getTuioCursor(cursors[0]);
		zone.setLastVSwipeCursor(t.getSessionID());

		VSwipeEvent e = new VSwipeEvent(x, y, cursors, swipeType);
		if (vSwipeEvent != null) {
			try {
				// fire event on PApplet
				vSwipeEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			//for (int i = zoneList.size() - 1; i >= 0; i--) {
			if (/*
			 * zoneList.get(i).contains(x, y) &&
			 */zone.vSwipeable) {
				if(TouchClient.debugMode) {
					System.out.println("Firing V Swipe: " + zone.toString());
				}

				zone.vSwipeEvent(e);
			}
			if (e.handled()) {
				return;
			}

			//}
		}
	}

	/**
	 * Fires a tap event. First fires the event in the Processing PApplet if the
	 * student has implemented the method. If the event has not been handled
	 * then it goes through the zone list until it has been handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param taps
	 *            int - The number of taps
	 */
	public void fireTapEvent(int x, int y, long[] cursors, int taps, Zone zone) {
		TapEvent e = new TapEvent(x, y, cursors, taps);
		if (tapEvent != null) {
			try {
				// fire event on PApplet
				tapEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if(TouchClient.debugMode) {
			System.out.println("About to Fire tap: " + zone.toString());
		}
		if (!e.handled()) {
			if(TouchClient.debugMode) {
				System.out.println("Firing tap: " + zone.toString());
			}
				zone.tapEvent(e);
				

		}
	}

	/**
	 * Fires a tap and hold event. First fires the event in the Processing
	 * PApplet if the student has implemented the method. If the event has not
	 * been handled then it goes through the zone list until it has been
	 * handled.
	 * 
	 * @param x
	 *            int - The x-coordinate of the event
	 * @param y
	 *            int - The y-coordinate of the event
	 * @param cursors
	 *            long[] - The cursors involved in the event (their session IDs)
	 * @param taps
	 *            int - The number of taps
	 */
	public void fireTapAndHoldEvent(int x, int y, long[] cursors, long time, Zone zone) {
		TapAndHoldEvent e = new TapAndHoldEvent(x, y, cursors, time);
		if (tapAndHoldEvent != null) {
			try {
				// fire event on PApplet
				tapAndHoldEvent.invoke(parent, e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!e.handled()) {
			// fire event on zones
			//for (int i = zoneList.size() - 1; i >= 0; i--) {
			if (/*
			 * zoneList.get(i).contains(x, y) &&
			 */zone.tapAndHoldable) {
				zone.tapAndHoldEvent(e);
			}
			if (e.handled()) {
				return;
			}

			//}
		}
	}

	/**
	 * Calculates and returns the angle between the three points in Radians.
	 * 
	 * @param a
	 *            Point.Float - First Point
	 * @param b
	 *            Point.Float - Second Point
	 * @param c
	 *            Point.Float - Third Point
	 * @return float The angle between the three points in Radians
	 */
	public static float getAngleABC(Point.Float a, Point.Float b, Point.Float c) {
		Point.Float ab = new Point.Float(b.x - a.x, b.y - a.y);
		Point.Float cb = new Point.Float(b.x - c.x, b.y - c.y);
		float angba = (float) Math.atan2(ab.y, ab.x);
		float angbc = (float) Math.atan2(cb.y, cb.x);
		return angba - angbc;

	}

}