import java.util.ArrayList;
import java.util.LinkedList;

import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;
import umontreal.iro.lecuyer.stat.TallyStore;


public abstract class ServerStocha {

	protected LinkedList<Customer> queue;
	protected QueueSystem obs;
	protected Customer currentCustomer;
	protected Simulator simulator; 
	protected RandomVariateGen distribution;
	protected Departure depart;
	protected QueueObserver queueSizeObserver;
	protected Tally queueSizeObs;
	
	/*
	 * Arriv� d'un nouveau client dans le system.
	 * Mets � jour l'observation de l'�tat du system.
	 * Genere le temps de service du client.
	 * Si le serveur est libre, le client d�marre son service, sinon il est plac� dans la file d'attente.
	 */
	public void requestServer(Customer cust)
	{		
		updateSystemObs();
		cust.setServTime(distribution.nextDouble());
		if(currentCustomer == null)
		{
			startOfService(cust);
		}
		else
		{
			queue.addLast(cust);
		}
		
	}
	//Planifie l'event de d�part du client entrant "en service".
	protected void startOfService(Customer cust) {
		currentCustomer = cust;
		if(cust instanceof ChangingCust)
		{
			cust.waitingTime = (simulator.time()-cust.getArrivalTime());
			((ChangingCust)cust).setCurrentServer(null);
		}
		obs.addWaitTimeObs(cust);
		depart.schedule(currentCustomer.getServTime());
	}
	protected void setObs(QueueSystem obs)
	{
		this.obs =obs;
	}
	//Rajoute une observation de l'�tat du system.
	private void updateSystemObs() 
	{		
		queueSizeObs.add(queue.size());
	}

	public void setSimu(Simulator sim)
	{
		simulator = sim;
		depart.setSimulator(simulator);
	}
	//"Retire" le client du serveur et le retourne.
	protected Customer endOfService()
	{
		Customer leavingC = currentCustomer;
		currentCustomer = null;
		return leavingC;
	}
	

	//Retourne 0 si personne dans le system. Sinon retourne la taille de la file + 1.
	public int customerInSystem()
	{
		int customerInServ =0;
		if(currentCustomer != null)
			customerInServ = queue.size()+1;
		return customerInServ;
	}
	public LinkedList<Customer> getQueue()
	{
		return queue;
	}
	
	public void setQueueSizeObserver(QueueObserver obs)
	{
		queueSizeObserver = obs;
	}
	
	public boolean isOpen()
	{
		return true;		
	}
	
	public Customer getCurrentCustomer() {
		return currentCustomer;
	}

	//Classe d'Event permettnt de g�rer les d�part des client.
	class Departure extends Event{

		@Override
		/*M�thode invoqu� lors que l'�vent survient.
		 * Met fin au service du client actuel.
		 * Met � jours l'obersvation du temps moyen pass� dans le system.
		 * Si des client sont pr�sent dans la file, le premier de la file commence son service et est retir� de la file.
		*/
		public void actions() {	
			
			obs.custoLeaving(endOfService());
			if(queue.size() >0)
			{
				startOfService(queue.removeFirst());
				
			}
			if(queueSizeObserver != null)
				queueSizeObserver.QueueReduced();
		}		
	}
}
