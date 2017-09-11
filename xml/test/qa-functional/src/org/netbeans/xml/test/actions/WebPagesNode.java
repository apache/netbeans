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

package org.netbeans.xml.test.actions;

import java.lang.reflect.Constructor;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** AbstractNode Class
 * @author ms113234 */
public class WebPagesNode extends Node {
    private static ProjectsTabOperator projectTabOperator = new ProjectsTabOperator();
    
    /** creates new DriversNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public WebPagesNode(JTreeOperator tree, String treePath) {
	super(tree, treePath);
    }
    
    /** creates new DriversNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public WebPagesNode(JTreeOperator tree, TreePath treePath) {
	super(tree, treePath);
    }
    
    /** creates new DriversNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public WebPagesNode(Node parent, String treePath) {
	super(parent, treePath);
    }
    
    
    /** This method tests if the specified children exists.
     * @param displayName children name
     * @return true if the specified children exists
     */
    
    public static WebPagesNode getInstance(String projectName){
	projectTabOperator.invoke();
	JTreeOperator tree = projectTabOperator.tree();
	Node node = new Node(tree, projectName);
	return new WebPagesNode(node, "Web Pages");
    }
    
    public boolean containsChild(String displayName) {
	String[] drivers = this.getChildren();
	for (int i = 0 ; i < drivers.length ; i++) {
	    if (displayName.equals(drivers[i]) ) return true;
	}
	return false;
    }
    
    /** This method creates a new node operator for child.
     * @param displayName children name
     * @param clazz children class
     * @return children node
     */
    public Node getChild(String displayName, Class clazz) {
	if (!Node.class.isAssignableFrom(clazz)) {
	    throw new IllegalArgumentException(clazz + " is not instance of org.netbeans.jellytools.nodes.Node");
	}
	if (!this.containsChild(displayName) ) return null;
	Node node = null;
	try {
	    Constructor constructor = clazz.getConstructor(new Class[] {Node.class, String.class});
	    node = (Node) constructor.newInstance(new Object[] {this, displayName});
	} catch (Exception ex) {
	    throw new RuntimeException("Cannot instantiate " + clazz, ex);
	}
	return node;
    }
    
}
