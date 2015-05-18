import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public class QueueSystem {
	protected StochasticServer[] servers; // Liste des serveurs du syst�me
	private double lambda;
	protected Simulator simulator; // Le scheduler, contient la liste des �v�nements de la simulation
	protected Arrival arrival; // Evenement d'arriv�e d'un client
	protected ExponentialGen expGen;
	protected double simulationTime; // Temps de simulation du syst�me
	private Tally queueSizeObservations; // Liste d'observations des tailles de file
	//private StatsObserver ob;
	protected Tally waitTimeObservations; // Liste d'observation des temps d'attente

	
	public QueueSystem(double lambda, double time, StochasticServer[] servers)
	{
		this.simulator = new Simulator(); 
		this.lambda = lambda;
		this.simulationTime = time;	
		this.waitTimeObservations = new Tally("Temps d'attente moyen");
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
		arrival.schedule(expGen.nextDouble()); // Programme l'event pour le temps t = simulator.time + X (X �tant g�n�r�)
		EndOfSim endofSimEvent = new EndOfSim();
		endofSimEvent.setSimulator(simulator);
		//ob.schedule(500);
		endofSimEvent.schedule(simulationTime); // Programme l'event pour le temps t = simulator.time + timeOfSim
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
	
	// Cette m�thode est utilis�e pour tout traitement � r�aliser lorsqu'un client quitte le syst�me
	public void customerLeaving(Customer cust)
	{
		return;
	}
	
	public void report()
	{
		if(waitTimeObservations.numberObs()>0)
		{
			System.out.println(waitTimeObservations.report()+"\n");
			System.out.println("Variance : "+waitTimeObservations.variance()+"\n");
		}
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
		
		StochasticServer chosenServer = servers[0];
		for(int i=1;i<servers.length;i++)
		{
			if(servers[i].isOpen())
			{
				// Si l'autre serveur a moins de clients
				if(servers[i].nbCustomersInSystem() < chosenServer.nbCustomersInSystem())
					chosenServer = servers[i];
				// Si les deux serveurs ont la m�me taille
				else if (servers[i].nbCustomersInSystem() == chosenServer.nbCustomersInSystem())
				{
					// On choisit le serveur al�atoirement
					double rand = Math.random(); // Distribution uniforme
					if(rand>0.5)
						chosenServer = servers[i];
				}
			}
			
		}
		chosenServer.addCustomer(cust);
	}
		
	public void addWaitTimeObservation(Customer cust)
	{
		double x = simulator.time()-cust.getArrivalTime();
		waitTimeObservations.add(x);
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


	// �v�nement d'arriv�e d'un client
	class Arrival extends Event{
		@Override
		public void actions() {		
			manageNewCustomer();			
		}	
		
	}
	
	// �v�nement de fin de la simulation
	class EndOfSim extends Event{
		@Override
		public void actions() {	
			simulator.stop();
		}		
	}
	
	// �v�nement produisant un "snapshot" de l'�tat du syst�me
	// Cet �v�nement est g�n�ralement planifi� � intervalle r�gulier
	class StatsObserver extends Event{
		public StatsObserver(Simulator sim)
		{
			this.setSimulator(sim);
		}
		public void actions(){
			System.out.println("-------------------------Temps : "+sim.time()+"-----------------------------");
			System.out.println(waitTimeObservations.report());
			System.out.println("Variance : "+waitTimeObservations.variance());
			//ob.schedule(500);
		}
	}

	

}
