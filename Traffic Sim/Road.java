package com.company;

public class Road
{
    private Vehicle[] body; // road array to store cars
    private int nextIn = 0; // next in for circular buffer
    private int nextOut = 0; // next out for circular buffer
    private int available; // total number of spaces available in array

    public Road(int size)
    {
        body = new Vehicle[size];
        available = 0;
    }

    // adds vehicle to buffer, only to be accessed when lock on road object is held,
    // must be used in combination with IsFull()
    public void AddVehicle(Vehicle vehicle)
    {
        body[nextIn] = vehicle;
        available = available + 1;
        nextIn++;

        if (nextIn == body.length)
        {
            nextIn = 0;
        }
    }

    // removes vehicle from buffer, only to be accessed when lock on road object is held,
    // must be used in combination with IsEmpty()
    public Vehicle RemoveVehicle()
    {
        Vehicle res;

        res = body[nextOut];
        available--;

        nextOut++;

        if (nextOut==body.length)
        {
            nextOut=0;
        }
        return res;
    }

    // peeks at the next vehicle without removing it, only to be accessed when lock on road object is held,
    // must be used in combination with IsEmpty()
    public Vehicle PeekNextVehicle()
    {
        return body[nextOut];
    }

    // returns true if road is full
    public boolean IsFull()
    {
        return available == body.length;
    }

    // returns true if road is empty
    public boolean IsEmpty()
    {
        return available == 0;
    }

    // returns current number of cars on road
    public int GetCurrentSize()
    {
        return available;
    }
}


