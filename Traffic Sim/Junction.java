package com.company;
import java.io.*;

public class Junction extends Thread {
    private Road outgoingNorth; // road connected to junction
    private Road outgoingSouth; // road connected to junction
    private Road outgoingEast; // road connected to junction
    private Road outgoingWest; // road connected to junction
    private Road incomingNorth; // road connected to junction
    private Road incomingSouth; // road connected to junction
    private Road incomingEast; // road connected to junction
    private Road incomingWest; // road connected to junction
    private int noOfEntries; // total number of incoming roads a junction has
    private int currentGreenLight; // determines which entry road is letting traffic through
    private final int lightSwitchTimeLimit; // how often the lights switch
    private final char junctionID; // junction name
    private int i = 0; // used to calculate times at which lights should change
    private int[] lightChangeTimes; // stores times at which lights should change
    private String incomingDirection = ""; // stores incoming direction for log output
    private int noOfCarsLetThrough = 0; // stores number of cars traffic light has let through on this cycle for log output
    private int noOfCarsWaiting = 0; // stores number of cars still waiting at the lights after this cycle for log output
    private BufferedWriter file; // writes above variables to log
    private final int junctionSleepTimer = 500; // how long it takes a car to pass over the junction

    public Junction(char name, int entryAmount, float lightTimer, Road northToC, Road eastToB, Road southToA, Road dToUniversity, Road dToStation,
                    Road cToShoppingCentre, Road aToIndustrialPark, Road aToB, Road bToA, Road bToC, Road cToB,
                    Road cToD)
    {
        noOfEntries = entryAmount - 1;
        junctionID = name;
        currentGreenLight = -1;
        lightSwitchTimeLimit = ((int) lightTimer);
        lightChangeTimes = CalculateLightChangeTimes(Main.clock.GetSimulationTimeLimit());
        try
        {
            file = new BufferedWriter(new FileWriter("log.txt", true));
        }
        catch (IOException e) {

        }
        // setting up road network, rather than have 4 constructor overloads I figured it would
        // be more modular to do it this way
        if (name == 'A')
        {
            incomingSouth = southToA;
            outgoingNorth = aToB;

            incomingNorth = bToA;
            outgoingSouth = null;

            incomingWest = null;
            outgoingEast = null;

            incomingEast = null;
            outgoingWest = aToIndustrialPark;
        }
        else if (name == 'B')
        {
            incomingSouth = aToB;
            outgoingNorth = null;

            incomingNorth = null;
            outgoingSouth = bToA;

            incomingWest = cToB;
            outgoingEast = null;

            incomingEast = eastToB;
            outgoingWest = bToC;
        }
        else if (name == 'C')
        {
            incomingSouth = null;
            outgoingNorth = null;

            incomingNorth = northToC;
            outgoingSouth = cToShoppingCentre;

            incomingWest = null;
            outgoingEast = cToB;

            incomingEast = bToC;
            outgoingWest = cToD;
        }
        else if (name == 'D')
        {
            incomingSouth = null;
            outgoingNorth = dToUniversity;

            incomingNorth = null;
            outgoingSouth = dToStation;

            incomingWest = null;
            outgoingEast = null;

            incomingEast = cToD;
            outgoingWest = null;
        }
    }

    public void run()
    {
        while (Main.clock.IsSimRunning())
        {
            // checks to see if lights should change, changes them if it's time.
            LightChange();
            // junction logic
            if (junctionID == 'A')
            {
                if (currentGreenLight == 0)
                {
                    incomingDirection = "South";
                    DoubleExit(incomingSouth, outgoingWest, outgoingNorth, "Industrial Park");
                    noOfCarsWaiting = incomingSouth.GetCurrentSize();
                }
                else
                {
                    incomingDirection = "North";
                    SingleExit(incomingNorth, outgoingWest);
                    noOfCarsWaiting = incomingNorth.GetCurrentSize();
                }
            }
            else if (junctionID == 'B')
            {
                if (currentGreenLight == 0)
                {
                    incomingDirection = "South";
                    SingleExit(incomingSouth, outgoingWest);
                    noOfCarsWaiting = incomingWest.GetCurrentSize();
                }
                else if (currentGreenLight == 1)
                {
                    incomingDirection = "West";
                    SingleExit(incomingWest, outgoingSouth);
                    noOfCarsWaiting = incomingWest.GetCurrentSize();
                }
                else if (currentGreenLight == 2)
                {
                    incomingDirection = "East";
                    DoubleExit(incomingEast, outgoingSouth, outgoingWest, "Industrial Park");
                    noOfCarsWaiting = incomingEast.GetCurrentSize();
                }
            }
            else if (junctionID == 'C')
            {
                if (currentGreenLight == 0)
                {
                    incomingDirection = "North";
                    TripleExit(incomingNorth, outgoingWest, outgoingSouth, outgoingEast, "University", "Station", "Shopping Centre");
                    noOfCarsWaiting = incomingNorth.GetCurrentSize();
                }
                else if (currentGreenLight == 1)
                {
                    incomingDirection = "East";
                    DoubleExit(incomingEast, outgoingWest, outgoingSouth, "University", "Station");
                    noOfCarsWaiting = incomingEast.GetCurrentSize();
                }
            }
            else if (junctionID == 'D')
            {
                if (currentGreenLight == 0)
                {
                    incomingDirection = "East";
                    DoubleExit(incomingEast, outgoingNorth, outgoingSouth, "University");
                    noOfCarsWaiting = incomingEast.GetCurrentSize();
                }
            }
        }
    }

    // gets lock on incoming road, then locks outgoing road and transfers the car over the junction if outgoing road has space
    // before sleeping. For single exit junctions.
    public void SingleExit(Road incomingRoad, Road outgoingRoad)
    {
        synchronized (incomingRoad)
        {
            if (!incomingRoad.IsEmpty())
            {
                synchronized (outgoingRoad)
                {
                    if (!outgoingRoad.IsFull())
                    {
                        outgoingRoad.AddVehicle(incomingRoad.RemoveVehicle());
                        noOfCarsLetThrough++;
                        try
                        {
                            Thread.sleep(junctionSleepTimer);
                        }
                        catch (InterruptedException e)
                        {

                        }
                    }
                }
            }
        }
    }

    // gets lock on incoming road, then locks outgoing road and transfers the car over the junction if outgoing road has space
    // before sleeping. For double exit junctions.
    public void DoubleExit(Road incomingRoad, Road outgoingRoadA, Road outgoingRoadB, String destinationA)
    {
        synchronized (incomingRoad)
        {
            if (!incomingRoad.IsEmpty())
            {
                String destination = incomingRoad.PeekNextVehicle().GetFinalDestination();
                if (destination.equals(destinationA))
                {
                    synchronized (outgoingRoadA)
                    {
                        if (!outgoingRoadA.IsFull())
                        {
                            outgoingRoadA.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
                else
                {
                    synchronized (outgoingRoadB)
                    {
                        if (!outgoingRoadB.IsFull())
                        {
                            outgoingRoadB.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
            }
        }
    }

    // double exit junction logic overload for more than 2 possible final destinations
    public void DoubleExit(Road incomingRoad, Road outgoingRoadA, Road outgoingRoadB, String destinationA, String destinationA2)
    {
        synchronized (incomingRoad)
        {
            if (!incomingRoad.IsEmpty())
            {
                String destination = incomingRoad.PeekNextVehicle().GetFinalDestination();
                if (destination.equals(destinationA) || destination.equals(destinationA2))
                {
                    synchronized (outgoingRoadA)
                    {
                        if (!outgoingRoadA.IsFull())
                        {
                            outgoingRoadA.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
                else
                {
                    synchronized (outgoingRoadB)
                    {
                        if (!outgoingRoadB.IsFull())
                        {
                            outgoingRoadB.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
            }
        }
    }

    // gets lock on incoming road, then locks outgoing road and transfers the car over the junction if outgoing road has space
    // before sleeping. For 3 exit junctions.
    public void TripleExit(Road incomingRoad, Road outgoingRoadA, Road outgoingRoadB, Road outgoingRoadC, String destinationA,
                           String destinationA2, String destinationB)
    {
        synchronized (incomingRoad)
        {
            if (!incomingRoad.IsEmpty())
            {
                String destination = incomingRoad.PeekNextVehicle().GetFinalDestination();
                if (destination.equals(destinationA) || destination.equals(destinationA2))
                {
                    synchronized (outgoingRoadA)
                    {
                        if (!outgoingRoadA.IsFull())
                        {
                            outgoingRoadA.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
                else if (destination.equals(destinationB))
                {
                    synchronized (outgoingRoadB)
                    {
                        if (!outgoingRoadB.IsFull())
                        {
                            outgoingRoadB.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
                else
                {
                    synchronized (outgoingRoadC)
                    {
                        if (!outgoingRoadC.IsFull())
                        {
                            outgoingRoadC.AddVehicle(incomingRoad.RemoveVehicle());
                            noOfCarsLetThrough++;
                            try
                            {
                                Thread.sleep(junctionSleepTimer);
                            }
                            catch (InterruptedException e)
                            {

                            }
                        }
                    }
                }
            }
        }
    }

    // checks time against light change time array to see if lights should change, log is written to on every light change
    public void LightChange()
    {
        if (Main.clock.GetTime() >= lightChangeTimes[i] && i < lightChangeTimes.length - 1)
        {
            if (currentGreenLight <= noOfEntries - 1)
            {
                currentGreenLight++;
            }
            else
            {
                currentGreenLight = 0;
            }
            // write details to file
            WriteLog();
            i++;
        }
    }

    // calculates all light change times and stores in array
    public int[] CalculateLightChangeTimes(int simTimeLimit)
    {
        int noOfDivisors = 0;
        for (int j = 0; j < simTimeLimit; j++)
        {
            if (j % lightSwitchTimeLimit == 0)
            {
                noOfDivisors++;
            }
        }

        int arrayIndex = 0;
        lightChangeTimes = new int[noOfDivisors];
        for (int k = 0; k < simTimeLimit; k++)
        {
            if (k % lightSwitchTimeLimit == 0)
            {
                lightChangeTimes[arrayIndex] = k;
                arrayIndex++;
            }
        }
        return lightChangeTimes;
    }

    // writes traffic details to log
    public void WriteLog()
    {
        if (noOfCarsLetThrough == 0 && noOfCarsWaiting > 0)
        {
            try
            {
                file.write("Time: " + Main.clock.GetTime() + " - Junction " + junctionID + ": " + noOfCarsLetThrough + " cars through from " + incomingDirection + ", " + noOfCarsWaiting + " cars waiting. GRIDLOCK");
                file.newLine();
                file.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                file.write("Time: " + Main.clock.GetTime() + " - Junction " + junctionID + ": " + noOfCarsLetThrough + " cars through from " + incomingDirection + ", " + noOfCarsWaiting + " cars waiting.");
                file.newLine();
                file.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        incomingDirection = "";
        noOfCarsLetThrough = 0;
        noOfCarsWaiting = 0;
    }
}
