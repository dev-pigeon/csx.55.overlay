package csx55.overlay.dijkstra;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import csx55.overlay.node.*;

public class Cache {

    /* Example route 
     * carrot-8-brocolli-4-ferrari-1-onion
     */
    
    public final ArrayList<CacheObject> cachedRoutes = new ArrayList<>();

   
    public void addCacheObject(Stack<PathObject> objectToMake) {
        CacheObject cacheObject = new CacheObject(objectToMake);
        cachedRoutes.add(cacheObject);
       // System.out.println("PRINTING SIXZE OF CACHED ROUTES");
        //System.out.println(cachedRoutes.size());
    }


    //this should return the path to the target as a list representation of pathObjects
    public CacheObject findCachedRoute(RegisteredNode target) {
        CacheObject cachedRoute = null;
        for(int i = 0; i < cachedRoutes.size(); ++i) {
            //check if the last index of cacheObject.from is equal to the target, if so break 
            int positionToCheck = cachedRoutes.get(i).cachedRouteObject.size() - 1;
            System.out.println("position to check .to node = " + cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.portNum);
            if(cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.equals(target)) {
                cachedRoute = cachedRoutes.get(i);
                break;
            }
        }
        return cachedRoute;
    }

    public CacheObject findForMessaging(RegisteredNode target) {
        CacheObject cachedRoute = null;
        for(int i = 0; i < cachedRoutes.size(); ++i) {
            //check if the last index of cacheObject.from is equal to the target, if so break 
            int positionToCheck = cachedRoutes.get(i).cachedRouteObject.size() - 1;
            System.out.println("position to check .to node = " + cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.portNum);
            if(cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.ip.equals(target.ip) && cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.portNum == target.portNum) {
                System.out.println("FOUND");
                cachedRoute = cachedRoutes.get(i);
                break;
            }
        }
        return cachedRoute;
    }

    

    public String convertRouteToString(CacheObject route) throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        System.out.println("converting route");
        PathObject firstElem = route.cachedRouteObject.get(0);
        String firstElemString = InetAddress.getByName(firstElem.from.ip).getHostName() + ":" + Integer.toString(firstElem.from.portNum) + "-" + Integer.toString(firstElem.weight) + "-" + InetAddress.getByName(firstElem.to.ip).getHostName() + ":" + Integer.toString(firstElem.to.portNum);
        System.out.println("something is null?");
        sb.insert(0, firstElemString);
        for(int i = 1; i < route.cachedRouteObject.size(); ++i) {
            String tempStr = buildPathString(route.cachedRouteObject.get(i));
            sb.append(tempStr);
        }
        return sb.toString();
    }

    public void displayCachedRoutes() throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        //loop through the caches cachedRoutes
        for(int i = 0; i < cachedRoutes.size(); ++i) {
            //each of these is a CachedObject that has an arraylist which is the actual path
            PathObject firstElem = cachedRoutes.get(i).cachedRouteObject.get(0);
            String firstElemString = InetAddress.getByName(firstElem.from.ip).getHostName() + ":" + Integer.toString(firstElem.from.portNum) + "-" + Integer.toString(firstElem.weight) + "-" + InetAddress.getByName(firstElem.to.ip).getHostName() + ":" + Integer.toString(firstElem.to.portNum);
            sb.insert(0, firstElemString);

            for(int j = 1; j < cachedRoutes.get(i).cachedRouteObject.size(); ++j) {
                String tempStr = buildPathString(cachedRoutes.get(i).cachedRouteObject.get(j));
                sb.append(tempStr);
            }
            System.out.println(sb.toString());
            sb.setLength(0);
        }
    }

    private String buildPathString(PathObject path) throws UnknownHostException {
        //this is for elem 1 and on
        String pathString = "-" + Integer.toString(path.weight) + "-" + InetAddress.getByName(path.to.ip).getHostName() +  ":" + Integer.toString(path.to.portNum);
        return pathString;
    }

    public int sumPathWeight(CacheObject pathToSum) {
        int sum = 0;
        for(int i = 0; i < pathToSum.cachedRouteObject.size(); ++i) {
            sum += pathToSum.cachedRouteObject.get(i).weight;
        }
        return sum;
    }



    /*a cached object is nothing more than an array list of 
     * path objects that equal the current path to a certain node when created
     * thus when queriing the cache you must return a CachObject, but that will have an arrayList
     * of path objects that is the actual path
     */
    public class CacheObject {
        ArrayList<PathObject> cachedRouteObject;
        public CacheObject(Stack<PathObject> cacheObjectIn) {
            this.cachedRouteObject = new ArrayList<>(cacheObjectIn);
            System.out.println(cachedRouteObject.toString());
        }
    }
}

