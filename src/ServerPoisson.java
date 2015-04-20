import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public class ServerPoisson extends ServerStocha {
	
	public ServerPoisson(MRG32k3a dis,double mu,Simulator sim)
	{
		waitingList = new LinkedList<>();
		distribution = new ExponentialGen(dis, mu);
		depart = new Departure();
		systemObs = new Tally();
		waitTimeObservation = new Tally();
		simulator = sim;
		depart.setSimulator(simulator);
		
	}

}
