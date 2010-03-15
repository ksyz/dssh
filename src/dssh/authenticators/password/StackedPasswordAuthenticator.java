/*
 * StackedPasswordAuthenticator.java
 * 
 * Created on May 13, 2007, 3:32:56 PM
 * 
 */

package dssh.authenticators.password;

import dssh.authenticators.password.PasswordAuthenticator;

/**
 *
 * @author juraj
 */
public class StackedPasswordAuthenticator implements PasswordAuthenticator {

	PasswordAuthenticator p1;
	PasswordAuthenticator p2;
	
    public StackedPasswordAuthenticator(PasswordAuthenticator p1, PasswordAuthenticator p2) {
	    this.p1=p1;
	    this.p2=p2;
    }

    public String getPassword(String username, String hostname, int port) {
        String pass = p1.getPassword(username, hostname, port);
	if (pass == null) 
		pass = p2.getPassword(username, hostname, port);
	return pass;
    }
    
    

}
