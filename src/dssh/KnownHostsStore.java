/*
 * KnownHostsStore.java
 *
 * Created on May 23, 2007, 9:52:27 PM
 *
 */

package dssh;

import com.trilead.ssh2.KnownHosts;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author juraj
 */
public class KnownHostsStore {
	
	private static KnownHosts instance = null;
	private static File knownHostsFile = null;
        private static boolean interactive = true;
	
	public static File getKnownHostsFile() {
		return knownHostsFile;
	}
	
	private KnownHostsStore() {
	}
	
	public static synchronized KnownHosts getKnownHostsInstance(File f) throws IOException {
		if (instance == null) {
			instance = new KnownHosts(f);
			knownHostsFile = f;
		}
		return instance;
	}
	
	public static synchronized KnownHosts getKnownHostsInstance() throws IOException {
		if (instance == null)
			return getKnownHostsInstance(new File(System.getenv("HOME")+File.separatorChar+".ssh"
				+File.separatorChar+"known_hosts"));
		return instance;
	}
        
        public static boolean isInteractive() {
            return interactive;
        }
        
        public static void setInteractive(boolean value) {
            interactive = value;
        }
}
