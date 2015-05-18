import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.util.*;

public class Mapper extends GUI {
    private static final double ZOOM_SCALE = 1.30;
    private static final double PAN_SCALE = 0.5;
    private static int MAX_SEARCH_RESULTS = 20;

    public Location origin;
    public double scale;

    public List<Node> selectedNodes = new ArrayList<Node>();
    public List<Segment> selectedSegments = new ArrayList<Segment>();
    public List<Node> selectedNodes_DEBUG = new ArrayList<Node>();
    public List<Segment> selectedSegments_DEBUG = new ArrayList<Segment>();
    
    public int oldMouseX;
    public int oldMouseY;
    public int newMouseX;
    public int newMouseY;
    public boolean isDragged;
    
    public boolean isFilesLoaded = false;

    public Map<Integer, Node> nodes = new HashMap<Integer, Node>();
    public Map<Integer, Road> roads = new HashMap<Integer, Road>();

    public Map<String, List<Road>> roadsByName = new HashMap<String, List<Road>>();
    public Set<String> roadNames = new HashSet<String>();
    public Trie trie = new Trie();
    
    public AStar aStarParam = AStar.NONE;

    public void setScaling() {
        double northBound = Double.NEGATIVE_INFINITY;
        double eastBound = Double.NEGATIVE_INFINITY;
        double southBound = Double.POSITIVE_INFINITY;
        double westBound = Double.POSITIVE_INFINITY;

        for (Node node : nodes.values()) {
            Location location = node.getLocation();
            if (location.x < westBound) {westBound = location.x;}
            if (location.x > eastBound) {eastBound = location.x;}
            if (location.y < southBound) {southBound = location.y;}
            if (location.y > northBound) {northBound = location.y;}
        }

        origin = new Location(westBound, northBound);
        scale = Math.min(getDrawingAreaDimension().getWidth() / (eastBound - westBound),
                         getDrawingAreaDimension().getHeight() / (northBound - southBound));
    }

    @Override
    protected void redraw(Graphics g) {
        for (Node node : nodes.values()) {
            node.draw(g, origin, scale);
        }
        for(Node node: selectedNodes_DEBUG) {
        	node.draw(g, origin, scale, MapperColours.COLOR_DEBUG);
        }
        for(Segment segment: selectedSegments_DEBUG) {
            segment.draw(g, origin, scale, MapperColours.COLOR_DEBUG);
        }
        for(Node node: selectedNodes) {
        	node.draw(g, origin, scale, MapperColours.COLOR_HIGHLIGHTED);
        }
        for(Segment segment: selectedSegments) {
            segment.draw(g, origin, scale, MapperColours.COLOR_HIGHLIGHTED);
        }
    }

	@Override
	protected void onAStarToggle(AStar method) {
		clearSelectedItems();
		if(isAStarSelection()) {
			aStarParam = method;
		}
	}

	@Override
	protected void onAPointToggle(APoint method) {
		clearSelectedItems();
		if(isAPointSelection()) {
			String methodStr = method == APoint.RECURSIVE ? "Recursively" : "Interatively"; 
			if(method == APoint.RECURSIVE) { //rec
				selectedNodes.addAll(recArtPts());
			} else if(method == APoint.ITERATIVE) {
				selectedNodes.addAll(iterArtsPts());
			} else if(method == APoint.BOTH) { //debug both
				selectedNodes.addAll(recArtPts());
				selectedNodes_DEBUG.addAll(iterArtsPts());
				StringBuilder sb = new StringBuilder();
				sb.append("Articulation Points Found Recursively: "+String.valueOf(selectedNodes.size())+"\n");
				sb.append("Articulation Points Found Interatively: "+String.valueOf(selectedNodes_DEBUG.size())); 
				getTextOutputArea().setText(sb.toString());
				return;
			}
			getTextOutputArea().setText("Articulation Points Found "+methodStr+": "+String.valueOf(selectedNodes.size())); 
		}
	}
	
    @Override
    protected void onMove(Move m) {
    	if(isFilesLoaded) {
	        switch(m) {
	            case NORTH:
	                origin = new Location(origin.x, origin.y+PAN_SCALE);
	                break;
	            case EAST:
	                origin = new Location(origin.x+PAN_SCALE, origin.y);
	                break;
	            case SOUTH:
	                origin = new Location(origin.x, origin.y-PAN_SCALE);
	                break;
	            case WEST:
	                origin = new Location(origin.x-PAN_SCALE, origin.y);
	                break;
	            case ZOOM_IN:
	                origin = new Location(origin.x + (PAN_SCALE*2), origin.y - (PAN_SCALE*2));
	                scale = scale*ZOOM_SCALE;
	                break;
	            case ZOOM_OUT:
	                origin = new Location(origin.x - (PAN_SCALE*2), origin.y + (PAN_SCALE*2));
	                scale = scale/ZOOM_SCALE;
	                break;
	        }
	        redraw();
    	}
    }

    @Override
    protected void onMousePressed(MouseEvent e) {
    	oldMouseX = e.getX();
    	oldMouseY = e.getY();
    }
    
    @Override
    protected void onMouseReleased(MouseEvent e) {
    	if(isFilesLoaded) {
	    	if(!isDragged) {
		        Location mouseLocation = Location.newFromPoint(e.getPoint(), origin, scale);
		        Node closestNode = null;
		        double closestNodeDistance = Double.POSITIVE_INFINITY;
		        for(Node node: nodes.values()) {
		            double dist = mouseLocation.distance(node.getLocation());
		            if(dist < closestNodeDistance) {
		                closestNode = node;
		                closestNodeDistance = dist;
		            }
		        }
		        if(isAStarSelection()) {
		        	if(selectedNodes.size() >= 2) {
		        		clearSelectedItems();
		        	}
		        } else {
		        	clearSelectedItems();
		        }
		        selectedNodes.add(closestNode);
		        
        		StringBuilder sb = new StringBuilder();
                for(Node node: selectedNodes) {
                	sb.append(node.toFancyString() +"\n");
                }
                
                if(selectedNodes.size() >= 2 && isAStarSelection()) {
                	sb.append("===============================================\n");
                	List<Segment> selectedSegmentsTmp;
                	List<Segment> selectedSegmentsTmpDist = new ArrayList<Segment>();
                	if(aStarParam == AStar.BOTH) {
                		Node s1 = selectedNodes.get(0);
                		Node s2 = selectedNodes.get(1);
                		selectedSegmentsTmp = findPath(s1, s2, AStar.STANDARD);
                		selectedSegmentsTmpDist = findPath(s1, s2, AStar.DISTANCE);                 		
                	} else {
                		selectedSegmentsTmp = findPath(selectedNodes.get(0), selectedNodes.get(1), aStarParam);
                	}
                	
                	if(selectedSegmentsTmp == null) {
                		sb.append("No Path Found!\n");
                	} else {
                		selectedSegments = selectedSegmentsTmp;
                		sb.append("Path Found!\n");
                		sb.append("Path Road & Length Information ");
                		if(aStarParam == AStar.BOTH) {
                			selectedSegments_DEBUG = selectedSegmentsTmpDist;
                			sb.append("(NOTE: Only showing for Standard)");
                		}
                		sb.append(":\n");
                		
                		Map<String, Double> pathLength = new HashMap<String, Double>();
                		
                		for(int i = 0; i < selectedSegmentsTmp.size(); i++) {
                			Segment segment = selectedSegmentsTmp.get(i);
                			String pathLabel = segment.getRoad().getLabel()+" @ "+segment.getRoad().getCity();
                			if(pathLength.containsKey(pathLabel)) {
                				pathLength.put(pathLabel, pathLength.get(pathLabel) + segment.getLength());
                			} else {
                				pathLength.put(pathLabel, segment.getLength());
                			}
                		}
                		
                		double length = 0;
                		for(String label : pathLength.keySet()) {
                			sb.append("\t"+label+": "+String.format("%.3fkm", pathLength.get(label))+"\n");
                			length += pathLength.get(label);
                		}
                		sb.append("\tTotal Distance: "+ String.format("%.3fkm", length));
                	}
                }
                getTextOutputArea().setText(sb.toString());
	    	} 
	        
	    	oldMouseX = 0;
	    	oldMouseY = 0;
	    	newMouseX = 0;
	    	newMouseY = 0;
	    	isDragged = false;
    	}
    }

	@Override
	protected void onMouseDragged(MouseEvent e) {
		if(isFilesLoaded) {
			isDragged = true;
	    	newMouseX = e.getX();
	    	newMouseY = e.getY();
	    	
	    	int xMoveDist = (int) ((oldMouseX - newMouseX)/scale);
	    	int yMoveDist = (int) ((oldMouseY - newMouseY)/scale);  
	
			Location mouseLocation = Location.newFromPoint(new Point(xMoveDist, yMoveDist), origin, scale);
	    	origin = new Location(mouseLocation.x, mouseLocation.y);	
		}
	}
    
    @Override
	protected void onMouseWheelMoved(MouseWheelEvent e) {
    	if(isFilesLoaded) {
			if(e.getWheelRotation() >= 0) {
				//postive -> scoll downwards -> ZOOM_OUT
	            origin = new Location(origin.x - (PAN_SCALE*2), origin.y + (PAN_SCALE*2));
	            scale = scale/ZOOM_SCALE;
			} else {
				//negative -> scoll upwards -> ZOOM_IN
	            origin = new Location(origin.x + (PAN_SCALE*2), origin.y - (PAN_SCALE*2));
	            scale = scale*ZOOM_SCALE;
			}
    	}
		
	}
    
    @Override
    protected void onLoad(File nodesFile, File roadsFile, File segmentsFile, File polygonsFile) {
        loadNodes(nodesFile);
        loadRoads(roadsFile);
        loadSegments(segmentsFile);
        setScaling();
        isFilesLoaded = true;
        getTextOutputArea().setText("Data Loaded Successfully");
    }

    public void loadNodes(File nodesFile) {
        this.nodes = new HashMap<Integer, Node>();
        try {
            Scanner scan = new Scanner(nodesFile);
            while(scan.hasNext()) {
                Node node = new Node(scan.nextInt(), scan.nextDouble(), scan.nextDouble());
                this.nodes.put(node.getNodeID(), node);
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadRoads(File roadsFile) {
        this.roads = new HashMap<Integer, Road>();
        this.roadsByName = new HashMap<String, List<Road>>();
        try {
            BufferedReader data = new BufferedReader(new FileReader(roadsFile));
            String line = data.readLine();
            while ((line = data.readLine()) != null) {
                String[] values = line.split("\t");

                int roadID = Integer.parseInt(values[0]);
                int type = Integer.parseInt(values[1]);
                String label = values[2];
                String city = values[3];
                boolean oneWay = ((Integer.parseInt(values[4]) == 1) ? true : false);
                int speed = Integer.parseInt(values[5]);
                int roadClass = Integer.parseInt(values[6]);
                boolean notForCar  = ((Integer.parseInt(values[7]) == 1) ? true : false);
                boolean notForPede = ((Integer.parseInt(values[8]) == 1) ? true : false);
                boolean noteForBicy = ((Integer.parseInt(values[9]) == 1) ? true : false);

                Road road = new Road(roadID, type, label, city, oneWay, speed, roadClass, notForCar, notForPede, noteForBicy);
                this.roads.put(roadID, road);

                //for search box (linear)
                roadNames.add(road.getLabel());
                if(!roadsByName.containsKey(road.getLabel())) {
                    roadsByName.put(road.getLabel(), new ArrayList<Road>());
                }
                roadsByName.get(road.getLabel()).add(road);
            }
            data.close();
            
            //for search box (trie)
            trie = new Trie();
			for (String roadName: roadNames) {
			    trie.add(roadName);
			}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSegments(File segmentsFile) {
        try {
            BufferedReader data = new BufferedReader(new FileReader(segmentsFile));
            String line = data.readLine(); //ignore first line (coz it contains the header crap)
            while ((line = data.readLine()) != null) {
                String[] values = line.split("\t", 5);

                int roadID = Integer.parseInt(values[0]);
                double length = Double.parseDouble(values[1]);
                int nodeID1 = Integer.parseInt(values[2]);
                int nodeID2 = Integer.parseInt(values[3]);
                String[] coordsArray = values[4].split("\t");
                double[] coords = new double[coordsArray.length];

                for(int i = 0; i < coordsArray.length; i++) {
                    coords[i] = Double.parseDouble(coordsArray[i]);
                }

                Segment segment = new Segment(roadID, length, nodeID1, nodeID2, coords);
                Node node1 = this.nodes.get(segment.getNodeID1());
                Node node2 = this.nodes.get(segment.getNodeID2());
                node1.getOutNeighbours().add(segment);
                node2.getInNeighbours().add(segment);
                segment.setNode1(node1);
                segment.setNode2(node2);
                
                node1.getAdjacentNodes().add(node2);
                node2.getAdjacentNodes().add(node1);

                Road road = this.roads.get(segment.getRoadID());
                road.getSegments().add(segment);
                segment.setRoad(road);

                if(!road.isOneWay()) {
                    Segment reverseSegment = segment.reverseSegment();
                    node2.getOutNeighbours().add(reverseSegment);
                    node1.getInNeighbours().add(reverseSegment);
                    reverseSegment.setNode1(node2);
                    reverseSegment.setNode2(node1);
                    reverseSegment.setRoad(road);
                }

                //System.out.println(segment.toString());
            }
            data.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSearch() {    	
    	if(!isFilesLoaded) {
    		getTextOutputArea().setText("Search : You cannot search when you have not loaded any data files");
    		getSearchBox().setText("");
    		return;
    	}
    	
        /* LINEAR
         if(roadsByName.containsKey(getSearchBox().getText())) {
            List<Road> roadsByNameRoads = roadsByName.get(getSearchBox().getText());

            selectedSegments = new ArrayList<Segment>();
            for(Road road: roadsByNameRoads) {
                for(Segment segment : road.getSegments()) {
                    selectedSegments.add(segment);
                }
            }
            getTextOutputArea().setText(getSearchBox().getText());
            selectedNode = null;
            redraw();
        }
        */
    	clearSelectedItems(true);
    	
    	String prefix = getSearchBox().getText();
    	List<String> searchResults = trie.getRoadsWithPrefix(prefix);

    	if(!searchResults.isEmpty()) { 
    		//found results 
	    	if(searchResults.contains(prefix)) { 
	    		//exact results found, only highlight
	    		String searchResult = searchResults.get(searchResults.indexOf(prefix));
	    		List<Road> roadsByNameRoads = roadsByName.get(searchResult);
	    		
	    		getTextOutputArea().setText("Search : " + ((searchResult.length() != 1) ? "Found Multiple Roads with Same Name" : "Found A Single Road with that name")+"\n");	
	    		for(Road road: roadsByNameRoads) {
	    			getTextOutputArea().setText(getTextOutputArea().getText() + road.toFancyString() + "\n");
	                for(Segment segment : road.getSegments()) {
	                    selectedSegments.add(segment);
	                }
	            }
	    		
	    	} else {
	    		//results found, but not exact name therefore display all results
	    		getTextOutputArea().setText("Search : Found Multiple Roads from selected search input\n");
	    		
	    		boolean showFancyString = true;
	    		for(String searchResult : searchResults) {
		    		List<Road> roadsByNameRoads = roadsByName.get(searchResult);
		    		
		    		if(searchResults.size() > MAX_SEARCH_RESULTS) {
		    			showFancyString = false;	
		    		}
		    		
		    		for(Road road: roadsByNameRoads) {
		    			if(showFancyString) {
		    				getTextOutputArea().setText(getTextOutputArea().getText() + road.toFancyString() + "\n");
		    			} 
		    			for(Segment segment : road.getSegments()) {
		                    selectedSegments.add(segment);
		                }
		    		}
	    		}
	    		if(!showFancyString) {
	    			getTextOutputArea().setText("Search : Found Multiple Roads from selected search input - Too Many Results to Display");
	    		}
	    	}
    	} else {
    		//no results
    		getTextOutputArea().setText("Search: No Roads match the content in the search box.");
    	}
    	getTextOutputArea().setCaretPosition(0);
    }
    
    
    
    public List<Segment> findPath(Node start, Node goal, AStar aStarParam) {
    	for(Node node: nodes.values()) {
    		node.setFrom(null);
    		node.setVisited(false);
    		node.setCostToHere(0);
    	}
    	
    	PriorityQueue<FringeNode> fringe = new PriorityQueue<FringeNode>();
    	fringe.offer(new FringeNode(start, null, 0, estimate(start,goal)));
    	
    	while(!fringe.isEmpty()) {
    		FringeNode fringeNode = fringe.poll();
    		Node node = fringeNode.getNode();
    		
    		if(!node.isVisited()) {
    			node.setVisited(true);
    			node.setFrom(fringeNode.getFrom());
    			node.setCostToHere(fringeNode.getCostToHere());
    			
    			if(node == goal) {
    				break;
    			}
    			
    			for(Segment segment : node.getOutNeighbours()) {
    				Node neigh = segment.getNode2();
    				if(!neigh.isVisited()) {
    					double costToNeigh = fringeNode.getCostToHere() + segment.getLength();
    					
    					if(aStarParam == AStar.DISTANCE) {
    						costToNeigh = adjustCostToNeighWithDistance(segment, costToNeigh);
    					}
    					
    					double estTotal = costToNeigh + estimate(neigh, goal);
						fringe.offer(new FringeNode(neigh, node, costToNeigh, estTotal));
    				}
    			}
    		}
    	}
		return findPathSegments(start, goal, aStarParam);
    }
    
   private double adjustCostToNeighWithDistance(Segment segment, double costToNeigh) {
	   double speed = 0;
		switch(segment.getRoad().getSpeed()) {
			case(1):
				speed = 0.20;
				break;
			case(2):
				speed = 0.40;
				break;
			case(3):
				speed = 0.60;
				break;
			case(4):
				speed = 0.80;
				break;
			case(5):
				speed = 1.00;
				break;
			case(6):
				speed = 1.10;
				break;
			case(7):
				speed = 1.30;
				break;	
		}	
		return costToNeigh-(costToNeigh*speed);
	}

   public double estimate(Node node, Node goal) {
	   return node.getLocation().distance(goal.getLocation());
   }
   
   public List<Segment> findPathSegments(Node start, Node goal, AStar aStarParam) {
	    List<Segment> path = new ArrayList<Segment>();  	
		Node currentNode = goal;
		boolean segmentFound = false;
		while(currentNode != start) {
			segmentFound = false;
			for(int i = 0; i < currentNode.getInNeighbours().size(); i++){			
				Segment segment = currentNode.getInNeighbours().get(i);
				if(segment.getNode1() == currentNode.getFrom()){
					path.add(segment);
					if(aStarParam == AStar.DISTANCE && this.aStarParam == AStar.BOTH) {
						selectedNodes_DEBUG.add(currentNode.getFrom());
					} else {
						selectedNodes.add(currentNode.getFrom());
					}
					currentNode = currentNode.getFrom();
					segmentFound = true;
					break;
				}
			}
			if(!segmentFound) {
				return null;
			}
		}
		//Collections.reverse(path);
		return path;
   }  
   	
    public Set<Node> recArtPts() {
	    Set<Node> articulationPoints = new HashSet<Node>();
	    Node start = null;
	    int numbSubtrees = 0;

    	for(Node node: nodes.values()) {
    		node.setDepth(Double.POSITIVE_INFINITY);
    		node.setVisited(false);
    	}
	    
	    
    	while(!isAllNodesVisted()) {
    		numbSubtrees = 0;
	    	for(Node node: nodes.values()) {
	    		if(!node.isVisited()) {
	    			start = node;
	    			start.setDepth(0);
	    			break;
	    		}
	    	}
    		
	    	for(Node neighbour: start.getAdjacentNodes()) {
	    		if(neighbour.getDepth() == Double.POSITIVE_INFINITY) {
	    			recArtPts(articulationPoints, neighbour, 1, start);
	    			numbSubtrees++;
	    		}
	    	}
	    	
	    	if(numbSubtrees > 1) {
	    		articulationPoints.add(start);
	    	}
    		start.setVisited(true);
    	}
	    return articulationPoints;
    }
   
    private double recArtPts(Set<Node> articulationPoints, Node node, int depth, Node fromNode) {
    	node.setVisited(true);
		node.setDepth(depth);
		double reachBack = depth;
		for(Node neighbour: node.getAdjacentNodes()) {
			if(neighbour == fromNode) {
				continue;
			}
			
			if(neighbour.getDepth() < Double.POSITIVE_INFINITY) {
				reachBack = Math.min(neighbour.getDepth(), reachBack);
			} else {
				double childReach = recArtPts(articulationPoints, neighbour, depth+1, node);
				reachBack = Math.min(childReach, reachBack);
				if(childReach >= depth) {
					articulationPoints.add(node);
				}
			}
		}
		return reachBack;
	}
    
    
    public Set<Node> iterArtsPts() {
    	Set<Node> articulationPoints = new HashSet<Node>();
    	Node start = null;
    	int numSubtrees = 0;
    	
    	for(Node node: nodes.values()) {
    		node.setDepth(Double.POSITIVE_INFINITY);
    		node.setVisited(false);
    	}
    	
    	while(!isAllNodesVisted()) {
    		numSubtrees = 0;
    		for(Node node: nodes.values()) {
        		if(!node.isVisited()) {
        			start = node;
        			start.setDepth(0);
        			break;
        		}
        	}
    		
	    	for(Node neighbour: start.getAdjacentNodes()) {
	    		if(neighbour.getDepth() == Double.POSITIVE_INFINITY) {
	    			FringeNode startFringe = new FringeNode(start, 0, null);
	    			articulationPoints.addAll(this.iterArtPts(neighbour, startFringe));
	    			numSubtrees++;
	    		}
	    	}
	    	
	    	if(numSubtrees > 1) {
	    		articulationPoints.add(start);
	    	}
	    	start.setVisited(true);
    	}
	    	
    	return articulationPoints;
    }
    
    private Set<Node> iterArtPts(Node firstNode, FringeNode root) {
    	Set<Node> articulationPoints = new HashSet<Node>();
    	Stack<FringeNode> fringe = new Stack<FringeNode>();
    	
    	fringe.push(new FringeNode(firstNode, 1, root));
    			
    	while(!fringe.isEmpty()) {
    		FringeNode elem = fringe.peek();
    		Node node = elem.getNode();
    		node.setVisited(true);

    		if(elem.getChildren() == null) {
    			node.setDepth(elem.getDepth());
    			elem.setReach(elem.getDepth());
    			elem.setChildren(new ArrayDeque<Node>());
    			
    			for(Node neighbour: node.getAdjacentNodes()) {
    				if(neighbour != elem.getParent().getNode()) {
    					elem.getChildren().offer(neighbour);
    				}
    			}
    		} else if(!elem.getChildren().isEmpty()) {
    			Node child = elem.getChildren().poll();
    			if(child.getDepth() < Double.POSITIVE_INFINITY) {
    				elem.setReach(Math.min(elem.getReach(), child.getDepth()));
    			} else {
    				fringe.push(new FringeNode(child, node.getDepth()+1, elem));
    			}
    		} else {
    			if(node != firstNode) {
    				if(elem.getReach() >= elem.getParent().getDepth()) {
    					articulationPoints.add(elem.getParent().getNode());
    				}
    				elem.getParent().setReach(Math.min(elem.getParent().getReach(), elem.getReach()));
    			}
    			fringe.pop();
    		}
    	}
    	
    	return articulationPoints;
    }
    
    public boolean isAllNodesVisted() {
		for(Node node: nodes.values()) {
			if(!node.isVisited()) {
				return false;
			}
		}
		return true;
    }

	public void clearSelectedItems() {
	    clearSelectedItems(false);
    }
    public void clearSelectedItems(boolean clearItemsOnly) {
    	selectedNodes.clear();
		selectedSegments.clear();
		selectedNodes_DEBUG.clear();
		selectedSegments_DEBUG.clear();
		if(!clearItemsOnly) {
			getSearchBox().setText(null);
			getTextOutputArea().setText(null);
		}
    }
    
    public static void main(String[] args) { 
    	new Mapper(); 
    }
}
