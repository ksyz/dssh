/*
 * AgentMain.java
 *
 * Created on May 13, 2007, 2:08:59 PM
 *
 */

package dssh.agent;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author juraj
 */
public class AgentMain {
	
	public AgentMain() {
	}
	
	public static void main(String[] args) {
		try {
			int port = 3000;
			if (args.length > 0)
				port = new Integer(args[0]);
			Registry registry = LocateRegistry.createRegistry(port, new SslRMIClientSocketFactory(),
				new SslRMIServerSocketFactory(null, null, true));
			Logger.getLogger("global").log(Level.INFO, "Running registry on port "+port);
			DSSHAgentServerImpl server = new DSSHAgentServerImpl();
			registry.bind("DSSHAgentServer", server);
			
		} catch (RemoteException ex) {
			Logger.getLogger("global").log(Level.SEVERE, null, ex);
		} catch (AlreadyBoundException ex) {
			Logger.getLogger("global").log(Level.SEVERE, null, ex);
		}
	}
	
}
