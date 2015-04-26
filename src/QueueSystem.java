import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;




public class QueueSystem {
	
	private static MRG31k3p randomSeedGen = new MRG31k3p();
	private ServerStocha[] servers;
	private double lambda;
	private Simulator simulator;
	private Arrival arrival;
	private ExponentialGen expGen;
	private double timeOfSim;
	private QueueSelection queueSelection;
	
	
	public QueueSystem(double lambda, double time, int nbServ, ProcessusService tempsService, QueueSelection queueSelection)
	{

		simulator = new Simulator(); //Instance de simulation, contient la liste des events, le scheduler.
		this.lambda = lambda;
		timeOfSim = time;		
		arrival = new Arrival();
		arrival.setSimulator(simulator);
		initializeArrivalGen();
		initializeServers(tempsService,nbServ);
		this.queueSelection = queueSelection;
	}

	public void LaunchSimu() {
		simulator.init();
		arrival.schedule(expGen.nextDouble());//Programme l'event pour le temps t =simulator.time+X (X étant généré)
		EndOfSim endofSimEvent = new EndOfSim();
		endofSimEvent.setSimulator(simulator);
		endofSimEvent.schedule(timeOfSim); //Programme l'event pour le temps t =simulator.time+timeOfSim
		simulator.start();
	}
	
	private void initializeServers(ProcessusService p,int nbServ) 
	{
		switch(p)
		{
			case Poisson:
				servers = new ServerPoisson[nbServ];
				break;
			case Erlang:
				servers = new ServerErlang[nbServ];
				break;
		}
		
		long[] seed = new long[6];
		
		for(int i =0;i<servers.length;i++)
		{
			for(int j =0 ; j<seed.length;j++)
			{
				seed[j] = randomSeedGen.nextInt(1, 429494444);
			}
			
			MRG32k3a pStream = new MRG32k3a();
			pStream.setSeed(seed);
			switch(p)
			{
				case Poisson:
					servers[i] = new ServerPoisson(pStream,2,simulator);
					break;
				case Erlang:
					servers[i] = new ServerErlang(pStream,(int)1,simulator);
					break;
			}
			
		}
		
	}

	private void initializeArrivalGen() {
		long[] seed = new long[6];
		for(int i =0 ; i<seed.length;i++)
		{
			seed[i] = 65956543;
		}
		MRG32k3a expStream = new MRG32k3a();
		expStream.setSeed(seed);
		expGen = new ExponentialGen(expStream, lambda);
		
	}

	class Arrival extends Event{

		@Override
		public void actions() {
			
			arrival.schedule(expGen.nextDouble());
			Customer cust = new Customer(simulator.time());
			
			ServerStocha choosenServ = servers[0];
			
			if (queueSelection == QueueSelection.shortestFirst) {
				for(int i=1;i<servers.length && (choosenServ.customerInSystem()>0);i++)
				{
					if(servers[i].customerInSystem()<choosenServ.customerInSystem())
						choosenServ = servers[i];
				}
			} else {
				double rand = Math.random() * (servers.length - 1); // Approximatively random => Maybe there is a better choice from ssj ?
				int discreteRand = (int) Math.round(rand);
				choosenServ = servers[discreteRand];
			}
			
			choosenServ.requestServer(cust);
		}
		
	}
	
	class EndOfSim extends Event{

		@Override
		public void actions() {
			
			
//			double rho = lambda/mu;
//			System.out.println("Rho : "+rho);
//			System.out.println("E[L] : "+ (rho)/(1-rho));
//			System.out.println("E[S] :"+ (1/mu)/(1-rho));
			for(int i=0;i<servers.length;i++)
			{
				servers[i].report(lambda);
			}
			simulator.stop();
			
		}
		
	}

}
