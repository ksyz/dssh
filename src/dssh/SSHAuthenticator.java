package dssh;

/**
 *
 * @author juraj
 */
public interface SSHAuthenticator {
	
	public boolean authenticate(SSHConnection c, String username, boolean allowInteractivity, boolean verbose);
	
}
