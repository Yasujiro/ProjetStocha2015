import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;


public class Main {
	
	public static MRG31k3p randomSeedGen = new MRG31k3p();
	private static double mu = 2;
	private static int nbServeur = 2;
	private static double lambda = 3.8;
	private static int kErlang = 5;
	private static double muK = mu*kErlang;
	private static int tempsSimu = 60000;
	public static void main(String[] args) 
	{
		
		//simulateSystem(1,1,tempsSimu);		
		//simulateSystem(1,nbServeur,tempsSimu);
		//simulateSystem(2,1);
		//simulateSystem(2,nbServeur,tempsSimu);
		//simulateSystem(3,1);
		//simulateSystem(3,nbServeur,tempsSimu);
		/*
		 * Simulation partie 2
		 * 
		 */		
		simulateSystem(4,nbServeur,tempsSimu);
		

	}
	
	private static void simulateSystem(int numSystem,int numServers,int tempsSimulation) 
	{
		for(int i =0;i<15;i++)
		{
			StochasticServer[] servSystem = null;
			Tally bobWaitTime = new Tally("Temps d'attente moyen du client 'changeant'");
			QueueSystem system = null;
			servSystem= new StochasticServer[numServers];
			
			switch(numSystem)
			{
			case 2:
				initializeServer(servSystem,ServerType.ERLANG,true);
				system = new QueueSystem(lambda, tempsSimulation,servSystem);
				break;
			case 3:
				initializeServer(servSystem,ServerType.POISSON,true);
				system = new RandomSelectionSystem(lambda, tempsSimulation,servSystem);
				break;
			case 4:
				initializeServer(servSystem, ServerType.WITH_CLOSING,true);
				system= new SecondSystem(lambda, tempsSimulation, servSystem);
				break;
			default:
				initializeServer(servSystem,ServerType.POISSON,true);
				system = new QueueSystem(lambda, tempsSimulation,servSystem);
				break;						
			}
	
		
		system.startSimulation();
		
	//	if(system instanceof SecondSystem)
	//		bobWaitTime.add(((SecondSystem)system).changingCustomerWaitTime());
	
			System.out.println("\n---------------------Résultat final-----------------------");
			System.out.println("System "+numSystem+" avec "+numServers+" server(s) : \n");
			system.report();
			
			if(system instanceof SecondSystem)
				((SecondSystem)system).bobReport();
			System.out.println("-------------------------------------------------------\n");
			
		}
	}
	private static void initializeServer(StochasticServer[] servSystem,ServerType typeServ,boolean randomSeed)
	{
		long[] seed = new long[6];	
		for(int i =0;i<servSystem.length;i++)
		{
			for(int j =0 ; j<seed.length;j++)
			{
				if(randomSeed)
					seed[j] = randomSeedGen.nextInt(1, 429494444);
				else
					seed[j] = 12345*(i+1);
			}			
			MRG32k3a pStream = new MRG32k3a();
			pStream.setSeed(seed);
			switch(typeServ)
			{
				case POISSON:
					servSystem[i] = new PoissonServer(pStream,mu);
					break;
				case ERLANG:
					servSystem[i] = new ErlangServer(pStream,kErlang,muK);
					break;
				case WITH_CLOSING:
					if(i%2==0)
						servSystem[i] = new PoissonServer(pStream,mu);
					else
						servSystem[i] = new PoissonServerWithClosing(pStream, mu,6);
					break;
			}
		}
	}

}
