import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ErlangGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;

public class ServerErlang extends ServerStocha {

	public ServerErlang(MRG32k3a dis,int k)
	{
		waitingList = new LinkedList<>();
		distribution = new ErlangGen(dis,k, muK(k));
		depart = new Departure();
		systemObs = new Tally();
		waitTimeObservation = new Tally();
		
	}
	
	private double muK(double k)
	{
		return 7;
	}
}
