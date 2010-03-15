/*
 * KeyboardInteractiveAuthenticator.java
 *
 */
package dssh.authenticators;

import dssh.*;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.crypto.PEMDecoder;
import com.trilead.ssh2.crypto.PEMStructure;
import com.trilead.ssh2.crypto.PublicKeyDecoder;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author juraj
 */
public class KeyboardInteractiveAuthenticator implements SSHAuthenticator {

    private ArrayList<String> privatekeyfiles;
    private boolean globalAllowInteractivity = true;

    /** Creates a new instance of KeyboardInteractiveAuthenticator */
    public KeyboardInteractiveAuthenticator(ArrayList<String> privatekeyfiles) {
        this.privatekeyfiles = privatekeyfiles;
    }

    public boolean authenticate(final SSHConnection c, final String username, boolean allowInteractivity, boolean verbose) {
        if (!globalAllowInteractivity) {
            allowInteractivity = false;
        }
        Connection conn = c.getConnection();
        boolean authenticated = false;
        try {

            for (String privatekeyfile : privatekeyfiles) {

                if ((privatekeyfile != null) &&
                        (!privatekeyfile.equals("")) && (conn.isAuthMethodAvailable(username, "publickey"))) {

                    boolean shouldTryPublicKey = true;
                    // if we have public key, try it first
                    final File publicKeyFile = new File(privatekeyfile + ".pub");
                    if (publicKeyFile.exists()) {
                        Object key = PublicKeyDecoder.parseKey(publicKeyFile);
                        if ((key != null) && (!conn.tryPublicKey(username, key))) {
                            shouldTryPublicKey = false;
                        }
                    }
                    if (shouldTryPublicKey) {
                        File keyfile = new File(privatekeyfile);

                        char[] buff = new char[256];

                        CharArrayWriter cw = new CharArrayWriter();

                        FileReader fr = new FileReader(keyfile);

                        while (true) {
                            int len = fr.read(buff);
                            if (len < 0) {
                                break;
                            }
                            cw.write(buff, 0, len);
                        }

                        fr.close();

                        PEMStructure ps = PEMDecoder.parsePEM(cw.toCharArray());
                        String passphrase = null;
                        if (PEMDecoder.isPEMEncrypted(ps)) {
                            if (!allowInteractivity) {
                                System.err.println("Password required to decrypt private key " + privatekeyfile +
                                        ", but we are in batch mode, skipping key.");
                                continue;
                            } else {
                                passphrase = getPassword("Password required to decrypt private key " + privatekeyfile + ": ");
                            }
                        }
                        authenticated = conn.authenticateWithPublicKey(username, cw.toCharArray(), passphrase);
                        if (authenticated) {
                            if (verbose)
                                System.err.println("Authenticated using public key \"" + privatekeyfile + "\"");
                           
                            break;
                        }
                    }


                }
            }

            if (allowInteractivity) {
                if ((!authenticated) && (conn.isAuthMethodAvailable(username, "password"))) {
                    String password = getPassword("Password for " + username + "@" + c.getHost() + ":" + c.getPort() + ": ");
                    return conn.authenticateWithPassword(username, password);
                }

                if ((!authenticated) && (conn.isAuthMethodAvailable(username, "keyboard-interactive"))) {
                    return c.getConnection().authenticateWithKeyboardInteractive(username, new InteractiveCallback() {

                        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws Exception {
                            if (numPrompts == 0) {
                                return new String[0];
                            }
                            String[] responses = new String[numPrompts];
                            System.err.print(username + "@" + c.getHost() + ":" + c.getPort() + ": ");
                            if ((name != null) && (name.length() > 0)) {
                                System.err.println(name);
                            }
                            if ((instruction != null) && (instruction.length() > 0)) {
                                System.err.println(instruction);
                            }
                            for (int i = 0; i < numPrompts; i++) {
                                if (echo[i]) {
                                    System.err.print(prompt[i]);
                                    responses[i] = System.console().readLine();
                                } else {
                                    responses[i] = KeyboardInteractiveAuthenticator.getPassword(prompt[i]);
                                }
                            }
                            return responses;
                        }
                    });
                }
            }

        } catch (IOException e) {
            System.err.println("dssh: " + e.getMessage());
            return false;
        }
        return authenticated;

    }

    public boolean isGlobalAllowInteractivity() {
        return globalAllowInteractivity;
    }

    public void setGlobalAllowInteractivity(boolean globalAllowInteractivity) {
        this.globalAllowInteractivity = globalAllowInteractivity;
    }

    public static String getPassword(String prompt) {
        System.out.print(prompt);
        if (System.console() != null) {
            return new String(System.console().readPassword());
        } else {
            System.err.print("This system has no console, can't read password");
            System.exit(1);
        }

        return null;


    }
}