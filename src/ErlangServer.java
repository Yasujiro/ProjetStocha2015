import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ErlangGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;

public class ErlangServer extends StochasticServer {

	public ErlangServer(MRG32k3a dis,int k,double muK)
	{
		queue = new LinkedList<>();
		distribution = new ErlangGen(dis,k, muK);
		departure = new Departure();
		queueSizeObservations = new Tally();
	}
}
