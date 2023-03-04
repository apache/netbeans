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

/*
 * PathFromRootVisitor.java
 *
 * Created on August 4, 2005, 6:43 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author Chris Webster
 */
public class PathFromRootVisitor extends ChildVisitor {

    public List<Node> findPath(org.w3c.dom.Document root, org.w3c.dom.Node target) {
        Document wroot = root instanceof Document ? (Document) root : null;
        Node wtarget = target instanceof Node ? (Node) target : null;
        return findPath(wroot, wtarget);
    }
    
    public List<Node> findPathToRootElement(org.w3c.dom.Element root, org.w3c.dom.Node target) {
        Element wroot = root instanceof Element ? (Element) root : null;
        Node wtarget = target instanceof Node ? (Node) target : null;
        assert root != null && target != null;
        
        this.target = wtarget;
        found = false;
        pathToTarget = null;
        wroot.accept(this);
        return pathToTarget;
    }
    
    public List<Node> findPath(Document root, Node target) {
        assert root != null;
//        assert target != null;
		if(target == null)
			return Collections.emptyList();
        this.target = target;
        found = false;
        pathToTarget = null;
        root.accept(this);
        return pathToTarget;
    }
    
    protected void visitNode(Node n) {
        // if already found just return
        if(found) return;
        if (target.getId() == n.getId()) {
            pathToTarget = new LinkedList<Node>();
            pathToTarget.add(n);
            found = true;
        } else {
            super.visitNode(n);
            if(found) {
                // add the ancestors to the list 
                pathToTarget.add(n);
            }
        }
    }
    
    private boolean found;
    private List<Node> pathToTarget;
    private Node target;
}
