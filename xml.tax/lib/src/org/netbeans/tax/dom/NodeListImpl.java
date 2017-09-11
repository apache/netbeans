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


package org.netbeans.tax.dom;

import java.util.Iterator;
import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 * NodeList taht filters out all unwrappable types.
 *
 *
 * @author  Petr Kuzel
 */
class NodeListImpl implements NodeList {

    public static final NodeList EMPTY = new NodeList() {
        public int getLength() { return 0; }
        public org.w3c.dom.Node item(int i) { return null; }
        public String toString() { return "NodeListImpl.EMPTY"; }
    };


    private final TreeObjectList peer;

    public NodeListImpl(TreeObjectList peer) {
        this.peer = peer;
    }
    
    /** The number of nodes in the list. The range of valid child node indices
     * is 0 to <code>length-1</code> inclusive.
     *
     */
    public int getLength() {
        int i = 0;
        Iterator it = peer.iterator();
        while (it.hasNext()) {
            TreeObject next = (TreeObject) it.next();
            if (accept(next)) i++;
        }
        return i;
    }
    
    /** Returns the <code>index</code>th item in the collection. If
     * <code>index</code> is greater than or equal to the number of nodes in
     * the list, this returns <code>null</code>.
     * @param index Index into the collection.
     * @return The node at the <code>index</code>th position in the
     *   <code>NodeList</code>, or <code>null</code> if that is not a valid
     *   index.
     *
     */
    public Node item(int index) {
        int i = 0;
        Iterator it = peer.iterator();
        while (it.hasNext()) {
            TreeObject next = (TreeObject) it.next();
            if (accept(next)) {
                if (i == index) {
                    return Wrapper.wrap((TreeObject)peer.get(index));
                } else {
                    i++;
                }
            }
        }
        return null;                
    }

    /**
     * Supported types.
     */
    private boolean accept(TreeObject o) {
        return o instanceof TreeText || o instanceof TreeElement;
    }
}
