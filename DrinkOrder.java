package barScheduling;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class DrinkOrder  {

    //DO NOT change the code below
    public enum Drink { 
        Beer("Beer", 10),
        Cider("Cider", 10),
        GinAndTonic("Gin and Tonic", 30),
        Martini("Martini", 50),
        Cosmopolitan("Cosmopolitan", 80),
        BloodyMary("Bloody Mary", 90),
        Margarita("Margarita", 100),
        Mojito("Mojito", 120),
        PinaColada("Pina Colada", 200),
        LongIslandIcedTea("Long Island Iced Tea", 300),
    	B52("B52", 500);
    	
    	
        private final String name;
        private final int preparationTime; // in minutes
        

        Drink(String name, int preparationTime) {
            this.name = name;
            this.preparationTime = preparationTime;
        }

        public String getName() {
            return name;
        }

        public int getPreparationTime() {
            return preparationTime;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    private final Drink drink;
    private static final Random random = new Random();
    private int orderer;
    private AtomicBoolean orderComplete;

    //round robin 
    private DrinkDetails drinkDetails;

 //constructor
    public DrinkOrder(int patron) {
    	drink=getRandomDrink();
    	orderComplete = new AtomicBoolean(false); //can also use this to get throughput by counting the number of orders completed in the scheduling simulation 
    	orderer=patron;
        //round robin 
        // Initialize drinkDetails here
        drinkDetails = new DrinkDetails(); // Assuming DrinkDetails has a default constructor
    }
    
    public static Drink getRandomDrink() {
        Drink[] drinks = Drink.values();  // Get all enum constants
        int randomIndex = random.nextInt(drinks.length);  // Generate a random index
        return drinks[randomIndex];  // Return the randomly selected drink
    }
    

    public int getExecutionTime() {
        return drink.getPreparationTime();
    }
    
    
    //barman signals when order is done
    public synchronized void orderDone() {
    	orderComplete.set(true);
        this.notifyAll();
    }
    
    //patrons wait for their orders
    public synchronized void waitForOrder() throws InterruptedException {
    	while(!orderComplete.get()) {
    		this.wait();
    	}
    }
    
    @Override
    public String toString() {
        return Integer.toString(orderer) +": "+ drink.getName();
    }

    public DrinkDetails getDrinkDetails() {
        return drinkDetails;
    }

    // DrinkDetails class to change preparation time 
    public static class DrinkDetails {
        private int preparationTime;

        public DrinkDetails() {
            this.preparationTime = 10; // Default preparation time
        }

        public int getPreparationTime() {
            return preparationTime;
        }

        public void setPreparationTime(int preparationTime) {
            this.preparationTime = preparationTime;
        }
    }
}