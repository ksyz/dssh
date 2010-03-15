/*
 * PasswordAuthenticator.java
 *
 * Created on May 13, 2007, 3:51:39 PM
 *
 */

package dssh.agent;

import au.com.bytecode.opencsv.CSVReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author juraj
 */
public class PasswordAuthenticator {
	
	private ArrayList<HostEntry> hosts;
	
	public PasswordAuthenticator() {
		hosts=new ArrayList<HostEntry>();
	}
	
	public HostEntry findHost(String hostname) {
		for (HostEntry host : hosts)
			if (host.isHostnameOrAlias(hostname))
				return host;
		return null;
	}
	
	public String getPassword(String username, String hostname, String service) {
		HostEntry host = findHost(hostname);
		if (host == null)
			return null;
		return host.getPassword(service, username);
	}

    public String getPassword(String username, String hostname, int port) {
		return getPassword(username, hostname, new Integer(port).toString());
	}
	
	
	public void uploadPasswordData(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		CSVReader reader = new CSVReader(new InputStreamReader(in),
			';', '"');
		String[] nextLine;
		try {
			while ((nextLine = reader.readNext()) != null)
				if (nextLine.length >= 4) {
					final String hostname = nextLine[0];
					final String service = nextLine[1];
					final String username = nextLine[2];
					final String password = nextLine[3];
					HostEntry host = findHost(hostname);
					if (host == null) {
						host = new HostEntry();
						host.setHostname(hostname);
						hosts.add(host);
					}
					host.addPassword(service, username, password);
					for (int i = 4; i<nextLine.length; i++)
						host.addAlias(nextLine[i]);
				}
		} catch (IOException e) {
			// should never ever happen, we are reading from byte array
			e.printStackTrace();
			return;
		}
	}
	
	public void flushPasswordData() {
		hosts.clear();
		System.gc();
	}
	
}
