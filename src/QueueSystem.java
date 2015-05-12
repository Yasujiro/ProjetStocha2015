import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;
import umontreal.iro.lecuyer.stat.TallyStore;




public class QueueSystem {
	
	
	protected ServerStocha[] servers;
	private double lambda;
	protected Simulator simulator;
	protected Arrival arrival;
	protected ExponentialGen expGen;
	protected double timeOfSim;
	private Tally custoInQueue;
	
	protected Tally meanWaitTime; //Liste d'observation des temps d'attente.

	
	public QueueSystem(double lambda, double time, ServerStocha[] servs)
	{
		simulator = new Simulator(); //Instance de simulation, contient la liste des events, le scheduler.
		this.lambda = lambda;
		timeOfSim = time;	
		meanWaitTime = new Tally("Temps d'attente moyen");
		custoInQueue = new Tally();
		arrival = new Arrival();
		arrival.setSimulator(simulator);
		initializeArrivalGen();
		servers = servs;
		for(ServerStocha serv:servers)
		{
			serv.setSimu(simulator);
			serv.setObs(this);
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
	public void report()
	{
		if(meanWaitTime.numberObs()>0)
			System.out.println(meanWaitTime.report(0.95,5)+"\n");
		if(meanWaitTime.numberObs()>2)
			System.out.println(meanWaitTime.formatCIStudent(0.95,5));
		QueueReport();
	}

	protected void QueueReport() {
		int i = 1;
		double meanQueue=0;
		for(ServerStocha serv: servers)
		{
			System.out.println("Nombre moyen de personne dans la file du serveur  : "+i+" "+serv.queueSizeObs.average());
			meanQueue+=serv.queueSizeObs.average();
			i++;
		}
		System.out.println("\nNombre moyen de personne dans les files " +meanQueue/2);
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

	public void addWaitTimeObs(double x)
	{
		meanWaitTime.add(x);
	}
	public void addQueueSizeObs(double x)
	{
		custoInQueue.add(x);
	}
	protected void manageNewCustomer() {
		arrival.schedule(expGen.nextDouble());
		Customer cust = new Customer(simulator.time());			
		ServerStocha choosenServ = chooseServer();
		choosenServ.requestServer(cust);
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

	

}
