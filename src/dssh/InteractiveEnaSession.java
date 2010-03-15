/*
 * InteractiveEnaSession.java
 * 
 * Created on Aug 13, 2007, 5:43:48 PM
 * 
 * (c) 2007 Juraj Bednar <juraj.bednar@digmia.com>
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
public class InteractiveEnaSession extends InteractiveSuSession {

    public InteractiveEnaSession(Session sess, String hostname, String username,
            PasswordAuthenticator authenticator) {
        super(sess, hostname, username, authenticator);
    }

    public InteractiveEnaSession(Session sess, String hostname, String username,
            String password) {
        super(sess, hostname, username, password);
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

        if (!exp.waitFor(Pattern.compile("[#>]"))) {
            return;
        }

        String password = authenticator.getPassword(username, hostname, 22);
        if ((password == null) || (password.length() == 0)) {
            if (verbose) {
                System.err.println("Su session wanted authentication with agent password, but we have");
                System.err.println("no password for this machine. Skipping su session.");
            }
        } else {

            stdin.println("ena");
            stdin.flush();
            if (!exp.waitFor(Pattern.compile("[Pp]assword:"))) {
                return;
            }


            if (verbose) {
                System.err.println("Using ena password from agent");
            }

            stdin.println(password);
            stdin.flush();

            if (command != null) {
                if (!stayLoggedIn) {
                    stdin.println(command);
                    stdin.println("exit");
                } else {
                    stdin.println(command);
                }
                stdin.flush();
            }
        }
    }
}
