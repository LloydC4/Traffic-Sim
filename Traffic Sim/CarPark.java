package com.company;
import java.util.concurrent.*;

public class CarPark extends Thread
{
    static private int noOfCarParks = 0; // keeps track of total number of car parks
    static private CyclicBarrier outputBarrier; // barrier to ensure simultaneous data output
    public static int[] detailPrintTimes; // stores times at which data should be output
    private int printOutFrequency; // how frequent the data outputs should be
    private int i = 0; // helps calculate times at which data should be output
    private int totalCapacity; // how many cars a car park can store
    private Road connectingRoad; // road linked to the car park
    private String carParkID; // car park name
    private int carsParked = 0; // number of cars currently parked
    private Vehicle[] carPark; // array that stores cars
    private final int carParkSleepTimer = 1200; // how long it takes a car park thread to park a car
    private final int minutesDivider = 6; // used to turn real time seconds in to simulated minutes
    private final int realTimeMultiplier = 10; // used to convert simulated time to real time

    public CarPark(int capacity, Road road, String name)
    {
        noOfCarParks++;
        totalCapacity = capacity;
        carPark = new Vehicle[capacity];
        connectingRoad = road;
        carParkID = name;
        printOutFrequency = 60;
        outputBarrier = new CyclicBarrier(noOfCarParks);
        CalculatePrintoutTimes();
    }

    public void run()
    {
        while (Main.clock.IsSimRunning())
        {
            // prints out details, then waits until next printout time
            if (Main.clock.GetTime() >= detailPrintTimes[i] && i < detailPrintTimes.length - 1)
            {
                PrintCarParkDetails();
                i++;
            }
            // parks car if car is available
            ParkCar();
        }
    }

    public int GetCarsParked()
    {
        return carsParked;
    }

    public boolean IsFull()
    {
        return carsParked == totalCapacity - 1;
    }

    // if car park is not full, locks connected road to prevent data race, and removes a car to park if a car is available
    public void ParkCar()
    {
        if (!IsFull())
        {
            synchronized (connectingRoad)
            {
                if (!connectingRoad.IsEmpty())
                {
                    Vehicle vehicle = connectingRoad.RemoveVehicle();
                    vehicle.SetArrivalTime(Main.clock.GetTime());
                    vehicle.SetSpawnLocation(carParkID);
                    carPark[carsParked] = vehicle;
                    carsParked++;
                    try
                    {
                        Thread.sleep(carParkSleepTimer);
                    }
                    catch (InterruptedException e)
                    {

                    }
                }
            }
        }
    }

    public String GetCarParkName()
    {
        return carParkID;
    }

    // calculates average journey time
    public float CalculateAverageJourneyTime()
    {
        float combinedJourneyTime = 0;
        for (int i = 0; i < carsParked; i++)
        {
            combinedJourneyTime += (carPark[i].GetArrivalTime() - carPark[i].GetCreationTime());
        }
        return (combinedJourneyTime/carsParked) * realTimeMultiplier;
    }

    // prints out no of cars parked and average journey time when sim is finished
    public String FinalOutput()
    {
        float journeyTime = CalculateAverageJourneyTime();
        int averageJourneyTimeMins = ((int) journeyTime)/60;
        int averageJourneyTimeSecs = ((int) journeyTime)%60;
        return carParkID + ": " + carsParked + " Cars parked, average journey time: " + averageJourneyTimeMins + "m" +
                averageJourneyTimeSecs + "s";
    }

    // calculates printout times by finding how many there are,
    // creating array of that size and adding all the printout times to array
    public void CalculatePrintoutTimes()
    {
        int noOfDivisors = 0;
        for (int j = 0; j <= Main.clock.GetSimulationTimeLimit(); j++)
        {
            if (j % printOutFrequency == 0)
            {
                noOfDivisors++;
            }
        }

        int arrayIndex = 0;
        detailPrintTimes = new int[noOfDivisors];
        for (int k = 0; k <= Main.clock.GetSimulationTimeLimit(); k++)
        {
            if (k % printOutFrequency == 0)
            {
                detailPrintTimes[arrayIndex] = k;
                arrayIndex++;
            }
        }
    }

    // using cyclic barrier to ensure simultaneous printout
    public void PrintCarParkDetails()
    {
        Thread.currentThread().isInterrupted();
        try
        {
            outputBarrier.await();
        }
        catch (InterruptedException | BrokenBarrierException e)
        {
            System.out.println("lel");
        }
        int spacesAvailable = totalCapacity - carsParked;
        System.out.println("time: " + Main.clock.GetTime() / minutesDivider + "m " + carParkID + ": " + spacesAvailable + " spaces");
    }
}
