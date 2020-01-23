package edu.stevens.cs549.dhts.remote;

import java.net.URI;
import java.net.URISyntaxException;

import edu.stevens.cs549.dhts.main.IContext;

public abstract class ContextBase implements IContext {
	
	protected static URI getServiceUri(String host, String port) throws URISyntaxException {
		return new URI(String.format("http://%s:%s/dht", host, port));
	}

	public static URI getControlServerUri(String host, String port) throws URISyntaxException {
		return new URI(String.format("ws://%s:%s/dht/control", host, port));
	}

	public static URI getControlClientUri(String host, String port, String name) throws URISyntaxException {
		return new URI(String.format("ws://%s:%s/dht/control/%s", host, port, name));
	}

	/*
	 * Flag for turning on and off debug output.
	 */
	private boolean debug = false;
	
	@Override
	public boolean debug() {
		return debug;
	}
	
	@Override
	public void toggleDebug() {
		debug = !debug;
	}

	/*
	 * Allow logging of background processing to be turned on and off.
	 */
	protected boolean background = true;

	@Override
	public void toggleBackground() {
		background = !background;
	}

}
