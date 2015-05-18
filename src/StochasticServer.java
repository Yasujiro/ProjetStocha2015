import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public abstract class StochasticServer {

	protected LinkedList<Customer> queue; // File de clients
	protected QueueSystem system; // Référence vers le système englobant
	protected Customer currentCustomer; // Client en cours de service
	protected Simulator simulator; // Référence vers le simulateur du système
	protected RandomVariateGen distribution;
	protected Departure departure; // Évènement de départ d'un client
	protected QueueObserver queueSizeObserver;
	protected Tally queueSizeObservations; // Liste d'observations des tailles de la file
	
	/* Arrivée d'un nouveau client dans le système.
	 * Si le serveur est libre, le client démarre son service, sinon il est placé dans la file d'attente.
	 */
	public void addCustomer(Customer customer)
	{		
		// Mets à jour l'observation de l'état du système
		updateSystemObservations();
		
		// Génère le temps de service du client
		customer.setServiceTime(distribution.nextDouble());
		
		// Si le serveur est libre
		if(currentCustomer == null)
		{
			scheduleDeparture(customer);
		}
		else
		{
			queue.addLast(customer);
		}
		
	}
	
	//Planifie l'évènement de départ du client entrant "en service".
	protected void scheduleDeparture(Customer customer) {
		currentCustomer = customer;
		if(customer instanceof ChangingCustomer)
		{
			customer.waitingTime = (simulator.time()-customer.getArrivalTime());
			((ChangingCustomer)customer).setCurrentServer(null);
		}
		system.addWaitTimeObservation(customer);
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

	// Évènement de départ d'un client
	class Departure extends Event{

		@Override
		/* Méthode invoquée lorsque l'évènement survient.
		 * Mets à jour l'observation du temps moyen passé dans le système.
		 * Si des client sont présents dans la file, le premier de celle-ci commence son service et est en est retiré
		*/
		public void actions() {	
			
			// Mets fin au service du client actuel
			system.customerLeaving(endOfService());
			
			if(queue.size() > 0)
			{
				scheduleDeparture(queue.removeFirst());
				
			}
			if(queueSizeObserver != null)
				queueSizeObserver.queueReduced();
		}		
	}
}
