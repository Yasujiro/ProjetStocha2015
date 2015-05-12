import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;


public class Main {
	
	public static MRG31k3p randomSeedGen = new MRG31k3p();
	private static double mu = 2;
	private static int nbServeur = 2;
	private static double lambda = 3;
	private static int kErlang = 2;
	private static double muK = 2*kErlang;
	private static int tempsSimu = 1000000;
	public static void main(String[] args) 
	{
		
		/*
		 * Déterminer temps stationnaire
		 */
		SimulationTempsCroissant(1, 2, 500);
		
		//simulateSystem(1,1);		
		//simulateSystem(1,nbServeur,tempsSimu);
		//simulateSystem(2,1);
		//simulateSystem(2,2);
		//simulateSystem(3,1);
		//simulateSystem(3,2);
		/*
		 * Simulation partie 2
		 * 
		 */		
		//simulateSystem(4,2);
		

	}
	private static void SimulationTempsCroissant(int numSystem, int nbServ, int tempsSimulation)
	{
		ServerStocha[] servSystem = new ServerStocha[nbServ];
		QueueSystem system = null;
		
		for(int i = 1; i<= tempsSimulation;i+=10)
		{
			switch(numSystem)
			{
			case 2:
				initializeServer(servSystem,TypeServeur.Erlang,false);
				system = new QueueSystem(lambda, i,servSystem);
				break;
			case 3:
				initializeServer(servSystem,TypeServeur.Poisson,false);
				system = new RandomSelectionSystem(lambda, i,servSystem);
				break;
			case 4:
				initializeServer(servSystem, TypeServeur.WithClose,false);
				system= new SecondSystem(lambda, i, servSystem);
				break;
			default:
				initializeServer(servSystem,TypeServeur.Poisson,false);
				system = new QueueSystem(lambda, i,servSystem);
				break;						
			}
			system.LaunchSimu();
			System.out.println("Durée simulation " + i+"\n");
			system.report();
			System.out.println("---------------------------\n");
		}
		
	}
	private static void simulateSystem(int numSystem,int numServers,int tempsSimulation) 
	{
		ServerStocha[] servSystem = null;
		Tally bobWaitTime = new Tally("Temps d'attente moyen du client 'changeant'");
		QueueSystem system = null;
		servSystem= new ServerStocha[numServers];
		
		switch(numSystem)
		{
		case 2:
			initializeServer(servSystem,TypeServeur.Erlang,true);
			system = new QueueSystem(lambda, tempsSimulation,servSystem);
			break;
		case 3:
			initializeServer(servSystem,TypeServeur.Poisson,true);
			system = new RandomSelectionSystem(lambda, tempsSimulation,servSystem);
			break;
		case 4:
			initializeServer(servSystem, TypeServeur.WithClose,true);
			system= new SecondSystem(lambda, tempsSimulation, servSystem);
			break;
		default:
			initializeServer(servSystem,TypeServeur.Poisson,true);
			system = new QueueSystem(lambda, tempsSimulation,servSystem);
			break;						
		}

	
	system.LaunchSimu();
	
	if(system instanceof SecondSystem)
		bobWaitTime.add(((SecondSystem)system).changingCustomerWaitTime());

		
		System.out.println("System "+numSystem+" avec "+numServers+" server(s) : \n");
		system.report();
		
		if(bobWaitTime.numberObs() >0)
		{
			System.out.println(bobWaitTime.report(0.95,5));
			System.out.println(bobWaitTime.formatCIStudent(0.95,5));
		}
		//Vérification de l'intervalle calculé par SSJ => OK.
//		double sd=0;
//		for(double time:system.meanWaiTime().getArray())
//		{
//			sd+=(Math.pow((time- system.meanWaiTime().average()), 2));
//		}		
//		sd *= 1.0/(system.meanWaiTime().numberObs()-1);
//		sd = 1.96*Math.sqrt((sd)/system.meanWaiTime().numberObs());
//		System.out.println("Intervalle : ["+(system.meanWaiTime().average()-sd)+" ; "+(system.meanWaiTime().average()+sd)+"]\n");
		System.out.println("-------------------------------------------------------\n");
		
	}
	
	private static void initializeServer(ServerStocha[] servSystem,TypeServeur typeServ,boolean randomSeed)
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
				case Poisson:
					servSystem[i] = new ServerPoisson(pStream,mu);
					break;
				case Erlang:
					servSystem[i] = new ServerErlang(pStream,kErlang,muK);
					break;
				case WithClose:
					if(i%2==0)
						servSystem[i] = new ServerPoisson(pStream,mu);
					else
						servSystem[i] = new ServerPoissonWithClose(pStream, mu,15);
					break;
			}
		}
	}

}
