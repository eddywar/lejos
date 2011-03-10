package lejos.robotics.pathfinding;

import java.util.ArrayList;
import java.util.Collection;

import lejos.robotics.navigation.WayPoint;

/**
 * 
 * @author BB
 *
 */
public class Node  { // extends Point2D.Float
	
	public float x;
	public float y;
	
	private float h_score = 0;
	private float g_score = 0;
	private Node cameFrom = null;
	private ArrayList <Node> neighbors = new ArrayList<Node>();
	//private String id = null;
	
	public Node(float x, float y) {
		//super(x, y);
		this.x = x; 
		this.y = y;
	}
	
	public Collection <Node> getNeighbors() {
		return neighbors;
	}
	
	/**
	 * Indicates the number of neighbors (nodes connected to this node).
	 * @return int Number of neighbors.
	 */
	public int neighbors() {
		return neighbors.size();
	}
	
	// Note: You have to add this node to neighbor, and then add neighbor to this node. This method doesn't do both.
	public boolean addNeighbor(Node neighbor) {
		// TODO: OPTION - Maybe code here should add each other as neighbors?
		// Check to make sure same isn't added twice. (Assuming ArrayList doesn't do this already?)
		if(neighbors.contains(neighbor)) return false;
		// Check to make sure doesn't add itself
		if(neighbor == this) return false;
		neighbors.add(neighbor);
		return true;
	}
	
	// Note: You have to remove this node from neighbor, and then remove neighbor from this node. This method doesn't do both.
	public boolean removeNeighbor(Node neighbor) {
		// TODO: Maybe code here should remove each other as neighbors?
		return neighbors.remove(neighbor);
	}
	
	public void setHeuristicEstimate(float h) {
		h_score = h;
	}
	
	public void setCost(float g) {
		g_score = g;
	}
	
	public float getCost(){
		return g_score;
	}
	
	// You can not set FScore because it is generated by adding the gscore and hscore.
	public float getFScore(){
		return g_score + h_score;
	}
	
	public Node getPredecessor() {
		return cameFrom;
	}
	
	public void setPredecessor(Node orig) {
		cameFrom = orig;
	}
	 
	// TODO: Move this somewhere else appropriate once Node and NodeMetaData split? Unique to A*.
	public static void reconstructPath(Node current_node, Node start, Collection <WayPoint> path){
		if(current_node == start) {
			path.add(new WayPoint(current_node.x, current_node.y));
			return; 
		} else reconstructPath(current_node.getPredecessor(), start, path);
		path.add(new WayPoint(current_node.x, current_node.y));
		return;
	}

}