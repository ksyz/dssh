/*
 * AgentClientMain.java
 *
 * Created on May 13, 2007, 4:42:24 PM
 *
 */
package dssh.agent.client;

import com.trilead.ssh2.crypto.PEMDecoder;
import com.trilead.ssh2.crypto.PEMStructure;
import dssh.agent.DSSHAgent;
import dssh.authenticators.KeyboardInteractiveAuthenticator;
import gnu.getopt.Getopt;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 *
 * @author juraj
 */
public class AgentClientMain {

    public static void printHelp() {
        final String help =
                "dssh-add (c) Juraj Bednar <juraj.bednar@digmia.com>\n\n" +
                "Usage: dssh-add [-q] [-i identity] [-s] [-l] -[f passwords|keys|all] [-a [host:]port]\n" +
                "       dssh-add -h\n" +
                "  where:\n" +
                "  -h               Print this help\n" +
                "  -s               Show private keys on server (not implemented)\n" +
                "  -l               Load passwords from stdin\n" +
                "  -i identity      Load private key from file identity and upload it to agent\n" +
                "  -r               Load private key passphrase(s) from stdin (when using -i)\n" +
                "  -f what          Remove \"what\" from agent. \"what\" can be \"passwords\",\n" +
                "                   \"keys\" or \"all\".\n" +
                "  -a [host:]port   Connect to DSSH Agent on host \"host\" (or localhost if not\n" +
                "                   specified) and port \"port\"\n" +
                "  -q               Tell agent to quit immediately\n" +
                "  -g hostname;service;username\n" +
                "                   Get password for specified hostname, service and username\n" +
                "                   Exits with 2, if there is no such value. \n" +
                "  -n               No newline when printing password with -g " +
                "\n";

        System.out.println(help);
    }

    public static void main(String[] args) {

        String agentString = System.getenv("DSSHAGENT");
        String agentHost = null;
        int agentPort;
        DSSHAgent agent = null;

        boolean flushKeys = false;
        boolean flushPasswords = false;
        boolean doQuit = false;
        boolean readFromStdin = false;
        boolean doGet = false;
        String getHost = null;
        String getService = null;
        String getUsername = null;
        boolean getNewLine = true;

        // TODO: implement this
        boolean doList = false;

        ArrayList<String> privatekeyfiles = new ArrayList<String>();

        Getopt g = new Getopt("dssh", args, "lshi:a:f:g:qrn");
        int c;
        String arg;
        boolean doLoad = false;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'r':
                    readFromStdin = true;
                    break;
                case 's':
                    doList = true;
                    break;
                case 'q':
                    doQuit = true;
                    break;
                case 'n':
                    getNewLine = false;
                    break;
                case 'f':
                    if (g.getOptarg().equalsIgnoreCase("passwords")) {
                        flushPasswords = true;
                    }
                    if (g.getOptarg().equalsIgnoreCase("keys")) {
                        flushKeys = true;
                    }
                    if (g.getOptarg().equalsIgnoreCase("all")) {
                        flushPasswords = true;
                        flushKeys = true;
                    }
                    break;
                case 'g':
                     {
                        if (g.getOptarg() == null) {
                            System.err.println("-g parameter required");
                            System.exit(1);
                        }
                        String[] params = g.getOptarg().split(";");
                        if (params.length != 3) {
                            System.err.println("Argument to -g must be in format: hostname;service;password");
                            System.exit(1);
                        }
                        getHost = params[0];
                        getService = params[1];
                        getUsername = params[2];
                        doGet = true;
                    }
                    break;
                case 'i':
                    privatekeyfiles.add(g.getOptarg());
                    break;
                case 'l':
                    doLoad = true;
                    break;
                case 'h':
                    printHelp();
                    System.exit(0);
                    break;
                case 'a':
                    agentString = g.getOptarg();
                    break;
                case '?':
                    printHelp();
                    System.exit(1);
                    break;
                //

            }
        }

        if (agentString == null) {
            System.err.println("Agent information is required. Use -h for help.");
            System.exit(1);
        }

        String[] p = agentString.split(":", 2);
        if (p.length == 2) {
            agentHost = p[0];
            agentPort = new Integer(p[1]);
        } else {
            agentHost = null;
            agentPort = new Integer(agentString);
        }
        try {
            Registry reg = LocateRegistry.getRegistry(agentHost,
                    agentPort, new SslRMIClientSocketFactory());
            agent = (DSSHAgent) reg.lookup("DSSHAgentServer");
        } catch (RemoteException e) {
            System.err.println("Warning: cannot connect to agent.\n" + e);
            agent = null;
        } catch (NotBoundException e) {
            System.err.println("Warning: cannot connect to agent.\n" + e);
            agent = null;
        }

        if (agent == null) {
            System.err.println("Unable to connect to agent. Is " + agentString + " correct?");
            System.exit(1);
        }

        if (doQuit) {
            try {
                agent.shutdownNow();
                return;
            } catch (RemoteException e) {
                // this is ok, this does unclean shutdown
                return;
            }
        }
        try {
            if (flushKeys) {
                agent.flushPrivateKeys();
            }
            if (flushPasswords) {
                agent.flushPasswordData();
            }
        } catch (RemoteException e) {
            System.err.println("dssh-add: " + e.getMessage());
            e.printStackTrace();
        }

        // load private key files
        try {

            for (String privatekeyfile : privatekeyfiles) {

                if ((privatekeyfile != null) && (!privatekeyfile.equals(""))) {

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
                        if (readFromStdin) {
                            passphrase = new BufferedReader(
                                    new InputStreamReader(System.in)).readLine();
                        } else {
                            passphrase = KeyboardInteractiveAuthenticator.getPassword("Password required to decrypt private key " + privatekeyfile + ": ");
                        }
                    }

                    Object key = PEMDecoder.decode(ps, passphrase);
                    agent.storePrivateKey(key);
                }
            }


        } catch (IOException e) {
            System.err.println("dssh-add: " + e.getMessage());
        }

        // load passwords from stdin
        if (doLoad) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] buffer = new byte[2048];
            int size;
            try {
                while ((size = System.in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
                agent.uploadPasswordData(out.toByteArray());
            } catch (IOException e) {
                System.err.println("dssh-add: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (doGet) {
            try {
                String pass = agent.getPassword(getUsername, getHost, getService);
                if ((pass == null) || (pass.length() == 0)) {
                    System.exit(2);
                } else {
                    if (getNewLine) {
                        System.out.println(pass);
                    } else {
                        System.out.print(pass);
                    }
                }
            } catch (RemoteException e) {
                System.err.println("dssh-add: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
