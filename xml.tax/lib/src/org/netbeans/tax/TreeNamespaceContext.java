/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
