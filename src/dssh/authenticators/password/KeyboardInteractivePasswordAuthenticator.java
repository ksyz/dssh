/*
 * KeyboardInteractivePasswordAuthenticator.java
 * 
 * Created on May 13, 2007, 10:00:05 PM
 * 
 */

package dssh.authenticators.password;

import dssh.authenticators.KeyboardInteractiveAuthenticator;

/**
 *
 * @author juraj
 */
public class KeyboardInteractivePasswordAuthenticator implements PasswordAuthenticator {

    public KeyboardInteractivePasswordAuthenticator() {
    }

    public String getPassword(String username, String hostname, int port) {
        return KeyboardInteractiveAuthenticator.getPassword("Password for \""+username+
		"@"+hostname+":"+port+"\": ");
    }

}
