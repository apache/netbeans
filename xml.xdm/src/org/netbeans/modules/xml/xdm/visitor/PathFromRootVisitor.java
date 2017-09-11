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
