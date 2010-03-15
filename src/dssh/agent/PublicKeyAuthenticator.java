/*
 * PublicKeyAuthenticator.java
 *
 * Created on May 13, 2007, 3:36:03 PM
 *
 */

package dssh.agent;

import com.trilead.ssh2.signature.DSAPrivateKey;
import com.trilead.ssh2.signature.DSASHA1Verify;
import com.trilead.ssh2.signature.RSAPrivateKey;
import com.trilead.ssh2.signature.RSASHA1Verify;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 *
 * @author juraj
 */
public class PublicKeyAuthenticator {
	
	ArrayList<Object> keyStore;
	SecureRandom rnd;
	
	public PublicKeyAuthenticator() {
		keyStore = new ArrayList<Object>();
		rnd = new SecureRandom();
	}
	
	public Object getPublicKey(int keyId) {
		Object key = keyStore.get(keyId);
		if (key instanceof DSAPrivateKey) {
			DSAPrivateKey k = (DSAPrivateKey) key;
			return k.getPublicKey();
		} else if (key instanceof RSAPrivateKey) {
			RSAPrivateKey k = (RSAPrivateKey) key;
			return k.getPublicKey();
		} else
			return null;
	}
	
	public byte[] generateSignature(byte[] msg, int keyId) {
		Object key = keyStore.get(keyId);
		if (key instanceof DSAPrivateKey) {
			return DSASHA1Verify.encodeSSHDSASignature(
				DSASHA1Verify.generateSignature(msg,
				(DSAPrivateKey) key, rnd));
		} else if (key instanceof RSAPrivateKey) {
			try {
				return RSASHA1Verify.encodeSSHRSASignature(
					RSASHA1Verify.generateSignature(msg,
					(RSAPrivateKey) key));
			} catch (IOException e)  {
				return null;
			}
		} else
			return null;
	}
	
	public void storePrivateKey(Object key) {
		if (!keyStore.contains(key))
			keyStore.add(key);
	}
	
	
	public int getKeystoreLength() {
		return keyStore.size();
	}
	
	public void flushPrivateKeys() {
		keyStore.clear();
		System.gc();
	}
	
}
