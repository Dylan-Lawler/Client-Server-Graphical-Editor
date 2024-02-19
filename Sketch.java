import java.util.TreeMap;
import java.awt.*;

/**
 * keeps track of all of the shapes in the editor
 * @author Rebecca Liu, Dylan Lawler, Dartmouth CS10, Spring 2021
 */

public class Sketch {
    // map of all shapes and their respective ids
    public TreeMap<Integer, Shape> shapeMap;

    /**
     * sketch constructor, initializes the shape map
     */
    public Sketch(){
        this.shapeMap = new TreeMap();
    }

    /**
     * adds id to key and shape to value of the map
     * @param id id of the shape
     * @param shape shape object
     */
    public synchronized void addShape(int id, Shape shape){
        shapeMap.put(id, shape);
    }

    /**
     * takes the shape and respective id out of the map
     * @param id id of the shape
     */
    public synchronized void removeShape(int id){
        shapeMap.remove(id);
    }

    /**
     * changes the color of the desired shape
     * @param id id of the shape being colored
     * @param color color the shape is being changed to
     */
    public synchronized void setColor(int id, Color color){
        shapeMap.get(id).setColor(color);
    }

    /**
     *
     * @param id id of the shape
     * @return the shape with the given id
     */
    public synchronized Shape getShape(int id){
        for(int i: shapeMap.keySet()){
            if(i == id){
                return shapeMap.get(i);
            }
        }
        return null;
    }

    /**
     *
     * @param mousepress clicking point
     * @return the shape that the mouse is clicking on
     */
    public synchronized Shape containsShape(Point mousepress){
        for(int id: shapeMap.descendingKeySet()){
            Shape s = shapeMap.get(id);
            if (s.contains(mousepress.x, mousepress.y)){
                return s;
            }
        }
        return null;
    }

    /**
     *
     * @param shape shape whose id is being requested
     * @return the id of the requested shape
     */
    public synchronized Integer getID(Shape shape){
        for(int id: shapeMap.keySet()){
            if(shape == shapeMap.get(id)){
                return id;
            }
        }
        return null;
    }
}
