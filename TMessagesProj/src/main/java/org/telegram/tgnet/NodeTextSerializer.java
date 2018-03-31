package org.telegram.tgnet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Ruben Bermudez
 * @version 1.0
 */
public class NodeTextSerializer extends StdSerializer<NodeText> {
    public NodeTextSerializer() {
        super(NodeText.class);
    }

    @Override
    public void serialize(NodeText value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getContent());
    }
}
