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
package org.netbeans.test.beans.operators;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.beans.PatternNode;
import org.netbeans.test.beans.BeansTestCase;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

import static org.junit.Assert.fail;

/**
 *
 * @author ssazonov
 */
public class Navigator extends TopComponentOperator {

    private JTreeOperator tree;

    public Navigator() {
        super("Navigator");        
    }
    
    public JTreeOperator getTreeOperator() {
        if(tree==null) {
            tree = new JTreeOperator(this);
        }
        return tree;
    }

    public void setScopeToMember() {
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem("Members");
        tree = null; 
    }

    public void setScopeToBeanPatterns() {
        JComboBoxOperator combo = new JComboBoxOperator(this, 0);
        combo.selectItem("Bean Patterns");
        tree = null;
    }
        
    public String getSelectedPath() {
        return Arrays.toString(getTreeOperator().getSelectionPaths());
    }

    public boolean clickTheNode(String node) {
        TreeNode[] nodePath = getPath(node);
        TreePath treePath   = new TreePath(nodePath);

        try {
            getTreeOperator().clickOnPath(treePath, 2);
            return true;
        } catch (Exception e) {
        }
        return false;
    }
    
    public void waitForString(final String expected) {
        Waitable waitable = new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                Navigator n = (Navigator) obj;
                String tree = n.getTree();
                if(expected.equals(tree)) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }

            @Override
            public String getDescription() {
                return "waiting for navigator tree";
            }
        };
        Waiter w = new Waiter(waitable);
        try {
            w.getTimeouts().setTimeout("Waiter.WaitingTime", 10000);            
            w.waitAction(this);
        } catch(InterruptedException ie) {
            
        } catch (TimeoutExpiredException tee) {
            fail("Expected tree "+expected+" but found "+this.getTree());
            
        }
    }

    //----------------------------------------------------------
    
    private String getTree() {
        return BeansTestCase.getTree(getTreeOperator(), new BeansTestCase.NodeConverter() {

            @Override
            public String getDisplayName(TreeNode node) {
                String text = node.toString();
                Node findNode = Visualizer.findNode(node);
                if (findNode instanceof PatternNode) {
                    PatternNode patternNode = (PatternNode) findNode;
                    if (patternNode.getShortDescription() != null) {
                        text = patternNode.getShortDescription();
                    } else if (patternNode.getHtmlDisplayName() != null) {
                        text = patternNode.getHtmlDisplayName();
                    }
                }
                return text;
            }
        });        
    }
    
    private TreeNode[] getPath(String node) {   
        TreePath treePath = BeansTestCase.getTreePath(getTreeOperator(), node, new BeansTestCase.NodeConverter() {
            
            @Override
            public String getDisplayName(TreeNode node) {
                return node.toString();
            }
        });        
        return (TreeNode[]) treePath.getPath();                        
    }
        
    
}
