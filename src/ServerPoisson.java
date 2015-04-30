import java.util.ArrayList;
import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public class ServerPoisson extends ServerStocha {
	
	private double mu;
	
	public ServerPoisson(MRG32k3a dis,double mu)
	{
		this.mu = mu;
		observer = new ArrayList<>();
		queue = new LinkedList<>();
		distribution = new ExponentialGen(dis, mu);
		depart = new Departure();
		systemObs = new Tally();
		servTimeObservation = new Tally();
		waitTimeObservation = new Tally();
		
	}
	public void report(double lambdaArrival)
	{
		double rho = lambdaArrival/mu;
		System.out.println("\nServeur Poisson");
//		System.out.println("Rho : "+rho);
//		System.out.println("E[L] : "+ (rho)/(1-rho));
//		System.out.println("E[S] :"+ (1/mu)/(1-rho));
//		System.out.println("E[W] : "+rho/(mu*(1-rho)));
		
		super.report(lambdaArrival);
	}

}
