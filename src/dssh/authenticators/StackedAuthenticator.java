/*
 * StackedAuthenticator.java
 * 
 * Created on May 13, 2007, 4:18:32 PM
 * 
 */

package dssh.authenticators;

import dssh.*;

/**
 *
 * @author juraj
 */
public class StackedAuthenticator implements SSHAuthenticator {

	SSHAuthenticator a1;
	SSHAuthenticator a2;
	
    public StackedAuthenticator(SSHAuthenticator a1, SSHAuthenticator a2) {
	    this.a1 = a1;
	    this.a2 = a2;
    }

    public boolean authenticate(SSHConnection c, String username, boolean allowInteractivity, boolean verbose) {
	if (a1.authenticate(c, username, allowInteractivity, verbose))
		return true;
	return a2.authenticate(c, username, allowInteractivity, verbose);
    }

}
