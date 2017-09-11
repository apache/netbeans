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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 * This class represents diff between 2 elements of 2 XML documents
 *
 * @author Ayub Khan
 */
public abstract class Difference {
    
    /** Creates a new instance of DiffEvent */
    public Difference(NodeInfo.NodeType nodeType,
            List<Node> ancestors1, List<Node> ancestors2,
            Node n1, Node n2, int n1Pos, int n2Pos) {
        this.nodeType = nodeType;
        if (! (n1 instanceof Document)) {
            assert ancestors1 != null && ! ancestors1.isEmpty() : "diff of non-root should have ancestors";
        }
        this.oldNodeInfo = new NodeInfo( n1, n1Pos, ancestors1, ancestors2);
        this.newNodeInfo = new NodeInfo( n2, n2Pos, new ArrayList(ancestors1), new ArrayList(ancestors2));
        if (newNodeInfo.getNode() != null && newNodeInfo.getNewAncestors().size() > 0) {
            assert newNodeInfo.getNewAncestors().get(0).getId() != newNodeInfo.getNode().getId();
        }
    }
    
    public NodeInfo.NodeType getNodeType() {
        return nodeType;
    }
    
    /**
     * @returns info on removed node.
     */
    public NodeInfo getOldNodeInfo() {
        return oldNodeInfo;
    }
    
    /**
     * @return info on added node.
     */
    public NodeInfo getNewNodeInfo() {
        return newNodeInfo;
    }
    
    /**
     * @return new path from parent to root.
     */
    public abstract List<Node> getNewAncestors();
    
    public abstract void setNewParent(Node n);
    
    public abstract Node getNewParent();
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private NodeInfo.NodeType nodeType;
    
    private NodeInfo oldNodeInfo;
    
    private NodeInfo newNodeInfo;
    
}
