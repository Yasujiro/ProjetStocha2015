
public class Main {

	public static void main(String[] args) {
		
		int nombreSimulation = 20;
		for(int i =0;i<nombreSimulation;i++)
		{
			QueueSystem system1 = new QueueSystem(1, 10000, 1,ProcessusService.Poisson);
			QueueSystem system2 = new QueueSystem(1, 10000, 1,ProcessusService.Erlang);
			System.out.println("Simulation "+i+" :\n\n");
			system1.LaunchSimu();
			system2.LaunchSimu();
		}
		

	}

}
