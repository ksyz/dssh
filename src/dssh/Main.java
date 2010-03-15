/*
 * Main.java
 *
 * Created on April 12, 2007, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package dssh;

import com.trilead.ssh2.KnownHosts;
import dssh.authenticators.StackedAuthenticator;
import dssh.authenticators.KeyboardInteractiveAuthenticator;
import dssh.authenticators.AgentAuthenticator;
import dssh.exceptions.UnauthenticatedSSHConnectionException;
import gnu.getopt.Getopt;
import java.io.File;
import java.io.IOException;
import dssh.agent.DSSHAgent;
import dssh.authenticators.password.AgentPasswordAuthenticator;
import dssh.authenticators.password.NullPasswordAuthenticator;
import dssh.authenticators.password.PasswordAuthenticator;
import dssh.exceptions.ConnectionAdministrativelyProhibitedException;
import dssh.scripting.BshScriptLoader;
import dssh.scripting.ScriptLoader;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class Main {

    public static String getVersion() {
        return "1.1k";
    }

    public static long getBuild() {
        return 1022;
    }

    public static void printHelp() {
        final String help = "dssh (c) Juraj Bednar <juraj.bednar@digmia.com>\n\n"
                + "Usage: dssh [-p port] [-h] [-l username] [-i identity] [-g script] [-k known_hosts] \\\n"
                + "            [-I] [-O] [--] [user@]host [command]\n"
                + "  where:\n"
                + "  -p port          Specify port to connect to (defaults to 22)\n"
                + "  -h               Print this help\n"
                + "  -l username      Login with username (defaults to USER environment variable)\n"
                + "  -i identity      Load private key from file identity\n"
                + "  -g script        Execute script from file script\n"
                + "  -k known_hosts   Specify known_hosts file path (default $HOME/.dssh/known_hosts)\n"
                + "  -a [host:]port   Connect to DSSH Agent on host (or localhost if not specified)\n"
                + "                   and specified port\n" + "  -L [bindaddress]:port:host:hostport\n"
                + "                   Opens a local port bound to bindaddress (or all interfaces if not\n"
                + "                   specified), which is tunneled through SSH connection and connected\n"
                + "                   to host, port hostport\n" + "  -R [bindaddress]:port:host:hostport\n"
                + "                   Opens a remote port bound to bindaddress (if allowed by server),\n"
                + "                   which is tunneled through SSH connection and connected\n"
                + "                   to host, port hostport on our side\n"
                + "  -I               Start interactive session and stay logged in after executing command\n"
                + "  -O               Keep all output after logging in (affects interactive sessions)\n"
                + "  -v               Be a little bit more verbose\n"
                + "  -V               Print version information and exit\n"
                + "  -B               Batch mode: do not ask anything (beware of man in the middle attacks!)\n"
                + "                   This also disables keyboard interactive password authenticator\n"
                + "  -s               Work in SCP mode. In this mode, there are no hostname and commmand arguments\n"
                + "                   and DSSH uses scp(1) semantics for copying files\n"
                + "\n"
                + "Example:\n" + "   dssh -p 2222 -i ~/.ssh/identity dorka@bratislava.digmia.com ls /tmp\n"
                + "   \n" + "   Login to bratislava.digmia.com port 2222 as user dorka with private key from\n"
                + "   file ~/.ssh/identity and execute command ls /tmp\n";

        System.out.println(help);
    }

    public static void main(String[] args) {

        String username = System.getenv("USER");
        String hostname = null;
        String agentString = System.getenv("DSSHAGENT");
        String agentHost = null;
        int agentPort;
        DSSHAgent agent = null;
        String knownHostsFile = "known_hosts";
        boolean interactive = true;
        boolean scpMode = false;
        boolean scpLocalToRemote = false;
        String[] scpFilesToCopy = {};
        String scpTargetDirectory = "";

        int port = 22;
        String command = null;
        ArrayList<String> privatekeyfiles = new ArrayList<String>();
        String scriptName = "dssh.bsh";
        boolean isInteractive = false;
        boolean stayLoggedIn = false;
        boolean keepAllOutput = false;
        boolean verbose = false;
        ArrayList<String[]> localPortForwards = new ArrayList<String[]>();
        ArrayList<String[]> remotePortForwards = new ArrayList<String[]>();

        if (System.getenv("DSSHSCRIPTNAME") != null) {
            scriptName = System.getenv("DSSHSCRIPTNAME");
        } else if (System.getenv("HOME") != null) {
            scriptName = System.getenv("HOME") + File.separator + ".dssh" + File.separator + scriptName;
        }
        if (System.getenv("HOME") != null) {
            knownHostsFile = System.getenv("HOME") + File.separator + ".dssh" + File.separator + "known_hosts";
        }
        Getopt g = new Getopt("dssh", args, "p:l:hi:g:a:L:R:k:IOvVBs");
        int c;
        String arg;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'p':
                    port = new Integer(g.getOptarg());
                    break;
                case 'l':
                    username = g.getOptarg();
                    break;
                case 'g':
                    scriptName = g.getOptarg();
                    break;
                case 'k':
                    knownHostsFile = g.getOptarg();
                    break;
                case 'i':
                    privatekeyfiles.add(g.getOptarg());
                    break;
                case 'L': {
                    String[] fwd = g.getOptarg().split(":");
                    if ((fwd.length != 3) && (fwd.length != 4)) {
                        System.err.println("Invalid -L value. " + "Use -h for details.");
                        System.exit(1);
                    }
                    localPortForwards.add(fwd);
                    break;
                }
                case 'R': {
                    String[] fwd = g.getOptarg().split(":");
                    if ((fwd.length != 3) && (fwd.length != 4)) {
                        System.err.println("Invalid -R value. " + "Use -h for details.");
                        System.exit(1);
                    }
                    remotePortForwards.add(fwd);
                    break;
                }
                case 'h':
                    printHelp();
                    System.exit(1);
                    break;
                case 'a':
                    agentString = g.getOptarg();
                    break;
                case 'I':
                    stayLoggedIn = true;
                    break;
                case 'O':
                    keepAllOutput = true;
                    break;
                case 'v':
                    verbose = true;
                    break;
                case 'V':
                    System.err.println("DSSH version " + getVersion() + " build " + getBuild());
                    System.exit(1);
                    break;
                case 'B':
                    interactive = false;
                    break;
                case 's':
                    scpMode = true;
                    break;
                case '?':
                    printHelp();
                    System.exit(1);
                    break;
                //
            }
        }

        if (g.getOptind() == args.length) {
            System.err.println("Error: hostname is an required argument. Use dssh -h for help.");
            System.exit(1);
        }


        if (scpMode) {
            // in SCP mode, rest are files to copy from and to
            Pattern scpPattern = Pattern.compile("((.*)@)?(.+):(.*)");
            Matcher m = scpPattern.matcher(args[g.getOptind()]);
            if (m.matches()) {
                // this is remote to local copy
                scpLocalToRemote = false;
                if ((args.length - g.getOptind()) != 2) {
                    System.err.println("When copying from remote directory, we only support one remote file name.");
                    // to implement many files copying, only argument parsing needs
                    // to be fixed, i.e. constructing of scpFilesToCopy below, everything
                    // else is set up
                    System.exit(1);
                }
                scpFilesToCopy = new String[]{m.group(4)};
                scpTargetDirectory = args[args.length - 1];

            } else {
                m = scpPattern.matcher(args[args.length - 1]);
                if (!m.matches()) {
                    System.err.println("Either first or last file must be remote. For local copies,\n"
                            + "please use standard utilities.");
                    System.exit(1);
                }
                scpLocalToRemote = true;
                scpTargetDirectory = m.group(4);
                if (scpTargetDirectory == null) {
                    scpTargetDirectory = ".";
                }

                scpFilesToCopy = new String[args.length - g.getOptind() - 1];
                for (int i = 0; i < (args.length - g.getOptind() - 1); i++) {
                    scpFilesToCopy[i] = args[i + g.getOptind()];
                }

            }

            scpTargetDirectory = scpTargetDirectory.trim();
            scpTargetDirectory = (scpTargetDirectory.length() > 0) ? scpTargetDirectory : ".";

            if (m.group(2) != null) {
                username = m.group(2);
            }
            hostname = m.group(3);

        } else {
            // parse hostname and command arguments
            hostname = args[g.getOptind()];
            final Matcher m = Pattern.compile("(.*)@(.*)").matcher(hostname);
            if (m.matches()) {
                username = m.group(1);
                hostname = m.group(2);
            }


            // rest of arguments are commands for execution
            StringBuffer b = new StringBuffer("");
            for (int i = g.getOptind() + 1; i < args.length; i++) {
                if (i > g.getOptind() + 1) {
                    b.append(' ');
                }
                b.append(args[i]);
            }

            if (b.length() > 0) {
                command = b.toString();
            }
        }

        if (agentString != null) {
            String[] p = agentString.split(":", 2);
            if (p.length == 2) {
                agentHost = p[0];
                agentPort = new Integer(p[1]);
            } else {
                agentHost = null;
                agentPort = new Integer(agentString);
            }
            try {
                Registry reg = LocateRegistry.getRegistry(agentHost, agentPort, new SslRMIClientSocketFactory());
                agent = (DSSHAgent) reg.lookup("DSSHAgentServer");
            } catch (RemoteException e) {
                System.err.println("Warning: cannot connect to agent.\n" + e);
                agent = null;
            } catch (NotBoundException e) {
                System.err.println("Warning: cannot connect to agent.\n" + e);
                agent = null;
            }
        }

        // read known hosts file
        try {
            File f = new File(knownHostsFile);
            if (!f.exists()) {
                System.err.println("Warning: Known hosts file \"" + knownHostsFile + "\" does not exist. Creating.");
                f.createNewFile();
            }
            KnownHosts kh = KnownHostsStore.getKnownHostsInstance(f);
        } catch (IOException e) {
            System.err.println("Error loading known hosts file \"" + knownHostsFile + "\". Error is: " + e.getMessage());
            System.exit(1);
        }

        KnownHostsStore.setInteractive(interactive);

        // create default authenticator
        KeyboardInteractiveAuthenticator kbdauth = new KeyboardInteractiveAuthenticator(privatekeyfiles);
        kbdauth.setGlobalAllowInteractivity(interactive);
        SSHAuthenticator auth = kbdauth;
        SSHConnection sshconn = null;

        if (agent != null) {
            auth = new StackedAuthenticator(new AgentAuthenticator(agent), auth);
        }

        SSHConnectionCreator creator = null;

        // execute a script
        ScriptLoader scriptLoader = new BshScriptLoader();


        try {
            creator = (SSHConnectionCreator) scriptLoader.getScriptInstance(scriptName);
            creator.setVerbose(verbose);
            sshconn = creator.getAuthenticatedSSHConnection(username, hostname, port, null, auth);
        } catch (IOException e) {
            if (e.getCause() instanceof java.net.ConnectException) {
                System.err.println("dssh: " + e.getCause().getMessage());
            } else {
                System.err.println("dssh: IOException caught: " + e.getMessage());
            }
            System.exit(254);
        } catch (UnauthenticatedSSHConnectionException e) {
            System.err.println("dssh: Not authenticated");
            System.exit(254);
        } catch (ConnectionAdministrativelyProhibitedException e) {
            System.err.println("dssh: Server returned: connection administratively prohibited");
            System.err.println("(probably means connection refused or host not found error)");
            System.exit(254);
        }


        // default action (no connection provided by user script)
        if (sshconn == null) {
            sshconn = new SSHConnection(hostname, port, null, auth);

            try {
                sshconn.connect();
                if (!sshconn.authenticate(username, true, verbose)) {
                    System.err.println("Unable to authenticate to " + hostname + ":" + port);
                    System.exit(1);
                }
            } catch (IOException e) {
                if (e.getCause() instanceof java.net.ConnectException) {
                    System.err.println("dssh: " + e.getCause().getMessage());
                } else {
                    System.err.println("dssh: IOException caught: " + e.getMessage());
                }
            } catch (UnauthenticatedSSHConnectionException e) {
                System.err.println("dssh: Not authenticated");
                System.exit(254);
            } catch (ConnectionAdministrativelyProhibitedException e) {
                System.err.println("dssh: Server returned: connection administratively prohibited");
                System.err.println("(probably means connection refused or host not found error)");
                System.exit(254);
            }
        }

        if (sshconn == null) {
            System.err.println("dssh: No open connection");
            System.exit(254);
        }

        if (!sshconn.isAuthenticated()) {
            System.err.println("dssh: Not authenticated");
            System.exit(254);
        }

        // do port forwards
        for (String[] fwd : localPortForwards) {
            int start = 0;
            String fwd_hostname = null;
            if (fwd.length == 4) {
                fwd_hostname = fwd[0];
                start++;
            }

            try {
                InetSocketAddress addr;
                if (fwd_hostname != null) {
                    addr = new InetSocketAddress(fwd_hostname, new Integer(fwd[start]));
                } else {
                    addr = new InetSocketAddress(new Integer(fwd[start]));
                }
                sshconn.getConnection().createLocalPortForwarder(addr, fwd[start + 1], new Integer(fwd[start + 2]));
            } catch (IOException e) {
                System.err.println("dssh: IOException caught while creating port forwarding: " + e.getMessage());
            }
        }

        for (String[] fwd : remotePortForwards) {
            int start = 0;
            String fwd_hostname = "";
            if (fwd.length == 4) {
                fwd_hostname = fwd[0];
                start++;
            }

            try {
                sshconn.getConnection().requestRemotePortForwarding(fwd_hostname, new Integer(fwd[start]), fwd[start + 1], new Integer(fwd[start + 2]));
            } catch (IOException e) {
                System.err.println("dssh: IOException caught while creating port forwarding: " + e.getMessage());
            }
        }

        // create password authenticator
        PasswordAuthenticator pauth;
        if (agent == null) {
            pauth = new NullPasswordAuthenticator();
        } else {
            pauth = new AgentPasswordAuthenticator(agent);
        }

        int returnValue = 0;
        // create interactive session
        try {
            InteractiveSession isess = null;
            if (creator != null) {
                isess = creator.getInteractiveSession(sshconn, username, pauth, hostname);
            } else {
                sshconn.openSession();
                isess = new InteractiveSession(sshconn.getSession());
            }


            if (scpMode) {
                returnValue = isess.doScp(scpLocalToRemote, scpFilesToCopy, scpTargetDirectory);
            } else {
                isess.initializeInteractiveSession(command, stayLoggedIn, keepAllOutput, verbose);
                returnValue = isess.startInteractiveSession();
            }
            sshconn.disconnect();
        } catch (IOException e) {
            System.err.println("dssh: Exception caught: " + e.getMessage());
            returnValue = 255;
        }
        System.exit(returnValue);
    }
}
