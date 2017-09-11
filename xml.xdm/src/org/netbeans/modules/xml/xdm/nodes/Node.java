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

package org.netbeans.modules.xml.xdm.nodes;

import org.netbeans.modules.xml.xdm.XDMModel;
import org.w3c.dom.*;
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;

/**
 * @author Ajit
 */
public interface Node extends org.w3c.dom.Node {
    
    int getId();
    
	/*
	 * Support for the visitor pattern
	 */
	void accept(XMLNodeVisitor visitor);
	
	/**
	 * A node can only be added to a tree once. Invoking this method signifies
	 * that a node has been placed into a tree and thus cannot be added. A node
	 * can be referenced by multiple trees but only added once. 
	 */
	void addedToTree(XDMModel model);
	
	/**
	 * @return tree if node has already been added to the tree. 
	 * @see #addedToTree()
	 */
	boolean isInTree();

	/**
	 * @return true the passed node has same id and belongs to same model. 
	 * @param node Node to compare
	 */
	boolean isEquivalentNode(Node node);

    /**
     * This api clones the node object and returns the clone. A node object has
     * content, attributes and children. The api will allow or disallow
     * modification of this underlying data based on the input.
     * @param cloneContent If true the content of clone can be modified.
     * @param cloneAttributes If true the attributes of the clone can be modified.
     * @param cloneChildren If true the children of the clone can be modified.
     * @return returns the clone of this node
     */
    Node clone(boolean cloneContent, boolean cloneAttributes, boolean cloneChildren);
    
    /**
     * Lookup child index of given child based on node ID.
     * @return child index of given child or -1 if not a child.
     */
    int getIndexOfChild(Node child);
    
    /**
     * Returns namespace of this node in the given Document tree.
     * @return namespace URI string if this node is in the Document tree and has namespace; otherwise null.
     */
    String getNamespaceURI(Document document);
}
