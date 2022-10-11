package org.w3c.dom;
public interface DOMConfiguration {
    void setParameter(String name, Object value) throws DOMException;
    Object getParameter(String name) throws DOMException;
    boolean canSetParameter(String name, Object value);
    DOMStringList getParameterNames();
}
