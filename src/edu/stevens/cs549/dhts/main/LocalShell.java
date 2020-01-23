/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stevens.cs549.dhts.main;

import java.io.EOFException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import edu.stevens.cs549.dhts.activity.DHT;
import edu.stevens.cs549.dhts.activity.IDHTBackground;
import edu.stevens.cs549.dhts.activity.IDHTNode;
import edu.stevens.cs549.dhts.activity.NodeInfo;
import edu.stevens.cs549.dhts.remote.ProxyContext;
import edu.stevens.cs549.dhts.remote.ShellBase;
import edu.stevens.cs549.dhts.state.IRouting;
import edu.stevens.cs549.dhts.state.IState;
import edu.stevens.cs549.dhts.state.Persist;

/*
 * CLI for a DHT node.
 * 
 * This is the local shell that executes all commands locally.
 */

public class LocalShell extends ShellBase {

	protected final IDHTNode node;

	protected final WebClient client;

	private LocalShell(long key, String name, IDHTNode node, WebClient client, IContext main) {
		super(key, name, main);
		this.node = node;
		this.client = client;
	}

	private LocalShell(NodeInfo info, String name, IState s, IRouting r, IContext m) {
		this(info.id, name, new DHT(info, s, r), new WebClient(), m);
	}

	public static LocalShell createTopLevel(NodeInfo info, String name, IState s, IRouting r, LocalContext m) {
		return new LocalShell(info, name, s, r, m);
	}

	public static LocalShell createRemotelyControlled(LocalShell shell, ProxyContext context) {
		return new LocalShell(shell.key, shell.name, shell.node, shell.client, context);
	}

	@Override
	public LocalShell getLocal() {
		return this;
	}
	
	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public IDHTBackground getDHT() {
		return (IDHTBackground) node;
	}

	@Override
	public void notifyPendingSession(String source) throws IOException {
		context.msgln("");
		context.msgln("** Pending connection request from " + source);
		context.msgln("");
	}

	@Override
	public void cli() {

		// Main command-line interface loop

		Dispatch d = new Dispatch(client, node);

		try {
			while (true) {
				msg("dht<" + key + "> ");
				String[] inputs = context.readline();
				if (inputs.length > 0) {
					String cmd = inputs[0];
					if (cmd.length() == 0)
						;
					else if ("get".equals(cmd)) {
						d.get(inputs);
					} else if ("add".equals(cmd)) {
						d.add(inputs);
					} else if ("del".equals(cmd)) {
						d.delete(inputs);
					} else if ("bindings".equals(cmd)) {
						d.bindings(inputs);
					} else if ("join".equals(cmd)) {
						d.join(inputs);
					} else if ("routes".equals(cmd)) {
						d.routes(inputs);
					} else if ("ping".equals(cmd)) {
						d.ping(inputs);
					} else if ("silent".equals(cmd)) {
						d.background(inputs);
					} else if ("debug".equals(cmd)) {
						d.debug(inputs);
					} else if ("weblog".equals(cmd)) {
						d.weblog(inputs);
					}
					// else if ("listenOn".equals(cmd))
					// d.listenOn(inputs);
					// else if ("listenOff".equals(cmd))
					// d.listenOff(inputs);
					// else if ("listeners".equals(cmd))
					// d.listeners(inputs);
					
					else if ("connect".equals(cmd)) {
						connect(inputs);
					} else if ("accept".equals(cmd)) {
						accept(inputs);
					} else if ("reject".equals(cmd)) {
						reject(inputs);
					} else if ("help".equals(cmd)) {
						help(inputs);
					} else if ("quit".equals(cmd)) {
						return;
					} else {
						msgln("Bad input.  Type \"help\" for more information.");
					}
				}
			}
		} catch (EOFException e) {
			
		} catch (IOException e) {
			try {
				err(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			stop();
		}

	}

	protected class Dispatch {

		protected WebClient client;
		protected IDHTNode node;

		protected Dispatch(WebClient c, IDHTNode n) {
			client = c;
			node = n;
		}

		public void get(String[] inputs) throws IOException {
			if (inputs.length == 2)
				try {
					String[] vs = node.getNet(inputs[1]);
					if (vs != null)
						msgln(Persist.displayVals(vs));
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: get <key>");
		}

		public void add(String[] inputs) throws IOException {
			if (inputs.length == 3)
				try {
					node.addNet(inputs[1], inputs[2]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: add <key> <value>");
		}

		public void delete(String[] inputs) throws IOException {
			if (inputs.length == 3)
				try {
					node.deleteNet(inputs[1], inputs[2]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: del <key> <value>");
		}

		public void bindings(String[] inputs) throws IOException {
			if (inputs.length == 1)
				try {
					node.display();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: display");
		}

		public void routes(String[] inputs) throws IOException {
			if (inputs.length == 1)
				try {
					node.routes();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: routes");
		}

		public void background(String[] inputs) throws IOException {
			if (inputs.length == 1)
				try {
					context.toggleBackground();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: background");
		}

		public void debug(String[] inputs) throws IOException {
			if (inputs.length == 1)
				try {
					context.toggleDebug();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: debug");
		}

		public void weblog(String[] inputs) throws IOException {
			if (inputs.length == 1)
				try {
					// client.toggleLogging();
					Log.setLogging();
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: silent");
		}

		public void ping(String[] inputs) throws IOException {
			if (inputs.length == 2)
				try {
					if (client.isFailed(new URI(inputs[1]))) {
						msgln("Server down.");
					} else {
						msgln("Server up.");
					}
				} catch (URISyntaxException e) {
					msgln("Badly formed URI: " + inputs[1]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: ping <uri>");
		}

		public void join(String[] inputs) throws IOException {
			if (inputs.length == 2)
				try {
					node.join(inputs[1]);
				} catch (Exception e) {
					err(e);
				}
			else
				msgln("Usage: insert <uri>");
		}

	}

}
