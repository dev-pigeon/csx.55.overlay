package csx55.overlay.dijkstra;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import csx55.overlay.node.*;


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
    public void findAllRoutes(RegisteredNode startNode) {
        RegisteredNode current = findStartPoint(startNode);
        this.startNode = current;
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
                currentPath.add(currentPathObject);
                current = smallestWeightObject.to;
                visitNodeProtocol(current);

                
                //everyhting above this point is tested and good
                //everything below is ass
            } else {
                current = startNode;
                totalWeight -= totalWeight;
                currentPath.clear();
                if(current.equals(smallestWeightObject.from)) {
                    //neighbor of start node baby
                    totalWeight += smallestWeightObject.localWeight;
                //we need to add this to curent path
                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    visitNodeProtocol(current);
                } else {
                    //we need to locate the from node
                    Cache.CacheObject path = cache.findCachedRoute(smallestWeightObject.from);
                    currentPath.addAll(path.cachedRouteObject);
                    totalWeight += cache.sumPathWeight(path);
                    current = smallestWeightObject.from;
                    totalWeight += smallestWeightObject.localWeight;

                    PathObject currentPathObject = new PathObject(current, smallestWeightObject.to, smallestWeightObject.localWeight);
                   
                    currentPath.add(currentPathObject);
                    current = smallestWeightObject.to;
                    visitNodeProtocol(current);
                }                
            }
                
        }
        try {
            cache.displayCachedRoutes();
        } catch (UnknownHostException uke) {
            System.out.println(uke.getMessage());
        }
    }
    
   public int collectPathWeight(String str) { //dead function
    int sum = 0;
    str = str.replace("-", " ");
    String[] path = str.split(" ");
   
    for(int i = 1; i < path.length - 1; i += 2) {
        sum += Integer.parseInt(path[i]);
    }
    return sum;
   }


    private RegisteredNode findStartPoint(RegisteredNode startNode) {
        //return masterList.g
        int index = masterList.indexOf(startNode);
        return masterList.get(index);
    }

    /*
     * this method is going to call spawn pool objects and then add itself to the visited set
     * on the condition that it is not already in the pool 
     */
    private void visitNodeProtocol(RegisteredNode currentNode) {
        if(!visitedNodes.contains(currentNode)) {
            spawnPoolObjects(currentNode);
            visitedNodes.add(currentNode);
            unvisitedNodes.remove(currentNode);
            //create cached route - this empties the current path list
            if(currentPath.size() != 0) {
                cache.addCacheObject(currentPath);
                //currentPath.clear();

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
                PathObject poolObject = new PathObject(fromNode, entry, poolObjectWeight);
                poolObject.setLocalWeight(edgeWeight);
                pool.add(poolObject);
            }
        }
    }


    private PathObject getSmallestPoolObject() {
        int minIndex = 0;
        for(int i = 0; i < pool.size(); ++i) {
            if(pool.get(i).weight < pool.get(minIndex).weight && (!visitedNodes.contains(pool.get(i).to))) {
                minIndex = i;
            }
        }
        return pool.remove(minIndex);
    }


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
        
    }    
}
