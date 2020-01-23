package edu.stevens.cs549.dhts.remote;

import java.util.Stack;

import edu.stevens.cs549.dhts.main.IShell;

/**
 * Maintain a stack of shells.
 * @author dduggan
 *
 */
public class ShellManager {
	
	private static final ShellManager SHELL_MANAGER = new ShellManager();
	
	public static ShellManager getShellManager() {
		return SHELL_MANAGER;
	}
		
	private Stack<IShell> shells = new Stack<IShell>();
	
	public IShell getCurrentShell() {
		return shells.peek();
	}

	/*
	 * On the client: 
	 * 		Push a proxy shell that sends commands to a server node.
	 * On the server:
	 * 		Push a shell that reads commands from a remote client.
	 * 
	 * If we are currently being executed by a remote client, we do not accept further
	 * remote control requests (they are rejected by the session manager).
	 * 
	 * We may have a stack of client proxy shells when we accept a remote control request.
	 * At that point, our local shell blocks until the remote client returns control to us.
	 */
	public void addShell(IShell shell) {
		shells.push(shell);
	}
	
	/*
	 * Remove the topmost shell (done at both client and server side when we exit a proxy shell).
	 */
	public void removeShell() {
		shells.pop();
	}

}
