
public class RandomSelectionSystem extends QueueSystem {

	public RandomSelectionSystem(double lambda, double time,StochasticServer[] serv) {
		super(lambda, time,serv);
	}
	
	protected void chooseServer(Customer cust) {
		StochasticServer chosenServer = servers[0];
		double rand = Math.random() * (servers.length - 1); // distribution uniforme
		int discreteRand = (int) Math.round(rand);
		chosenServer = servers[discreteRand];
		chosenServer.addCustomer(cust);
	}

}
