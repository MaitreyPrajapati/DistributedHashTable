package edu.stevens.cs549.dhts.main;

import java.io.IOException;

import edu.stevens.cs549.dhts.activity.IDHTBackground;

public interface IShell {
	
	public static final String QUIT = "quit";

	/**
	 * Get the local shell on the shell stack.
	 */
	public LocalShell getLocal();
	
	/**
	 * Get the (local or proxy) main for this shell.
	 */
	public IContext getContext();
	
	/**
	 * Test if a proxy shell has terminated (always false for local shell).
	 */
	public boolean isTerminated();
	
	/**
	 * Get the business logic for the local node.
	 */
	public IDHTBackground getDHT();
	
	/**
	 * Check the debug flag for the local node.  Unaffected by remote controllers.
	 */
	public boolean debug();
	
	/**
	 * Notify the shell that a remote control request has been received.
	 */
	public void notifyPendingSession(String source) throws IOException;
	
	/**
	 * Used to create a remote controller.
	 */	
	public long getKey();
	
	public String getName();
	
	/**
	 * If the shell is remotely controlled, commands from the controller are buffered here.
	 */
	public void addCommandLine(String[] inputs);
	
	
	public void msg(String m) throws IOException;

	public void msgln(String m) throws IOException;

	public void err(Throwable t) throws IOException ;

	
	public void cli();
	
	public void stop();
	
}