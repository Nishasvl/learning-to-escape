package mycontroller;

import java.util.HashMap;
import java.util.List;

import controller.AIController;
import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAIController extends CarController{

	Navigation navigation;
	List<Coordinate> route;
	AIController ai;
	private int wallSensitivity = 2;
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private boolean isFollowingCoordinate = false;
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private final float CAR_SPEED = 1;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 300;
	
	public MyAIController(Car car) {
		super(car);
		ai = new AIController(car);
		navigation = new Navigation(getMap0(), new DijkstraPathFinder());
		route = navigation.planRoute(new Coordinate(this.getPosition()));
	}

	@Override
	public void update(float delta) {
		
		Coordinate currentCoordinate = new Coordinate(this.getPosition());
		System.out.println(route);
		HashMap<Coordinate, MapTile> currentView = getView();
		//ai.update(delta);
		//System.out.print(" " + ai.getPosition());
		if(currentCoordinate.equals(route.get(0))) {
			route.remove(0);
		}
		if (navigation.updateMap(this.getView(), currentCoordinate)) {
			route = navigation.getRoute();
		}
		System.out.println("South: "+checkSouth(currentCoordinate));
		checkStateChange();
		System.out.println(isFollowingCoordinate);
		if(!isFollowingCoordinate) {
			if(getSpeed() < CAR_SPEED){
				applyForwardAcceleration();
			}
			if(checkNorth(currentCoordinate)){
				if(getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.WEST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			if(checkSouth(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.WEST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			if(checkEast(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			if(checkWest(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			
		}
		
		else {
			readjust(lastTurnDirection,delta);
			if(isTurningRight){
				applyRightTurn(getOrientation(),delta);
			}
			else if(isTurningLeft){
				// Apply the left turn if you are not currently near a wall.
				applyLeftTurn(getOrientation(),delta);
			}
			// Try to determine whether or not the car is next to a wall.
			else if(checkFollowingCoordinate(getOrientation(),currentCoordinate)){
				// Maintain some velocity
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				// If there is wall ahead, turn right!
				
			}
			else if(!checkFollowingCoordinate(getOrientation(),currentCoordinate)){
				this.applyBrake();
				isFollowingCoordinate = false;
			}
		}
	}

		

		//System.out.println(route);
		/*Hi maria can u see this*/
		/*How about if I changed this*/
		/*You should be able to see what im typing hereeeeeeee*/
	
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(getAngle() > WorldSpatial.NORTH_DEGREE){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE){
				turnRight(delta);
			}
			break;
		case WEST:
			if(getAngle() > WorldSpatial.WEST_DEGREE){
				turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(getAngle() < WorldSpatial.NORTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(getAngle() < WorldSpatial.SOUTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(getAngle() < WorldSpatial.WEST_DEGREE){
				turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnRight(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Check if the car is following next coordinate
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingCoordinate(WorldSpatial.Direction orientation, Coordinate currentCoordinate) {
		
		switch(orientation){
		case EAST:
			return checkEast(currentCoordinate);
		case NORTH:
			return checkNorth(currentCoordinate);
		case SOUTH:
			return checkSouth(currentCoordinate);
		case WEST:
			return checkWest(currentCoordinate);
		default:
			return false;
		}
		
	}
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(Coordinate currentCoordinate){
		// Check tiles to my right
		Coordinate next = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
		//System.out.println("next position: "+next);
		//System.out.println("next route position: "+route.get(0));
		if(route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkWest(Coordinate currentCoordinate){
		// Check tiles to my left
		Coordinate next = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
		if(route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkNorth(Coordinate currentCoordinate){
		// Check tiles to towards the top
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
		if(route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkSouth(Coordinate currentCoordinate){
		// Check tiles towards the bottom
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
		if(route.get(0).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	
	}
}
