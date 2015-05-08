
public class RandomSelectionSystem extends QueueSystem {

	public RandomSelectionSystem(double lambda, double time,ServerStocha[] serv) {
		super(lambda, time,serv);
	}
	
	protected ServerStocha chooseServer() {
		ServerStocha choosenServ = servers[0];
		double rand = Math.random() * (servers.length - 1); // Approximatively random => Maybe there is a better choice from ssj ?
		int discreteRand = (int) Math.round(rand);
		choosenServ = servers[discreteRand];
		return choosenServ;
	}

}
