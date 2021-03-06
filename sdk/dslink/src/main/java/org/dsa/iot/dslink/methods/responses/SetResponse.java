package org.dsa.iot.dslink.methods.responses;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.methods.Response;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodePair;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.node.value.ValueUtils;
import org.dsa.iot.dslink.util.json.JsonObject;

/**
 * Handles a set response
 *
 * @author Samuel Grenier
 */
public class SetResponse extends Response {

    private final int rid;
    private final DSLink link;
    private final String path;

    public SetResponse(int rid, DSLink link, String path) {
        this.rid = rid;
        this.link = link;
        this.path = path;
    }

    @Override
    public int getRid() {
        return rid;
    }

    @Override
    public void populate(JsonObject in) {
    }

    @Override
    public JsonObject getJsonResponse(JsonObject in) {
        updateNode(in);

        JsonObject obj = new JsonObject();
        obj.put("rid", rid);
        obj.put("stream", StreamState.CLOSED.getJsonName());
        return obj;
    }

    @Override
    public JsonObject getCloseResponse() {
        return null;
    }

    private void updateNode(JsonObject in) {
        final Value value = ValueUtils.toValue(in.get("value"));
        NodePair pair = link.getNodeManager().getNode(path, false, false);
        Node node = pair.getNode();
        if (node == null) {
            DSLinkHandler handler = link.getLinkHandler();
            handler.onSetFail(path, value);
            return;
        }

        String ref = pair.getReference();
        if (ref != null) {
            if (ref.startsWith("@")) {
                ref = ref.substring(1);
                node.setAttribute(ref, value);
            } else {
                throw new RuntimeException("Unable to set reference: " + ref);
            }
        } else {
            Writable writable = node.getWritable();
            if (writable == null || writable == Writable.NEVER) {
                throw new RuntimeException("Not writable");
            }

            Value current = node.getValue();
            if (!node.getValueType().compare(ValueType.DYNAMIC)) {
                checkValue(current, value);
            }
            node.setValue(value, true);
        }
    }

    private void checkValue(Value current, Value other) {
        if (current == null) {
            return;
        }

        ValueType currType = current.getType();
        ValueType otherType = other.getType();
        if (currType != otherType) {
            String expected = currType.toJsonString();
            String got = otherType.toJsonString();
            String error = "Type mismatch ";
            error += "(got: " + got + ", expected: " + expected + ")";
            throw new RuntimeException(error);
        }
    }
}
