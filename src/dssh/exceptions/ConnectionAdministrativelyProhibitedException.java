/*
 * ConnectionAdministrativelyProhibitedException.java
 * 
 * Created on Jul 16, 2007, 6:01:01 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dssh.exceptions;

import java.io.IOException;

/**
 *
 * @author juraj
 */
public class ConnectionAdministrativelyProhibitedException extends Exception{

    public ConnectionAdministrativelyProhibitedException(String message) {
	    super(message);
    }

}
