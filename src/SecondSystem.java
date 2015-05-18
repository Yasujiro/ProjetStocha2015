import java.util.ArrayList;

import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;



public class SecondSystem extends QueueSystem {

	private Tally bobTimeObs;
	private boolean bobAlreadyThere;
	private AcceptBob accBob;
	public SecondSystem(double lambda, double time,ServerStocha[] serv) {
		super(lambda, time, serv);
		bobAlreadyThere = false;
		bobTimeObs = new Tally("Temps d'attente moyen de Bob");
	}
	/*
	 * Lorsqu'un serveur se ferme, dispatch tous les clients de sa file dans les files des autres serveurs.
	 */
	public void serverClosed(ServerStocha serv ) {
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
			servers[j].requestServer(cust);
		}
	}
	@Override
	public void addWaitTimeObs(Customer cust)
	{
		double x = simulator.time()-cust.getArrivalTime();
		if(cust instanceof ChangingCust)
		{
			bobTimeObs.add(x);
		}
		else
			meanWaitTime.add(x);
	}
	@Override
	public void scheduleEvents()
	{
		super.scheduleEvents();
	}
	public void bobReport()
	{
		System.out.println(bobTimeObs.report());
	}
	@Override
	public void report()
	{
		super.report();
	}
	@Override
	public void custoLeaving(Customer cust)
	{
		if(cust instanceof ChangingCust)
		{
			accBob = new AcceptBob(simulator);
			accBob.schedule(500*Math.random());
			for(ServerStocha serv:servers)
			{
				serv.setQueueSizeObserver(null);				
				
			}
		}
	}
	/*
	 * Sélectionne le serveur où envoyer le client.
	 * Choisit le serveur ouvert du système avec le moins de personne dedans.
	 * @see QueueSystem#chooseServer()
	 */
	protected void chooseServer(Customer cust) {
		if(cust instanceof ChangingCust)
		{
			ArrayList<ServerStocha> servList = new ArrayList<>();			
			for(ServerStocha serv : servers)
			{
				if(serv instanceof ServerPoissonWithClose)
				{
					if(((ServerPoissonWithClose)serv).isAccepChangingCust())
						servList.add(serv);
				}
				else
					servList.add(serv);
			}
			ServerStocha choosenServ = servList.get(0);
			for(int i=1;i<servList.size() && (choosenServ.customerInSystem()>0);i++)
			{
				if(servList.get(i).isOpen() && servList.get(i).customerInSystem()<choosenServ.customerInSystem())
				{
					choosenServ = servList.get(i);
				}
				else if (servList.get(i).customerInSystem()==choosenServ.customerInSystem()) // Si taille égal, choix random.
				{
					double rand = Math.random();
					if(rand>0.5)
						choosenServ = servList.get(i);
				}
			}
			choosenServ.requestServer(cust);
			((ChangingCust)cust).setCurrentServer(choosenServ);
		}
		else
			super.chooseServer(cust);
	}
	@Override
	protected void manageNewCustomer()
	{
		if(!bobAlreadyThere && simulator.time() >= 20000)
		{
			bobAlreadyThere = true;
			arrival.schedule(expGen.nextDouble());
			ChangingCust chanCust = new ChangingCust(simulator.time(), servers);
			for(ServerStocha serv:servers)
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
			bobAlreadyThere = false;
		}		
	}
}
