/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.spi.dom.ROException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Read-only implementation of NamedNodeMap delegating to a Java <code>Map</code>.
 * The underlaying map must use {@link #createKey} as its keys. Also keeps
 * fidelity of the attribute order.
 *
 * @author  Ayub Khan
 */
public final class NamedNodeMapImpl implements NamedNodeMap {
    
    private List<Attribute> attributes;
    
    /** Read-only empty map. */
    public static final NamedNodeMap EMPTY = 
            new NamedNodeMapImpl(new ArrayList(0));
    
    /**
     * Creates new NamedNodeMapImpl
     * @param attributes a map to delegate to. It must not be modified after this contructor call!
     */
    public NamedNodeMapImpl(List<Attribute> attributes) {
        if (attributes == null) throw new NullPointerException();
        this.attributes = new ArrayList(attributes);
    }
    
    public int getLength() {
        return attributes.size();
    }
    
    public org.w3c.dom.Node removeNamedItem(String str) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getNamedItemNS(String uri, String local) {
        String key = (String)createKey(uri, local);
        if(key == null) return null;
        return getNode(key);
    }
    
    public org.w3c.dom.Node item(int param) {
        if(param < attributes.size())
            return (org.w3c.dom.Node) attributes.get(param);
        return null;
    }
    
    public org.w3c.dom.Node getNamedItem(String str) {
        String key = (String)createKey(str);
        if(key == null) return null;
        return getNode(key);
    }
    
    public org.w3c.dom.Node removeNamedItemNS(String str, String str1) 
    throws org.w3c.dom.DOMException {
        throw new ROException();
    }
        
    private Node getNode(String key) {
        assert(key != null);        
        for(Attribute attr: attributes) {            
            if(key.equals(attr.getName())) {
                return attr;
            }
        }
        return null;
    }
    
    /**
     * Create proper key for map entry
     */
    public static Object createKey(String qname) {
        return qname;
    }
    
    /**
     * Create proper key for map entry
     */
    public static Object createKey(String uri, String local) {
        return uri + ":" + local;                                               // NOI18N
    }
    
}
