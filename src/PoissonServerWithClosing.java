import umontreal.iro.lecuyer.randvar.ExponentialGen;
import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.simevents.Event;
import umontreal.iro.lecuyer.simevents.Simulator;


public class PoissonServerWithClosing extends PoissonServer {

	private boolean isOpen;
	private boolean acceptChangingCustomers;
	private int nbCustomersToClose; //Nombre de personne avant de fermer.
	private static MRG31k3p randomGen = new MRG31k3p();
	private ExponentialGen closingTimeGen;
	private boolean tresholdCrossed;
	private AcceptNewChangingCust acceptNewChangingCust;
	public PoissonServerWithClosing(MRG32k3a dis, double mu,int custToClose) {
		super(dis, mu);
		isOpen = true;
		closingTimeGen = new ExponentialGen(randomGen, 2/mu);
		acceptChangingCustomers = false;
		tresholdCrossed = false;
		this.nbCustomersToClose = custToClose;
	}
	
	@Override
	public boolean isOpen()
	{
		return isOpen;
	}
	@Override
	public void setSimulator(Simulator sim)
	{
		super.setSimulator(sim);
	}
	protected void setSystem(QueueSystem obs)
	{
		this.system = (SecondSystem)obs;
	}
	public boolean isAccepChangingCust() {
		return acceptChangingCustomers&&isOpen;
	}
	@Override
	public void addCustomer(Customer cust)
	{
		super.addCustomer(cust);
		if(queue.size() == 2*nbCustomersToClose)
		{			
			tresholdCrossed = true;
		}
	}
	@Override
	protected Customer endOfService()
	{
		Customer leavingCust = super.endOfService();
		if(queue.size()==nbCustomersToClose && tresholdCrossed)
		{
			isOpen = false;
			acceptChangingCustomers = false;
			tresholdCrossed = false;
			Opening op = new Opening();
			op.setSimulator(simulator);
			op.schedule(closingTimeGen.nextDouble());
			((SecondSystem)system).serverClosed(this);
			
		}
		return leavingCust;
	}
	class Opening extends Event{

		@Override
		/*Méthode invoqué lors que l'évent survient.
		*/
		public void actions() {
			isOpen = true;
			acceptNewChangingCust = new AcceptNewChangingCust(simulator);
			acceptNewChangingCust.schedule(closingTimeGen.nextDouble());
		}
		
	}
	class AcceptNewChangingCust extends Event{

		public AcceptNewChangingCust(Simulator simulator) {
			setSimulator(simulator);
		}
		@Override
		/*Méthode invoqué lors que l'évent survient.
		*/
		public void actions() {	
			if(isOpen)
				acceptChangingCustomers = true;
		}
		
	}

}
