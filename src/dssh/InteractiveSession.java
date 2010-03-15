/*
 * InteractiveSession.java
 *
 * Created on April 14, 2007, 9:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package dssh;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;
import dssh.streamutils.DoubleExpectInputStream;
import dssh.streamutils.TeeInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 *
 * @author juraj
 */
public class InteractiveSession {

    private boolean shouldEnd = false;
    protected Session sess;
    protected Terminal term;

    public void handleResize(Session sess) throws IOException {
        if (term.shouldChangeWindowSize()) {
            sess.requestWindowSizeChange(term.getWsCol(), term.getWsRow(), term.getWsXPixel(), term.getWsYPixel());
        }
    }

    public void shouldEnd() {
        shouldEnd = true;
        term.finishConsole();
    }

    /** Creates a new instance of InteractiveSession */
    public InteractiveSession(Session sess) {
        this.sess = sess;
        term = new Terminal();
    }

    public int copyNonBlocking(InputStream in, OutputStream out, byte[] b) throws IOException {

        int ab;
        int nb = 0;
        if ((ab = in.available()) > 0) {
            nb = in.read(b, 0, ab < b.length ? ab : b.length);
            if (nb == -1) {
                shouldEnd = true;
            } else {
                out.write(b, 0, nb);
            }
        }
        return nb;
    }

    public void initializeInteractiveSession(String command, boolean stayLoggedIn,
            boolean keepAllOutput, boolean verbose) throws IOException {
        term.initConsole();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                term.finishConsole();
            }
        }));

        sess.requestPTY(System.getenv("TERM"), term.getWsCol(), term.getWsRow(), term.getWsXPixel(), term.getWsYPixel(), null);
        if ((command != null) && (!stayLoggedIn)) {
            sess.execCommand(command);
        } else if (stayLoggedIn) {
            sess.startShell();
            PrintWriter stdin = new PrintWriter(sess.getStdin());

            InputStream stdout = sess.getStdout();
            InputStream stderr = sess.getStderr();

            // TODO are we interested in this?
            if (keepAllOutput) {
                stdout = new TeeInputStream(stdout, System.out);
                stderr = new TeeInputStream(stderr, System.err);
            }

            DoubleExpectInputStream exp = new DoubleExpectInputStream(stdout, stderr, 1024);

            if (!exp.waitFor(Pattern.compile("[$#]"))) {
                return;
            }

            if (command != null) {
                stdin.println(command);
                stdin.flush();
            }
        } else {
            // command == null
            sess.startShell();
        }
    }

    public int startInteractiveSession() throws IOException {

        final OutputStream stdin = sess.getStdin();
        final InputStream stdout = sess.getStdout();
        final InputStream stderr = sess.getStderr();

        new Thread(new Runnable() {

            public void run() {
                try {
                    while (!((shouldEnd) && (stdout.available() == 0) &&
                            (stderr.available() == 0))) {

                        try {
                            handleResize(sess);
                        } catch (IOException e) {
                            // TODO
                        }

                        final byte[] b = new byte[1024];


                        boolean readSomething = false;

                        try {
                            if (copyNonBlocking(System.in, stdin, b) > 0) {
                                readSomething = true;
                            }
                            if (copyNonBlocking(stdout, System.out, b) > 0) {
                                readSomething = true;
                            }
                            if (copyNonBlocking(stderr, System.err, b) > 0) {
                                readSomething = true;
                            }
                        } catch (IOException e) {
                            shouldEnd = true;
                        }

                        if (!readSomething) {
                            try {
                                Thread.sleep(10L);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Should not happen, received IOException in available():" + e.getMessage());
                }
            }
        }).start();

        sess.waitForCondition(ChannelCondition.CLOSED, 0);
        shouldEnd = true;
        term.finishConsole();
        return sess.getExitStatus();
    }

    public int doScp(boolean localToRemote, String[] filesToCopy, String targetDirectory) {

        DSSHSCPClient scpClient = new DSSHSCPClient(sess);

        try {

            if (localToRemote) {
                // TODO !!!!!!!!!!!! -- need a way to shell escape this
                sess.execCommand("scp -t -d " + targetDirectory);
                scpClient.put(filesToCopy, targetDirectory);
            
            } else {
                
                StringBuffer command = new StringBuffer("scp -f");

                for (int i = 0; i < filesToCopy.length; i++) {
                    if (filesToCopy[i] == null) {
                        throw new IllegalArgumentException("Cannot accept null filename.");
                    }
                    String tmp = filesToCopy[i].trim();

                    if (tmp.length() == 0) {
                        throw new IllegalArgumentException("Cannot accept empty filename.");
                    }
                    command.append(" " + tmp);
                }
                
                sess.execCommand(command.toString());
                
                
                scpClient.get(filesToCopy, targetDirectory);
            }

        } catch (IOException e) {
            System.err.println("SCP Caught IOException: " + e.getMessage() + "\n" +
                    e.getCause().getMessage());
            return 1;
        }
        return 0;

    }
}
