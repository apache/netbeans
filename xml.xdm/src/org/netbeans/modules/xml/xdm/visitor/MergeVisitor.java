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


package org.netbeans.modules.xml.xdm.visitor;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * This class provides a way to merge two trees. A merge is defined as taking a
 * tree and muting this tree (firing events) to cause the tree to be the same
 * as the target tree. This allows the original tree to retain nodes which have
 * not been changed. A change is defined as having both the same syntax and
 * semantics. The current tree model preserves spacing where appropriate, so
 * this requires comparing spacing as well as semantics.
 * @author Chris Webster
 * @author Vidhya Narayanan
 */
public class MergeVisitor extends DefaultVisitor {
    /**
     * This method merges the currentModel and the given newDocument. Events
     * will be fired on currentModel.
     * @param model the model to merge the changes required to transform
     * the current document to something equivalent to newDocument.
     * @param newDoc to replicate in the current model.
     */
    public void merge(XDMModel model, Document newDoc) {
        xmlModel = model;
        oldtree = xmlModel.getDocument();
        target = newDoc;
        oldtree.accept(this);
    }
    
    protected void visitNode(Node node) {
        CompareVisitor comparer = new CompareVisitor();
        boolean result = comparer.compare(node, target);
        if (!result) {
            Node newNode = (Node)target.clone(false,false,false);
            List<Node> path = pathVisitor.findPath(oldtree, node);
            assert !path.isEmpty();
            Node oldNode = path.get(0);
            int offset = 0;
            NodeList children = ((Node)path.get(1)).getChildNodes();
            for (;offset<children.getLength();offset++) {
                if (oldNode.equals(children.item(offset))) {
                    break;
                }
            }
            xmlModel.delete(oldNode);
            xmlModel.add((Node)path.get(1), newNode, offset);
        } else {
            compareByIndex((Node)target, node);
            compareAttrsByIndex((Node)target, node);
        }
    }
    
    private void compareByIndex(Node newNode, Node current) {
        NodeList newnodes = newNode.getChildNodes();
        NodeList children = current.getChildNodes();
        int oldTreesize = children.getLength();
        int newTreesize = newnodes.getLength();
        
        int lastEqualIndex = Math.min(oldTreesize, newTreesize);
        // these nodes are comparable
        for (int i = 0; i < lastEqualIndex; i++) {
            target = (Node)newnodes.item(i);
            Node n = (Node)children.item(i);
            n.accept(this);
        }
        // reset target as the rest of the tree will need to be walked
        target = newNode;
        
        // delete removed nodes from oldTree
        for (int i = oldTreesize-1; i >= lastEqualIndex; i--) {
            xmlModel.delete((Node)children.item(i));
        }
        
        // add nodes from newTree
        for (int i = lastEqualIndex; i < newTreesize; i++) {
            Node n = (Node)newnodes.item(i);
            xmlModel.add(current, (Node)n.clone(false,false,false), i);
        }
    }
    
    private void compareAttrsByIndex(Node newNode, Node current) {
        NamedNodeMap newAttributes = newNode.getAttributes();
        NamedNodeMap attributes = current.getAttributes();
        int oldTreesize = attributes.getLength();
        int newTreesize = newAttributes.getLength();
        
        int lastEqualIndex = Math.min(oldTreesize, newTreesize);
        // these nodes are comparable
        for (int i = 0; i < lastEqualIndex; i++) {
            target = (Node)newAttributes.item(i);
            Node n = (Node)attributes.item(i);
//            System.out.println(n.getNodeName());
            n.accept(this);
        }
        // reset target as the rest of the tree will need to be walked
        target = newNode;
        
        // delete removed nodes from oldTree
        for (int i = oldTreesize-1; i >= lastEqualIndex; i--) {
            xmlModel.delete((Node)attributes.item(i));
        }
        
        // add nodes from newTree
        for (int i = lastEqualIndex; i < newTreesize; i++) {
            Node n = (Node)newAttributes.item(i);
            xmlModel.add(current, ((NodeImpl)n).cloneShallowWithModelContext(), i);
        }
    }
    
    private XDMModel xmlModel;
    private Document oldtree;
    private Node target;
    private PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
}
