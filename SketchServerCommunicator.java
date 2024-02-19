import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Rebecca Liu, Dylan Lawler, Dartmouth CS10, Spring 2021
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			System.out.println("current state of world:" + '\n');
			for(int i: server.getSketch().shapeMap.keySet()){
				// prints out the current state of the world in the console
				System.out.println("draw " +i+ " " + server.getSketch().shapeMap.get(i).toString() + '\n');
				out.println("draw " +i + " "+ server.getSketch().shapeMap.get(i).toString());
			}

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String message;
			//while there's still a message to read
			while( (message = in.readLine()) != null){
				// create a new Messages object
				Messages msg = new Messages(message);
				msg.parseLine();
				// add shapes to the shapemaps if mode is in draw
				if(msg.mode.equals("draw")) {
					if (msg.shapeType.equals("ellipse")) {
						Shape shape = new Ellipse(msg.x1, msg.y1, msg.x2, msg.y2, msg.color);
						server.getSketch().addShape(msg.id, shape);
					}
					if (msg.shapeType.equals("rectangle")) {
						Shape shape = new Rectangle(msg.x1, msg.y1, msg.x2, msg.y2, msg.color);
						server.getSketch().addShape(msg.id, shape);
					}
					if (msg.shapeType.equals("segment")) {
						Shape shape = new Segment(msg.x1, msg.y1, msg.x2, msg.y2, msg.color);
						server.getSketch().addShape(msg.id, shape);
					}
					if (msg.shapeType.equals("polyline")) {
						Shape shape = new Polyline(msg.xPoints, msg.yPoints, msg.color);
						server.getSketch().addShape(msg.id, shape);
					}

				}
				// sets the color to the new color in shapemap
				if(msg.mode.equals("recolor")){
					server.getSketch().setColor(msg.id, msg.color);
				}
				//moves the object and readds the shape in the new location in the shapemap
				if(msg.mode.equals("move")){
					Shape shape = server.getSketch().shapeMap.get(msg.id);
					shape.moveBy(msg.dx, msg.dy);
					server.getSketch().addShape(msg.id, shape);
				}
				// remove id from the shape map in delete mode
				if(msg.mode.equals("delete")){
					server.getSketch().removeShape(msg.id);
				}
				//broadcast the message to all clients
				server.broadcast(message);

			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
