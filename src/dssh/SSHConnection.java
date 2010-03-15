/*
 * SSHConnection.java
 *
 * Created on April 24, 2007, 2:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package dssh;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.ConnectionInfo;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamConnection;
import dssh.exceptions.ConnectionAdministrativelyProhibitedException;
import dssh.exceptions.UnauthenticatedSSHConnectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 *
 * @author juraj
 */
public class SSHConnection {

    protected SSHConnection parentConnection = null;
    protected Connection conn = null;
    protected Session session = null;
    protected SSHAuthenticator auth = null;
    protected String host = null;
    protected String orighost = null;
    protected int port = 22;
    protected boolean connected = false;
    protected boolean authenticated = false;

    /** Creates a new instance of SSHConnection */
    public SSHConnection(String host, int port, SSHConnection conn, SSHAuthenticator auth) {
        this.parentConnection = conn;
        this.host = host;
        this.orighost = host;
        this.port = port;
        this.auth = auth;
    }

    /** Creates a new instance of SSHConnection */
    public SSHConnection(String orighost, String host, int port, SSHConnection conn, SSHAuthenticator auth) {
        this.parentConnection = conn;
        this.orighost = orighost;
        this.host = host;
        this.port = port;
        this.auth = auth;
    }

    public SSHConnection() {
    }

    protected boolean readYesNo() {
        String answer;
        boolean firstTry = true;
        do {
            if (!firstTry) {
                System.err.print("Error! Please type either \"yes\" or \"no\": ");
            } else {
                firstTry = false;
            }
            try {
                answer = new BufferedReader(new InputStreamReader(System.in)).readLine();
            } catch (IOException e) {
                return false;
            }
            if (answer.equalsIgnoreCase("yes")) {
                return true;
            }
        } while (!answer.equalsIgnoreCase("no"));
        return false;
    }

    public void connect() throws ConnectionAdministrativelyProhibitedException, IOException, UnauthenticatedSSHConnectionException {
        if (parentConnection == null) {
            conn = new Connection(host, port);
        } else {
            if (parentConnection.isAuthenticated()) {
                try {
                    conn = new StreamConnection(host, port, parentConnection.getConnection());
                } catch (IOException e) {
                    Pattern p = Pattern.compile("Could not open channel.*SSH_OPEN_ADMINISTRATIVELY_PROHIBITED");
                    if (p.matcher(e.getMessage()).matches()) {
                        throw new ConnectionAdministrativelyProhibitedException(e.getMessage());
                    } else {
                        throw e;
                    }
                }
            } else {
                throw new UnauthenticatedSSHConnectionException(parentConnection.toString());
            }
        }

        conn.connect();
        KnownHosts kh = KnownHostsStore.getKnownHostsInstance();
        ConnectionInfo ci = conn.getConnectionInfo();
        switch (kh.verifyHostkey(orighost, ci.serverHostKeyAlgorithm, ci.serverHostKey)) {
            case KnownHosts.HOSTKEY_HAS_CHANGED:
                System.err.println("Warning! Host key for \"" + orighost + "\" (" + host + ") has changed.");
                System.err.println("New host key fingerprint: " +
                        KnownHosts.createBubblebabbleFingerprint(ci.serverHostKeyAlgorithm,
                        ci.serverHostKey));
                if (KnownHostsStore.isInteractive()) {
                    System.err.print("Do you want to continue connecting? (yes/no): ");
                    if (!readYesNo()) {
                        System.exit(1);
                    }
                } else {
                    System.err.println("In batch mode, connecting anyways (I hope you know what you are doing)");
                }
                break;
            case KnownHosts.HOSTKEY_IS_NEW:
                System.err.println("Host key for \"" + orighost + "\" (" + host + ") not found in database.");
                System.err.println("Host key fingerprint is: " +
                        KnownHosts.createBubblebabbleFingerprint(ci.serverHostKeyAlgorithm,
                        ci.serverHostKey));
                if (KnownHostsStore.isInteractive()) {
                    System.err.print("Do you want to continue connecting and save this host's key? (yes/no): ");
                    if (!readYesNo()) {
                        System.exit(1);
                    }
                } else {
                    System.err.println("In batch mode, connecting and saving key anyways (I hope you know what you are doing)");

                }
                kh.addHostkeyToFile(KnownHostsStore.getKnownHostsFile(),
                        new String[]{orighost}, ci.serverHostKeyAlgorithm, ci.serverHostKey);

                break;
            case KnownHosts.HOSTKEY_IS_OK:
                // we are safe, nothing to do
                break;
        }
    }

    public boolean authenticate(String username, boolean allowInteractivity, boolean verbose) {
        authenticated = this.auth.authenticate(this, username, allowInteractivity, verbose);
        return authenticated;
    }

    public boolean authenticate(String username) {
        return authenticate(username, true, false);
    }

    public Session openSession() throws IOException {
        session = conn.openSession();
        return session;
    }

    public void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public void disconnect() {
        if (isSessionOpen()) {
            closeSession();
        }
        conn.close();
        authenticated = false;
        connected = false;
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isSessionOpen() {
        return (session != null);
    }

    public String toString() {
        return "SSHConnection to " + host + ":" + port +
                ((parentConnection == null) ? "directly" : " via " + parentConnection.toString());
    }

    public String getHost() {
        return host;
    }

    public String getOrighost() {
        return orighost;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SSHAuthenticator getAuth() {
        return auth;
    }

    public void setAuth(SSHAuthenticator auth) {
        this.auth = auth;
    }

    public Session getSession() {
        return session;
    }
}
