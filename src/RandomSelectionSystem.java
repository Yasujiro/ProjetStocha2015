
public class RandomSelectionSystem extends QueueSystem {

	public RandomSelectionSystem(double lambda, double time,StochasticServer[] serv) {
		super(lambda, time,serv);
	}
	
	protected void chooseServer(Customer cust) {
		StochasticServer chosenServer = servers[0];
		double rand = Math.random() * (servers.length - 1); // Approximatively random => Maybe there is a better choice from ssj ?
		int discreteRand = (int) Math.round(rand);
		chosenServer = servers[discreteRand];
		chosenServer.addCustomer(cust);
	}

}
