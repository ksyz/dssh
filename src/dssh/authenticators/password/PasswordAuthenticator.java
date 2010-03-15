/*
 * PasswordAuthenticator.java
 *
 * Created on May 3, 2007, 11:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh.authenticators.password;

/**
 *
 * @author juraj
 */
public interface PasswordAuthenticator {
	public String getPassword(String username, String hostname, int port);
}
