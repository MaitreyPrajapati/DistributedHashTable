package edu.stevens.cs549.dhts.remote;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class CommandLineDecoder implements Decoder.Text<String[]>{

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig arg0) {
	}

	@Override
	public String[] decode(String arg) throws DecodeException {
		return arg.split(" ");
	}

	@Override
	public boolean willDecode(String arg0) {
		return true;
	}

}
