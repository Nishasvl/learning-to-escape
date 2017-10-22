package mycontroller;

import java.util.HashMap;
import java.util.List;

import controller.AIController;
import controller.CarController;
import tiles.MapTile;
import tiles.MudTrap;
import tiles.TrapTile;
import utilities.Coordinate;
import utilities.PeekTuple;
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
	private boolean isGoingBackward = false;
	private boolean isFollowingCoordinate = false;
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private float CAR_SPEED = 2;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
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
		checkStateChange();
		/*car is going to change direction in next move(not following the route)*/
		if(!isFollowingCoordinate) {
			if(getSpeed() < CAR_SPEED && !isGoingBackward){
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
				else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)) {
					if(getSpeed() >0.03) {
						applyBrake();
					}
					isGoingBackward = true;
					isFollowingCoordinate = true;
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			else if(checkSouth(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.WEST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.NORTH)) {
					if(getSpeed() >0.03) {
						applyBrake();
					}
					isGoingBackward = true;
					isFollowingCoordinate = true;
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			else if(checkEast(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.WEST)) {
					if(getSpeed() >0.03) {
						applyBrake();
					}
					isGoingBackward = true;
					isFollowingCoordinate = true;
				}
				else{
					isFollowingCoordinate = true;
				}
			}
			else if(checkWest(currentCoordinate)) {
				if(getOrientation().equals(WorldSpatial.Direction.NORTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
					applyLeftTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.SOUTH)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else if(getOrientation().equals(WorldSpatial.Direction.EAST)) {
					if(getSpeed() >0.03) {
						applyBrake();
					}
					isGoingBackward = true;
					isFollowingCoordinate = true;
				
				}
				else{
					isFollowingCoordinate = true;
				}
			}
		}
		/*car is following the route planned*/
		else {
			readjust(lastTurnDirection,delta);
			if(isTurningRight){
				applyRightTurn(getOrientation(),delta);
			}
			else if(isTurningLeft){
				applyLeftTurn(getOrientation(),delta);
			}
			else if(isGoingBackward) {
				applyReverseAcceleration();
				System.out.println("following reverse route?:"+!checkReverseFollowingCoordinate(getOrientation(),currentCoordinate));
				if(!checkReverseFollowingCoordinate(getOrientation(),currentCoordinate)) {
					isGoingBackward = false;
					isFollowingCoordinate = false;
				}
			}
				//System.out.println("Following backward route?:"+checkReverseFollowingCoordinate(getOrientation(),currentCoordinate));
			/* car is moving forward*/

			else if(checkFollowingCoordinate(getOrientation(),currentCoordinate)){
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				/*check if next 3 coordinate in route, if there is change in front then slow down*/
				CheckTurningAhead(getOrientation(),currentCoordinate,currentView, delta);
				
				}
				/*if trap is in ahead*/
				/*if(currentView.get(currentCoordinate) instanceof TrapTile) {
					if(((TrapTile)currentView.get(currentCoordinate)).canAccelerate()) {
						applyForwardAcceleration();
					}
				}*/
			}
			/*
			else if(!checkFollowingCoordinate(getOrientation(),currentCoordinate)){
				System.out.println("going not to follow coordinate:");
				isFollowingCoordinate = false;
				CAR_SPEED = (float) 1.4;
			}*/
		}
		//System.out.println("Following coordinate?: "+isFollowingCoordinate);

		/*Supporting functions down here*/
	
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
	
	private void CheckTurningAhead(WorldSpatial.Direction orientation, Coordinate currentCoordinate,HashMap<Coordinate, MapTile> currentView ,float delta) {
		Coordinate ahead1;
		Coordinate ahead2;
		Coordinate ahead3;
		switch(orientation){
		case EAST:
			
			//System.out.println("1,2,3: "+ahead1+ahead2+ahead3);
			if(route.size() >= 3) {
				ahead1 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
				ahead2 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y);
				ahead2 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y-1);
				ahead3 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y-2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningRight = true;
				}
			}
			break;
			

		case NORTH:
			if(route.size() >= 3) {
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				ahead2 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x-2, currentCoordinate.y+1);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				ahead2 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x+2, currentCoordinate.y+1);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningRight = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				ahead2 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
					isTurningRight = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y+1);
				ahead2 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y+2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
					isTurningRight = true;
				}
			}
			break;
		case SOUTH:
			if(route.size() >= 3) {
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
				ahead2 = new Coordinate(currentCoordinate.x+1, currentCoordinate.y-1);
				ahead3 = new Coordinate(currentCoordinate.x+2, currentCoordinate.y-1);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x, currentCoordinate.y-1);
				ahead2 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y-1);
				ahead3 = new Coordinate(currentCoordinate.x-2, currentCoordinate.y-1);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningRight = true;
				}
			}
			break;

		case WEST:
			if(route.size() >= 3) {
				ahead1 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
				ahead2 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y-1);
				ahead3 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y-2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningLeft = true;
				}
				ahead1 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y);
				ahead2 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+1);
				ahead3 = new Coordinate(currentCoordinate.x-1, currentCoordinate.y+2);
				if(route.get(0).equals(ahead1) && route.get(1).equals(ahead2) && route.get(2).equals(ahead3)){
					isTurningRight = true;
				}
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
		Coordinate next = new Coordinate(currentCoordinate.x+2, currentCoordinate.y);
		if(route.size() >= 1 && route.get(1).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkWest(Coordinate currentCoordinate){
		// Check tiles to my left
		Coordinate next = new Coordinate(currentCoordinate.x-2, currentCoordinate.y);
		if(route.size() >= 1 && route.get(1).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkNorth(Coordinate currentCoordinate){
		// Check tiles to towards the top
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y+2);
		if(route.size() >= 1 && route.get(1).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean checkSouth(Coordinate currentCoordinate){
		// Check tiles towards the bottom
		Coordinate next = new Coordinate(currentCoordinate.x, currentCoordinate.y-2);
		if(route.size() >= 1 && route.get(1).equals(next)) {
			return true;
		}
		else {
			return false;
		}
	
	}
private boolean checkReverseFollowingCoordinate(WorldSpatial.Direction orientation, Coordinate currentCoordinate) {
		
		switch(orientation){
		case EAST:
			return checkWest(currentCoordinate);
		case NORTH:
			return checkSouth(currentCoordinate);
		case SOUTH:
			return checkNorth(currentCoordinate);
		case WEST:
			return checkEast(currentCoordinate);
		default:
			return false;
		}
		
	}
}
