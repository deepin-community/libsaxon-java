package org.w3c.dom;
public interface UserDataHandler {
    static final short NODE_CLONED = 1;
    static final short NODE_IMPORTED = 2;
    static final short NODE_DELETED = 3;
    static final short NODE_RENAMED = 4;
    static final short NODE_ADOPTED = 5;
    void handle(short operation, String key, Object data, Node src, Node dst);
}