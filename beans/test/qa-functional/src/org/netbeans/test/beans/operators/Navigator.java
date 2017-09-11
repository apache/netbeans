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
package org.netbeans.test.beans.operators;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import static junit.framework.Assert.fail;
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
