import java.util.LinkedList;
import umontreal.iro.lecuyer.randvar.ErlangGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;

public class ServerErlang extends ServerStocha {

	private double k;
	public ServerErlang(MRG32k3a dis,int k,Simulator sim)
	{
		simulator = sim;
		waitingList = new LinkedList<>();
		this.k = k;
		distribution = new ErlangGen(dis,k, muK());
		depart = new Departure();
		depart.setSimulator(simulator);
		systemObs = new Tally();
		servTimeObservation = new Tally();
		waitTimeObservation = new Tally();
		
		
	}
	
	private double muK()
	{
		return 2*k;
	}
	
	public void report(double lambdaArrival)
	{
		double rho = lambdaArrival*(k/muK());
		double coefVarService = (Math.sqrt(k/(Math.pow(muK(), 2)))) / (k/muK());
		double num = Math.pow(rho, 2)*(1+Math.pow(coefVarService,2));
		double deno = 2*(1-rho);
		double moyenneService =k/muK();
		double moment2 =(k/Math.pow(muK(), 2))+ Math.pow(moyenneService, 2);//Variance+E[X]²
		
		System.out.println("\nServeur Erlang");
		System.out.println("Rho : "+rho);
//		System.out.println("E[L] : "+ (rho + (num/deno)));
//		System.out.println("E[B] :"+ moyenneService );
//		System.out.println("E[W] : "+(rho/(1-rho))*(moment2/(2*moyenneService)));
		super.report(lambdaArrival);
		
	}
}
