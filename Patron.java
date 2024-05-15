//M. M. Kuttel 2024 mkuttel@gmail.com
package barScheduling;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basicclass, representing the patrons at the bar
 */

public class Patron extends Thread {
	
	private Random random = new Random();// for variation in Patron behaviour

	private CountDownLatch startSignal; //all start at once, actually shared
	private Barman theBarman; //the Barman is actually shared though

	private int ID; //thread ID 
	private int lengthOfOrder;
	private long startTime, endTime; //for all the metrics
	private long firstDrinkStartTime, firstDrinkEndTime;//response time timer 
	private DrinkOrder firstDrink;
	private boolean isServed; //attempt for through put 
	private AtomicBoolean atomicIsServed = new AtomicBoolean(true);//attempt for throughput 
	public static FileWriter fileW;
	//throughput attempt 
	static int counter;


	private DrinkOrder [] drinksOrder;
	//adding in parameter to save time served
	Patron( int ID,  CountDownLatch startSignal, Barman aBarman) {
		this.ID=ID;
		this.startSignal=startSignal;
		this.theBarman=aBarman;
		this.lengthOfOrder=random.nextInt(5)+1;//between 1 and 5 drinks
		drinksOrder=new DrinkOrder[lengthOfOrder];
		//through put attempt 
		//this.counter = counter;
	}
	
	public  void writeToFile(String data) throws IOException {
	    synchronized (fileW) {
	    	fileW.write(data);
	    }
	}
	//signal to barman that it has been served 
	public boolean getPatronServed()
	{
			return isServed; 
	}


	public void run() {
		try {
			//Do NOT change the block of code below - this is the arrival times
			startSignal.countDown(); //this patron is ready
			startSignal.await(); //wait till everyone is ready
	        int arrivalTime = random.nextInt(300)+ID*100;  // patrons arrive gradually later
	        sleep(arrivalTime);// Patrons arrive at staggered  times depending on ID 
			System.out.println("thirsty Patron "+ this.ID +" arrived");
			//END do not change
			int totalPreparationTime = 0; 

	        //create drinks order
	        for(int i=0;i<lengthOfOrder;i++) {
	        	drinksOrder[i]=new DrinkOrder(this.ID);
	        	
	        }
					 
					

			System.out.println("Patron "+ this.ID + " submitting order of " + lengthOfOrder +" drinks"); //output in standard format  - do not change this
	        startTime = System.currentTimeMillis();//started placing orders
			for(int i=0;i<lengthOfOrder;i++) {
				System.out.println("Order placed by " + drinksOrder[i].toString());
				theBarman.placeDrinkOrder(drinksOrder[i]); //place first order 
				//get prepartion time
				totalPreparationTime += drinksOrder[i].getExecutionTime();
				//start timer for first drink delivered which is response time 
				if(i == 0)
				{
					//save first drink for response time 
					firstDrink = drinksOrder[i];
					//start timer for response time 
					firstDrinkStartTime = System.currentTimeMillis();//started placing orders
				}
				 
			}
			for(int i=0;i<lengthOfOrder;i++) {
				drinksOrder[i].waitForOrder();
				if(i == 0)
				{
					//end timer for response time
					firstDrinkEndTime = System.currentTimeMillis();//started placing orders
				}
			}

			//signal served 
			SchedulingSimulation.counter++;
			SchedulingSimulation.atomicCounter.getAndIncrement();

			endTime = System.currentTimeMillis();
         
			//turnaround time is the time it takes from when a patron places their order to when it is completed
			long totalTime = endTime - startTime;
			//add to total turnaround time 
      SchedulingSimulation.atomicTurnaroundTimeCounter.getAndAdd((int)totalTime);
      //Response time is the time when the first drink calculation 
			long totalFirstDrinkTime = firstDrinkEndTime - firstDrinkStartTime;
			//add to total response time 
      SchedulingSimulation.atomicResponseTimeCounter.getAndAdd((int)totalFirstDrinkTime);   
      //wait time is the total amount of preparation time from order out of the total time  
      long waitTime = totalTime - totalPreparationTime;
			//add to total waiting time 
			SchedulingSimulation.atomicWaitingTimeCounter.getAndAdd((int)waitTime);
      
			
         
			//save metrics to file 
			writeToFile( String.format("ID= %d, Arrival= %d, TurnaroundTime= %d, ResponseTime= %d, TotalPreparationTime= %d, WaitTime = %d\n",ID,arrivalTime,totalTime,totalFirstDrinkTime,totalPreparationTime,waitTime));
			//time to get order 
			//System.out.println("Patron "+ this.ID + " arrived " + arrivalTime);
			//System.out.println("Patron "+ this.ID + " got order in " + totalTime);
			//response: time to get first drink 
			//System.out.println("Patron "+ this.ID + " got first drink in " + totalFirstDrinkTime + " and it was "+ drinksOrder[0]);
      //time for order to be completed
			//System.out.println("Patron "+ this.ID + " total prep time was " + totalPreparationTime); 
      //System.out.println("Patron "+ this.ID + " was waiting while their order is not being worked on for " + waitTime);

			
		} catch (InterruptedException e1) {  //do nothing
		} catch (IOException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
}
}
	

