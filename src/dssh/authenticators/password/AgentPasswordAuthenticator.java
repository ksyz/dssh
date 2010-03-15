/*
 * AgentPasswordAuthenticator.java
 * 
 * Created on May 13, 2007, 3:31:08 PM
 * 
 */

package dssh.authenticators.password;

import dssh.authenticators.password.PasswordAuthenticator;
import dssh.*;
import dssh.agent.DSSHAgent;
import java.rmi.RemoteException;

/**
 *
 * @author juraj
 */
public class AgentPasswordAuthenticator implements PasswordAuthenticator {

	private DSSHAgent agent;
	
    public AgentPasswordAuthenticator(DSSHAgent agent) {
	    this.agent = agent;
    }

    public String getPassword(String username, String hostname, int port) {
	    try {
        return agent.getPassword(username, hostname, port);
	    } catch (RemoteException e)
	    {
		    System.err.println("Error communicating with agent while trying to fetch password"+
			    e.getMessage());
		    return null;
	    }
    }

}
