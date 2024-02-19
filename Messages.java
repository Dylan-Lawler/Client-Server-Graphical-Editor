import java.awt.*;
import java.io.*;
import java.util.*;
/**
 * Parses through a specific message to send to server
 * @author Rebecca Liu, Dylan Lawler, Dartmouth CS10, Spring 2021
 */


public class Messages {
    public String[] parsedLine; // array of the parsed message components
    public String mode;         // current user mode (draw, recolor, move, delete)
    public Integer id;          // id of the current shape
    public Color color;         // color of the current shape
    public String shapeType;    // current shapeType (ellipse, rectangle, segment or polyline)
    public Integer x1;          // first point instances of ellipse, rectangle, or segment
    public Integer x2;
    public Integer y1;          // second point parameters of ellipse, rectangle, or segment
    public Integer y2;
    public ArrayList<Integer> xPoints;  // all x coordinates of the polyline
    public ArrayList<Integer> yPoints;  // all y coordinates of the polyline
    public Integer dx;                  //change in x and y coordinates while moving
    public Integer dy;


    /**
     * message constructor, initializes string split array and the polyline coordinates
     * @param line message to the server be parsed through
     */
    public Messages(String line){
        parsedLine = line.split(" ");
        xPoints = new ArrayList<>();
        yPoints = new ArrayList<>();
    }

    /**
     * parses through the server message for all of the information need to do complete the user action
     */
    public void parseLine(){
        // interactive mode and shape id are the first and second components respectively
        mode = parsedLine[0];
        id = Integer.parseInt(parsedLine[1]);
        if (mode.equals("draw")){
            //if drawing, the shapeType is the third component
            shapeType = parsedLine[2];
            // for rectangle, ellipse, and segment, the coordinates are components 4-7 and color is 8th
            if (shapeType.equals("rectangle") || shapeType.equals("ellipse")|| shapeType.equals("segment")) {
                x1 = Integer.parseInt(parsedLine[3]);
                y1 = Integer.parseInt(parsedLine[4]);
                x2 = Integer.parseInt(parsedLine[5]);
                y2 = Integer.parseInt(parsedLine[6]);
                color = new Color(Integer.parseInt(parsedLine[7]));
            }
            // for freehand drawing, the coordinates are the fourth component and color is the fifth
            if (shapeType.equals("polyline")){
                String coordinates = parsedLine[3];
                String[] splitCoordinates = coordinates.split(",");
                for (int i = 0; i < splitCoordinates.length; i++){
                    if(i%2 == 0){
                        xPoints.add(Integer.parseInt(splitCoordinates[i]));
                    }
                    if(i%2 != 0){
                        yPoints.add(Integer.parseInt(splitCoordinates[i]));
                    }
                }
                color = new Color(Integer.parseInt(parsedLine[4]));
            }

        }

        //if recoloring, the third component is the color
        if (mode.equals("recolor")){
            color = new Color(Integer.parseInt(parsedLine[2]));
        }

        // if moving, the change in x and y are the 3rd and 4th components respectively
        if (mode.equals("move")) {
            dx = Integer.parseInt(parsedLine[2]);
            dy = Integer.parseInt(parsedLine[3]);
        }
    }

}

