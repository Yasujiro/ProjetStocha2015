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
			waitingList.addLast(cust);
		}
		
	}
	//Planifie l'event de d�part du client entrant "en service".
	private void startOfService(Customer cust) {
		currentCustomer = cust;
		depart.schedule(currentCustomer.getServTime());
	}
	
	//Rajoute une observation de l'�tat du system.
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
	
	//Retourne la moyenne du nombre de client dans le syst�me.
	public double avrgCustomerInSystem()
	{		
		return systemObs.average();
	}
	//Retourne la moyenne du temps pass� dans le system.
	public double avgWaitingTime()
	{
		return waitTimeObservation.average();
	}
	
	public void report(double lambdaArrival)
	{
		System.out.println("Nombre moyen de personne dans la system "+avrgCustomerInSystem());
		System.out.println("Temps moyen dans le system "+avgWaitingTime());
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
			
			Customer leavingCust = endOfService();
			waitTimeObservation.add(simulator.time()-leavingCust.getArrivalTime());
			if(waitingList.size() >0)
			{
				startOfService(waitingList.removeFirst());
				
			}
		}
		
	}
}
