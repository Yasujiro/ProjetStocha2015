import java.util.ArrayList;

import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;



public class SecondSystem extends QueueSystem {

	private Tally bobTimeObservations;
	private boolean bobIsAlreadyThere;
	private AcceptBob acceptBob;
	public SecondSystem(double lambda, double time, StochasticServer[] serv) {
		super(lambda, time, serv);
		bobIsAlreadyThere = false;
		bobTimeObservations = new Tally("Temps d'attente moyen de Bob");
	}
	/*
	 * Lorsqu'un serveur se ferme, dispatch tous les clients de sa file dans les files des autres serveurs.
	 */
	public void serverClosed(StochasticServer serv ) {
		Customer cust;
		int j =0;
		while(serv.getQueue().size()>0)
		{
			if(servers[j] == serv)
			{
				j++;				
			}
			if(j==servers.length)  j = 0;
			cust = serv.getQueue().removeFirst();
			servers[j].addCustomer(cust);
		}
	}
	@Override
	public void addWaitTimeObservation(Customer cust)
	{
		double x = simulator.time()-cust.getArrivalTime();
		if(cust instanceof ChangingCustomer)
		{
			bobTimeObservations.add(x);
		}
		else
			meanWaitTimeObservations.add(x);
	}
	@Override
	public void scheduleEvents()
	{
		super.scheduleEvents();
	}
	public void bobReport()
	{
		System.out.println(bobTimeObservations.report());
	}
	@Override
	public void report()
	{
		super.report();
	}
	@Override
	public void customerLeaving(Customer cust)
	{
		if(cust instanceof ChangingCustomer)
		{
			acceptBob = new AcceptBob(simulator);
			acceptBob.schedule(500*Math.random());
			
			// TEST
			this.simulator.stop();
			
			for(StochasticServer serv:servers)
			{
				serv.setQueueSizeObserver(null);				
				
			}
		}
	}
	/*
	 * S�lectionne le serveur o� envoyer le client.
	 * Choisit le serveur ouvert du syst�me avec le moins de personne dedans.
	 * @see QueueSystem#chooseServer()
	 */
	protected void chooseServer(Customer cust) {
		if(cust instanceof ChangingCustomer)
		{
			ArrayList<StochasticServer> servList = new ArrayList<>();			
			for(StochasticServer serv : servers)
			{
				if(serv instanceof PoissonServerWithClosing)
				{
					if(((PoissonServerWithClosing)serv).isAccepChangingCust())
						servList.add(serv);
				}
				else
					servList.add(serv);
			}
			StochasticServer choosenServ = servList.get(0);
			for(int i=1;i<servList.size() && (choosenServ.nbCustomersInSystem()>0);i++)
			{
				if(servList.get(i).isOpen() && servList.get(i).nbCustomersInSystem()<choosenServ.nbCustomersInSystem())
				{
					choosenServ = servList.get(i);
				}
				else if (servList.get(i).nbCustomersInSystem()==choosenServ.nbCustomersInSystem()) // Si taille �gal, choix random.
				{
					double rand = Math.random();
					if(rand>0.5)
						choosenServ = servList.get(i);
				}
			}
			choosenServ.addCustomer(cust);
			((ChangingCustomer)cust).setCurrentServer(choosenServ);
		}
		else
			super.chooseServer(cust);
	}
	@Override
	protected void manageNewCustomer()
	{
		if(!bobIsAlreadyThere && simulator.time() >= 20000)
		{
			bobIsAlreadyThere = true;
			arrival.schedule(expGen.nextDouble());
			ChangingCustomer chanCust = new ChangingCustomer(simulator.time(), servers);
			for(StochasticServer serv:servers)
			{
				serv.setQueueSizeObserver(chanCust);				
				
			}
			chooseServer(chanCust);
			
		}
		else{
			super.manageNewCustomer();
		}
	}
	
	class AcceptBob extends Event{
		public AcceptBob(Simulator sim)
		{
			setSimulator(sim);
		}
		@Override
		public void actions() {	
			bobIsAlreadyThere = false;
		}		
	}
}
