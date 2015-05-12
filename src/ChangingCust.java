

public class ChangingCust extends Customer implements QueueObserver {

	private ServerStocha[] serverList;
	private ServerStocha currentServer;
	private int sizeDiff =3 ;
	public ChangingCust(double arrival,ServerStocha[] servers) {
		super(arrival);
		serverList =servers;
	}	
	/*
	 * Lorsque la file d'un serveur se r�duit, Si this n'a pas d�j� �t� servi ou n'est pas en train d'�tre servie,
	 * compare la taille de la file de currentServer avec celles des autres serveurs.
	 * Si une file pr�sente moins de personne que le nombre de personne pr�sent devant this - n, changer de serveur.
	 * @see QueueObserver#QueueReduced()
	 */
	@Override
	public void QueueReduced() {
		
		if(currentServer == null || currentServer.getCurrentCustomer() == this )return; // Si d�j� servi ou en train d'�tre servi, pas besoin de changer
		ServerStocha nextServ = currentServer;
		for(ServerStocha serv : serverList)
		{
			if(serv!=currentServer)
			{
				if(serv.isOpen()
						||(serv instanceof ServerPoissonWithClose && ((ServerPoissonWithClose)serv).isAccepChangingCust()))
				{
					if(serv.getQueue().size() < currentServer.getQueue().indexOf(this) - sizeDiff )
					{
						nextServ = serv;
					}
				}
			}
		}
		if(nextServ != currentServer)
		{
			currentServer.getQueue().remove(this);
			nextServ.requestServer(this);
			currentServer = nextServ;
		}
	}


	public void setCurrentServer(ServerStocha currentServer) {
		this.currentServer = currentServer;
	}

}
