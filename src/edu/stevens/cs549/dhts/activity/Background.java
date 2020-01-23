package edu.stevens.cs549.dhts.activity;

import edu.stevens.cs549.dhts.main.LocalContext;

/*
 * Background thread performs periodic stabilization (on successor)
 * and fixes finger pointers.
 */
public class Background implements Runnable {

	protected long interval;
	protected int ntimes;
	protected LocalContext main;
	protected IDHTBackground dht;

	public Background(long msecs, int n, LocalContext m, IDHTBackground d) {
		interval = msecs;
		ntimes = n;
		main = m;
		dht = d;
	}

	public void run() {
		try {
			while (!main.isTerminated()) {
				try {
					Thread.sleep(interval);
					main.bgInfo("Performing background stabilization.");
					dht.checkPredecessor();
					dht.stabilize();
					dht.fixFingers(ntimes);
				} catch (DHTBase.Failed e) {
					main.bgWarning("Remote failure during background processing: " + e);
				}
			}
		} catch (InterruptedException e) {
			main.bgSevere("Exiting background thread: " + e);
		} catch (DHTBase.Error e) {
			main.bgSevere("Internal error during background processing: " + e);
		}
	}

}
