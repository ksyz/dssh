/*
 * AgentAuthenticator.java
 *
 * Created on May 13, 2007, 4:22:31 PM
 *
 */

package dssh.authenticators;

import com.trilead.ssh2.InteractiveCallback;
import dssh.*;
import dssh.agent.DSSHAgent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juraj
 */
public class AgentAuthenticator implements SSHAuthenticator {
	
	DSSHAgent agent;
	public AgentAuthenticator(DSSHAgent agent) {
		this.agent = agent;
	}
	
	public boolean authenticate(SSHConnection c, String username, boolean allowInteractivity, boolean verbose) {
		int keyStoreLength = 0;
		try {
			keyStoreLength = agent.getKeystoreLength();
		} catch (RemoteException e) {
			System.err.println("RemoteException while fetching keystore length:"+e.getMessage());
			return false;
		}
		// HACK HACK HACK HACK HACK
		// TODO
		// Well, this is actually pretty ugly. I mean it...
		// This will _NOT_ work if Connection has already authentication manager
		// registered. That means, that this Authenticator _has_ to be the first
		// that touches this Connection before auth. To be fixed.
		AgentAuthenticationManager am = new
			AgentAuthenticationManager(c.getConnection().getTransportManager(),
			agent);
		
		c.getConnection().setAuthenticationManager(am);
		// /HACK HACK HACK HACK HACK
		
		try {
			if (c.getConnection().isAuthMethodAvailable(username, "publickey")) {
				for (int keyId = 0 ; keyId < keyStoreLength ; keyId++) {
					Object publicKey = agent.getPublicKey(keyId);
					if (publicKey == null) {
						continue;
					}
					if (c.getConnection().tryPublicKey(username, publicKey)) {
						boolean authenticated = am.authenticatePublicKey(username, keyId, publicKey);
						c.getConnection().setAuthenticated(authenticated);
                                                if ((verbose) && (authenticated))
                                                    System.err.println("Authenticated using public key via agent");
						// even if our try was not sucessful, most servers
						// will deny another public key auth request, so there's
						// no point trying another key if we got this far
						return authenticated;
					}
				}
			}
		} catch (IOException ex) {
			Logger.getLogger("global").log(Level.SEVERE, "Public key authentication failed", ex);
		}
		
		// now try authentication with password from agent
		try {
			final String password = agent.getPassword(username, c.getOrighost(), c.getPort());
			if (password != null) {
				if (c.getConnection().isAuthMethodAvailable(username, "password")) {
                                        boolean authenticated =  c.getConnection().authenticateWithPassword(username, password);
                                        c.getConnection().setAuthenticated(authenticated);
                                        if ((verbose) && (authenticated))
                                            System.err.println("Authenticated using password from agent");
                                        return authenticated;
				} else
					if (c.getConnection().isAuthMethodAvailable(username, "keyboard-interactive")) {
						boolean authenticated = c.getConnection().authenticateWithKeyboardInteractive(username,new InteractiveCallback() {
							
							public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws Exception {
								if (numPrompts == 0)
									return new String[0];
								if ((numPrompts == 1) && (prompt[0].startsWith("Password")))
									return new String[] { password };
								System.err.println("Warning/FIX: don't know what to do with keyboard-interactive method with unknown prompts");
								System.err.println("Prompts are: ");
								for (String p : prompt)
									System.err.println(p);
								return new String[0];
							}
							
						});
                                                if ((verbose) && (authenticated))
                                                    System.err.println("Authenticated using keyboard interactive password via agent");
                                                c.getConnection().setAuthenticated(authenticated);
                                                return authenticated;
					}
			}
		} catch (IOException ex) {
			Logger.getLogger("global").log(Level.SEVERE, "Password authentication failed", ex);
		}
		return false;
	}
	
}
