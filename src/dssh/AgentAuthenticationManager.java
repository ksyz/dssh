/*
 * AgentAuthenticationManager.java
 *
 * Created on May 13, 2007, 2:50:06 PM
 *
 */
package dssh;

import com.trilead.ssh2.auth.AuthenticationManager;
import com.trilead.ssh2.packets.PacketUserauthFailure;
import com.trilead.ssh2.packets.PacketUserauthRequestPublicKey;
import com.trilead.ssh2.packets.Packets;
import com.trilead.ssh2.packets.TypesWriter;
import com.trilead.ssh2.signature.DSAPublicKey;
import com.trilead.ssh2.signature.DSASHA1Verify;
import com.trilead.ssh2.signature.RSAPrivateKey;
import com.trilead.ssh2.signature.RSAPublicKey;
import com.trilead.ssh2.signature.RSASHA1Verify;
import com.trilead.ssh2.transport.GenericTransportManager;
import dssh.agent.DSSHAgent;
import java.io.IOException;

/**
 *
 * @author juraj
 */
public class AgentAuthenticationManager extends AuthenticationManager {

    private DSSHAgent agent;

    public AgentAuthenticationManager(GenericTransportManager tm, DSSHAgent agent) {
        super(tm);
        this.agent = agent;
    }

    public boolean authenticatePublicKey(String user, int keyId, Object publicKey)
            throws IOException {
        try {
            initialize(user);

            if (methodPossible("publickey") == false) {
                throw new IOException("Authentication method publickey not supported by the server at this stage.");
            }
            if (publicKey instanceof DSAPublicKey) {
                DSAPublicKey pk = (DSAPublicKey) publicKey;
                byte[] pk_enc = DSASHA1Verify.encodeSSHDSAPublicKey(pk);


                TypesWriter tw = new TypesWriter();

                byte[] H = tm.getSessionIdentifier();

                tw.writeString(H, 0, H.length);
                tw.writeByte(Packets.SSH_MSG_USERAUTH_REQUEST);
                tw.writeString(user);
                tw.writeString("ssh-connection");
                tw.writeString("publickey");
                tw.writeBoolean(true);
                tw.writeString("ssh-dss");
                tw.writeString(pk_enc, 0, pk_enc.length);

                byte[] msg = tw.getBytes();

                byte[] ds_enc = agent.generateSignature(msg, keyId);

                PacketUserauthRequestPublicKey ua = new PacketUserauthRequestPublicKey("ssh-connection", user,
                        "ssh-dss", pk_enc, ds_enc);
                tm.sendMessage(ua.getPayload());
            } else if (publicKey instanceof RSAPublicKey) {
                RSAPublicKey pk = (RSAPublicKey) publicKey;

                byte[] pk_enc = RSASHA1Verify.encodeSSHRSAPublicKey(pk);

                TypesWriter tw = new TypesWriter();

                byte[] H = tm.getSessionIdentifier();

                tw.writeString(H, 0, H.length);
                tw.writeByte(Packets.SSH_MSG_USERAUTH_REQUEST);
                tw.writeString(user);
                tw.writeString("ssh-connection");
                tw.writeString("publickey");
                tw.writeBoolean(true);
                tw.writeString("ssh-rsa");
                tw.writeString(pk_enc, 0, pk_enc.length);


                byte[] msg = tw.getBytes();

                byte[] rsa_sig_enc = agent.generateSignature(msg, keyId);

                PacketUserauthRequestPublicKey ua = new PacketUserauthRequestPublicKey("ssh-connection", user,
                        "ssh-rsa", pk_enc, rsa_sig_enc);
                tm.sendMessage(ua.getPayload());
            } else {
                throw new IOException("Unknown private key type returned by the PEM decoder.");
            }

            byte[] ar = getNextMessage();

            if (ar[0] == Packets.SSH_MSG_USERAUTH_SUCCESS) {
                authenticated = true;
                tm.removeMessageHandler(this, 0, 255);
                return true;
            }

            if (ar[0] == Packets.SSH_MSG_USERAUTH_FAILURE) {
                PacketUserauthFailure puf = new PacketUserauthFailure(ar, 0, ar.length);

                remainingMethods = puf.getAuthThatCanContinue();
                isPartialSuccess = puf.isPartialSuccess();

                return false;
            }

            throw new IOException("Unexpected SSH message (type " + ar[0] + ")");

        } catch (IOException e) {
            tm.close(e, false);
            throw (IOException) new IOException("Publickey authentication failed.").initCause(e);
        }
    }
}