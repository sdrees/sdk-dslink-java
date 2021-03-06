package org.dsa.iot.dslink.handshake;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.dsa.iot.dslink.util.URLInfo;
import org.dsa.iot.dslink.util.http.HttpClient;
import org.dsa.iot.dslink.util.http.HttpResp;
import org.dsa.iot.dslink.util.json.EncodingFormat;
import org.dsa.iot.dslink.util.json.JsonObject;

/**
 * Handshake information retrieved from the server.
 *
 * @author Samuel Grenier
 */
public class RemoteHandshake {

    private final RemoteKey remoteKey;
    private final String wsUri;
    private final String salt;
    private final String path;
    private final EncodingFormat format;

    /**
     * Populates the handshake with data from the server.
     *
     * @param keys Local client keys necessary to create the remote key.
     * @param in   JSON object retrieved from the server.
     */
    public RemoteHandshake(LocalKeys keys, JsonObject in) {
        String tempKey = in.get("tempKey");
        if (tempKey != null) {
            this.remoteKey = RemoteKey.generate(keys, tempKey);
        } else {
            this.remoteKey = null;
        }
        this.wsUri = in.get("wsUri");
        this.salt = in.get("salt");
        this.path = in.get("path");
        this.format = EncodingFormat.toEnum((String) in.get("format"));
    }

    /**
     * @return The remote key.
     */
    public RemoteKey getRemoteKey() {
        return remoteKey;
    }

    /**
     * @return The web socket data endpoint URI for connecting to the server.
     */
    public String getWsUri() {
        return wsUri;
    }

    /**
     * @return The salt used in the handshake data endpoint.
     */
    public String getSalt() {
        return salt;
    }

    /**
     * @return The path that the DSLink will be located at on the broker.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The format to be used when communicating to the endpoint.
     */
    public EncodingFormat getFormat() {
        return format;
    }

    /**
     * Generates a remote handshake by connecting to the authentication
     * endpoint. Once the handshake is complete, a populated handshake
     * is returned. This enables the DSLink to connect to the data
     * endpoint of the server.
     *
     * @param lh  Handshake information
     * @param url URL for the authentication endpoint
     * @return Remote handshake information
     */
    public static RemoteHandshake generate(LocalHandshake lh, URLInfo url) {
        if (url == null) {
            throw new NullPointerException("url");
        }

        HttpClient client = new HttpClient(url);
        String fullPath = url.path + "?dsId=" + lh.getDsId();

        String token = lh.getToken();
        if (token != null) {
            fullPath += "&token=" + token;
        }

        byte[] content = lh.toJson().encode();
        HttpResp resp = client.post(fullPath, content);
        HttpResponseStatus status = resp.getStatus();
        if (status.code() != HttpResponseStatus.OK.code()) {
            throw new RuntimeException("Bad status: " + status);
        }

        JsonObject o = new JsonObject(resp.getBody());
        LocalKeys k = lh.getKeys();
        return new RemoteHandshake(k, o);
    }
}
