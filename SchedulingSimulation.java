//M. M. Kuttel 2024 mkuttel@gmail.com

package barScheduling;
// the main class, starts all threads


import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
//trying to store counter 
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



public class SchedulingSimulation {
	static int noPatrons=100; //number of customers - default value if not provided on command line
	static int sched=0; //which scheduling algorithm, 0= FCFS
			
	static CountDownLatch startSignal;

	
	static Patron[] patrons; // array for customer threads
	static Barman Andre;
	static FileWriter writer;

	private static long startTime; //for all the metrics
	private static long endTime;
	//throughput
	static int counter; 
	static AtomicInteger atomicCounter = new AtomicInteger(0);
	//turnaround time 
	static AtomicInteger atomicTurnaroundTimeCounter = new AtomicInteger(0);

	//waiting time
	static AtomicInteger atomicWaitingTimeCounter = new AtomicInteger(0);

	//response time
	static AtomicInteger atomicResponseTimeCounter = new AtomicInteger(0);


	//writing to a file
public static void appendData(String fileName, String data) throws IOException {
	try (FileWriter writer = new FileWriter(fileName, true)) {
		writer.write(data + "\n"); // Append data with a newline character
	}
}


	public  void writeToFile(String data) throws IOException {
	    synchronized (writer) {
	    	writer.write(data);
	    }
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		
		

		//deal with command line arguments if provided
		if (args.length==1) {
			noPatrons=Integer.parseInt(args[0]);  //total people to enter room
			
		} else if (args.length==2) {
			noPatrons=Integer.parseInt(args[0]);  //total people to enter room
			sched=Integer.parseInt(args[1]); 
			if(sched == 0){System.out.println("Using FIRST COME FIRST SERVE");}
			if(sched == 1){System.out.println("Using SHORTEST JOB FIRST");}
			if(sched == 2){System.out.println("Using ROUND ROBIN");}
		}
		
		writer = new FileWriter("turnaround_time_"+Integer.toString(sched)+".txt", false);
		Patron.fileW=writer;

		startSignal= new CountDownLatch(noPatrons+2);//Barman and patrons and main method must be raeady
		
		//create barman
        Andre= new Barman(startSignal,sched); 
     	Andre.start();
  
	    //create all the patrons, who all need access to Andre
		patrons = new Patron[noPatrons];
		for (int i=0;i<noPatrons;i++) {
			patrons[i] = new Patron(i,startSignal,Andre);
			patrons[i].start();
			
		}
		
		System.out.println("------Andre the Barman Scheduling Simulation------");
		System.out.println("-------------- with "+ Integer.toString(noPatrons) + " patrons---------------");
		//start time and end time measure total time all patrons are served 
		startTime = System.currentTimeMillis();//started placing orders
      	startSignal.countDown(); //main method ready
				//gets total orders completed at intervals for throughput
      	Thread helloWorldThread = new Thread(() -> {
					int previousCount = 0;
					String fileName = "thread_output.txt";
					try (FileWriter writer = new FileWriter(fileName)) {
						while (true) {
							long endTime = System.currentTimeMillis();
							// int countInterval = SchedulingSimulation.counter - previousCount;
							String output = String.format("%d %d\n", (endTime - startTime), SchedulingSimulation.counter);
							//testing atomic counter
							String output1 = String.format("%d %d\n", (endTime - startTime), SchedulingSimulation.atomicCounter.get());
							System.out.println(output1);
							writer.write(output1); // Write the output before sleep
							previousCount = SchedulingSimulation.counter;
							try {
								Thread.sleep(10000); // Sleep for 10 milliseconds at a time
							} catch (InterruptedException e) {
								break;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					/*while (true) {
						//get time at this point
						endTime = System.currentTimeMillis();
						//int countInterval = SchedulingSimulation.counter - previousCount;
						System.out.println(endTime-startTime + " " + SchedulingSimulation.counter);
						String output = String.format("%d %d\n", (endTime - startTime), SchedulingSimulation.counter);
      			System.out.println("Writing "+output);
						
						previousCount = SchedulingSimulation.counter; 
						//System.out.println();
							try {
								try (FileWriter writer = new FileWriter(fileName)) {
									writer.write(output);
								}catch (IOException e) {
										e.printStackTrace();
									}
									Thread.sleep(100); // Sleep for 10 milliseconds at a time 
									//reset counter 
									//SchedulingSimulation.counter=0;

							} catch (InterruptedException e) {
									break;
							}
					}
					*/
				});
			helloWorldThread.start();
      	//wait till all patrons done, otherwise race condition on the file closing!
      	//
				
				
			//counter for patrons
			//int counter = 0 ;
			//array list to store 
			//ArrayList<Integer> counterStore = new ArrayList<>();
			//long lastSavedTime = startTime; // Initialize the last saved time with the start time

         for (int i=0;i<noPatrons;i++) {
      
			patrons[i].join();

		}
		
		endTime = System.currentTimeMillis();
		
		long totalTime = endTime - startTime;
		helloWorldThread.interrupt();
		helloWorldThread.join();
		//write to a file 
		/*FileWriter writer = new FileWriter("thread_output.txt");
		try{
				String output = String.format("%d %d\n", (endTime - startTime), SchedulingSimulation.counter);
				writer.write(output); // Write the output before sleep
		} 
		catch (IOException e) {
			e.printStackTrace();
		}*/

		System.out.println("Served " + Integer.toString(noPatrons) + " patrons in "+totalTime );
		String lastLineOfThroughPut = totalTime +" "+Integer.toString(noPatrons);
		double Avgthroughput = (double) noPatrons / totalTime;//millisecond
		Avgthroughput*=1000; //millisecond to second 
		System.out.println("Throughput: " + Avgthroughput + " patrons per second");
		String lastLineOfThroughPutAvgthroughput = "Throughput: " + Avgthroughput + " patrons per second";
		appendData("thread_output.txt", lastLineOfThroughPut);
		appendData("thread_output.txt", lastLineOfThroughPutAvgthroughput);
		
		//total turnaround time 
		String totalTurnaroundTime = "Total turnaround time= "+atomicTurnaroundTimeCounter;
		appendData("thread_output.txt",totalTurnaroundTime );
		
		//total response time 
		String totalResponseTime = "Total response time= "+atomicResponseTimeCounter;
		appendData("thread_output.txt", totalResponseTime);
		
		//total wait time 
		String totalWaitTime = "Total wait time= "+atomicWaitingTimeCounter;
		appendData("thread_output.txt", totalWaitTime);
		


		//System.out.println("Every "+ 10 + " seconds served " + counterStore.toString() + " patrons");
    	System.out.println("------Waiting for Andre------");
    	Andre.interrupt();   //tell Andre to close up
    	Andre.join(); //wait till he has
      	writer.close(); //all done, can close file
      	System.out.println("------Bar closed------");
		
	}

}
