package dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import node.*;


public class Djikstra {
    
    Cache cache;
    Stack<PathObject> currentPath = new Stack<>();
    ArrayList<PathObject> pool = new ArrayList<PathObject>();

    Set<RegisteredNode> visitedNodes = new HashSet<RegisteredNode>();

    ArrayList<RegisteredNode> unvisitedNodes = new ArrayList<>();
    ArrayList<RegisteredNode> masterList;

    int totalWeight = 0;

    RegisteredNode startNode;

    public Djikstra(Cache cache, ArrayList<RegisteredNode> masterList) {
        this.cache = cache;
        this.masterList = masterList;
        populateUnvisited(masterList);
    }

    private void populateUnvisited(ArrayList<RegisteredNode> masterList) {
        for(RegisteredNode node : masterList) {
            unvisitedNodes.add(node);
        }
    }

    /*
     * this method is going to find the shortest distance to all 
     * other nodes in the overlay from starting point
     * in my actual implementation starting point will likely be something like string IP
     */
    public void findAllRoutes(String startPointAddress, int port) {
        RegisteredNode current = findStartPoint(startPointAddress /*  port*/);
        this.startNode = current;

        System.out.println("DEBUG: start node = " + current.ip);
        //create pool objects
        visitNodeProtocol(current);
        //in the while loop first thing is to find the shortest weight in the pool
        while(unvisitedNodes.size() > 0) {
            //find the smallest edge weight
            PathObject smallestWeightObject = getSmallestPoolObject();
            //grab the path needed to get there
            if(current.equals(smallestWeightObject.from)) {

                //travel there and add to total weight
                //grab local weight from object
                totalWeight += smallestWeightObject.localWeight;
                //we need to add this to curent path
                PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                System.out.println("path object that was seelcted has a total weight of " + currentPathObject.weight);
                currentPath.add(currentPathObject);
                current = smallestWeightObject.to;
                visitNodeProtocol(current);

                
                //everyhting above this point is tested and good
                //everything below is ass
            } else {
                System.out.println("have to backtrack, clearing currentPath");
                current = startNode;
                totalWeight -= totalWeight;
                System.out.println("I have reset myself to node " + current.ip + " and the total weight is " + totalWeight);
                currentPath.clear();
                if(current.equals(smallestWeightObject.from)) {
                    //neighbor of start node baby
                    totalWeight += smallestWeightObject.localWeight;
                //we need to add this to curent path
                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                    System.out.println("path object that was seelcted has a total weight of " + currentPathObject.weight);
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    visitNodeProtocol(current);
                } else {
                    //we need to locate the from node
                    System.out.println("trying to find cached route with target = " + smallestWeightObject.from.ip);
                    Cache.CacheObject path = cache.findCachedRoute(smallestWeightObject.from);
                    currentPath.addAll(path.cachedRouteObject);
                    totalWeight += cache.sumPathWeight(path);
                    current = smallestWeightObject.from;
                    totalWeight += smallestWeightObject.localWeight;

                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                    System.out.println("path object that was seelcted has a total weight of " + currentPathObject.weight);
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    visitNodeProtocol(current);
                }
                /* 
                System.out.println("going back to start node");
                current = reverse(current); //takes you back the start node
                System.out.println("after reversing my curernt node is  " + current.ip + " and the total weight is " + totalWeight);
                if(current.equals(smallestWeightObject.from)) {
                    //make the move, and do visited protocol
                    System.out.println("after reversing making connection without traveling");
                    totalWeight += smallestWeightObject.localWeight;
                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    visitNodeProtocol(current);

                } else {
                    System.out.println("traveling to from node and that is node = " + smallestWeightObject.from.ip);
                    current = travelToFromNode(current, smallestWeightObject.from);
                    System.out.println("current now equals " + current.ip);
                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    System.out.println("current now equals " + current.ip);
                    visitNodeProtocol(current);
                }
                */
            }
                
        }


        System.out.println("FINAL: priting cached routes");
        cache.displayCachedRoutes();

    }

    private RegisteredNode reverse(RegisteredNode current) {
        //take an entry from the stack
        
        while(true) {
        if(currentPath.size() != 0) {
            System.out.println("actively reversing from " + current.ip);
            PathObject pathToGo = currentPath.pop();
            System.out.println("I am supposed to reverse to " + pathToGo.from.ip);
            totalWeight -= pathToGo.weight; //really its the local weight
            current = pathToGo.from; 
            if(totalWeight == 0) {
                System.out.println("ahah");
                break;
            }
        }
    }
        return current;

    }

    /* 
    private RegisteredNode travelToFromNode(RegisteredNode current, RegisteredNode fromNode) {
        String pathStr = cache.getRoute(fromNode.ip);
        pathStr = pathStr.replace("-", " ");
        String[] pathArr = pathStr.split(" ");
       
        System.out.println("path arr at  zero  " + pathArr[0]);
        for(int i = 0; i < pathArr.length - 3; i += 2) {
            //make path object with reg at i, weight at i + 1, and reg at i + 2
            RegisteredNode from = findStartPoint(pathArr[i]);
            System.out.println("CREATED A FROM NODE QITH IP = " + from.ip);
            int weight = Integer.parseInt(pathArr[i + 1]);
            RegisteredNode to = findStartPoint(pathArr[i + 2]);
            System.out.println("CREATED A TO NODE QITH IP = " + to.ip);
            PathObject pathObject = new PathObject(from, to, weight);
            currentPath.add(pathObject);
        }
       
        current = fromNode;
        return current;
    }
    */

   public int collectPathWeight(String str) { //dead function
    int sum = 0;
    str = str.replace("-", " ");
    String[] path = str.split(" ");
    System.out.println(Arrays.toString(path));
    for(int i = 1; i < path.length - 1; i += 2) {
        sum += Integer.parseInt(path[i]);
    }
    return sum;
   }

    private RegisteredNode findStartPoint(String address /*int port*/) {
        RegisteredNode startPoint = null;
        for(int i = 0; i < masterList.size(); ++i) {
            if(masterList.get(i).ip.equals(address) /*  && unvisitedNodes.get(i).portNum == port*/) {
                startPoint = masterList.get(i);
                break;
            }
        }
        return startPoint;
    }

    /*
     * this method is going to call spawn pool objects and then add itself to the visited set
     * on the condition that it is not already in the pool 
     */
    private void visitNodeProtocol(RegisteredNode currentNode) {
        if(!visitedNodes.contains(currentNode)) {
            spawnPoolObjects(currentNode);
            visitedNodes.add(currentNode);
           // System.out.println(currentNode.ip + " added to visited list");
            unvisitedNodes.remove(currentNode);
            //create cached route - this empties the current path list
            if(currentPath.size() != 0) {
                System.out.println("ADDING CACHED OBJECT WITH CURRENT PATH SIZE = " + currentPath.size());
                cache.addCacheObject(currentPath);
                //currentPath.clear();
                System.out.println("NEW CURRENT PATH SIZE " + currentPath.size());

            }
           // createCachedRoute();
            //remove all instances of current node as the TOnode from the pool
            removeAllPoolInstances(currentNode);
        }
    }

    /*
     * this method creates pool objects from the current node that we are visiting
     * the only rule is the TO node of a pool object cannot be in the visited set
     * pool objects are spawned from neighbors of fromNode
     * the weight of a poolObject is the localWeight (the weight of a certain edge) 
     * plus the current totalWeight
     */
    private void spawnPoolObjects(RegisteredNode fromNode) {
        for(RegisteredNode entry : fromNode.peerNodes.keySet()) {
            if(!visitedNodes.contains(entry)) {
                //grab the weight of that edge
                int edgeWeight = fromNode.peerNodes.get(entry);
                int poolObjectWeight = totalWeight + edgeWeight; 
                //System.out.println( "DEBUG: adding pool object " + entry.ip + " with weight " + poolObjectWeight +  " and to Node " + entry.ip + " and from node " + fromNode.ip); 
                PathObject poolObject = new PathObject(fromNode, entry, poolObjectWeight);
                poolObject.setLocalWeight(edgeWeight);
                pool.add(poolObject);
            }
        }
        //System.out.println("total weight = " + totalWeight);
    }


    private PathObject getSmallestPoolObject() {
        int minIndex = 0;
        for(int i = 0; i < pool.size(); ++i) {
            if(pool.get(i).weight < pool.get(minIndex).weight && (!visitedNodes.contains(pool.get(i).to))) {
                minIndex = i;
            }
        }
        //System.out.println("returning total weight = " + pool.get(minIndex).weight);
        return pool.remove(minIndex);
    }

    /*
     * this is going to pop off the contents of current path and form 
     * A string out of them by appending the contents to the front of a string builder
     * first pop off the top elem, as from-weight-to
     * if A - 5 - B is pos 1, then B - 2 - C could be a possible option
     * from elem 1 on only add the From - weight to avoid dupliucates
     */

     /* 
    private void createCachedRoute() {
        if(currentPath.size() > 0 ) {
        StringBuilder sb = new StringBuilder();
        PathObject firstElem = currentPath.get(0);
        String firstElemString = firstElem.from.ip + "-" + Integer.toString(firstElem.weight) + "-" + firstElem.to.ip;
        sb.insert(0, firstElemString);
        for(int i = 1; i < currentPath.size(); ++i) {
            String tempStr = buildPathString(currentPath.get(i));
            sb.append(tempStr);
        }

        System.out.println("DEBUG: adding route " + sb.toString());
        cache.addRoute(sb.toString());
    }
    }
    */

    private String buildPathString(PathObject path) {
        //this is for elem 1 and on
        String pathString = "-" + Integer.toString(path.weight) + "-" + path.to.ip;
        return pathString;
    }




    private void removeAllPoolInstances(RegisteredNode target) {
        ArrayList<Integer> indecies = new ArrayList<>();

        int index = 0;
        for(PathObject poolEntry : pool) {
            if(poolEntry.to.equals(target)) {
                indecies.add(index);
            }
            index++;
        }

        for(int i = indecies.size() - 1; i >= 0; --i) {
            int indexToRemove = indecies.get(i);
            pool.remove(indexToRemove);
        }

        //System.out.println("DEBUG: pritning pool size in removeallinstances");
        
    }    
}
