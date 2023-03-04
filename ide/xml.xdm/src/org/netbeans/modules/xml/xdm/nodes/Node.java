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
