import java.util.ArrayList;



public class SecondSystem extends QueueSystem {

	private ChangingCust chanCust;
	private int nbFermeture;
	private boolean bobAlreadyThere;
	public SecondSystem(double lambda, double time,ServerStocha[] serv) {
		super(lambda, time, serv);
//		for(ServerStocha serveur : serv)
//		{
//			if(serveur instanceof ServerPoissonWithClose)
//				((ServerPoissonWithClose)serveur).setObs(this);
//		}
		bobAlreadyThere = true;
		nbFermeture = 0;
	}
	/*
	 * Lorsqu'un serveur se ferme, dispatch tous les clients de sa file dans les files des autres serveurs.
	 */
	public void serverClosed(ServerStocha serv ) {
		Customer cust;
		int j =0;
		nbFermeture++;
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
		super.scheduledEvents();
	}
	public double changingCustomerWaitTime()
	{
		return chanCust.waitingTime;
	}
	@Override
	public void report()
	{
		super.report();
		System.out.println("Nombre de fermeture de serveur : "+nbFermeture+"\n");
	}
	/*
	 * Sélectionne le serveur où envoyer le client.
	 * Choisit le serveur ouvert du système avec le moins de personne dedans.
	 * @see QueueSystem#chooseServer()
	 */
	protected ServerStocha chooseServer() {
//		ArrayList<ServerStocha> openedServ = new ArrayList<>();
//		
//		for(ServerStocha serv : servers)
//		{
//			if(serv.isOpen())
//				openedServ.add(serv);
//		}
//		ServerStocha choosenServ = openedServ.get(0);
//		for(int i=1;i<servers.length && (choosenServ.customerInSystem()>0);i++)
//		{
//			if(servers[i].isOpen() && servers[i].customerInSystem()<choosenServ.customerInSystem())
//			{
//				choosenServ = servers[i];
//			}
//		}
		return super.chooseServer();
	}
	@Override
	protected void manageNewCustomer()
	{
		if(!bobAlreadyThere && simulator.time() >= timeOfSim/10)
		{
			bobAlreadyThere = true;
			arrival.schedule(expGen.nextDouble());
			chanCust = new ChangingCust(simulator.time(), servers);
			for(ServerStocha serv:servers)
			{
				serv.addObserver(chanCust);
				
				
			}
			ServerStocha choosenServ = chooseServer();			
			choosenServ.requestServer(chanCust);
			chanCust.setCurrentServer(choosenServ);
			
		}
		else{
			super.manageNewCustomer();
		}
	}
}
