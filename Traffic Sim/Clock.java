package com.company;

public class Clock extends Thread
{
    private int time; // current time
    private int simulationTimeLimit; // how long sim should run for
    private volatile boolean simRunning = true; // dictates how long threads run for

    public Clock(int totalTime)
    {
        time = 0;
        simulationTimeLimit = totalTime;
    }

    // simple clock mechanism, sleeps for 1 second, increment time variable, repeat until time limit reached.
    public synchronized void run()
    {
        while (simRunning)
        {
            try
            {
                Thread.sleep(1000);
                time++;
            }
            catch (InterruptedException e)
            {

            }

            if (time >= simulationTimeLimit)
            {
                simRunning = false;
            }
        }
    }

    public int GetTime() {
        return time;
    }

    public int GetSimulationTimeLimit() {
        return simulationTimeLimit;
    }

    public boolean IsSimRunning() {
        return simRunning;
    }
}
