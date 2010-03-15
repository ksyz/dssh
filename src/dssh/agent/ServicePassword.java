/*
 * ServicePassword.java
 * 
 * Created on May 13, 2007, 7:37:54 PM
 * 
 */

package dssh.agent;

/**
 *
 * @author juraj
 */
public class ServicePassword {

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

	private String service;
	private String password;
	
    public ServicePassword() {
    }

}
