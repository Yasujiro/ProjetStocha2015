import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public class QueueSystem {
	protected StochasticServer[] servers;
	private double lambda;
	protected Simulator simulator;
	protected Arrival arrival;
	protected ExponentialGen expGen;
	protected double simulationTime;
	private Tally queueSizeObservations;
	//private StatsObserver ob;
	protected Tally meanWaitTimeObservations; //Liste d'observation des temps d'attente.

	
	public QueueSystem(double lambda, double time, StochasticServer[] servers)
	{
		this.simulator = new Simulator(); //Instance de simulation, contient la liste des events, le scheduler.
		this.lambda = lambda;
		this.simulationTime = time;	
		this.meanWaitTimeObservations = new Tally("Temps d'attente moyen");
		this.queueSizeObservations = new Tally();
		this.arrival = new Arrival();
		//ob = new StatsObserver(simulator);
		this.arrival.setSimulator(simulator);
		initializeArrivalGen();
		this.servers = servers;
		for(StochasticServer server:this.servers)
		{
			server.setSimulator(simulator);
			server.setSystem(this);
		}
	}

	public void startSimulation() {
		simulator.init();
		scheduleEvents();
		simulator.start();		
	}

	protected void scheduleEvents() {
		arrival.schedule(expGen.nextDouble());//Programme l'event pour le temps t =simulator.time+X (X étant généré)
		EndOfSim endofSimEvent = new EndOfSim();
		endofSimEvent.setSimulator(simulator);
		//ob.schedule(500);
		endofSimEvent.schedule(simulationTime); //Programme l'event pour le temps t =simulator.time+timeOfSim
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
	public StochasticServer getServer(int i)
	{
		return servers[i];
	}
	public void customerLeaving(Customer cust)
	{
		/*
		 * Used this methode for any update to make when customer leave the system.
		 */
		return;
	}
	public void report()
	{
		if(meanWaitTimeObservations.numberObs()>0)
		{
			System.out.println(meanWaitTimeObservations.report()+"\n");
			System.out.println("Variance : "+meanWaitTimeObservations.variance()+"\n");
		}
		//QueueReport();
	}

	protected void queueReport() {
		int i = 1;
		double meanQueue=0;
		for(StochasticServer serv: servers)
		{
			System.out.println("Nombre moyen de personne dans la file du serveur  : "+i+" "+serv.queueSizeObservations.report());
			meanQueue+=serv.queueSizeObservations.average();
			i++;
		}
		System.out.println("\nNombre moyen de personne dans les files " +meanQueue/2);
	}
	protected void chooseServer(Customer cust) {
		
		StochasticServer choosenServ = servers[0];
		for(int i=1;i<servers.length;i++)
		{
			if(servers[i].isOpen())
			{
				if(servers[i].nbCustomersInSystem()<choosenServ.nbCustomersInSystem())
					choosenServ = servers[i];
				else if (servers[i].nbCustomersInSystem()==choosenServ.nbCustomersInSystem()) // Si taille égal, choix random.
				{
					double rand = Math.random();
					if(rand>0.5)
						choosenServ = servers[i];
				}
			}
			
		}
		choosenServ.addCustomer(cust);
	}
		
	public void addWaitTimeObservation(Customer cust)
	{
		double x = simulator.time()-cust.getArrivalTime();
		meanWaitTimeObservations.add(x);
	}
	public void addQueueSizeObservation(double x)
	{
		queueSizeObservations.add(x);
	}
	protected void manageNewCustomer() {
		arrival.schedule(expGen.nextDouble());
		Customer cust = new Customer(simulator.time());			
		chooseServer(cust);
		
	}

	
	
	class Arrival extends Event{
		@Override
		public void actions() {		
			manageNewCustomer();			
		}	
		
	}
	
	class EndOfSim extends Event{
		@Override
		public void actions() {	
			simulator.stop();
		}		
	}
	
	class StatsObserver extends Event{
		public StatsObserver(Simulator sim)
		{
			this.setSimulator(sim);
		}
		public void actions(){
			System.out.println("-------------------------Temps : "+sim.time()+"-----------------------------");
			System.out.println(meanWaitTimeObservations.report());
			System.out.println("Variance : "+meanWaitTimeObservations.variance());
			//ob.schedule(500);
		}
	}

	

}
