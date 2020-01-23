package edu.stevens.cs549.dhts.remote;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.websocket.Session;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import edu.stevens.cs549.dhts.main.LocalShell;

/**
 * Maintain a stack of shells.
 * @author dduggan
 *
 */
public class SessionManager {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SessionManager.class.getCanonicalName());
	
	public static final String ACK = "ACK";
	
	private static final ShellManager shell_manager = ShellManager.getShellManager();

	private static final SessionManager SESSION_MANAGER = new SessionManager();
	
	public static SessionManager getSessionManager() {
		return SESSION_MANAGER;
	}
	
	private Lock lock = new ReentrantLock();
	
	private ControllerServer currentServer;
	
	public boolean isSession() {
		return currentServer != null;
	}

	public Session getCurrentSession() {
		return currentServer != null ? currentServer.getSession() : null;
	}

	public boolean setCurrentSession(ControllerServer server) {
		lock.lock();
		try {
			if (currentServer == null) {
				currentServer = server;
				return true;
			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void acceptSession() throws IOException {
		lock.lock();
		try {
			/*
			 *  TODO We are accepting a remote control request.  Push a local shell with a proxy context
			 *  on the shell stack and flag that initialization has completed.  Confirm acceptance of the 
			 *  remote control request by sending an ACK to the client.  The CLI of the newly installed shell
			 *  will be executed by the underlying CLI as part of the "accept" command.
			 */
			
			ProxyContext proxy_context = ProxyContext.createProxyContext(currentServer.getSession().getBasicRemote());
			LocalShell current_shell  = LocalShell.createRemotelyControlled(ShellManager.getShellManager().getCurrentShell().getLocal(), proxy_context);
			ShellManager.getShellManager().addShell(current_shell);
			
			currentServer.endInitialization();	
			currentServer.getSession().getBasicRemote().sendText(ACK);

		} finally {
			lock.unlock();
		}
	}
	
	public void rejectSession() {
		lock.lock();
		try {
			// TODO reject remote control request by closing the session (provide a reason!)

			currentServer.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"!! REQUEST HAS BEEN REJECTED !!"));
			currentServer = null;
		}
		catch(Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	public void closeCurrentSession() {
		lock.lock();
		try {
			// TODO normal shutdown of remote control session (provide a reason!)
			currentServer.getSession().close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE,"!! SERVER STOPPED THE CONNECTION !!"));
			currentServer = null;
		}catch(IOException e){
			e.printStackTrace();
		}  finally {
			lock.unlock();
		}
	}

}
