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

package org.netbeans.jellytools.modules.junit.nodes;

/*
 * JUnitNode.java
 *
 * Created on 2/6/03 2:21 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.junit.actions.*;
import org.netbeans.jellytools.nodes.Node;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JTreeOperator;

/** JUnitNode Class
 * @author dave */
public class JUnitNode extends Node {

    private static final Action createTestsAction = new CreateTestsAction();
    private static final Action executeTestAction = new ExecuteTestAction();
    private static final Action openTestAction = new OpenTestAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new JUnitNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public JUnitNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new JUnitNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public JUnitNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }

    /** creates new JUnitNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public JUnitNode(Node parent, String treePath) {
        super(parent, treePath);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            createTestsAction,
            executeTestAction,
            openTestAction,
            propertiesAction
        });
    }

    /** performs CreateTestsAction with this node */
    public void createTests() {
        createTestsAction.perform(this);
    }

    /** performs ExecuteTestAction with this node */
    public void executeTest() {
        executeTestAction.perform(this);
    }

    /** performs OpenTestAction with this node */
    public void openTest() {
        openTestAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
}
