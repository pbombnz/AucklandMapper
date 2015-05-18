import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public class Node {
    public static final int DRAW_NODE_RADIUS = 2;

    private int nodeID;
    private double latitude;
    private double longitude;
    private Location location;

    private List<Segment> outNeighbours;
    private List<Segment> inNeighbours;
    
    // General Variables for A* and Articulation point
	private Node from;
	private boolean visited;
	
    // A* Search variables
	private double costToHere;
	
	// Articulation point variables
	private Set<Node> adjacentNodes; //ONLY for out neigboured nodes
	private double depth;


	public Node(int nodeID, double latitude, double longitude) {
        this.nodeID = nodeID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.outNeighbours = new ArrayList<Segment>();
        this.inNeighbours= new ArrayList<Segment>();
        this.adjacentNodes = new HashSet<Node>();
        this.location = Location.newFromLatLon(this.latitude, this.longitude);
    }

    public boolean isVisited() { return visited; }
	
    public void setVisited(boolean visited) { this.visited = visited; }
	
	public Node getFrom() { return from; }
	
	public void setFrom(Node from) { this.from = from; }
	
	public double getCostToHere() { return costToHere; }
	
	public void setCostToHere(double costToHere) { this.costToHere = costToHere; }
	
	public double getDepth() { return depth; }
	
	public void setDepth(double depth) { this.depth = depth; }

    public Set<Node> getAdjacentNodes() { return adjacentNodes; }

	public int getNodeID() { return this.nodeID; }
   
	public double getLatitude() { return this.latitude; }
   
    public double getLongitude() { return this.longitude; }
   
    public Location getLocation() { return this.location; }
    
    public List<Segment> getOutNeighbours() { return outNeighbours; }
    
    public List<Segment> getInNeighbours() { return inNeighbours; }

    public void draw(Graphics g, Location origin, double scale) {
        draw(g, origin, scale, MapperColours.COLOR_NORMAL);
    }

    public void draw(Graphics g, Location origin, double scale, Color color) {
        Point point = location.asPoint(origin, scale);
        if(!outNeighbours.isEmpty()) {
            for(Segment segment: outNeighbours) {
                g.setColor(MapperColours.COLOR_NORMAL);
                segment.draw(g, origin, scale);
            }
        }
        //draws the oval on top of segments with the correct color
        g.setColor(color);
        g.fillOval(point.x - DRAW_NODE_RADIUS, point.y - DRAW_NODE_RADIUS, DRAW_NODE_RADIUS*2, DRAW_NODE_RADIUS*2);
    }

    public String toString() { //for debug only
        return "NodeID:"+nodeID+" Lat:"+latitude+" Lon:" +longitude
                +" Location.x:"+location.x+" Location.y:"+location.y
                +" outNeighbours:"+((outNeighbours.isEmpty()) ? "N/A" : "Yes")
                +" inNeighbours:"+((inNeighbours.isEmpty()) ? "N/A" : "Yes");
    }

    public String toFancyString() {
        Set<String> roadNames = new HashSet<String>();
        StringBuilder string = new StringBuilder("NodeID: "+nodeID+" | Roads Intersecting:");

        for(Segment seg: outNeighbours) {
            roadNames.add(seg.getRoad().getLabel());
        }
        for(Segment seg: inNeighbours) {
            roadNames.add(seg.getRoad().getLabel());
        }
        for(String rn : roadNames) {
            string.append(" \""+rn+"\"");
        }
        return string.toString();
    }
}
