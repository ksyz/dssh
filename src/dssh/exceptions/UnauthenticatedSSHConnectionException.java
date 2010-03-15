/*
 * UnauthenticatedSSHConnectionException.java
 *
 * Created on May 1, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dssh.exceptions;

/**
 *
 * @author juraj
 */
public class UnauthenticatedSSHConnectionException extends Exception {
	
	/** Creates a new instance of UnauthenticatedSSHConnectionException */
	public UnauthenticatedSSHConnectionException(String message) {
		super(message);
	}
	
}
