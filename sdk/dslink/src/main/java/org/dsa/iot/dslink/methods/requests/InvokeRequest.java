package org.dsa.iot.dslink.methods.requests;

import org.dsa.iot.dslink.methods.Request;
import org.vertx.java.core.json.JsonObject;

/**
 * Used to invoke an action on a node.
 *
 * @author Samuel Grenier
 */
public class InvokeRequest implements Request {

    private final String path;
    private final JsonObject params;
    private boolean waitForStreamClose;

    public InvokeRequest(String path) {
        this(path, null);
    }

    public InvokeRequest(String path, JsonObject params) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        this.params = params;
        this.path = path;
    }

    @Override
    public String getName() {
        return "invoke";
    }

    public String getPath() {
        return path;
    }

    /**
     * The invocation response handler will not be called until the stream
     * for the invocation is closed, if {@code wait} is {@code true}.
     *
     * @param wait Whether to wait or not.
     */
    @SuppressWarnings("unused")
    public void setWaitForStreamClose(boolean wait) {
        this.waitForStreamClose = wait;
    }

    public boolean waitForStreamClose() {
        return waitForStreamClose;
    }

    @Override
    public void addJsonValues(JsonObject out) {
        out.putString("path", path);
        if (params != null) {
            out.putObject("params", params);
        }
    }
}
