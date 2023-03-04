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
package org.netbeans.modules.xml.tax.cookies;

import javax.swing.text.Element;

import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeNode;

/**
 * Everything what can be represented as the tree can return this cookie.
 * It particurary can be DTD and XML document (fragment).
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface TreeDocumentCookie extends Node.Cookie {

    /**
     * Return current state of model (it may be different from state when cookie was queried).
     * @return root of tree hiearchy or null if source can not be represented as tree
     * (can not be parsed etc.)
     */
    public TreeDocumentRoot getDocumentRoot ();
    
    
    /**
     */
    /* public */ static interface Editor extends TreeDocumentCookie, EditorCookie {
	
	/**
	 */
	Element treeToText (TreeNode treeNode);

	/**
	 */
	TreeNode textToTree (Element textElement);

    }

}
