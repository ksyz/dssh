/*
 * SimplePasswordAuthenticator.java
 *
 * Created on May 3, 2007, 11:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh.authenticators.password;

import dssh.authenticators.password.PasswordAuthenticator;

/**
 *
 * @author juraj
 */
public class SimplePasswordAuthenticator implements PasswordAuthenticator {
	
	private String password;
	
	/** Creates a new instance of SimplePasswordAuthenticator */
	public SimplePasswordAuthenticator(String password) {
		this.password = password;
	}

	public String getPassword(String username, String hostname, int port) {
		return password;
	}
	
}
