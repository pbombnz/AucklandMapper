import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Segment {
    private int roadID;
    private double length;

    private int nodeID1;
    private int nodeID2;

    private Node node1; // start node
    private Node node2; // end node
    private Road road;

    private List<List<Double>> coords;
    private List<Location> coordsLocation;

    public Segment(int roadID, double length, int nodeID1, int nodeID2, double[] coords) {
        this.roadID = roadID;
        this.length = length;

        this.nodeID1 = nodeID1;
        this.nodeID2 = nodeID2;

        this.coords = new ArrayList<List<Double>>();
        this.coordsLocation = new ArrayList<Location>();

        int i = 0;
        while(i < coords.length) {
            //System.out.println(Arrays.toString(coords));
            //System.out.println(coords[i]);
            for(int j=0; j < 1; j++) {
                List<Double> coordPair = new ArrayList<Double>(2);
                coordPair.add(coords[i]);
                coordPair.add(coords[++i]);
                i++;
                this.coords.add(coordPair);
                //System.out.println(coordPair.get(0) + " " + coordPair.get(1));
                this.coordsLocation.add(Location.newFromLatLon(coordPair.get(0),coordPair.get(1)));
            }
        }
    }
    public Segment(int roadID, double length, int nodeID1, int nodeID2, List<List<Double>> coords, List<Location> coordsLocation) {
        this.roadID = roadID;
        this.length = length;

        this.nodeID1 = nodeID1;
        this.nodeID2 = nodeID2;

        this.coords = coords;
        this.coordsLocation = coordsLocation;
    }

    public int getRoadID() { return this.roadID; }
    public double getLength() { return this.length; }
    public int getNodeID1() { return nodeID1; }
    public int getNodeID2() { return nodeID2; }
    public Node getNode1() { return node1; }
    public Node getNode2() { return node2; }
    public Road getRoad() { return road; }
    public List<Location> getCoordsLocation() { return coordsLocation; }

    public void setNode1(Node node1) { this.node1 = node1; }
    public void setNode2(Node node2) { this.node2 = node2; }
    public void setRoad(Road road) { this.road = road; }

    public Segment reverseSegment(){
        return new Segment(roadID, length, nodeID2, nodeID1, coords, coordsLocation);
    }

    public void draw(Graphics g, Location origin, double scale) { 
    	draw(g, origin, scale, MapperColours.COLOR_NORMAL); 
    }

    public void draw(Graphics g, Location origin, double scale, Color color) {
        if (!this.coordsLocation.isEmpty()) {
            Point oldPoint = this.coordsLocation.get(0).asPoint(origin, scale);
            for(int i = 1; i < this.coordsLocation.size(); i++) {
                Point newPoint = this.coordsLocation.get(i).asPoint(origin, scale);
                g.setColor(color);
                g.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);
                oldPoint = newPoint;
            }
        }
    }

    public String toString() {
         StringBuilder string = new StringBuilder("RoadID:"+getRoadID()+" Length:"+getLength()+
                 " nodeID1:"+getNodeID1()+" nodeID2:"+getNodeID2()+
                 " Node1:" + ((node1 == null) ? "null" : node1.toString())+
                 " Node2:" + ((node1 == null) ? "null" : node2.toString())+
                 " Road:" + ((road == null) ? "null" : road.toString()) + " Coords: [");

        for(int i=0; i < coords.size(); i++) {
            string.append(Arrays.toString(coords.get(i).toArray()));
            if((i+1) != coords.size()) {
                string.append(",");
            }
        }

        string.append("] coordsLocation: [");

         for(int i=0; i < coordsLocation.size(); i++) {
             string.append(coordsLocation.get(i).toString());
             if((i+1) != (coordsLocation.size())) {
                 string.append(",");
             }
         }
         string.append("]");

        return string.toString();

    }
}
