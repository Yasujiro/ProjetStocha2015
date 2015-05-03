import java.util.ArrayList;

import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;


public class SecondSystem extends QueueSystem {

	private ChangingCust chanCust;
	public ChangingCustArrival chArrival;
	public SecondSystem(double lambda, double time,ServerStocha[] serv) {
		super(lambda, time, serv);
		for(ServerStocha serveur : serv)
		{
			if(serveur instanceof ServerPoissonWithClose)
				((ServerPoissonWithClose)serveur).setObs(this);
		}		
		
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
	public void scheduledEvents()
	{
		scheduleChCustArrival();
		super.scheduledEvents();
	}
	public double changingCustomerWaitTime()
	{
		return chanCust.waitingTime;
	}
	
	/*
	 * Sélectionne le serveur où envoyer le client.
	 * Choisit le serveur ouvert du système avec le moins de personne dedans.
	 * @see QueueSystem#chooseServer()
	 */
	protected ServerStocha chooseServer() {
		ArrayList<ServerStocha> openedServ = new ArrayList<>();
		
		for(ServerStocha serv : servers)
		{
			if(serv.isOpen())
				openedServ.add(serv);
		}
		ServerStocha choosenServ = openedServ.get(0);
		for(int i=1;i<servers.length && (choosenServ.customerInSystem()>0);i++)
		{
			if(servers[i].isOpen() && servers[i].customerInSystem()<choosenServ.customerInSystem())
			{
				choosenServ = servers[i];
			}
		}
		return choosenServ;
	}
	private void scheduleChCustArrival()
	{
		//MRG31k3p randomGen = new MRG31k3p();
		chArrival = new ChangingCustArrival(simulator);
		chArrival.schedule(timeOfSim/2); //scheduled arrival in the 2nd part of the simulation.
		
	}
	class ChangingCustArrival extends Event{

		public ChangingCustArrival(Simulator simu) {
			this.setSimulator(simu);
		}
		//Arrivé du client "Bob"
		@Override
		public void actions() {
			
			chanCust = new ChangingCust(simulator.time(), servers);
			for(ServerStocha serv:servers)
			{
				serv.addObserver(chanCust);
			}
			ServerStocha choosenServ = chooseServer();			
			choosenServ.requestServer(chanCust);
			chanCust.setCurrentServer(choosenServ);
		}
	}
}
