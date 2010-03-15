/*
 * NullPasswordAuthenticator.java
 * 
 * Created on May 15, 2007, 6:23:29 PM
 * 
 */

package dssh.authenticators.password;

/**
 *
 * @author juraj
 */
public class NullPasswordAuthenticator implements PasswordAuthenticator {

    public NullPasswordAuthenticator() {
    }

    public String getPassword(String username, String hostname, int port) {
        return null;
    }

}
