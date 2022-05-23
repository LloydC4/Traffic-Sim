package com.company;

public class Vehicle
{
    private String finalDestination; // holds final destination of car
    private String spawnLocation; // where car was spawned
    private int creationTime; // time created
    private int arrivalTime = 0; // time arrived

    public Vehicle(String whereGoing, String whereSpawned, int currentTime)
    {
        finalDestination = whereGoing;
        spawnLocation = whereSpawned;
        creationTime = currentTime;
    }

    public int GetArrivalTime() {
        return arrivalTime;
    }

    public void SetArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void SetSpawnLocation(String destination)
    {
        spawnLocation = destination;
    }

    public String GetFinalDestination()
    {
        return finalDestination;
    }

    public int GetCreationTime()
    {
        return creationTime;
    }
}
