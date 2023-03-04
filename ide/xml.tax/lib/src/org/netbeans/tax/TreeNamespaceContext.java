/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.tax;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Context allows to determive namespace URI by its context prefix.
 * <p>
 * Default namespace prefix is "".
 * <p>
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNamespaceContext {

    /**
     * Namespace context is defined by element nesting
     * so we seach for parent context by quering parent element namespace context.
     */
    private TreeElement element;

    /**
     * It hold only namespaces defined at peer element.
     * It avoids problems with the following scenario:
     * <pre>
     *   &lt;ns1:root xmlns:ns1="original ns1">
     *      &lt;ns2:child xmlns:ns2="original ns2">
     *        &lt;ns1:kid xmlns:ns1="context redefined ns1 binding">
     * </pre>
     */
    private static Map definedNS = new HashMap ();
    
    static {
        TreeNamespace namespace;
        
        namespace = TreeNamespace.XML_NAMESPACE;
        definedNS.put (namespace.getPrefix (), namespace);
        
        namespace = TreeNamespace.XMLNS_NAMESPACE;
        definedNS.put (namespace.getPrefix (), namespace);
    }
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeNamespaceContext.
     * Only TreeElement can do so.
     */
    protected TreeNamespaceContext (TreeElement element) {
        this.element = element;
    }
    
    
    /**
     * Traverse over parents and parse their attributes until given prefix is resolved.
     * @param prefix namespace prefix ("" default)
     * @return null it is not defined
     */
    public String getURI (String prefix) {
        
        // well known prefixes are in this map
        
        TreeNamespace ns = (TreeNamespace)definedNS.get (prefix);
        if (ns != null) {
            return ns.getURI ();
        }
        
        // look for attributes that defines namespaces
        // take cate to default namespace definition attribute
        
        TreeNamedObjectMap attrs = element.getAttributes ();
        if (attrs != null) {
            Iterator it = attrs.iterator ();
            while (it.hasNext ()) {
                TreeAttribute next = (TreeAttribute) it.next ();
                TreeName name = next.getTreeName ();
                if ("xmlns".equals (name.getPrefix ())) { // NOI18N
                    if (prefix.equals (name.getName ())) {
                        return next.getValue ();
                    }
                } else if ("xmlns".equals (name.getQualifiedName ())) { // NOI18N
                    return next.getValue ();
                }
            }
        }
        
        // try my parent
        
        TreeParentNode parentNode = element.getParentNode ();
        if ( parentNode instanceof TreeElement ) {
            TreeElement parentElement = (TreeElement)parentNode;
            if (parentElement != null) {
                return parentElement.getNamespaceContext ().getURI (prefix);
            }
        }
        return null;
    }
    
}
