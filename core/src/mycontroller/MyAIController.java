package mycontroller;

import java.util.List;

import controller.AIController;
import controller.CarController;
import utilities.Coordinate;
import world.Car;

public class MyAIController extends CarController{

	Navigation navigation;
	List<Coordinate> route;
	AIController ai;
	
	public MyAIController(Car car) {
		super(car);
		ai = new AIController(car);
		navigation = new Navigation(getMap0(), new DijkstraPathFinder());
		route = navigation.planRoute(new Coordinate(this.getPosition()));
	}

	@Override
	public void update(float delta) {
		ai.update(delta);
		//System.out.print(" " + ai.getPosition());
		if (navigation.updateMap(this.getView(), new Coordinate(this.getPosition()))) {
			route = navigation.getRoute();
		};	
		System.out.println(route);
		/*Hi maria can u see this*/
		/*How about if I changed this*/
		/*You should be able to see what im typing hereeeeeeee*/
	}

}
