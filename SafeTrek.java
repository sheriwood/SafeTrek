import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
//import de.fhpotsdam.unfolding.examples.SimpleMapApp;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.providers.OpenStreetMap.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

//delete node 420830572 from .osm file before running.
public class HelloUnfoldingWorld extends PApplet {

	private static final long serialVersionUID = 1L;

	public static void main(String args[]) {
		PApplet.main(new String[] { HelloUnfoldingWorld.class.getName() });
		System.out.println("here");
	}
	//AI_Project.Map m;
	UnfoldingMap map;
	MapSearch mapSearch;
	
	Vector<Double> coordinates;
	
	public void setup() {
		try {
			
			coordinates = MapSearch.go();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double centerLatitude = coordinates.get(0);
		double centerLongitude = coordinates.get(1);
		
		Location location = new Location(centerLatitude, centerLongitude);
		SimplePointMarker marker = new SimplePointMarker(location);
		SimpleLinesMarker connectionMarker;
		marker.setColor(color(255, 0, 0));
		
		map = new UnfoldingMap(this, new OpenStreetMapProvider());
		map.zoomAndPanTo(15, new Location(centerLatitude, centerLongitude));
		map.addMarker(marker);
		
		for(int i = 0; i < coordinates.size()-3; i+=2)
		{
			Location startLocation = new Location(coordinates.get(i), coordinates.get(i+1)); //fix these now that coordinates is now one vector
			Location endLocation = new Location(coordinates.get(i+2), coordinates.get(i+3));
			connectionMarker = new SimpleLinesMarker(startLocation, endLocation);
			connectionMarker.setStrokeColor(color(255, 0, 0));
			connectionMarker.setStrokeWeight(4);
			//marker = new SimplePointMarker(endLocation);
			//map.addMarker(marker);
						
			map.addMarkers(connectionMarker);
		}
		size(800, 600, OPENGL);

		MapUtils.createDefaultEventDispatcher(this, map);
	}

	public void draw() {
		background(0);
		map.draw();
	}

}



class MapSearch {
	
	static float targetDistance;
	static Node solution;
	static Node startNode;
	static Vector<Double> coordinates=new Vector<Double>();
   // static Vector<Float> longitudes=new Vector<Float>();
	
   //static HelloUnfoldingWorld mapRenderer;
    
    public static void printPath(Node target)
    {
        List<Node> path = new ArrayList<Node>();
    
	    for(Node node = target; node!=null; node = node.parent)
	        path.add(node);
	
	    Collections.reverse(path);
	    
	    for(int i = 0; i < path.size(); i++)
	    {	//System.out.print(path.get(i) + " ");
	    	coordinates.add(path.get(i).latitude);
	    	coordinates.add(path.get(i).longitude);
	    	System.out.print(path.get(i).id + "(" + path.get(i).adjacencies.size() +")"+  " Distance: " + path.get(i).distance + " Score: " + path.get(i).f_score + " DistanceBack: " + path.get(i).distanceBack + " " );
	    	//for(int j = 0; j < path.get(i).adjacencies.size(); j++)
		          // System.out.println("edges: " + path.get(i).adjacencies.get(j).id);
	    }
	    System.out.println("");
    }
    
    public static void AstarSearch(Node source, float targetDistance, SAXHandler handler)
    {
	    Set<Node> visited = new HashSet<Node>();
	
	    PriorityQueue<Node> queue = new PriorityQueue<Node>();

	    source.g_score = 0;
	
	    queue.add(source);
	
	    boolean done = false;
	
	    while(!queue.isEmpty() && !done)
	    {
	        Node current = queue.poll(); //retrieve and remove head
	
	        visited.add(current);
	
	        for(Edge e : current.adjacencies)
	        {
	        	Node child;
	        	if(e.refsList.get(0).equals(current.id))
	        		child = handler.nodeMap.get(e.refsList.get(1));
	        	else
	        		child = handler.nodeMap.get(e.refsList.get(0));
	        		
	        	if(visited.contains(child))
	         		continue;
	        	     	          	
	            double cost = e.d_score;  
	            double dist = e.distance;
	            double temp_distance = current.distance + dist;
	            double temp_g_score = current.g_score + cost;
	                 
	            if(queue.contains(child) )
	            	if(temp_g_score >= child.g_score)
	            		continue;
	            	else queue.remove(child);
	                                          
	            child.distance = temp_distance;
	            child.error = child.computeError(startNode, targetDistance, current.distance);
	            child.f_score = temp_g_score + child.error;
	            child.parent = current; 
	            child.g_score = temp_g_score;
	                 
	            queue.add(child);
	            //System.out.println("value: " + child.value + " f_score: " + child.f_score);
	           
	            //printPath(child);
	            
	            //if(child.distance >= targetDistance + .2)
	            if(child.distance > targetDistance/2 && child.distanceBack < .17)
	            	done = true;
	            
	            solution = child;     
	           }
	        if(queue.isEmpty())
	        	System.out.println("empty!");
	        else if(done)
	        	System.out.println("done");
	        }
	    	    
	   printPath(solution);
	    System.out.println(" ");
}       

  //public static void main(String[] args) throws Exception {
    public static Vector<Double> go() throws ParserConfigurationException, SAXException, IOException {
    SAXParserFactory parserFactor = SAXParserFactory.newInstance();
    SAXParser parser = parserFactor.newSAXParser();
    SAXHandler handler = new SAXHandler();
    parser.parse(ClassLoader.getSystemResourceAsStream("newmap.xml"), 
                 handler);
    
    handler.parse();
          
    targetDistance = 6;
    //double startLat= 41.726074; //fair grounds
    //double startLon = -111.848040;
    //double startLat = 41.748604; //Mt. Logan
    //double startLon = -111.829408;
    //double startLat = 41.744546; //university
    //double startLon = -111.814177;
    //double startLat = 41.758431; //1400 north top of hill
    //double startLon = -111.798011;
    double startLat = 41.748330; //USU Spectrum (6)
    double startLon = -111.813947;
   
    
    boolean foundNode = false;
    double minDistanceToNode = 100;
    
    for (HashMap.Entry<String,Node> entry : handler.nodeMap.entrySet()) {
        
        String key = entry.getKey();
    	Node node = entry.getValue();
    	    	
        //if(Math.abs(node.latitude - startLat) < .0005 && Math.abs(node.longitude - startLon) < .0005)
    	
    	double distanceToNode = Math.sqrt(Math.pow((node.latitude-startLat), 2) + Math.pow((node.longitude-startLon), 2));
    	if(distanceToNode < minDistanceToNode)
        {
        	 minDistanceToNode = distanceToNode; 
    		 startNode = handler.nodeMap.get(key);
        	 foundNode = true;
        	 continue;
        }    
    }
    //startNode = handler.nodeMap.get("428959019");
    if(!foundNode)
    	startNode = handler.nodeMap.get("3756777137");
    AstarSearch(startNode, targetDistance, handler); 
    
    return coordinates;
    //Printing the list
   // for ( Edge edge : handler.edgeList){
    //  System.out.println(edge);
    //} 
  }
}
/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

 HashMap<String, Node> nodeMap = new HashMap<String, Node>();
  //List<Node> nodeList = new ArrayList<>();
    
  static List<Edge> edgeList = new ArrayList<>();
  Edge edge = null;
  Node node = null;
  HashMap<Node, Integer> node_histogram = new HashMap<Node, Integer>();
  
  boolean isNode = false;
  
  String content = null;
  @Override
  //Triggered when the start of tag is found.
  public void startElement(String uri, String localName, 
                           String qName, Attributes attributes) 
                           throws SAXException {
    switch(qName){
      //Create a new Node object when the start tag is found  
    	case "node":
    	{
        node = new Node();
         
        node.id = attributes.getValue("id");
        
        node.latitude = Double.parseDouble(attributes.getValue("lat"));
        node.longitude = Double.parseDouble(attributes.getValue("lon"));
       
        nodeMap.put(node.id, node);

        //System.out.println(nodeMap.get(node.id).id);

        isNode = true;
        break;
    	}
        
      case "way":
          edge = new Edge();
          edge.id = attributes.getValue("id");
          isNode = false;
         
          break;
          
      case "tag":
    	  
    	  if(!isNode)
    	  {
    		  edge.keys.add(attributes.getValue("k"));
    		  edge.values.add(attributes.getValue("v"));
    	  }	  
          break;
          
      case "nd":
    	   nodeMap.get(attributes.getValue("ref")).adjacencies.add(edge);
    	       		 
	    	  edge.refsList.add(attributes.getValue("ref"));
          break;
      case "bounds":
      {
    	  break;
      }
    } 
  }

  @Override
  public void endElement(String uri, String localName, 
                         String qName) throws SAXException {
	   switch(qName){
	  
	     case "way":
	         edgeList.add(edge);
	       break;	    
	   }
  }

  @Override
  public void characters(char[] ch, int start, int length) 
          throws SAXException {
    content = String.copyValueOf(ch, start, length).trim();
  }
  
  public double getEdgeDistance(Edge e) {
		
		double lon1 = nodeMap.get(e.refsList.get(0)).longitude;
		double lon2 = nodeMap.get(e.refsList.get(1)).longitude;
		double lat1 = nodeMap.get(e.refsList.get(0)).latitude;
		double lat2 = nodeMap.get(e.refsList.get(1)).latitude;
		
		double theta = lon1 - lon2;
		double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		dist = dist * 60 * 1.1515;
		
		return (dist);
	}
  
  public void removeEdge(Edge edge, int edgeIndex)
  {
	  if(edge.refsList.size() > 0)
	  {  for(int i = 0; i < edge.refsList.size(); i++)
			nodeMap.get(edge.refsList.get(i)).adjacencies.remove(edge);
	  }
	  edgeList.remove(edgeIndex);
  }
  
  public void parse()
  {
	  int listSize = edgeList.size();
	  
	  //take out all the nodes connected to only one edge
	  boolean changes = true;
	  
	 // while(changes)
	  //{
		  changes = false;
		  Vector<String> nodesToRemove=new Vector<String>();
		  
		  for (HashMap.Entry<String, Node> entry : nodeMap.entrySet()) 
		  {
			 
			String key = entry.getKey();
			Node node = entry.getValue();
			
			if(node.adjacencies.size() < 2) //if the node is only on one edge, remove it.
			{	for(int adjIdx = 0 ; adjIdx < node.adjacencies.size(); adjIdx++)
				{	
					int edgeIdx = edgeList.indexOf(node.adjacencies.get(adjIdx)); //take the node from any edgeList refsList
					edgeList.get(edgeIdx).refsList.remove(key);
				}
				nodesToRemove.addElement(key);
				changes = true;
				
			}			
		  }
		  for(int removeIdx = 0; removeIdx < nodesToRemove.size(); removeIdx++)
		  {
			  nodeMap.remove(nodesToRemove.get(removeIdx));
		  }
		  
		  //take out all the edges with only one node
		  listSize = edgeList.size();
		  Vector<Edge> edgesToRemove=new Vector<Edge>();
		  
		  for(int i = 0; i < listSize; i++)
		  {
			  Edge currEdge = edgeList.get(i);
			  if(currEdge.refsList.size() < 2)
			  {  
				  for(int j = 0; j < currEdge.refsList.size(); j++)
					  nodeMap.get(currEdge.refsList.get(j)).adjacencies.remove(currEdge); //remove edge from any nodes' adjacency list
				  
				  edgesToRemove.addElement(currEdge);
				 // edgeList.remove(i); //remove edge from edgeList
				  changes = true;
			  }		
		  }
		  for(int removeIdx = 0; removeIdx < edgesToRemove.size(); removeIdx++)
		  {
			  edgeList.remove(edgesToRemove.get(removeIdx));
		  }
	  //}
	  
	  //get rid of footways, rivers, and such. Add scores for sidewalks.
	  for(int i = 0; i < listSize; i++)
	  {
		 
		Edge currEdge =edgeList.get(i);
				
		for(int keyIndex = 0; keyIndex < currEdge.keys.size(); keyIndex++)
		{
			if(currEdge.keys.get(keyIndex).equals("highway"))
				currEdge.isRoad = true;
			
			if(currEdge.keys.get(keyIndex).equals("sidewalk"))
				currEdge.hasSidewalkKey = true;
		}
		for(int valIndex = 0; valIndex < currEdge.values.size(); valIndex++)
		{
			if(currEdge.values.get(valIndex).equals("footway") || currEdge.values.get(valIndex).equals("cycleway"))
				currEdge.isOtherWay = true;
			if(currEdge.values.get(valIndex).equals("left") || currEdge.values.get(valIndex).equals("right") || currEdge.values.get(valIndex).equals("both"))
				currEdge.hasSidewalkTrueValue = true;
		}
		
		if(currEdge.isRoad && !currEdge.isOtherWay)
			currEdge.keepEdge = true;
		
		if(currEdge.hasSidewalkKey && currEdge.hasSidewalkTrueValue)
			edgeList.get(i).d_score = 0;
		
		if(!currEdge.keepEdge)
			edgeList.get(i).id = "0";
		
		//remove all nodes with 1 or 0 edges

	
		//remove edges that are dead ends (1 or 0 nodes)
		/*for(int adjIndex = 0; adjIndex < currEdge.refsList.size(); adjIndex++)
		{
		 String nodeId = currEdge.refsList.get(adjIndex);
		
     	   if(nodeMap.get(nodeId).adjacencies.size() < 2)
     	   {
     		   currEdge.refsList.remove(nodeId); 		  
     		   nodeMap.remove(nodeId);
     		   edgeList.get(i).id = "0";
     	   }
		}*/
		
		/*if (currEdge.refsList.size() < 2)//if a way has only one node, delete it out of the edgelist
		{   edgeList.get(i).id = "0";
		//if(currEdge.refsList.size() == 1)
			//nodeMap.get(currEdge.refsList.get(0)).adjacencies.remove(currEdge);
			continue;
		}*/
		if(currEdge.refsList.size() == 2)
		{		
			edgeList.get(i).distance = getEdgeDistance(currEdge);
			continue;
		}
		
	//loop through this until edges have only two nodes adjacent to them
        else
        {   
        	int j = 0;
        	while( currEdge.refsList.size() > 2) //if there are > 2 nodes for edge
			{				   
        		
        	   String rightNodeId = currEdge.refsList.get(1);
        	   
        	   String leftNodeId = currEdge.refsList.get(0);
        	   
        	   /*if(nodeMap.get(rightNodeId).adjacencies.size() == 1) //if the node is only on that edge, remove it
        	   {
        		   edgeList.get(i).refsList.remove(rightNodeId);
        		   nodeMap.remove(rightNodeId);
        		   if(currEdge.refsList.size() == 2)
        			   continue;
        	   }*/
        	        	   
        	   //else if(nodeMap.get(rightNodeId).adjacencies.size() > 1) // if the 2nd node has more than 1 edge
        	  // {
        		   //split the edge into 2:	
        	   
					Edge newEdge = new Edge();
					newEdge.refsList.add(leftNodeId);
					newEdge.refsList.add(rightNodeId);

					String newIndex = Integer.toString(j);
					String newId = currEdge.id.concat(newIndex);
					newEdge.id = newId;
					newEdge.keys = currEdge.keys;
					newEdge.values = currEdge.values;
					newEdge.distance = getEdgeDistance(newEdge);
					edgeList.add(newEdge);
					
					nodeMap.get(leftNodeId).adjacencies.add(newEdge);
					nodeMap.get(rightNodeId).adjacencies.add(newEdge);
					
					edgeList.get(i).refsList.remove(leftNodeId); //delete left node id from refs list of original edge (remove edgeList[i].refsList[0])
					
					int adjIndex = nodeMap.get(leftNodeId).adjacencies.indexOf(currEdge);
					nodeMap.get(leftNodeId).adjacencies.remove(adjIndex); //delete original edge from the node's adjacency list
				 // }
            	   
        	   		if(currEdge.refsList.size() == 2)
        	   			edgeList.get(i).distance = getEdgeDistance(currEdge);
        	   		j++;
				}
               
            }	//edgeList.set(i, currEdge);
		}
		//for(int i = 0; i < edgeList.size(); i++){
		//	if (edgeList.get(i).id.equals( "0"));	
				//removeEdge(edgeList.get(i), i);
		//}
	  //int newListSize = edgeList.size();
	  for(int i = 0; i < edgeList.size(); i++)
	  {
	    Edge currEdge = edgeList.get(i);
		 
	      if(currEdge.id.compareTo( "1000") < 0) //if the id has been set to 0 or 00.
		  {
			  if(currEdge.refsList.size() > 0) //if the edge has nodes, remove all edges from nodes
			  {  
				  for(int j = 0; j < currEdge.refsList.size(); j++) 
				  {
					  nodeMap.get(currEdge.refsList.get(j)).adjacencies.remove(currEdge);
				  }
			  }
		  edgeList.remove(i); //remove edge from list
		  i--; 
		  }
	  
	   }
	  for(int i = 0; i < nodeMap.get("82911829").adjacencies.size(); i++)
		  System.out.println(nodeMap.get("82911829").adjacencies.get(i).id);
	  
	  for(int i = 0; i < edgeList.size(); i++)
	  {
		  if(edgeList.get(i).id.equals("1829519713") )
			  System.out.println("danger score" + edgeList.get(i).d_score);
	  }
	 /* for(int i = 0; i < edgeList.size(); i++)
	  {
		 if(edgeList.get(i).id.equals("100842607"))
		  System.out.println(i + "*******" +edgeList.get(i).refsList.get(0) + " " + edgeList.get(i).refsList.get(1)); 	 
	  }*/
	}
}

class Node implements Comparable<Node> {

  String id;
  public Vector<Edge> adjacencies=new Vector<Edge>();
  
  //public final String value;
  public double g_score = Integer.MAX_VALUE; //total distance from start node to this one
  public double error = 0;
  public double f_score = 0; // g+h
  public Node parent;
  public double distance;
  public double latitude;
  public double longitude;
  double distanceBack;
    
  public Node()
  {    
      distance = 0;   
  }
  
  double computeError(Node startNode, float targetDistance, double currDistance)
  {
  	double startLat = startNode.latitude;
  	double startLon = startNode.longitude;
  	double endLat = this.latitude;
  	double endLon = this.longitude;
  	
  	//double straightLineDistance = Math.sqrt(Math.pow((startLat-endLat), 2) + Math.pow((startLon-endLon), 2));
  	//System.out.println(value + ": " + "SLD: " + straightLineDistance );
  	double theta = startLon - endLon;
	double straightLineDistance = Math.sin(Math.toRadians(startLat)) * Math.sin(Math.toRadians(endLat)) + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) * Math.cos(Math.toRadians(theta));
	straightLineDistance = Math.acos(straightLineDistance);
	straightLineDistance = Math.toDegrees(straightLineDistance);
	straightLineDistance = straightLineDistance * 60 * 1.1515;
    //double distanceBack = Math.abs(startLat-endLat) + Math.abs(startLon-endLon);
    this.distanceBack = straightLineDistance;
  	//System.out.println(value + ": distance: "+ this.distance);
  	
  	//return Math.abs(targetDistance -(this.distance + straightLineDistance))*3000; 	
    //return Math.abs(targetDistance -(currDistance + distanceBack)) * 3000;
    return Math.abs(targetDistance -(distance + straightLineDistance))*3000;
  
  }
  
  public boolean equals(Node i, Node j)
  {
		 if(i.f_score == j.f_score)
			 return true;
		 else return false;
  }

  @Override
  public int compareTo(Node i)
  {
   	if(this.equals(i))
   	{	
   		return 0;
   	}
   	
   	else if(f_score > i.f_score)   		
   		return 1;
   	
   	else 
		return -1;
  } 
   
  @Override
  public String toString() {
    return "id: "  + id + " lat: " + latitude + " lon: " + longitude + " adjacency: " + adjacencies.get(0).id;
  }
}
 class Edge {
	public String value;
	public String key;
	public String ref;
	public Vector<String> refsList=new Vector<String>();
	public String nd;
	public String id;
	public boolean isRoad;
	boolean isOtherWay;
	boolean hasSidewalkKey;
	boolean hasSidewalkTrueValue;
	boolean keepEdge;
	public Vector<String> keys=new Vector<String>();
	public Vector<String> values=new Vector<String>();
	
	@Override
	  public String toString() {
		    return "id: "  + id + " ref: " + refsList.get(0) ;
		  }
	public double distance;
	public double d_score;

	 public Edge()
	 {
	        // = targetNode; //fix
	         distance = 0;  
	         d_score = 300;
	         isRoad = false;
	         isOtherWay = false;
	         hasSidewalkKey = false;
	         hasSidewalkTrueValue = false;
	         keepEdge = false;
	 }
 }
 
 
