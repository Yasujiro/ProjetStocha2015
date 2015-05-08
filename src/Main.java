import umontreal.iro.lecuyer.rng.MRG31k3p;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stat.Tally;


public class Main {
	
	private static MRG31k3p randomSeedGen = new MRG31k3p();
	private static double mu = 2;
	private static double lambda = 3;
	private static int kErlang = 3;
	private static double muK = 2*kErlang;
	private static int nombreSimulation = 1;
	private static int tempsSimu = 2000000;
	public static void main(String[] args) {		
		
		
		//simulateSystem(1,1);
		simulateSystem(1,2);
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
	private static void simulateSystem(int numSystem,int numServers) 
	{
		ServerStocha[] servSystem = null;
		Tally waitTime = new Tally("Temps d'attente");
		Tally bobWaitTime = new Tally();
		QueueSystem system = null;
		
		for(int i =0;i<nombreSimulation;i++)
		{
				servSystem= new ServerStocha[numServers];
				switch(numSystem)
				{
				case 2:
					initializeServer(servSystem,TypeServeur.Erlang);
					system = new QueueSystem(lambda, tempsSimu,servSystem);
					break;
				case 3:
					initializeServer(servSystem,TypeServeur.Poisson);
					system = new RandomSelectionSystem(lambda, tempsSimu,servSystem);
					break;
				case 4:
					initializeServer(servSystem, TypeServeur.WithClose);
					system= new SecondSystem(lambda, tempsSimu, servSystem);
					break;
				default:
					initializeServer(servSystem,TypeServeur.Poisson);
					system = new QueueSystem(lambda, tempsSimu,servSystem);
					break;						
				}

			
			system.LaunchSimu();
			for(double obs:system.meanWaiTime().getArray())
			{
				waitTime.add(obs);
			}
			if(system instanceof SecondSystem)
				bobWaitTime.add(((SecondSystem)system).changingCustomerWaitTime());
		}
		
		System.out.println("System "+numSystem+" avec "+numServers+" server(s) : \n");
		System.out.println("Nombre de simulation : "+nombreSimulation+"\n");
		System.out.println("Temps moyen d'attente: "+waitTime.report(0.95,5)+"\n");
		System.out.println(waitTime.formatCIStudent(0.95,5));
		if(bobWaitTime.numberObs() >0)
			System.out.println("Temps d'attente moyen de Bob : "+bobWaitTime.average());
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
	
	private static void initializeServer(ServerStocha[] servSystem,TypeServeur typeServ)
	{
		long[] seed = new long[6];	
		for(int i =0;i<servSystem.length;i++)
		{
			for(int j =0 ; j<seed.length;j++)
			{
				seed[j] = randomSeedGen.nextInt(1, 429494444);
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
						servSystem[i] = new ServerPoissonWithClose(pStream, mu, 3);
					break;
			}
		}
	}

}
