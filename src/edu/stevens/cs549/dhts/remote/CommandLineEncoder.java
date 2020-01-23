package edu.stevens.cs549.dhts.remote;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class CommandLineEncoder implements Encoder.Text<String[]> {

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public String encode(String[] args) throws EncodeException {
		StringBuilder sb = new StringBuilder();
		for (int ix=0; ix<args.length; ix++) {
			sb.append(args[ix]);
			if (ix < args.length-1) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

}
