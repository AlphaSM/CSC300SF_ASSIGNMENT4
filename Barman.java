package barScheduling;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
//trying to get throughput 
import java.util.concurrent.atomic.AtomicBoolean;

/*
 Barman Thread class.
 */

public class Barman extends Thread {
	// round robin
	private int timeQuantum; // Time slice for preemption
	private int mode;

	private CountDownLatch startSignal;
	private BlockingQueue<DrinkOrder> orderQueue;

	// trying to get patrons served per unit time
	private int ordersCompleted;

	public void incrementOrdersCompleted() {
		ordersCompleted++;
	}

	public int getOrdersCompleted() {
		return ordersCompleted;
	}

	Barman(CountDownLatch startSignal, int schedAlg) {
		if (schedAlg == 0)
			this.orderQueue = new LinkedBlockingQueue<>();
		mode = schedAlg;
		// FIX below
		// implement a priority queue
		if (schedAlg == 1) {
			// compares by drink
			// this.orderQueue = new PriorityBlockingQueue<>(10,
			// Comparator.comparingInt(DrinkOrder::getExecutionTime));
			//can fill up the queue with 500 drinks and sorts them based on the drinks with lowest preparation times
			this.orderQueue = new PriorityBlockingQueue<>(500, Comparator.comparingInt(DrinkOrder::getExecutionTime));
			mode = schedAlg;
		}

		else if (schedAlg == 2) {
			this.orderQueue = new LinkedBlockingQueue<>();
			timeQuantum = 40;//40
			mode = schedAlg;
		}

		this.startSignal = startSignal;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
		orderQueue.put(order);
	}


private void RoundRobin() {
	try {
			DrinkOrder nextOrder;
			LinkedBlockingQueue<DrinkOrder> roundRobinQueue = new LinkedBlockingQueue<>(); // Use LinkedBlockingQueue

			startSignal.countDown(); // Barman ready
			startSignal.await(); // Check latch - don't start until told to do so
			ordersCompleted = 0; // No orders completed

			while (true) {
					// Process orders from the blocking queue first
					while (!orderQueue.isEmpty()) {
							nextOrder = orderQueue.poll();
							roundRobinQueue.offer(nextOrder);
					}

					// If the round-robin queue is not empty, process orders using round-robin
					if (!roundRobinQueue.isEmpty()) {
							nextOrder = roundRobinQueue.poll();

							System.out.println("---Barman preparing order for patron " + nextOrder.toString());

							// Track remaining preparation time for the order
							long remainingTime = nextOrder.getDrinkDetails().getPreparationTime();

							while (remainingTime > 0) {
								long processingTime = Math.min(remainingTime, timeQuantum);
								sleep(processingTime); // Process the order for the time quantum
								remainingTime -= processingTime;
						
								
								if (remainingTime > 0) {
										System.out.println("---Stopping order for patron " + nextOrder.toString() + ", putting it back in the queue");
										// Order not complete, put it back in the queue for later processing
										roundRobinQueue.offer(nextOrder);
										//increase wait time 
										//nextOrder.getDrinkDetails().setPreparationTime((int)remainingTime);
										break; // Move to the next order in the queue
								}
							}

							if (remainingTime == 0) {
									System.out.println("---Barman has made order for patron " + nextOrder.toString());
									nextOrder.orderDone();
									incrementOrdersCompleted(); // Increment orders completed
							}
					}
			}
	} catch (InterruptedException e) {
			System.out.println("---Barman is packing up ");
	}
}

public void run() {
		if (mode == 2) {
			RoundRobin();
		} else {
			try {
				DrinkOrder nextOrder;

				startSignal.countDown(); // barman ready
				startSignal.await(); // check latch - don't start until told to do so
				ordersCompleted = 0; // no orders completed

				while (true) {
					nextOrder = orderQueue.take();
					System.out.println("---Barman preparing order for patron " + nextOrder.toString());
					sleep(nextOrder.getExecutionTime()); // processing order
					System.out.println("---Barman has made order for patron " + nextOrder.toString());
					nextOrder.orderDone();
				}

			} catch (InterruptedException e1) {
				System.out.println("---Barman is packing up ");
			}
		}
	}

}