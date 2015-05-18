import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;


public class Main {
	
	public static MRG31k3p randomSeedGen = new MRG31k3p();
	private static double mu = 2;
	private static int nbServers = 2;
	private static double lambda = 3.8;
	private static int kErlang = 5;
	private static double muK = mu*kErlang;
	private static int simulationTime = 60000;
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
		simulateSystem(4,nbServers,simulationTime);
		

	}
	
	private static void simulateSystem(int systemNumber,int nbServers,int simulationTime) 
	{
		for(int i =0;i<15;i++)
		{
			StochasticServer[] servSystem = null;
			QueueSystem system = null;
			servSystem= new StochasticServer[nbServers];
			
			switch(systemNumber)
			{
			case 2:
				initializeServer(servSystem,ServerType.ERLANG,true);
				system = new QueueSystem(lambda, simulationTime,servSystem);
				break;
			case 3:
				initializeServer(servSystem,ServerType.POISSON,true);
				system = new RandomSelectionSystem(lambda, simulationTime,servSystem);
				break;
			case 4:
				initializeServer(servSystem, ServerType.WITH_CLOSING,true);
				system= new SecondSystem(lambda, simulationTime, servSystem);
				break;
			default:
				initializeServer(servSystem,ServerType.POISSON,true);
				system = new QueueSystem(lambda, simulationTime,servSystem);
				break;						
			}
	
		
		system.startSimulation();
	
			System.out.println("\n---------------------Résultat final-----------------------");
			System.out.println("System "+systemNumber+" avec "+nbServers+" server(s) : \n");
			system.report();
			
			if(system instanceof SecondSystem)
				((SecondSystem)system).bobReport();
			System.out.println("-------------------------------------------------------\n");
			
		}
	}
	private static void initializeServer(StochasticServer[] systemServers, ServerType serverType, boolean randomSeed)
	{
		long[] seed = new long[6];	
		for(int i =0;i<systemServers.length;i++)
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
			switch(serverType)
			{
				case POISSON:
					systemServers[i] = new PoissonServer(pStream,mu);
					break;
				case ERLANG:
					systemServers[i] = new ErlangServer(pStream,kErlang,muK);
					break;
				case WITH_CLOSING:
					if(i%2==0)
						systemServers[i] = new PoissonServer(pStream,mu);
					else
						systemServers[i] = new PoissonServerWithClosing(pStream, mu,6);
					break;
			}
		}
	}

}
