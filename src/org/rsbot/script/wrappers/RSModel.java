package org.rsbot.script.wrappers;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.LinkedList;

import org.rsbot.accessors.LDModel;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

/**
 * A screen space model.
 * 
 * @author Jacmob
 */
public abstract class RSModel extends MethodProvider {
	
	private int[] xPoints;
	private int[] yPoints;
	private int[] zPoints;
	
	private short[] indices1;
	private short[] indices2;
	private short[] indices3;
	
	public RSModel(MethodContext ctx, LDModel model) {
		super(ctx);
		xPoints = model.getXPoints().clone();
		yPoints = model.getYPoints().clone();
		zPoints = model.getZPoints().clone();
		
		indices1 = model.getIndices1().clone();
		indices2 = model.getIndices2().clone();
		indices3 = model.getIndices3().clone();
	}
	
	protected abstract int getLocalX();
	protected abstract int getLocalY();
	
	/**
	 * Returns a random screen point.
	 * 
	 * @return A screen point, or Point(-1, -1) if
	 * the model is not on screen.
	 */
	public Point getPoint() {
		int len = indices1.length;
		int sever = random(0, len);
		Point point = getPointInRange(sever, len);
		if (point != null) {
			return point;
		}
		point = getPointInRange(0, sever);
		if (point != null) {
			return point;
		}
		return new Point(-1, -1);
	}
	
	/**
	 * Returns an array of triangles containing
	 * the screen points of this model.
	 * 
	 * @return The on screen triangles of this model.
	 */
	public Polygon[] getTriangles() {
		LinkedList<Polygon> polygons = new LinkedList<Polygon>();
		int locX = getLocalX();
		int locY = getLocalY();
		int len = indices1.length;
		int height = methods.calc.tileHeight(locX, locY);
		for (int i = 0; i < len; ++i) {
			Point one = methods.calc.worldToScreen(locX + xPoints[indices1[i]], height + yPoints[indices1[i]], locY + zPoints[indices1[i]]);
			Point two = methods.calc.worldToScreen(locX + xPoints[indices2[i]], height + yPoints[indices2[i]], locY + zPoints[indices2[i]]);
			Point three = methods.calc.worldToScreen(locX + xPoints[indices3[i]], height + yPoints[indices3[i]], locY + zPoints[indices3[i]]);
			
			if (one.x >= 0 && two.x >= 0 && three.x >= 0) {
				polygons.add(new Polygon(new int[] {one.x, two.x, three.x}, new int[] {one.y, two.y, three.y}, 3));
			}
		}
		return polygons.toArray(new Polygon[polygons.size()]);
	}
	
	/**
	 * Returns true if the provided object is an RSModel
	 * with the same x, y and z points as this model.
	 * This method compares all of the values in the
	 * three vertex arrays.
	 * 
	 * @return <tt>true</tt> if the provided object is a
	 * model with the same points as this.
	 */
	public boolean equals(Object o) {
		if (o instanceof RSModel) {
			RSModel m = (RSModel) o;
			return Arrays.equals(xPoints, m.xPoints) &&
				   Arrays.equals(yPoints, m.yPoints) &&
				   Arrays.equals(zPoints, m.zPoints);
		}
		return false;
	}
	
	private Point getPointInRange(int start, int end) {
		int locX = getLocalX();
		int locY = getLocalY();
		int height = methods.calc.tileHeight(locX, locY);
		for (int i = start; i < end; ++i) {
			Point one = methods.calc.worldToScreen(locX + xPoints[indices1[i]], height + yPoints[indices1[i]], locY + zPoints[indices1[i]]);
			int x = -1, y = -1;
			if (one.x >= 0) {
				x = one.x;
				y = one.y;
			}
			Point two = methods.calc.worldToScreen(locX + xPoints[indices2[i]], height + yPoints[indices2[i]], locY + zPoints[indices2[i]]);
			if (two.x >= 0) {
				if (x >= 0) {
					x = (x + two.x) / 2;
					y = (y + two.y) / 2;
				} else {
					x = two.x;
					y = two.y;
				}
			}
			Point three = methods.calc.worldToScreen(locX + xPoints[indices3[i]], height + yPoints[indices3[i]], locY + zPoints[indices3[i]]);
			if (three.x >= 0) {
				if (x >= 0) {
					x = (x + three.x) / 2;
					y = (y + three.y) / 2;
				} else {
					x = three.x;
					y = three.y;
				}
			}
			if (x >= 0) {
				return new Point(x, y);
			}
		}
		return null;
	}

}
