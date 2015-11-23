
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

public class Map {
	
	static float targetDistance;
	static Node solution;
	static Node startNode;
	
    public static void printPath(Node target)
    {
        List<Node> path = new ArrayList<Node>();
    
	    for(Node node = target; node!=null; node = node.parent)
	        path.add(node);
	
	    Collections.reverse(path);
	    
	    for(int i = 0; i < path.size(); i++)
	    	//System.out.print(path.get(i) + " ");
	    	System.out.print(path.get(i) + " ");//+ ": Score: " + path.get(i).f_score + " " );
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
	        //System.out.println("Current: " + current);
	
	        visited.add(current);
	
	        for(Edge e : current.adjacencies)
	        {
	        	Node child = handler.nodeMap.get(e.refsList.get(0));
	        	//Node child = e.target;
	        	
	        	if(visited.contains(child))
	         		continue;
	        	     	          	
	            double cost = e.d_score;  /////////////here
	            double dist = e.distance;
	            double temp_distance = current.distance + dist;
	            double temp_g_score = current.g_score + cost;
	                 
	            if(queue.contains(child) )
	            	if(temp_g_score >= child.g_score)
	            		continue;
	            	else queue.remove(child);
	                                          
	            child.distance = temp_distance;
	            child.computeError(startNode, targetDistance);
	            child.f_score = temp_g_score + child.error;
	            child.parent = current; 
	            child.g_score = temp_g_score;
	                 
	            queue.add(child);
	            //System.out.println("value: " + child.value + " f_score: " + child.f_score);
	           
	            printPath(child);
	            
	            if(child.distance >= targetDistance)
	            	done = true;
	            
	            solution = child;     
	           }
	        }
	    
	    
	    //printPath(solution);
	    System.out.println(" ");
}       

  public static void main(String[] args) throws Exception {
    SAXParserFactory parserFactor = SAXParserFactory.newInstance();
    SAXParser parser = parserFactor.newSAXParser();
    SAXHandler handler = new SAXHandler();
    parser.parse(ClassLoader.getSystemResourceAsStream("map.xml"), 
                 handler);
    handler.parse();
  for(int i = 0; i < handler.edgeList.size(); i++)
    	System.out.println(handler.edgeList.get(i).refsList.size());
    targetDistance = 14;
    startNode = handler.nodeMap.get("3783880214");
    AstarSearch(startNode, targetDistance, handler); 

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
  Node node = null;
  
  List<Edge> edgeList = new ArrayList<>();
  Edge edge = null;
  
  HashMap<Node, Integer> node_histogram = new HashMap<Node, Integer>();
  
  boolean isNode = false;
  boolean isRoad = false;
  boolean isFootway = false;
  boolean hasSidewalkKey = false;
  boolean hasSidewalkTrueValue = false;
  boolean keepEdge = false;
  
  String content = null;
  @Override
  //Triggered when the start of tag is found.
  public void startElement(String uri, String localName, 
                           String qName, Attributes attributes) 
                           throws SAXException {
   
    Node node = new Node();

    switch(qName){
      //Create a new Node object when the start tag is found
      
    	case "node":
        node = new Node();
        node.id = attributes.getValue("id");
        node.latitude = Float.parseFloat(attributes.getValue("lat"));
        node.longitude = Float.parseFloat(attributes.getValue("lon"));
        nodeMap.put(node.id, node);
        isNode = true;
        break;
        
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
	    	  //System.out.println(nodeMap.get(attributes.getValue("ref")));
    	 
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
		
		float lon1 = nodeMap.get(e.refsList.get(0)).longitude;
		float lon2 = nodeMap.get(e.refsList.get(1)).longitude;
		float lat1 = nodeMap.get(e.refsList.get(0)).latitude;
		float lat2 = nodeMap.get(e.refsList.get(1)).latitude;
		
		double theta = lon1 - lon2;
		double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
		dist = Math.acos(dist);
		dist = Math.toDegrees(dist);
		dist = dist * 60 * 1.1515;
		
		return (dist);
	}
  
  public void parse()
  {
	 int count = 0;
	for(int i = 0; i < edgeList.size(); i++)
	{
		Edge currEdge =edgeList.get(i);
		for(int keyIndex = 0; keyIndex < currEdge.keys.size(); keyIndex++)
		{
			if(currEdge.keys.get(keyIndex) == "highway")
				isRoad = true;
			
			if(currEdge.keys.get(keyIndex) == "sidewalk")
				hasSidewalkKey = true;
		}
		for(int valIndex = 0; valIndex < currEdge.values.size(); valIndex++)
		{
			if(currEdge.values.get(valIndex) == "footway")
				isFootway = true;
			if(currEdge.values.get(valIndex) == "left" || currEdge.values.get(valIndex) == "right" || currEdge.values.get(valIndex) == "both")
				hasSidewalkTrueValue = true;
		}
		
		if(isRoad && !isFootway)
			keepEdge = true;
		
		if(hasSidewalkKey && hasSidewalkTrueValue)
			edgeList.get(i).d_score = 0;
		
		if(!keepEdge)
		{
			edgeList.get(i).id = "0";
			
		}
			
		//if a way has a key of highway, set isRoad to true and use that bool to keep or not
		//if a way has a value of footway, set isFootway to true and also use that
		
		if (currEdge.refsList.size() < 2)//if a way has only one node, delete it out of the osm collection
		{   edgeList.get(i).id = "0";
			if(currEdge.refsList.size() == 1)
				nodeMap.get(currEdge.refsList.get(0)).adjacencies.remove(currEdge);
			continue;
		}
		else if(currEdge.refsList.size() == 2)
		{		
			if(currEdge.id != edgeList.get(i).id)
			{System.out.println("What????"); count++; System.out.print(count);}
			
			edgeList.get(i).distance = getEdgeDistance(currEdge);
			continue;
		}
		
	//loop through this until edges have only two nodes adjacent to them
        else
        {   
        	int j = 0;
        	while( currEdge.refsList.size() > 2) //if there are > 2 nodes for edge
			{				   
        	   String middleNodeId = currEdge.refsList.get(1);
        	   String leftNodeId = currEdge.refsList.get(0);
        	   
        	   if(nodeMap.get(middleNodeId).adjacencies.size() > 1) // if the middle node has more than 1 edge
        	   {
        		   //split the edge into 2:
																
					Edge newEdge = new Edge();
					newEdge.refsList.add(leftNodeId);
					newEdge.refsList.add(middleNodeId);

					String newIndex = Integer.toString(j);
					String newId = currEdge.id + newIndex;
					newEdge.id = newId;
					newEdge.keys = currEdge.keys;
					newEdge.values = currEdge.values;
					newEdge.distance = getEdgeDistance(newEdge);
					edgeList.add(newEdge);
					
					nodeMap.get(leftNodeId).adjacencies.addElement(newEdge);
					nodeMap.get(middleNodeId).adjacencies.addElement(newEdge);
					
					currEdge.refsList.remove(leftNodeId); //delete left node id from refs list of original edge (remove edgeList[i].refsList[0])
					nodeMap.get(leftNodeId).adjacencies.remove(nodeMap.get(leftNodeId).adjacencies.indexOf(currEdge)); //delete original edge from the node's adjacency list
				  }
            	   else
            	   {
            		   currEdge.refsList.remove(leftNodeId);
            		   nodeMap.get(leftNodeId).adjacencies.remove(currEdge);
            	   }
        	   		if(currEdge.refsList.size() == 2)
        	   			edgeList.get(i).distance = getEdgeDistance(currEdge);
        	   			
				}
               edgeList.set(i, currEdge);
            }	
		}
		for(int i = 0; i < edgeList.size(); i++){
			if (edgeList.get(i).id == "0");
			edgeList.remove(i);
	
		}
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
  public float latitude;
  public float longitude;
    
  public Node()
  {    
      distance = 0;   
  }
  
  void computeError(Node startNode, float targetDistance)
  {
  	float startLat = startNode.latitude;
  	float startLon = startNode.longitude;
  	float endLat = this.latitude;
  	float endLon = this.longitude;
  	double straightLineDistance = Math.sqrt(Math.pow((startLat-endLat), 2) + Math.pow((startLon-endLon), 2));
  	//System.out.println(value + ": " + "SLD: " + straightLineDistance );
  	//double distanceBack = Math.abs(startLat-endLat) + Math.abs(startLon-endLon);
  	//System.out.println(value + ": distance: "+ this.distance);
  	this.error = Math.abs(targetDistance -(this.distance + straightLineDistance))*155; 	
  	//error = Math.abs(targetDistance -(distance + distanceBack));  	      	 
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
   	{	//System.out.println("equals!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
   		return 0;
   	}
   	
   	else if(f_score > i.f_score)
   	{
   		//System.out.println(value + ": " + f_score + "is greater than " + i.value + ": " + i.f_score + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
   		return 1;
   	}
   	else 
		{
   		//System.out.println(value + ": " + f_score + " is less than " + i.value + ": " + i.f_score + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		return -1;
		}
  } 
   
  @Override
  public String toString() {
    return "id: "  + id + " lat: " + latitude + " lon: " + longitude + " adjacency: " + adjacencies.get(0);
  }
}
 class Edge {
	public String value;
	public String key;
	public String ref;
	public Vector<String> refsList=new Vector<String>();
	public String nd;
	String id;
	public Vector<String> keys=new Vector<String>();
	public Vector<String> values=new Vector<String>();
	

	@Override
	  public String toString() {
		    return "id: "  + id + " ref: " + refsList.get(0) ;
		  }
	//search stuff
	public double distance;
	 //public final Node target;
	 public double d_score;

	 public Edge()
	 {
	        // = targetNode; //fix
	         distance = 0;  
	         d_score = 300;
	 }
 }
 
 
 
