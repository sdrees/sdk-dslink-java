package org.dsa.iot.dslink.connection;

import io.netty.util.CharsetUtil;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.dsa.iot.dslink.handshake.LocalHandshake;
import org.dsa.iot.dslink.handshake.RemoteHandshake;
import org.dsa.iot.dslink.util.URLInfo;
import org.dsa.iot.dslink.util.UrlBase64;
import org.dsa.iot.dslink.util.json.EncodingFormat;

/**
 * Common interface for handling remote endpoints.
 *
 * @author Samuel Grenier
 */
public abstract class RemoteEndpoint extends NetworkHandlers implements NetworkClient {

    private LocalHandshake localHandshake;
    private RemoteHandshake remoteHandshake;
    private URLInfo endpoint;

    public abstract void start();

    /**
     * @param handshake Local handshake information to set.
     */
    public void setLocalHandshake(LocalHandshake handshake) {
        this.localHandshake = handshake;
    }

    /**
     * @return Local handshake information
     */
    public LocalHandshake getLocalHandshake() {
        return localHandshake;
    }

    /**
     * @param handshake Remote handshake set after the authentication to the
     *                  auth endpoint was successful.
     */
    public void setRemoteHandshake(RemoteHandshake handshake) {
        this.remoteHandshake = handshake;
    }

    /**
     * @return Remote handshake.
     */
    public RemoteHandshake getRemoteHandshake() {
        return remoteHandshake;
    }

    /**
     * @param endpoint Handshake URL endpoint
     */
    public void setEndpoint(URLInfo endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return Handshake URL endpoint
     */
    public URLInfo getEndpoint() {
        return endpoint;
    }

    public EncodingFormat getFormat() {
        return remoteHandshake.getFormat();
    }

    public String getUri() {
        RemoteHandshake handshake = getRemoteHandshake();
        String uri = handshake.getWsUri() + "?auth=";
        String s = handshake.getSalt();
        if (s != null) {
            byte[] salt = handshake.getSalt().getBytes(CharsetUtil.UTF_8);
            byte[] sharedSecret = handshake.getRemoteKey().getSharedSecret();

            byte[] bytes = new byte[salt.length + sharedSecret.length];
            System.arraycopy(salt, 0, bytes, 0, salt.length);
            System.arraycopy(sharedSecret, 0, bytes, salt.length, sharedSecret.length);

            SHA256.Digest sha = new SHA256.Digest();
            byte[] digested = sha.digest(bytes);
            uri += UrlBase64.encode(digested);
        } else {
            // Fake auth parameter
            uri += "_";
        }

        String token = getLocalHandshake().getToken();
        if (token != null) {
            uri += "&token=" + token;
        }

        uri += "&format=" + handshake.getFormat().toJson();
        uri += "&dsId=" + getLocalHandshake().getDsId();
        return uri;
    }
}
