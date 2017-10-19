package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tiles.MapTile;
import utilities.Coordinate;

public class Navigation {

	HashMap<Coordinate, MapTile> map;
	List<Coordinate> finish;
	List<Coordinate> route;
	IPathFinder pathfinder;
	
	
	public Navigation(HashMap<Coordinate, MapTile> map, IPathFinder pathfinder) {
		
		this.map = map;
		this.pathfinder = pathfinder;
		
		//adds finish tiles
		finish = new ArrayList<>();
		map.forEach((k,v) -> {
			if (v.isType(MapTile.Type.FINISH)) {
			finish.add(k);
			}
		});
	}
	
	public List<Coordinate> planRoute(Coordinate location) {
		route = pathfinder.planRoute(location, finish, map);
		return route;
	}
	
/** updates the map with new traps, and replans route if necessary
 *  returns true when route is replanned  **/
	public boolean updateMap(HashMap<Coordinate, MapTile> view, Coordinate location) {
		boolean replan = false;
		for (HashMap.Entry<Coordinate, MapTile> entry: view.entrySet()) {
			if (entry.getValue().isType(MapTile.Type.TRAP) && !map.get(entry.getKey()).isType(MapTile.Type.TRAP)) {
				map.remove(entry.getKey());
				map.put(entry.getKey(), entry.getValue());
				if (route.contains(entry.getKey())) {
					replan = true;
				}
			}
		}
		if (replan == true) {
			route = planRoute(location);
		}
		return replan;	
	}
	
	List<Coordinate> getRoute() {
		return this.route;
	}

}
