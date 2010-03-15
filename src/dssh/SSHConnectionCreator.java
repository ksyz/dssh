/*
 * SSHConnectionCreator.java
 *
 * Created on May 3, 2007, 9:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh;

import dssh.authenticators.password.PasswordAuthenticator;
import dssh.exceptions.ConnectionAdministrativelyProhibitedException;
import dssh.exceptions.UnauthenticatedSSHConnectionException;
import java.io.IOException;

/**
 *
 * @author juraj
 */
public interface SSHConnectionCreator {
	
	public SSHConnection getAuthenticatedSSHConnection(String username, String host, int port, SSHConnection parent, SSHAuthenticator auth)
		throws IOException, UnauthenticatedSSHConnectionException, ConnectionAdministrativelyProhibitedException;
	public InteractiveSession getInteractiveSession(SSHConnection conn, String username, PasswordAuthenticator pass, String host);
	public void setVerbose(boolean verbose);
	
}
