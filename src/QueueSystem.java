import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.TallyStore;




public class QueueSystem {
	
	
	protected ServerStocha[] servers;
	private double lambda;
	protected Simulator simulator;
	private Arrival arrival;
	private ExponentialGen expGen;
	protected double timeOfSim;
	
	protected TallyStore meanWaitTime; //Liste d'observation des temps d'attente.
	
	public QueueSystem(double lambda, double time, ServerStocha[] servs)
	{
		simulator = new Simulator(); //Instance de simulation, contient la liste des events, le scheduler.
		this.lambda = lambda;
		timeOfSim = time;	
		meanWaitTime = new TallyStore();
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
	public TallyStore meanWaiTime()
	{
		return meanWaitTime;
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
			for(ServerStocha serv: servers)
			{
				double[] obs = serv.avgTimeInQueue().getArray();
				for(double observation:obs)
				{
					meanWaitTime.add(observation);
				}
			}
			simulator.stop();
		}		
	}

	

}
