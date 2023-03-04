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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.RuntimeViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Services TopComponent.<p>
 * Functionality related to Services tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 * Example:<p>
 * <pre>
 *      RuntimeTabOperator rto = RuntimeTabOperator.invoke();
 *      // or when Runtime pane is already opened
 *      //RuntimeTabOperator rto = new RuntimeTabOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = rto.tree();
 *      // work with nodes
 *      rto.getRootNode().select();
 *      Node node = new Node(rto.getRootNode(), "subnode|sub subnode");
 * </pre> 
 *
 * @see RuntimeViewAction
 */
public class RuntimeTabOperator extends TopComponentOperator {

    static final String RUNTIME_CAPTION = Bundle.getString("org.netbeans.core.ide.resources.Bundle", "UI/Runtime");
    private static final RuntimeViewAction viewAction = new RuntimeViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Runtime TopComponent through all IDE. */    
    public RuntimeTabOperator() {
        super(waitTopComponent(null, RUNTIME_CAPTION, 0, new RuntimeTabSubchooser()));
    }
    
    /** invokes Runtime and returns new instance of RuntimeTabOperator
     * @return new instance of RuntimeTabOperator */    
    public static RuntimeTabOperator invoke() {
        viewAction.perform();
        return new RuntimeTabOperator();
    }
    
    /** getter for Runtime JTreeOperator
     * @return JTreeOperator of Runtime tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /**
     * Collapse all nodes.
     */
    public void collapseAll() {
        JTreeOperator tree = tree();
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.collapseRow(i);
        }
    }

    /** getter for Runtime root node
     * @return RuntimeRootNode */    
    public Node getRootNode() {
        return new Node(tree(), "");
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.core.NbMainExplorer$MainTab
     * Used in constructor.
     */
    private static final class RuntimeTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equals("org.netbeans.core.ide.ServicesTab");
        }
        
        public String getDescription() {
            return "org.netbeans.core.ide.ServicesTab";
        }
    }
}
