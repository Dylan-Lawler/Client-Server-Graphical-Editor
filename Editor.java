import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;

import javax.sound.sampled.Line;
import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Rebecca Liu, Dylan Lawler,  Dartmouth CS 10, Spring 2021
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	// different shapes available to the user, used as temps to set equal to curr
	private Ellipse ellipse = null;
	private Rectangle rectangle = null;
	private Polyline polyLine = null;
	private Segment segment = null;
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private int currId = -1;					// new id for new shape being added
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged
	private String message;
	private int dx;								// moving change in x
	private int dy;								// moving change in y

	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");
		message = "";
		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		if(curr!= null){
			curr.draw(g);
		}

		for (Integer id: sketch.shapeMap.keySet()){// draw all preexisting shapes\
			sketch.shapeMap.get(id).draw(g);
		}
	}

	// Helpers for event handlers

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */

	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE

		// when drawing, send message to server with shape id
		if (mode.equals(Mode.DRAW)){
			currId +=1;
			message += "draw" + " " + currId;

			// draws shape from current clicked point
			drawFrom = p;
			// sets current shape to a new ellipse when user changes mode to ellipse
			if (shapeType.equals("ellipse")) {
				ellipse = new Ellipse((int)p.getX(), (int)p.getY(), color);
				curr = ellipse;
			}
			// sets current shape to a new rectangle when user changes mode to rectangle
			if (shapeType.equals("rectangle")) {
				rectangle = new Rectangle((int)p.getX(), (int)p.getY(), color);
				curr = rectangle;
			}
			// sets current shape to a new segment when user changes mode to segment
			if (shapeType.equals("segment")) {
				segment = new Segment((int)p.getX(), (int)p.getY(), color);
				curr = segment;
			}
			// sets the current shape to a polyline when the user wants to freehand draw
			// gets the current point to add to the polyline point list
			if (shapeType.equals("freehand")) {
				polyLine = new Polyline(new ArrayList<Integer>(), new ArrayList<Integer>(), color);
				polyLine.getXPoints().add(p.x);
				polyLine.getYPoints().add(p.y);
				curr = polyLine;
			}
		}

		// if the user sets the mode to move, get the shape the user clicked on and update the current shape id
		if (mode.equals(Mode.MOVE)){
			if(sketch.containsShape(p)!= null){
				Shape shape = sketch.containsShape(p);
				movingId = sketch.getID(shape);
				curr = sketch.getShape(movingId);
				moveFrom = p;
			}
		}

		// if the user sets the mode to recolor, send request to the server and the get the shape the user clicked on
		if (mode.equals(Mode.RECOLOR)){
			message += "recolor";
			if(sketch.containsShape(p)!= null){
				Shape shape = sketch.containsShape(p);
				movingId = sketch.getID(shape);
				curr = sketch.getShape(movingId);
				curr.setColor(color);
			}

			message += " " + movingId + " " + color.getRGB();
		}
		// if the user sets the mode to delete, send request to the server and get the shape the user clicked on
		if (mode.equals(Mode.DELETE)){
			if(sketch.containsShape(p)!= null){
				Shape shape = sketch.containsShape(p);
				movingId = sketch.getID(shape);
				curr = sketch.getShape(movingId);
			}
			message += "delete" + " " + movingId;
		}

		// Be sure to refresh the canvas (repaint) if the appearance has changed
		repaint();
	}

	public void setId(int x){
		currId = x;
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE

		if (mode.equals(Mode.DRAW)) {
			// if the user is drawing an ellipse, update the corners to where the mouse is
			if (curr == ellipse) {
				ellipse.setCorners((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY());
			}
			// if the user is drawing a rectangle, update the corners to where the mouse is
			if (curr == rectangle) {
				rectangle.setCorners((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY());
			}
			// if the user is drawing a segment, keep moving the endpoint with the mouse
			if (curr == segment) {
				segment.setEnd(p.x, p.y);
			}
			// if the user is freehand drawing, continuously send requests to the server with the mouse points
			if (curr == polyLine) {
				polyLine.getXPoints().add(p.x);
				polyLine.getYPoints().add(p.y);
				message = "draw " + currId + " "+ curr.toString();
				comm.send(message);
			}
		}
		// In moving mode, shift the object and keep track of where next step is from
		if (mode.equals(Mode.MOVE) && moveFrom != (null)){
			dx = (int)p.getX() - (int)moveFrom.getX();
			dy = (int)p.getY() - (int)moveFrom.getY();
			moveFrom = p;
			message ="move" + " " + movingId + " " + dx + " " + dy;
			if(curr != null) {
				comm.send(message);
			}
		}

		// Be sure to refresh the canvas (repaint) if the appearance has changed
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		// tells the server a shape has been drawn
		if(mode.equals(Mode.DRAW)){
			message += " " + curr.toString();
		}
		// stops moving the shape upon release
		if(mode.equals(Mode.MOVE)){
			moveFrom = null;
		}
		// when their is no current shape, send complete request to the server
		if(curr != null) {
			comm.send(message);
		}
		// clears request, current shape, and canvas
		message = "";
		curr = null;
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}
