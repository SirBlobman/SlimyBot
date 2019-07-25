package com.SirBlobman.discord.utility;

import java.util.Timer;
import java.util.TimerTask;

public final class SchedulerUtil extends Util {
	private static final Timer TIMER = new Timer();
	
	/**
	 * Execute something later, instead of right now
	 * @param delay Amount of time to wait (milliseconds)
	 * @param task {@link Runnable} to run
	 * @return 
	 */
	public static TimerTask runLater(long delay, Runnable task) {
	    TimerTask timer = new TimerTask() {public void run() {task.run(); cancel();}};
	    TIMER.schedule(timer, delay);
	    return timer;
	}
	
	/**
	 * Execute something multiple times after the delay.
	 * @param delay Amount of time to wait (milliseconds)
	 * @param period Amount of time before it runs again (milliseconds)
	 * @param task {@link Runnable} to run
	 */
	public static TimerTask runUntilStopped(long delay, long period, Runnable task) {
	    TimerTask timer = new TimerTask() {public void run() {task.run();}};
	    TIMER.schedule(timer, delay, period);
	    return timer;
	}
}