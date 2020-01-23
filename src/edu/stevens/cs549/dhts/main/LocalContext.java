package edu.stevens.cs549.dhts.main;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.websocket.DeploymentException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.tyrus.server.Server;

import edu.stevens.cs549.dhts.activity.Background;
import edu.stevens.cs549.dhts.activity.NodeInfo;
import edu.stevens.cs549.dhts.remote.ContextBase;
import edu.stevens.cs549.dhts.remote.ControllerServer;
import edu.stevens.cs549.dhts.remote.ShellManager;
import edu.stevens.cs549.dhts.state.IRouting;
import edu.stevens.cs549.dhts.state.State;

/**
 * Top-level local IO. There should only be one of these per node, used by all
 * shells that are reading commands locally (even if commands are executed on a
 * remote node).
 */
public class LocalContext extends ContextBase {

	public static final String serverPropsFile = "/server.properties";
	public static final String loggerPropsFile = "/log4j.properties";

	private static final Logger logger = Logger.getLogger(LocalContext.class.getCanonicalName());

	public static void severe(String s) {
		logger.severe(s);
	}

	public static void warning(String s) {
		logger.info(s);
	}

	public static void info(String s) {
		logger.info(s);
	}

	private BufferedReader input;

	@Override
	public void addCommandLine(String[] inputs) {
		StringBuilder sb = new StringBuilder();
		if (inputs.length > 0) {
			for (String input : inputs) {
				sb.append(input);
				sb.append(' ');
			}
		}
		throw new IllegalStateException("Trying to send a command line to a top-level shell: " + sb.toString());
	}

	@Override
	public String[] readline() throws EOFException, IOException {
		String line = input.readLine();
		if (line == null) {
			throw new EOFException();
		}
		String[] inputs = line.split("\\s+");
		return inputs;
	}

	@Override
	public void msg(String m) {
		System.out.print(m);
	}

	@Override
	public void msgln(String m) {
		System.out.println(m);
		System.out.flush();
	}

	@Override
	public void err(Throwable t) {
		t.printStackTrace();
	}

	@Override
	public void stop() {
		System.exit(-1);
	}

	public void bgSevere(String s) {
		if (!background)
			severe(s);
	}

	public void bgWarning(String s) {
		if (!background)
			info(s);
	}

	public void bgInfo(String s) {
		if (!background)
			info(s);
	}

	/*
	 * Hostname and port for HTTP server URL.
	 */
	private static String host;

	private static int httpPort;

	private static int wsPort;

	private static String name;

	private static URI SERVICE_URI;

	private static URI CONTROL_URI;

	public static URI getServiceUri() {
		try {
			return getServiceUri(host, Integer.toString(httpPort));
		} catch (URISyntaxException e) {
			IllegalStateException e1 = new IllegalStateException("Badly formed service URI.");
			e1.initCause(e);
			throw e1;
		}
	}

	public static URI getControlUri() {
		try {
			return getControlServerUri(host, Integer.toString(wsPort));
		} catch (URISyntaxException e) {
			IllegalStateException e1 = new IllegalStateException("Badly formed control URI.");
			e1.initCause(e);
			throw e1;
		}
	}

	private static State stateServer;

	public static State stateServer() {
		return stateServer;
	}

	private static IRouting routingServer;

	public static IRouting routingServer() {
		return routingServer;
	}

	public final static String DHT_STATE = "DHT_STATE";

	/*
	 * The key for our place in the Chord ring. We will set it to a random id,
	 * and then set if a value was specified on the command line.
	 */
	private static int nodeId;

	protected static NodeInfo INFO;

	protected State stub;

	private LocalContext(String[] args) {
		/*
		 * Input command stream
		 */
		this.input = new BufferedReader(new InputStreamReader(System.in));
	}

	protected static void initialize(String[] args) throws Exception {

		/*
		 * Load server properties.
		 */
		Properties props = new Properties();
		InputStream in = LocalContext.class.getResourceAsStream(serverPropsFile);
		props.load(in);
		in.close();

		host = (String) props.getProperty("server.host", "localhost");

		httpPort = Integer.parseInt((String) props.getProperty("server.port.http", "8080"));

		wsPort = Integer.parseInt((String) props.getProperty("server.port.ws", "8181"));

		name = (String) props.getProperty("server.name", "anonymous");

		nodeId = new Random().nextInt(IRouting.NKEYS);

		/*
		 * Properties may be overridden by command line options.
		 */
		processArgs(args);

		SERVICE_URI = getServiceUri();

		CONTROL_URI = getControlUri();

		INFO = new NodeInfo(nodeId, SERVICE_URI);

	}

	protected static List<String> processArgs(String[] args) {
		List<String> commandLineArgs = new ArrayList<String>();
		int ix = 0;
		Hashtable<String, String> opts = new Hashtable<String, String>();

		while (ix < args.length) {
			if (args[ix].startsWith("--")) {
				String option = args[ix++].substring(2);
				if (ix == args.length || args[ix].startsWith("--"))
					severe("Missing argument for --" + option + " option.");
				else if (opts.containsKey(option))
					severe("Option \"" + option + "\" already set.");
				else
					opts.put(option, args[ix++]);
			} else {
				commandLineArgs.add(args[ix++]);
			}
		}
		/*
		 * Overrides of values from configuration file.
		 */
		Enumeration<String> keys = opts.keys();
		while (keys.hasMoreElements()) {
			String k = keys.nextElement();
			if ("host".equals(k))
				host = opts.get("host");
			else if ("http".equals(k))
				httpPort = Integer.parseInt(opts.get("http"));
			else if ("ws".equals(k))
				wsPort = Integer.parseInt(opts.get("ws"));
			else if ("name".equals(k))
				name = opts.get("name");
			else if ("id".equals(k))
				nodeId = Integer.parseInt(opts.get("id"));
			else
				severe("Unrecognized option: --" + k);
		}

		return commandLineArgs;
	}

	protected State startStateServer() throws IOException {
		info("Starting state server...");
		State stub = new State(INFO);
		stateServer = stub;
		routingServer = stub;
		info("State server bound.");
		return stub;
	}

	protected HttpServer startHttpServer() throws IOException {
		info("Starting HTTP server.");
		return GrizzlyHttpServerFactory.createHttpServer(SERVICE_URI, new Application());
	}

	protected Server startWsServer() {
		info("Starting WS server.");
		Server server = new Server(host, wsPort, "/dht", null, ControllerServer.class);
		try {
			server.start();
		} catch (DeploymentException e) {
			IllegalStateException e1 = new IllegalStateException("Failure to start WS server.");
			e1.initCause(e);
			throw e1;
		}
		return server;
	}

	protected boolean terminated = false;

	public void setTerminated() {
		terminated = true;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public static void main(String[] args) throws Exception {

		initialize(args);

		LocalContext main = new LocalContext(args);
		/*
		 * Start the state server for this node.
		 */
		main.startStateServer();
		/*
		 * Start the HTTP server (for Web services) and Websockets server (for
		 * remote control).
		 */
		LocalShell toplevel = LocalShell.createTopLevel(INFO, name, stateServer(), routingServer(), main);
		ShellManager.getShellManager().addShell(toplevel);

		HttpServer httpServer = main.startHttpServer();

		Server wsServer = main.startWsServer();

		try {

			/*
			 * Start the background thread for stabilize.
			 */
			Thread t = new Thread(new Background(5000, 8, main, toplevel.getDHT()));
			t.start();
			/*
			 * Start the command-line loop.
			 */
			info(String.format(
					"Server started with DHT service at " + SERVICE_URI + " and control service at " + CONTROL_URI));
			toplevel.cli();

		} finally {

			/*
			 * Executes when the CLI terminates.
			 */
			info("Terminating background processing...");
			main.setTerminated();
			
			info("Shutting down Web servers...");
			httpServer.shutdownNow();
			wsServer.stop();

		}

	}
}
