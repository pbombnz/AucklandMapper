import java.util.Queue;


public class FringeNode implements Comparable {
	private Node node;
	private Node from;
	private double costToHere;
	private double totalCost;
	private double depth;
	
	private FringeNode parent;
	private Queue<Node> children;
	private double reach;
	
	public FringeNode(Node node, Node from, double costToHere, double estimate){
		this.node = node;
		this.from = from;
		this.costToHere = costToHere;
		this.totalCost = costToHere + estimate;
	}
	
	public FringeNode(Node firstNode, double depth, FringeNode parent) {
		this.node = firstNode;
		this.depth = depth;
		this.setParent(parent);
	}

	public double getReach() {
		return reach;
	}

	public void setReach(double reach) {
		this.reach = reach;
	}

	public Queue<Node> getChildren() { 
		return children; 
	}
	
	public void setChildren(Queue<Node> children) { 
		this.children = children; 	
	}
	
	public FringeNode getParent() {
		return parent;
	}

	public void setParent(FringeNode parent) {
		this.parent = parent;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public Node getNode(){
		return this.node;
	}
	
	public Node getFrom(){
		return this.from;
	}
	
	public double getCostToHere(){
		return costToHere;
	}
	
	public double getTotalCostToGoal(){
		return totalCost;
	}

	@Override
	public int compareTo(Object o) {
		FringeNode other = (FringeNode) o;
		if(this.totalCost < other.totalCost) {
			return -1;
		} else if(this.totalCost > other.totalCost){
			return 1;
		}
		return 0;
	}
}