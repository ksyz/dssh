/*
 * DSSHAgentServer.java
 * 
 * Created on May 13, 2007, 2:19:16 PM
 * 
 */

package dssh.agent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author juraj
 */
public class DSSHAgentServerImpl extends UnicastRemoteObject implements DSSHAgent {
	
	   private PublicKeyAuthenticator pka;
	   private PasswordAuthenticator pass;
	    
    public DSSHAgentServerImpl() throws RemoteException {
	    super(0, new SslRMIClientSocketFactory(), 
		    new SslRMIServerSocketFactory(null, null, true));
	    pka = new PublicKeyAuthenticator();
	    pass = new PasswordAuthenticator();
    }

    public Object getPublicKey(int keyId) {
        return pka.getPublicKey(keyId);
    }

    public byte[] generateSignature(byte[] msg, int keyId) {
	    return pka.generateSignature(msg, keyId);
    }

    public String getPassword(String username, String hostname, int port) {
        return pass.getPassword(username, hostname, port);
    }

    public String getPassword(String username, String hostname, String service) {
        return pass.getPassword(username, hostname, service);
    }

    public void storePrivateKey(Object key) {
        pka.storePrivateKey(key);
    }

    public int getKeystoreLength() {
        return pka.getKeystoreLength();
    }

    public void uploadPasswordData(byte[] data) {
        pass.uploadPasswordData(data);
    }

    public void flushPasswordData() {
        pass.flushPasswordData();
    }

    public void flushPrivateKeys() {
        pka.flushPrivateKeys();
    }

    public void shutdownNow() {
        System.exit(1);
    }
    
    
}
