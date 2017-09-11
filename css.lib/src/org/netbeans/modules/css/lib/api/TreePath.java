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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a path between two {@link Node}s.
 * 
 * Can be encoded to {@link String} by {@link NodeUtil#encodeToString(org.netbeans.modules.css.lib.api.TreePath)}
 *
 * @since 1.42
 * @author mfukala@netbeans.org
 */
public class TreePath {

    private Node first,  last;
    
    /**
     * Path from root of the parse tree.
     * Same as {@link TreePath#TreePath(org.netbeans.modules.css.lib.api.Node, org.netbeans.modules.css.lib.api.Node)} with null first argument.
     * @param last 
     */
    public TreePath(Node last) {
        this(null, last);
    }
    
    /** @param first may be null; in such case a path from the root is created */
    public TreePath(Node first, Node last) {
        this.first = first;
        this.last = last;
    }

    public Node first() {
        return first;
    }
    
    public Node last() {
        return last;
    }
     
    /** returns a list of nodes from the first node to the last node including the boundaries. */
    public List<Node> path() {
        List<Node> path = new  ArrayList<>();
        Node node = last;
        while (node != null) {
            path.add(node);
            if(node == first) {
                break;
            }
            node = node.parent();
        }
        return path;
    }

    @Override
    public String toString() {
        return getNodePath();
    }
    
    private String getNodePath() {
        return NodeUtil.encodeToString(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TreePath)) {
            return false;
        }
        TreePath path = (TreePath)o;
        return getNodePath().equals(path.getNodePath());
    }
    
    @Override
    public int hashCode() {
        return getNodePath().hashCode();
    }
    
    
}
