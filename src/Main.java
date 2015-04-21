
public class Main {

	public static void main(String[] args) {
		
		QueueSystem system1 = new QueueSystem(1, 100000, 1,ProcessusService.Erlang);
		QueueSystem system3 = new QueueSystem(.3, 100000, 1,ProcessusService.Poisson);
		QueueSystem system2 = new QueueSystem(.5, 100000, 1,ProcessusService.Poisson);
		System.out.println("System 1 :");
		system1.LaunchSimu();
		System.out.println("\n\n\nSystem 2 :");
		system2.LaunchSimu();
		System.out.println("\n\n\nSystem 3 :");
		system3.LaunchSimu();
		
		

	}

}
