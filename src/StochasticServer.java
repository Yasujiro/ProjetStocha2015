import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public abstract class StochasticServer {

	protected LinkedList<Customer> queue;
	protected QueueSystem system;
	protected Customer currentCustomer;
	protected Simulator simulator; 
	protected RandomVariateGen distribution;
	protected Departure departure;
	protected QueueObserver queueSizeObserver;
	protected Tally queueSizeObservations;
	
	/*
	 * Arrivé d'un nouveau client dans le system.
	 * Mets à jour l'observation de l'état du system.
	 * Genere le temps de service du client.
	 * Si le serveur est libre, le client démarre son service, sinon il est placé dans la file d'attente.
	 */
	public void addCustomer(Customer cust)
	{		
		updateSystemObservations();
		cust.setServiceTime(distribution.nextDouble());
		if(currentCustomer == null)
		{
			scheduleService(cust);
		}
		else
		{
			queue.addLast(cust);
		}
		
	}
	//Planifie l'event de départ du client entrant "en service".
	protected void scheduleService(Customer cust) {
		currentCustomer = cust;
		if(cust instanceof ChangingCustomer)
		{
			cust.waitingTime = (simulator.time()-cust.getArrivalTime());
			((ChangingCustomer)cust).setCurrentServer(null);
		}
		system.addWaitTimeObservation(cust);
		departure.schedule(currentCustomer.getServiceTime());
	}
	protected void setSystem(QueueSystem system)
	{
		this.system = system;
	}
	//Rajoute une observation de l'état du system.
	private void updateSystemObservations() 
	{		
		queueSizeObservations.add(queue.size());
	}

	public void setSimulator(Simulator simulator)
	{
		this.simulator = simulator;
		departure.setSimulator(this.simulator);
	}
	//"Retire" le client du serveur et le retourne.
	protected Customer endOfService()
	{
		Customer leavingC = currentCustomer;
		currentCustomer = null;
		return leavingC;
	}
	

	//Retourne 0 si personne dans le system. Sinon retourne la taille de la file + 1.
	public int nbCustomersInSystem()
	{
		int nbCustomersInSystem = 0;
		if(currentCustomer != null)
			nbCustomersInSystem = queue.size() + 1;
		return nbCustomersInSystem;
	}
	public LinkedList<Customer> getQueue()
	{
		return queue;
	}
	
	public void setQueueSizeObserver(QueueObserver observer)
	{
		queueSizeObserver = observer;
	}
	
	public boolean isOpen()
	{
		return true;		
	}
	
	public Customer getCurrentCustomer() {
		return currentCustomer;
	}

	//Classe d'Event permettnt de gérer les départ des client.
	class Departure extends Event{

		@Override
		/*Méthode invoqué lors que l'évent survient.
		 * Met fin au service du client actuel.
		 * Met à jours l'obersvation du temps moyen passé dans le system.
		 * Si des client sont présent dans la file, le premier de la file commence son service et est retiré de la file.
		*/
		public void actions() {	
			
			system.customerLeaving(endOfService());
			if(queue.size() >0)
			{
				scheduleService(queue.removeFirst());
				
			}
			if(queueSizeObserver != null)
				queueSizeObserver.queueReduced();
		}		
	}
}
