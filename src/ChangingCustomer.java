

public class ChangingCustomer extends Customer implements QueueObserver {

	private StochasticServer[] serversList;
	private StochasticServer currentServer; // Serveur dans lequel se trouve ce client
	private int sizeDifference = 1; // Diff�rence de taille de file n�cessaire avant un changement de file
	public ChangingCustomer(double arrival, StochasticServer[] servers) {
		super(arrival);
		serversList =servers;
	}
	
	/*
	 * Lorsque la file d'un serveur se r�duit, si this n'a pas d�j� �t� servi ou n'est pas en train d'�tre servi,
	 * compare la taille de la file de currentServer avec celles des autres serveurs.
	 * Si une file pr�sente moins de personne que le nombre de personnes pr�sentes devant this - n, change de serveur.
	 * @see QueueObserver#QueueReduced()
	 */
	@Override
	public void queueReduced() {
		
		// Si le client a d�j� �t� servi ou est en cours de service, pas besoin de changer
		if(currentServer == null || currentServer.getCurrentCustomer() == this)
			return;
		
		StochasticServer nextServ = currentServer;
		for(StochasticServer serv : serversList)
		{
			if(serv!=currentServer)
			{
				if(!(serv instanceof PoissonServerWithClosing) 
						|| ((PoissonServerWithClosing)serv).isAcceptingChangingCustomer())
				{
					if(serv.getQueue().size() < currentServer.getQueue().indexOf(this) - sizeDifference )
					{
						nextServ = serv;
					}
				}
			}
		}
		
		if(nextServ != currentServer)
		{
			currentServer.getQueue().remove(this);
			nextServ.addCustomer(this);
			currentServer = nextServ;
		}
	}

	public void setCurrentServer(StochasticServer currentServer) {
		this.currentServer = currentServer;
	}

}
