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

package org.netbeans.modules.xml.xdm.diff;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * Class that represents diff node info
 *
 * @author Ayub Khan
 */
public class NodeInfo {
    
    public NodeInfo(Node n, int pos, List<Node> ancestors1, List<Node> ancestors2) {
        if (! (n instanceof Document)) {
            assert ancestors1 != null && ! ancestors1.isEmpty() : "bad ancestors1";
            assert ancestors2 != null && ! ancestors2.isEmpty() : "bad ancestors1";
        }
        this.n = n;
        this.pos = pos;
        this.ancestors1 = ancestors1;
        this.ancestors2 = ancestors2;
    }
    
    public Node getNode() {
        return n;
    }
    
    /**
     * Only to update new version of same nodeid.
     */
    void setNode(Node node) {
        if (updated) {
            assert node.getId() == n.getId() : "expect id="+n.getId()+" got id="+node.getId();
        }
        updated = true;
        n = node;
    }
    
    /**
     * @returns position of removed in the original parent or
     *          position of the added in the final parent.
     */
    public int getPosition() {
        return pos;
    }
    
    public Node getParent() {
        if (ancestors1 != null && ancestors1.size() > 0) {
            return ancestors1.get(0);
        }
        return null;
    }
    
    /**
     * @returns document the node is captured in (a node in xdm tree can be in
     * multiple document roots).
     */
    public Document getDocument() {
        return (Document) ancestors1.get(ancestors1.size()-1);
    }
    
    /**
     * @returns original path from parent to root.
     */
    public List<Node> getOriginalAncestors() {
        return Collections.unmodifiableList(ancestors1);
    }
    
    /**
     * @returns new path to root from parent of added or removed node.
     * Note that this path need to be updated
     */
    public List<Node> getNewAncestors() {
        if (ancestors2 == null) {
            assert parent2 != null : "expect parent2 is set";
            ancestors2 = DiffFinder.getPathToRoot(parent2);
        }
        return Collections.unmodifiableList(ancestors2);
    }
    
    public void setNewAncestors(List<Node> ancestors2) {
        assert ancestors2 != null && ! ancestors2.isEmpty();
        this.ancestors2 = ancestors2;
        parent2 = ancestors2.get(0);
    }
    
    public void setNewParent(Node parent) {
        assert parent != null && parent.isInTree() : "new parent should be not null and inTree";
        ancestors2 = null;
        parent2 = parent;
    }
    
    public Node getNewParent() {
        if (parent2 == null && ! (getNode() instanceof Document)) {
            assert ancestors2 != null && ancestors2.size() > 0;
            return ancestors2.get(0);
        }
        return parent2;
    }
    
    public String toString() {
        int parentId = getParent() == null ? -1 : getParent().getId();
        return DiffFinder.getNodeType(n) + "." + pos + " ids[" + n.getId() + "," +
                parentId + "]";
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////////
    
    public static enum NodeType { ELEMENT, ATTRIBUTE, TEXT, WHITE_SPACE };
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private Node n;
    
    private boolean updated = false;
    
    private final int pos;
    
    private final List<Node> ancestors1;
    
    private List<Node> ancestors2;  // new ancestors or would-have-been ancestors in case of delete
    
    private Node parent2;
}
