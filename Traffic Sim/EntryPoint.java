package com.company;
import java.util.Random;

public class EntryPoint extends Thread
{
    private Road connectingRoad; // road connected to entry point
    private Random carLocationDecider = new Random(5); // used to randomly decide where spawned car is going
    private int upperBound; // upper limit for random number generator
    private String entryPointName; // name of the entry point
    private int carsSpawned = 0; // number of cars spawned
    private float carSpawnTimer; // determines how often entrypoints should spawn cars

    public EntryPoint(Road road, String Name, float spawnTimer)
    {
        connectingRoad = road;
        upperBound = 10;
        entryPointName = Name;
        carSpawnTimer = spawnTimer;
    }

    public void run()
    {
        while (Main.clock.IsSimRunning())
        {
            // create vehicles and add to road at pre-determined rate
            int locationDecider = carLocationDecider.nextInt(upperBound);
            if (locationDecider <= 3)
            {
                // car goes to industrial park
                SpawnNewCar("Industrial Park");
            }
            else if (locationDecider >= 4 && locationDecider <= 6)
            {
                SpawnNewCar("Shopping Centre");
            }
            else if (locationDecider >= 7 && locationDecider <= 8)
            {
                SpawnNewCar("Station");
            }
            else if (locationDecider == 9)
            {
                SpawnNewCar("University");
            }

            try
            {
                Thread.sleep(((int) carSpawnTimer));
            }
            catch (InterruptedException e)
            {

            }
        }
    }

    public void SpawnNewCar(String destination)
    {
        synchronized(connectingRoad)
        {
            if (!connectingRoad.IsFull())
            {
                Vehicle vehicle = new Vehicle(destination, entryPointName, Main.clock.GetTime());
                connectingRoad.AddVehicle(vehicle);
                carsSpawned++;
            }
        }
    }

    public int GetCarsSpawned()
    {
        return carsSpawned;
    }
}
