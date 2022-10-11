package org.w3c.dom;
public interface TypeInfo {
    static int DERIVATION_EXTENSION = 2;
    static int DERIVATION_LIST = 8;
    static int DERIVATION_RESTRICTION = 1;
    static int DERIVATION_UNION = 4;
    String getTypeName();
    String getTypeNamespace();
    boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod);
}
