package dijkstra;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import node.*;

public class Cache {

    /* Example route 
     * carrot-8-brocolli-4-ferrari-1-onion
     */
    
    public final ArrayList<CacheObject> cachedRoutes = new ArrayList<>();

    /* 
    public void addRoute(String newRoute) {
        cahcedRoutes.add(newRoute);
        
    }
    */

    /*
     * This method searches the cached routes in a loop
     * at every iteration will split it into a string array
     * the length of which will always be a multiple of four,
     * if the last index in this array equals the identifer,
     * which is likely to be the host name / IP of whatever node
     * then it will return that index of cachedRoutes
     */

     /* 
    public String getRoute(String identifier) {
        String route = "";
        for(int i = 0; i < cahcedRoutes.size(); ++i) {
            String temp = cahcedRoutes.get(i);
            String[] tempArr = temp.split("-");
            if(tempArr[tempArr.length-1].equals(identifier)) {
                route = String.join("-", tempArr);
                break;
            }
        }
        return route;
    }
    */

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
            if(cachedRoutes.get(i).cachedRouteObject.get(positionToCheck).to.equals(target)) {
                cachedRoute = cachedRoutes.get(i);
                break;
            }
        }
        return cachedRoute;
    }

    public void displayCachedRoutes() throws UnknownHostException {
        StringBuilder sb = new StringBuilder();
        //loop through the caches cachedRoutes
        for(int i = 0; i < cachedRoutes.size(); ++i) {
            //each of these is a CachedObject that has an arraylist which is the actual path
            PathObject firstElem = cachedRoutes.get(i).cachedRouteObject.get(0);
            String firstElemString = InetAddress.getByName(firstElem.from.ip).getHostName() + "-" + Integer.toString(firstElem.weight) + "-" + InetAddress.getByName(firstElem.to.ip).getHostName();
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
        String pathString = "-" + Integer.toString(path.weight) + "-" + InetAddress.getByName(path.to.ip).getHostName();
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

