/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.beans;

import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.ListModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.beans.PatternNode;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.explorer.view.Visualizer;

/**
 *
 * @author jprox
 */
public class BeansTestCase extends JellyTestCase {

    private static final String sample_project = "Beans";
 

    public BeansTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
     protected void setUp() throws Exception {
        super.setUp();
        this.openDataProjects("projects/" + sample_project);        
     }

    protected void openFile(String pack, String className) {
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(sample_project),"Source Packages|"+pack.replace('.', '|')+"|"+className);
        new OpenAction().performAPI(openFile);
    }
           
    public boolean openDialog(EditorOperator operator) {
        operator.pressKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();        
        JListOperator list = new JListOperator(jdo);
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            if(cg.getDisplayName().equals("Add Property...")) {
                list.setSelectedIndex(i);
                jdo.pushKey(KeyEvent.VK_ENTER);                
                new EventTool().waitNoEvent(250);
                return true;
            }
        }
        fail("Dialog not found");
        return false;
    }
    
    protected EditorOperator openEditor(String className) {
        openFile("beans", className);
        EditorOperator operator = new EditorOperator(className);
        return operator;
    }
    
    protected EditorOperator openEditor(String packageName, String className) {
        openFile(packageName, className);
        EditorOperator operator = new EditorOperator(className);
        return operator;
    }

    protected void checkEditorContent(EditorOperator operator) {
        ref(operator.getText());
        compareReferenceFiles();
    }
    
    public static TreePath getTreePath(JTreeOperator treeOperator, String targetNode, NodeConverter converter) {
        Stack<TreeNode> lifo = new Stack<TreeNode>();        
        lifo.push((TreeNode) treeOperator.getRoot());
        while(!lifo.isEmpty()) {
            TreeNode actNode = lifo.pop();
            if(targetNode.equals(converter.getDisplayName(actNode))) {
                List<TreeNode> path = new LinkedList<TreeNode>();
                path.add(actNode);
                actNode = actNode.getParent();
                while(actNode!=null) {
                    path.add(0,actNode);
                    actNode = actNode.getParent();
                }
                TreeNode[] res = path.toArray(new TreeNode[path.size()]);                
                return new TreePath(res);
            }
            final Enumeration children = actNode.children();            
            while(children.hasMoreElements()) {
                lifo.add((TreeNode)children.nextElement());
            }            
        }
        return null;
    }
    
    
    public static String getTree(JTreeOperator operator, NodeConverter converter) {
        return (getTree((TreeNode) operator.getRoot(), "", new StringBuilder(), converter)).toString();
    }

    private static StringBuilder getTree(TreeNode root, String deep, StringBuilder accumulator, NodeConverter converter) {                        
        String text = converter.getDisplayName(root);
        accumulator.append(deep).append(text).append('\n');
        for (int i = 0; i < root.getChildCount(); i++) {
            getTree(root.getChildAt(i), deep + "__", accumulator, converter);
        }
        return accumulator;
    }
    
    public interface NodeConverter {
        String getDisplayName(TreeNode node);
    }
    

}
