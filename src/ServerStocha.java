import java.util.LinkedList;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;
import umontreal.iro.lecuyer.stat.Tally;


public abstract class ServerStocha {

	protected LinkedList<Customer> waitingList; 
	protected Customer currentCustomer;
	protected Simulator simulator; 
	protected RandomVariateGen distribution;
	protected Tally systemObs;
	protected Tally waitTimeObservation;
	protected Departure depart;
	
	/*
	 * Arrivé d'un nouveau client dans le system.
	 * Mets à jour l'observation de l'état du system.
	 * Genere le temps de service du client.
	 * Si le serveur est libre, le client démarre son service, sinon il est placé dans la file d'attente.
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
			waitingList.addLast(cust);
		}
		
	}
	//Planifie l'event de départ du client entrant "en service".
	private void startOfService(Customer cust) {
		currentCustomer = cust;
		depart.schedule(currentCustomer.getServTime());
	}
	
	//Rajoute une observation de l'état du system.
	private void updateSystemObs() {
		
		int nbPerso = customerInSystem();
		systemObs.add(nbPerso);
	}
	
	//"Retire" le client du serveur et le retourne.
	private Customer endOfService()
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
			customerInServ = waitingList.size()+1;
		return customerInServ;
	}
	
	//Retourne la moyenne du nombre de client dans le système.
	public double avrgCustomerInSystem()
	{		
		return systemObs.average();
	}
	//Retourne la moyenne du temps passé dans le system.
	public double avgWaitingTime()
	{
		return waitTimeObservation.average();
	}
	
	public void report(double lambdaArrival)
	{
		System.out.println("Nombre moyen de personne dans la system "+avrgCustomerInSystem());
		System.out.println("Temps moyen dans le system "+avgWaitingTime());
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
			
			Customer leavingCust = endOfService();
			waitTimeObservation.add(simulator.time()-leavingCust.getArrivalTime());
			if(waitingList.size() >0)
			{
				startOfService(waitingList.removeFirst());
				
			}
		}
		
	}
}
