package org.telegram.tgnet;

//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import org.telegram.telegraph.api.TelegraphObject;
//import org.telegram.telegraph.jsonutilities.NodeDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * This abstract object represents a DOM Node. It can be a String which represents a DOM text node or a NodeElement object.
 */
@JsonDeserialize(using = NodeDeserializer.class)
public abstract class Node implements TelegraphObject {
    public int start;
    public int end;
}