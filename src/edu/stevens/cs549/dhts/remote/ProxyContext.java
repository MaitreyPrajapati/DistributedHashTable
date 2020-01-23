package edu.stevens.cs549.dhts.remote;

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import javax.websocket.RemoteEndpoint;

/**
 * A proxy context wraps a remote client for a local shell, providing an API for
 * reading commands sent by the client (instead of from the console) and sending
 * back responses. There will be one of these for each remote client that is
 * executing remote commands (but we currently restrict this to one client).
 * 
 * @author dduggan
 *
 */
public class ProxyContext extends ContextBase {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ProxyContext.class.getCanonicalName());
	
	private ProxyContext(RemoteEndpoint.Basic sender) {
		this.sender = sender;
	}
	
	public static ProxyContext createProxyContext(RemoteEndpoint.Basic sender) {
		return new ProxyContext(sender);
	}

	/*
	 * We buffer commands from the client here.
	 */
	private Queue<String[]> commandLines = new LinkedList<String[]>();

	private boolean stopped = false;

	/*
	 * Cache the current remote control session, which should not change until
	 * we exit this session.
	 */
	@SuppressWarnings("unused")
	private RemoteEndpoint.Basic sender;

	@Override
	public void addCommandLine(String[] inputs) {
		synchronized (commandLines) {
			commandLines.add(inputs);
			commandLines.notify();
		}
	}

	protected String[] getCommandLine() throws EOFException {
		synchronized (commandLines) {
			while (commandLines.isEmpty() && !stopped) {
				try {
					commandLines.wait();
				} catch (InterruptedException e) {
				}
			}
			if (!stopped) {
				return commandLines.remove();
			} else {
				throw new EOFException();
			}
		}
	}

	/*
	 * The shell reads a command here, and blocks if no command is available.
	 */
	@Override
	public String[] readline() throws EOFException, IOException {
		return getCommandLine();
	}

	@Override
	public void msg(String m) throws IOException {
		// TODO display the message on the remote client console
		this.sender.sendText(m);

	}

	@Override
	public void msgln(String m) throws IOException {
		// TODO display the message on the remote client console
		this.sender.sendText("\n"+m+"\n");

	}

	@Override
	public void err(Throwable t) throws IOException {
		// TODO print the stack trace on the remote client console
		this.sender.sendText(t.toString());

	}

	@Override
	public void stop() {
		/*
		 * Notify the local CLI loop that execution has finished.
		 */
		stopped = true;
		synchronized (commandLines) {
			commandLines.notifyAll();
		}
	}

}
