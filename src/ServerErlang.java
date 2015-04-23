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
		waitTimeObservation = new Tally();
		
		
	}
	
	private double muK()
	{
		return 4*k;
	}
	
	public void report(double lambdaArrival)
	{
		double rho = lambdaArrival*(k/muK());
		double coefVarService = (Math.sqrt(k/(Math.pow(muK(), 2)))) / (k/muK());
		double num = Math.pow(rho, 2)*(1+Math.pow(coefVarService,2));
		double deno = 2*(1-rho);
		System.out.println("Serveur Erlang");
		System.out.println("Rho : "+rho);
		System.out.println("E[L] : "+ (rho + (num/deno)));
		super.report(lambdaArrival);
		//System.out.println("E[S] :"+ (1/k)/(1-rho));
		
	}
}
