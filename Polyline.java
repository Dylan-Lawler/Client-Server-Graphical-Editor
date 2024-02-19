import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Rebecca Liu, Dylan Lawler, Dartmouth CS10, Spring 2021
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	// list of all the coordinates to draw lines between
	private ArrayList<Point> points;
	// separates the coordinates into x and y
	private ArrayList<Integer> xPoints;
	private ArrayList<Integer> yPoints;
	//color of the polyline
	private Color color;

	/**
	 * initiates the lists and color and adds all the given points to the main points list
	 * @param x list of the x coordinates
	 * @param y list of the y coordinates
	 * @param color color of the polyline
	 */
	public Polyline(ArrayList<Integer> x, ArrayList<Integer> y,  Color color){
		xPoints = x;
		yPoints = y;
		this.points = new ArrayList<>();
		for(int i = 0; i< xPoints.size(); i ++){
			Point point = new Point(xPoints.get(i), yPoints.get(i));
			points.add(point);
		}
		this.color = color;
	}

	/**
	 * updates the position of the polyline
	 * @param dx change in x when moving
	 * @param dy change in y when moving
	 */
	@Override
	public void moveBy(int dx, int dy) {
		for(Point point: points){
			point.x += dx;
			point.y += dy;
		}
	}

	/**
	 *
	 * @return the color of the polyline
	 */
	@Override
	public Color getColor() { return this.color;
	}

	/**
	 *
	 * @param color The shape's color
	 */
	@Override
	public void setColor(Color color) { this.color = color;
	}

	/**
	 *
	 * @return the point list of the polyline
	 */
	public ArrayList<Point> getPoints(){return this.points;}

	/**
	 * @return the x coordinates of the polyline
	 */
	public ArrayList<Integer> getXPoints(){return this.xPoints;}

	/**
	 *
	 * @return the y coordinates of the polyline
	 */
	public ArrayList<Integer> getYPoints(){return this.yPoints;}

	/**
	 * determines whether or not a point is on the polyline
	 * @param x x coordinate of point
	 * @param y y coordinate of point
	 * @return whether or not the point is in the polyline
	 */
	@Override
	public boolean contains(int x, int y) {
		for (int i = 0; i < points.size() - 2; i++) {
			if (Segment.pointToSegmentDistance(x, y, points.get(i).x,  points.get(i).y,  points.get(i + 1).x, points.get(i + 1).y) < 2){
				return true;
			}
		}
		return false;
	}

	/**
	 * draws the lines between all the points
	 * @param g graphic
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int i = 0; i < points.size() - 2; i++) {
			g.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
		}

	}

	/**
	 *
	 * @return string of the polyline points
	 */
	public String toString(){
		String res = "";
		for(int i = 0; i< xPoints.size(); i++){
			res += xPoints.get(i)+","+ yPoints.get(i);
			if(i< xPoints.size()-1){
				res+=",";
			}
		}
		return "polyline " + res + " " + this.color.getRGB();
	}
}
