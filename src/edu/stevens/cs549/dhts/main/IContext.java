package edu.stevens.cs549.dhts.main;

import java.io.EOFException;
import java.io.IOException;

public interface IContext {
	
	// Used to buffer remote input to a shell from its controller
	public void addCommandLine(String[] inputs);
	
	// The shell reads (from stdin or from buffered remote input) here.
	public String[] readline() throws EOFException, IOException;

	// Either write to console (local) or send to client (proxy)
	public void msg(String m) throws IOException;

	public void msgln(String m) throws IOException;

	public void err(Throwable t) throws IOException;
	
	// Local to a shell session
	public boolean debug();
	
	public void toggleBackground();
	
	public void toggleDebug();
	
	// Stop the processor (a no-op for a proxy main, and otherwise should only be
	// called by local shell on local main).
	public void stop();
	
}
