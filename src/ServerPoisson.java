import java.util.ArrayList;
import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.TallyStore;


public class ServerPoisson extends ServerStocha {
	
	
	
	public ServerPoisson(MRG32k3a dis,double mu)
	{
		observer = new ArrayList<>();
		queue = new LinkedList<>();
		distribution = new ExponentialGen(dis, mu);
		depart = new Departure();
		waitTimeObservation = new TallyStore();
		
	}

}
