import java.util.ArrayList;
import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ErlangGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;
import umontreal.iro.lecuyer.stat.TallyStore;

public class ServerErlang extends ServerStocha {

	public ServerErlang(MRG32k3a dis,int k,double muK)
	{
		queue = new LinkedList<>();
		distribution = new ErlangGen(dis,k, muK);
		depart = new Departure();
		queueSizeObs = new Tally();
	}
}
