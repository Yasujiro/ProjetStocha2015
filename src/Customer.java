
public class Customer {

	private double servTime;
	private double arrivalTime;
	
	public Customer(double arrival)
	{
		arrivalTime = arrival;
	}
	
	public double getServTime()
	{
		return servTime;
	}
	public double getArrivalTime()
	{
		return arrivalTime;
	}
	public void setServTime(double t)
	{
		servTime = t;
	}
	public void setArrivaltime(double t)
	{
		arrivalTime = t;
	}
}
