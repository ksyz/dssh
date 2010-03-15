/*
 * DSSHAgent.java
 * 
 * Created on May 13, 2007, 3:03:59 PM
 * 
 */

package dssh.agent;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author juraj
 */
public interface DSSHAgent extends Remote {
	
	public Object getPublicKey(int keyId) throws RemoteException;
	public byte[] generateSignature(byte[] msg, int keyId) throws RemoteException;
	public String getPassword(String username, String hostname, int port) throws RemoteException;
	public String getPassword(String username, String hostname, String Service) throws RemoteException;
    public int getKeystoreLength() throws RemoteException;
	
	// Adding key material
	public void storePrivateKey(Object key) throws RemoteException;
	public void uploadPasswordData(byte[] data) throws RemoteException;
	public void flushPasswordData() throws RemoteException;
	public void flushPrivateKeys() throws RemoteException;
	public void shutdownNow() throws RemoteException;

}
