
public class Customer {

	private double serviceTime;
	private double arrivalTime;
	public double waitingTime;
	
	public Customer(double arrival)
	{
		arrivalTime = arrival;
	}
	
	public double getServiceTime()
	{
		return serviceTime;
	}
	public double getArrivalTime()
	{
		return arrivalTime;
	}
	public void setServiceTime(double t)
	{
		serviceTime = t;
	}
	public void setArrivaltime(double t)
	{
		arrivalTime = t;
	}
}
