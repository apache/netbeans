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

package org.netbeans.modules.xml.spi.dom;

import org.w3c.dom.*;
import java.util.*;

/**
 * Read-only implementation of NamedNodeMap delegating to a Java <code>Map</code>.
 * The underlaying map must use {@link #createKey} as its keys.
 *
 * @author  Petr Kuzel
 */
public final class NamedNodeMapImpl implements NamedNodeMap {

    private final Map peer;

    /** Read-only empty map. */
    public static final NamedNodeMap EMPTY = new NamedNodeMapImpl(new HashMap(0));

    /**
     * Creates new NamedNodeMapImpl
     * @param peer a map to delegate to. It must not be modified after this contructor call!
     */
    public NamedNodeMapImpl(Map peer) {
        if (peer == null) throw new NullPointerException();
        this.peer = peer;
    }

    public int getLength() {
        return peer.size();
    }
    
    public org.w3c.dom.Node removeNamedItem(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getNamedItemNS(String uri, String local) {
        return (Node) peer.get(createKey(uri, local));
    }
    
    public org.w3c.dom.Node item(int param) {
        int i = 0;
        Iterator it = peer.values().iterator(); 
        while (it.hasNext()) {
            Object next = it.next();
            if (param == i) return (Node) next;
            i++;
        }
        return null;
    }
    
    public org.w3c.dom.Node getNamedItem(String str) {
        return (Node) peer.get(createKey(str));
    }
    
    public org.w3c.dom.Node removeNamedItemNS(String str, String str1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }

    public String toString() {
        return peer.toString();
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
