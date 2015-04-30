import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;




public class QueueSystem {
	
	
	protected ServerStocha[] servers;
	private double lambda;
	protected Simulator simulator;
	private Arrival arrival;
	private ExponentialGen expGen;
	protected double timeOfSim;
	
	protected Tally meanWaitTime; //Liste d'observation des temps moyen d'attente de chaque serveur.
	
	public QueueSystem(double lambda, double time, ServerStocha[] servs)
	{
		simulator = new Simulator(); //Instance de simulation, contient la liste des events, le scheduler.
		this.lambda = lambda;
		timeOfSim = time;	
		meanWaitTime = new Tally();
		arrival = new Arrival();
		arrival.setSimulator(simulator);
		initializeArrivalGen();
		servers = servs;
		for(ServerStocha serv:servers)
		{
			serv.setSimu(simulator);
		}
	}

	public void LaunchSimu() {
		simulator.init();
		scheduledEvents();
		simulator.start();
		Customer cust;
		for(int i = 0;i<100;i++)
		{
			cust = new Customer(0);
			servers[i%2].requestServer(cust);
		}
		
	}

	protected void scheduledEvents() {
		arrival.schedule(expGen.nextDouble());//Programme l'event pour le temps t =simulator.time+X (X étant généré)
		EndOfSim endofSimEvent = new EndOfSim();
		endofSimEvent.setSimulator(simulator);
		endofSimEvent.schedule(timeOfSim); //Programme l'event pour le temps t =simulator.time+timeOfSim
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
	public ServerStocha getServer(int i)
	{
		return servers[i];
	}
	public double meanWaiTime()
	{
		return meanWaitTime.average();
	}
	protected ServerStocha chooseServer() {
		
		ServerStocha choosenServ = servers[0];
		for(int i=1;i<servers.length && (choosenServ.customerInSystem()>0);i++)
		{
			if(servers[i].isOpen() && servers[i].customerInSystem()<choosenServ.customerInSystem())
				choosenServ = servers[i];
		}
		return choosenServ;
	}

	class Arrival extends Event{
		@Override
		public void actions() {			
			arrival.schedule(expGen.nextDouble());
			Customer cust = new Customer(simulator.time());			
			ServerStocha choosenServ = chooseServer();
			choosenServ.requestServer(cust);
		}	
		
	}
	
	class EndOfSim extends Event{
		@Override
		public void actions() {			
			for(int i=0;i<servers.length;i++)
			{
				meanWaitTime.add(servers[i].avgTimeInQueue());
			}
			simulator.stop();
		}		
	}

	

}
