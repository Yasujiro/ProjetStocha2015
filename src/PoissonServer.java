import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;


public class PoissonServer extends StochasticServer {
	
	
	
	public PoissonServer(MRG32k3a dis,double mu)
	{
		queue = new LinkedList<>();
		distribution = new ExponentialGen(dis, mu);
		departure = new Departure();
		queueSizeObservations = new Tally();		
	}

}
