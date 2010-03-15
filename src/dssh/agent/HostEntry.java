/*
 * HostEntry.java
 * 
 * Created on May 13, 2007, 7:32:23 PM
 * 
 */

package dssh.agent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author juraj
 */
public class HostEntry {
	
	private String hostname;
	private ArrayList<String> aliases;
	private HashMap<String,ArrayList<ServicePassword>> userpasswordMap;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public HostEntry() {
	    aliases = new ArrayList<String>();
	    userpasswordMap = new HashMap<String,ArrayList<ServicePassword>>();
    }
    
    public void addAlias(String alias) {
	    if (!aliases.contains(alias))
		    aliases.add(alias);
    }
    
    public boolean isHostnameOrAlias(String alias) {
	    if (hostname.equals(alias))
		    return true;
	    if (aliases.contains(alias))
		    return true;
	    return false;
    }
    
    public String getPassword(String service, String username) {
	    ArrayList<ServicePassword> services = userpasswordMap.get(username);
	    if (services != null) {
		    for (ServicePassword s : services) {
			    if (s.getService().equals(service))
				    return s.getPassword();
		    }
	    }
	    return null;
    }
    
    public void addPassword(String service, String username, String password) {
	    ArrayList<ServicePassword> services = userpasswordMap.get(username);
	    if (services == null) {
		    services = new ArrayList<ServicePassword>();
		    userpasswordMap.put(username, services);
	    }
	    
	    for (ServicePassword s : services) {
		    if (s.getService().equals(service)) {
			    services.remove(s);
			    break;
		    }
	    }
	    
	    ServicePassword pass = new ServicePassword();
	    pass.setService(service);
	    pass.setPassword(password);
	    services.add(pass);
		    
    }

}

