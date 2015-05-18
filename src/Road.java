import java.util.ArrayList;
import java.util.List;

public class Road {
    private int roadID;
    private int type;
    private String label;
    private String city;

    private boolean oneWay;
    private int speed;
    private int roadClass;
    private boolean notForCar;
    private boolean notForPede;
    private boolean notForBicy;

    private List<Segment> segments;

    public Road(int roadID, int type, String label, String city, boolean oneWay, int speed, int roadClass, boolean notForCar, boolean notForPede, boolean notForBicy) {
        this.roadID = roadID;
        this.type = type;
        this.label = label;
        this.city = city;
        this.oneWay = oneWay;
        this.speed = speed;
        this.roadClass = roadClass;
        this.notForCar = notForCar;
        this.notForPede = notForPede;
        this.notForBicy = notForBicy;
        this.segments = new ArrayList<Segment>();
    }

    public int getRoadID() { return roadID; }
    public int getType() { return type; }
    public String getLabel() { return label; }
    public String getCity() { return city; }
    public boolean isOneWay() { return oneWay; }
    public int getSpeed() { return speed; }
    public int getRoadClass() { return roadClass; }
    public boolean isNotForCar() { return notForCar; }
    public boolean isNotForPede() { return notForPede; }
    public boolean isNotForBicy() { return notForBicy; }
    public List<Segment> getSegments() { return segments; }

    public String toString() {
        StringBuilder str = new StringBuilder("roadID:"+roadID+" Type:"+type+" Label:"+label+" City:"+city
                +" oneWay:"+oneWay+" Speed:"+speed+" roadClass:"+roadClass+" !Car:"+notForCar+" !Pede:"+notForPede
                +" !Bicy:"+notForBicy+" Segments:"+((segments.isEmpty()) ? "Yes" : "N/A"));


        // CAUSE STACK OVERFLOW - ONLY ENABLE IF NEEDED (EXPECT ERROR)
        /*if(segments.size() > 0) {
            if(segments.size() < 5) {
                str.append(System.lineSeparator() + "Segments:" + System.lineSeparator());
                for (Segment seg : segments) {
                    str.append("\t" + segments.toString() + System.lineSeparator());
                }
            }
            else {
                str.append(" Segments:TooLongToList");
            }
        } else {
            str.append(" Segments:N/A");
        }*/
        return str.toString();
    }

	public String toFancyString() {
		return "\""+label+" in "+city+"\" | oneWay: "+oneWay +" | Speed:"+speed+" | roadClass:"+roadClass+
				" | Car Usage Allowed:"+!notForCar+" |"+ " | Pedestrian Usage Allowed :"+!notForPede +
				" | Bicycle Usage Allowed: "+!notForBicy;
	
	}
}
