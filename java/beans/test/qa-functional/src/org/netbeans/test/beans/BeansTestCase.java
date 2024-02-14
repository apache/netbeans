/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                TreeNode[] res = path.toArray(new TreeNode[0]);                
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
