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

package org.netbeans.modules.xml.xdm.visitor;

import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * This class provides the ability to walk nodes which have children.
 * @author Chris Webster
 */
public class ChildVisitor extends DefaultVisitor {
    
    protected void visitNode(Node container) {
	NodeList children = container.getChildNodes();
	for (int i =0; i<children.getLength(); i++) {
	    Node l = (Node)children.item(i);
	    l.accept(this);
	}
	NamedNodeMap attributes = container.getAttributes();
	for (int i =0; i<attributes.getLength(); i++) {
	    Node l = (Node)attributes.item(i);
	    l.accept(this);
	}
    }
}
