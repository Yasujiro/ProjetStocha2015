import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;


public class ServerPoissonWithClose extends ServerPoisson {

	private boolean serverOpen;
	private boolean accepChangingCust;
	private int custToClose; //Nombre de personne avant de fermer.
	private static MRG31k3p randomGen = new MRG31k3p();
	private ExponentialGen closeTimeGen;
	private boolean seuilFranchi;
	private AcceptNewChangingCust acpt;
	public ServerPoissonWithClose(MRG32k3a dis, double mu,int custToClose) {
		super(dis, mu);
		serverOpen = true;
		closeTimeGen = new ExponentialGen(randomGen, 2/mu);
		accepChangingCust = false;
		seuilFranchi = false;
		this.custToClose = custToClose;
		acpt = new AcceptNewChangingCust();
	}
	
	@Override
	public boolean isOpen()
	{
		return serverOpen;
	}
	@Override
	public void setSimu(Simulator sim)
	{
		super.setSimu(sim);
		acpt.setSimulator(sim);
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
			((SecondSystem)obs).serverClosed(this);
			
		}
		return leavingCust;
	}
	class Opening extends Event{

		@Override
		/*Méthode invoqué lors que l'évent survient.
		*/
		public void actions() {
			serverOpen = true;
			acpt.schedule(closeTimeGen.nextDouble());
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
