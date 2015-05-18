

public class ChangingCustomer extends Customer implements QueueObserver {

	private StochasticServer[] serversList;
	private StochasticServer currentServer;
	private int sizeDifference = 1;
	public ChangingCustomer(double arrival, StochasticServer[] servers) {
		super(arrival);
		serversList =servers;
	}	
	/*
	 * Lorsque la file d'un serveur se r�duit, Si this n'a pas d�j� �t� servi ou n'est pas en train d'�tre servie,
	 * compare la taille de la file de currentServer avec celles des autres serveurs.
	 * Si une file pr�sente moins de personne que le nombre de personne pr�sent devant this - n, changer de serveur.
	 * @see QueueObserver#QueueReduced()
	 */
	@Override
	public void queueReduced() {
		
		if(currentServer == null || currentServer.getCurrentCustomer() == this )return; // Si d�j� servi ou en train d'�tre servi, pas besoin de changer
		StochasticServer nextServ = currentServer;
		for(StochasticServer serv : serversList)
		{
			if(serv!=currentServer)
			{
				if(!(serv instanceof PoissonServerWithClosing) || ((PoissonServerWithClosing)serv).isAccepChangingCust())
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
