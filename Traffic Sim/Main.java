package com.company;
import java.io.IOException;
import java.io.*;

public class Main {
    // how long sim should run for in seconds(1 simulated second = 10 real time seconds)
    static int simTimeLimit = 360;
    // create global clock object
    static Clock clock  = new Clock(simTimeLimit);

    public static void main(String[] args)
    {
        // file to read data in from
        String fileName= "details.txt";
        System.out.println("Config Filename: " + fileName);

        String[] array = new String[0]; // array to store read in data

        int noOfEntryPoints = 3; // total number of entry points in sim
        int noOfJunctions = 4; // total number of junctions in sim
        float northSpawnRate = 0; // how fast north entry point spawns
        float eastSpawnRate = 0; // how fast east entry point spawns
        float southSpawnRate = 0; // how fast south entry point spawns
        float aChangeRate = 0; // A junction light change rate
        float bChangeRate = 0; // B junction light change rate
        float cChangeRate = 0; // C junction light change rate
        float dChangeRate = 0; // D junction light change rate

        // read in data
        try
        {
            array = ExtractDetailsFromFile(fileName, noOfEntryPoints, noOfJunctions);
        }
        catch (IOException e)
        {

        }

        boolean entryPoints = false;
        boolean junctions = false;
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].contains("ENTRYPOINTS"))
            {
                entryPoints = true;
                junctions = false;
                i++;
            }
            else if (array[i].contains("JUNCTIONS"))
            {
                junctions = true;
                entryPoints = false;
                i++;
            }

            // using regex to extract the integer(total cars to spawn in simulation) from read in data,
            // then calculate how often entry point should spawn cars in simulated time
            if (entryPoints)
            {
                if (array[i].contains("North"))
                {
                    northSpawnRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / clock.GetSimulationTimeLimit() * 1000;
                }
                else if (array[i].contains("East"))
                {
                    eastSpawnRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / clock.GetSimulationTimeLimit() * 1000;
                }
                else if (array[i].contains("South"))
                {
                    southSpawnRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / clock.GetSimulationTimeLimit() * 1000;
                }
            }
            // using regex to extract the integer(light change frequency) from read in data,
            // then calculate how often junction should change lights in simulated time
            else if (junctions)
            {
                if (array[i].contains("A"))
                {
                    aChangeRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / 10;
                }
                else if (array[i].contains("B"))
                {
                    bChangeRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / 10;
                }
                else if (array[i].contains("C"))
                {
                    cChangeRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / 10;
                }
                else if (array[i].contains("D"))
                {
                    dChangeRate = Float.parseFloat(array[i].replaceAll("[\\D]", "")) / 10;
                }
            }
        }

        // create roads
        Road northToC = new Road(50);
        Road eastToB = new Road(30);
        Road southToA = new Road(60);
        Road dToUniversity = new Road(15);
        Road dToStation = new Road(15);
        Road cToShoppingCentre = new Road(7);
        Road aToIndustrialPark = new Road(15);
        Road aToB = new Road(7);
        Road bToA = new Road(7);
        Road bToC = new Road(10);
        Road cToB = new Road(10);
        Road cToD = new Road(10);

        // create junctions
        Junction A = new Junction('A', 2, aChangeRate,northToC, eastToB, southToA, dToUniversity,
                                    dToStation, cToShoppingCentre, aToIndustrialPark, aToB, bToA, bToC, cToB, cToD);
        Junction B = new Junction('B', 3, bChangeRate,northToC, eastToB, southToA, dToUniversity,
                                    dToStation, cToShoppingCentre, aToIndustrialPark, aToB, bToA, bToC, cToB, cToD);
        Junction C = new Junction('C', 2, cChangeRate,northToC, eastToB, southToA, dToUniversity,
                                    dToStation, cToShoppingCentre, aToIndustrialPark, aToB, bToA, bToC, cToB, cToD);
        Junction D = new Junction('D', 1, dChangeRate,northToC, eastToB, southToA, dToUniversity,
                                    dToStation, cToShoppingCentre, aToIndustrialPark, aToB, bToA, bToC, cToB, cToD);

        // create car parks
        CarPark university = new CarPark(100, dToUniversity, "University");
        CarPark station = new CarPark(150, dToStation, "Station");
        CarPark shoppingCentre = new CarPark(400, cToShoppingCentre, "Shopping Centre");
        CarPark industrialPark = new CarPark(1000, aToIndustrialPark, "Industrial Park");

        // create entry points
        EntryPoint north = new EntryPoint(northToC, "north", northSpawnRate);
        EntryPoint east = new EntryPoint(eastToB, "east", eastSpawnRate);
        EntryPoint south = new EntryPoint(southToA, "south", southSpawnRate);

        // start threaded objects
        clock.start();

        university.start();
        station.start();
        shoppingCentre.start();
        industrialPark.start();

        A.start();
        B.start();
        C.start();
        D.start();

        north.start();
        east.start();
        south.start();

        // join threaded objects when sim is finished running
        try
        {
            clock.join();

            north.join();
            east.join();
            south.join();

            university.join();
            station.join();
            shoppingCentre.join();
            industrialPark.join();

            A.join();
            B.join();
            C.join();
            D.join();
        }
        catch (InterruptedException e)
        {

        }

        // output car park final details
        System.out.println(university.FinalOutput());
        System.out.println(station.FinalOutput());
        System.out.println(industrialPark.FinalOutput());
        System.out.println(shoppingCentre.FinalOutput());

        // output total cars spawned vs total cars in existence to prove sim is thread safe
        int totalCarsInNetwork = northToC.GetCurrentSize() + eastToB.GetCurrentSize() + southToA.GetCurrentSize() +
                                dToUniversity.GetCurrentSize() + dToStation.GetCurrentSize() + cToShoppingCentre.GetCurrentSize() +
                                aToIndustrialPark.GetCurrentSize() + aToB.GetCurrentSize() + bToA.GetCurrentSize() +
                                bToC.GetCurrentSize() + cToB.GetCurrentSize() + cToD.GetCurrentSize();
        int totalCarsParked = university.GetCarsParked() + station.GetCarsParked() + industrialPark.GetCarsParked() + shoppingCentre.GetCarsParked();
        int totalCarsSpawned = south.GetCarsSpawned() + east.GetCarsSpawned() + north.GetCarsSpawned();

        System.out.println("Total Cars Created: " + totalCarsSpawned + ", Total Cars Queued: " + totalCarsInNetwork + ", Total Cars Parked: " + totalCarsParked);
    }

    // reads data in from file and returns string array
    static public String[] ExtractDetailsFromFile(String fileName, int noOfEntryPoints, int noOfJunctions)
            throws IOException
    {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String currentLine;

        int miscLines = 3; // number of lines without data(i.e titles and white space)
        int noOfLines = noOfEntryPoints + noOfJunctions + miscLines; // total number of lines in file

        int i = 0;
        String[] array = new String[noOfLines];
        while (i < noOfLines)
        {
            currentLine = reader.readLine();
            array[i] = currentLine;
            i++;
        }
        reader.close();
        return array;
    }
}

