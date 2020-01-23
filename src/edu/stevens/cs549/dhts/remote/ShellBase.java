package edu.stevens.cs549.dhts.remote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.DeploymentException;

import edu.stevens.cs549.dhts.main.IContext;
import edu.stevens.cs549.dhts.main.IShell;

public abstract class ShellBase implements IShell {

	protected final IContext context;

	protected final long key;

	protected final String name;

	protected SessionManager sessionManager = SessionManager.getSessionManager();

	protected ShellManager shellManager = ShellManager.getShellManager();

	protected ShellBase(long key, String name, IContext context) {
		this.key = key;
		this.name = name;
		this.context = context;
	}

	@Override
	public long getKey() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IContext getContext() {
		return context;
	}

	@Override
	public void addCommandLine(String[] inputs) {
		context.addCommandLine(inputs);
	}

	@Override
	public void msg(String m) throws IOException {
		context.msg(m);
	}

	@Override
	public void msgln(String m) throws IOException {
		context.msgln(m);
	}

	@Override
	public void err(Throwable t) throws IOException {
		context.err(t);
	}

	@Override
	public boolean debug() {
		return context.debug();
	}

	@Override
	public void stop() {
		context.stop();
	}

	protected void help(String[] inputs) throws IOException {
		if (inputs.length == 1) {
			msgln("Commands are:");
			// Network-wide commands
			msgln("  get key: get values under a key");
			msgln("  add key value: add a value under a key");
			msgln("  del key value: delete a value under a key");
			// Network protocol
			msgln("  join uri: join a DHT as a new node");
			msgln("  bindings: display all key-value bindings");
			msgln("  routes: display routing information");
			msgln("  ping uri: check if a remote node is active");
			// Logging commands.
			msgln("  background: toggle on and off logging of background processing");
			msgln("  weblog: toggle on and off web service logging");
			msgln("  debug: toggle on and off debug logging");
			// Remote control commands.
			msgln("  connect host port: connect to a site to control it remotely");
			msgln("  accept: accept the current pending remote control request");
			msgln("  reject: reject the current pending remote control request");
			msgln("  quit: exit the current shell");
			// // Listening commands.
			// msgln(" listenOn key: request notification of a change in binding
			// for this key");
			// msgln(" listenOff key: disable any further notifications for this
			// key");
			// msgln(" listeners: keys for which listeners are defined");

			msgln("  quit: exit the client");
		}
	}

	protected void connect(String[] inputs) throws IOException {
		if (inputs.length != 3) {
			msgln("Usage: connect host port");
		} else {
			try {
				URI uri = ContextBase.getControlClientUri(inputs[1], inputs[2], name);
				ControllerClient client = new ControllerClient(this);
				client.connect(uri);
			} catch (URISyntaxException e) {
				msgln("Badly formatted URI.");
			} catch (DeploymentException e) {
				err(e);
			}
		}
	}

	/**
	 * TODO Accept the pending session (see SessionManager) and 
	 * start running the CLI for the new shell that will have been
	 * pushed on the shell stack.
	 */
	protected void accept(String[] inputs) throws IOException {
		if (inputs.length != 1) {
			msgln("Usage: accept");
		} else {
			// TODO
			
			sessionManager.acceptSession();
			shellManager.getCurrentShell().cli();
		}
	}

	/**
	 * TODO Reject and remove the pending session (see SessionManager).
	 */
	protected void reject(String[] inputs) throws IOException {
		if (inputs.length != 1) {
			msgln("Usage: reject");
		} else {
			// TODO
			sessionManager.rejectSession();

		}
	}

}
