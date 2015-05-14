import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;


public class ServerPoissonWithClose extends ServerPoisson {

	private boolean serverOpen;
	private boolean accepChangingCust;
	private int custToClose; //Nombre de personne avant de fermer.
	private static MRG31k3p randomGen = new MRG31k3p();
	private ExponentialGen closeTimeGen;
	private boolean seuilFranchi;
	public ServerPoissonWithClose(MRG32k3a dis, double mu,int custToClose) {
		super(dis, mu);
		serverOpen = true;
		closeTimeGen = new ExponentialGen(randomGen, 2/mu);
		accepChangingCust = false;
		seuilFranchi = false;
		this.custToClose = custToClose;
	}
	
	@Override
	public boolean isOpen()
	{
		return serverOpen;
	}
	protected void setObs(QueueSystem obs)
	{
		this.obs = (SecondSystem)obs;
	}
	public boolean isAccepChangingCust() {
		return accepChangingCust&&serverOpen;
	}
	@Override
	public void requestServer(Customer cust)
	{
		super.requestServer(cust);
		if(queue.size() == 2*custToClose)
		{			
			seuilFranchi = true;
		}
	}
	@Override
	protected Customer endOfService()
	{
		Customer leavingCust = super.endOfService();
		if(queue.size()==custToClose && seuilFranchi)
		{
			serverOpen = false;
			accepChangingCust = false;
			seuilFranchi = false;
			Opening op = new Opening();
			op.setSimulator(simulator);
			op.schedule(closeTimeGen.nextDouble());
			AcceptNewChangingCust acpt = new AcceptNewChangingCust();
			acpt.setSimulator(simulator);
			acpt.schedule(closeTimeGen.nextDouble());
			((SecondSystem)obs).serverClosed(this);
			//System.out.println("\n########## Fermeture"+simulator.time()+"########################\n");
			
		}
		return leavingCust;
	}
	class Opening extends Event{

		@Override
		/*Méthode invoqué lors que l'évent survient.
		*/
		public void actions() {
			//System.out.println("\n########## Overture"+simulator.time()+"########################\n");
			serverOpen = true;
		}
		
	}
	class AcceptNewChangingCust extends Event{

		@Override
		/*Méthode invoqué lors que l'évent survient.
		*/
		public void actions() {	
			accepChangingCust=true;
		}
		
	}

}
