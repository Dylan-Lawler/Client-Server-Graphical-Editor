import java.io.*;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Rebecca Liu, Dylan Lawler, Dartmouth CS10, Spring 2021
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			// TODO: YOUR CODE HERE
			String message;
			// while there's still a message handle the message
			while((message = in.readLine())!= null){
//				System.out.println(message);
				handleMessage(message);
				editor.repaint();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	//	// TODO: YOUR CODE HERE
	// handling information sent in message
	public void handleMessage(String msg){
		Messages message = new Messages(msg);
		System.out.println(msg); // for testing what infomation the message holds
		message.parseLine(); // parse the message
		// if mode is equal to draw, add the new shape to the shapemaps
		if(message.mode.equals("draw")){
			if(message.shapeType.equals("ellipse")){
				Shape shape = new Ellipse(message.x1, message.y1, message.x2, message.y2, message.color);
				editor.getSketch().addShape(message.id, shape);
			}
			if(message.shapeType.equals("rectangle")){
				Shape shape = new Rectangle (message.x1, message.y1, message.x2, message.y2, message.color);
				editor.getSketch().addShape(message.id, shape);
			}
			if(message.shapeType.equals("segment")){
				Shape shape = new Segment(message.x1, message.y1, message.x2, message.y2, message.color);
				editor.getSketch().addShape(message.id, shape);
			}
			if(message.shapeType.equals("polyline")){
				Shape shape = new Polyline(message.xPoints, message.yPoints, message.color);
				editor.getSketch().addShape(message.id, shape);
			}
			editor.setId(message.id); // set the curr id
		}

		// reset the color if mode is recolor
		if(message.mode.equals("recolor")){
			editor.getSketch().setColor(message.id, message.color);
		}
		// move the shape is mode is in move
		if(message.mode.equals("move")){
			Shape shape = editor.getSketch().shapeMap.get(message.id);
			shape.moveBy(message.dx, message.dy);
			editor.getSketch().addShape(message.id, shape);
		}
		// delete shape from shapemap if mode is in delete
		if(message.mode.equals("delete")){
			editor.getSketch().removeShape(message.id); // remove id from the shape map
		}

	}

}
