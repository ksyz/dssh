/*
 * InteractiveSuSession.java
 *
 * Created on May 3, 2007, 11:25 PM
 *
 */
package dssh;

import dssh.authenticators.password.SimplePasswordAuthenticator;
import dssh.authenticators.password.PasswordAuthenticator;
import com.trilead.ssh2.Session;
import dssh.streamutils.DoubleExpectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import dssh.streamutils.TeeInputStream;

/**
 *
 * @author juraj
 */
public class InteractiveSuSession extends InteractiveSession {

    protected PasswordAuthenticator authenticator;
    protected String username;
    protected String hostname;
    protected static Pattern commandPromptPattern = Pattern.compile("[$#]");

    public InteractiveSuSession(Session sess, String hostname, String username,
            PasswordAuthenticator authenticator) {
        super(sess);
        this.authenticator = authenticator;
        this.hostname = hostname;
        this.username = username;
    }

    public InteractiveSuSession(Session sess, String hostname, String username,
            String password) {
        super(sess);
        this.authenticator = new SimplePasswordAuthenticator(password);
        this.hostname = hostname;
        this.username = username;
    }

    public void initializeInteractiveSession(String command,
            boolean stayLoggedIn, boolean keepAllOutput, boolean verbose) throws IOException {

        term.initConsole();
        sess.requestPTY(System.getenv("TERM"), term.getWsCol(), term.getWsRow(),
                term.getWsXPixel(), term.getWsYPixel(), null);
        sess.startShell();
        PrintWriter stdin = new PrintWriter(sess.getStdin());

        InputStream stdout = sess.getStdout();
        InputStream stderr = sess.getStderr();

        if (keepAllOutput) {
            stdout = new TeeInputStream(stdout, System.out);
            stderr = new TeeInputStream(stderr, System.err);
        }

        DoubleExpectInputStream exp = new DoubleExpectInputStream(stdout, stderr, 1024);

        if (!exp.waitFor(commandPromptPattern)) {
            return;
        }

        String password = authenticator.getPassword(username, hostname, 22);
        if ((password == null) || (password.length() == 0)) {
            if (verbose) {
                System.err.println("Su session wanted authentication with agent password, but we have");
                System.err.println("no password for this machine. Skipping su session.");
            }
        } else {
            stdin.println("su - ; exit $?");
            stdin.flush();
            if (!exp.waitFor(Pattern.compile("[Pp]assword:"))) {
                return;
            }

            if (verbose) {
                System.err.println("Using su password from agent");
            }
            stdin.println(password);
            stdin.flush();

            if (command != null) {
                if (!stayLoggedIn) {
                    stdin.println("(" + command + ");exit $?");
                } else {
                    stdin.println(command);
                }
                stdin.flush();
            }
        }
    }

    @Override
    public int doScp(boolean localToRemote, String[] filesToCopy, String targetDirectory) {
        System.err.println("Warning: This implementation does not yet work with su sessions\n" +
                "this scp session is _not_ logged in as root");
        return super.doScp(localToRemote, filesToCopy, targetDirectory);
    }
}
