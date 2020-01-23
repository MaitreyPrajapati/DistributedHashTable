package edu.stevens.cs549.dhts.remote;

import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;

import edu.stevens.cs549.dhts.activity.IDHTBackground;
import edu.stevens.cs549.dhts.main.IShell;
import edu.stevens.cs549.dhts.main.LocalShell;

/**
 * A proxy shell sends its commands to a remote server node, rather than
 * executing them locally.
 */
public class ProxyShell extends ShellBase {

	private static final Logger logger = Logger.getLogger(ProxyShell.class.getCanonicalName());

	private IShell base;

	private RemoteEndpoint.Basic sender;
	
	private boolean terminated = false;

	    ProxyShell(IShell base, RemoteEndpoint.Basic sender) {
		super(base.getKey(), base.getName(), base.getContext());
		this.base = base;
		this.sender = sender;
	}

	/*
	 * Web services require access to local shell for the local business logic.
	 */
	@Override
	public IDHTBackground getDHT() {
		return base.getDHT();
	}

	@Override
	public LocalShell getLocal() {
		return base.getLocal();
	}
	
	@Override
	public boolean isTerminated() {
		return terminated;
	}

	/**
	 * Notify via the local shell when a remote control request is received if
	 * we are already remotely controlling another node.
	 */
	@Override
	public void notifyPendingSession(String source) throws IOException {
		base.notifyPendingSession(source);
	}

	/*
	 * Debugging by remote controller does not affect local debug setting.
	 */
	@Override
	public boolean debug() {
		return context.debug();
	}

	/**
	 * Create a shell proxy that sends commands to a remote server. 
	 */
	public static IShell createRemoteController(IShell base, RemoteEndpoint.Basic sender) {
		return new ProxyShell(base, sender);
	}

	@Override
	public void addCommandLine(String[] inputs) {
		context.addCommandLine(inputs);
	}

	@Override
	public void cli() {
		try {
			try {
				while (true) {
					String[] inputs = context.readline();
					if (inputs.length > 0) {
						String cmd = inputs[0];
						/*
						 * We handle some commands locally: help, and accept,
						 * reject and connect. These commands are always
						 * performed on the client shell.
						 * 
						 * "quit" is performed both on the client and the
						 * server, to ensure shutdown of the control session on
						 * both sides. EOF is interpreted as a quit.
						 */
						if ("connect".equals(cmd)) {
							connect(inputs);
						} else if ("accept".equals(cmd)) {
							accept(inputs);
						} else if ("reject".equals(cmd)) {
							reject(inputs);
						} else if ("quit".equals(cmd)) {
							quitShell();
							return;
						} else if ("help".equals(cmd)) {
							help(inputs);
						} else {
							sender.sendObject(inputs);
						}
					}
				}
				
			} catch (EOFException e) {
				quitShell();
			}
			
		} catch (IOException | EncodeException e) {
			try {
				err(e);
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Unable to report error to user.", e1);
			}
			stop();
		}
	}

	/**
	 * When quitting a proxy session, we leave it to the server to close the connection.
	 * We remove the client proxy shell now because we are exiting the CLI for the shell.
	 * This means that some output from the remote shell may be displayed on the restored client shell.
	 */
	private void quitShell() throws IOException, EncodeException {
		terminated = true;
		ShellManager.getShellManager().removeShell();
		sender.sendObject(new String[] { "quit" });
	}

}
