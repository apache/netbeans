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
package org.netbeans.test.html5;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.openide.explorer.view.Visualizer;

/**
 * Operator for DOM window
 *
 * @author Vladimir Riha
 * @version 1.0
 */
public class DomOperator extends TopComponentOperator {

    public DomOperator() {
        super("Browser DOM");
    }
    private JTreeOperator _treeDOM;

    //******************************
    // Subcomponents definition part
    //******************************
    /**
     * Tries to find JTreeOperator in this dialog.
     *
     * @return JTreeOperator
     */
    private JTreeOperator treeDOM() {
        if (_treeDOM == null) {
            _treeDOM = new JTreeOperator(this);
        }
        return _treeDOM;
    }

    @Override
    public String toString() {
        TreeModel tm = treeDOM().getModel();
        return traverse(tm, tm.getRoot());
    }

    private String traverse(TreeModel model, Object o) {
        StringBuilder sb = new StringBuilder();
        int size = model.getChildCount(o);
        TreeNode child;

        for (int i = 0; i < size; i++) {
            org.openide.nodes.Node node = Visualizer.findNode(model.getChild(o, i));
            child = (TreeNode) model.getChild(o, i);
            sb.append(getTreePathForNode(child).toString());
            if (!model.isLeaf(child)) {
                sb.append(traverse(model, child));
            }
        }
        return sb.toString();
    }

    /**
     * Clicks on element in Navigator window.
     *
     * @param domPath DOM Path to element with "|" as separator between nodes
     * (e.g. "html|body|div" to click on div)
     * @param domOrder Order of each element in domPath separated with "|" and
     * ordered from 0 (e.g. "0|0|1" means first html, first body in html and
     * second div in html|body)
     *
     */
    public void focusElement(String domPath, String domOrder) {
        (new Node(treeDOM(), treeDOM().findPath(domPath, domOrder, "|"))).select();
    }

    /**
     * Retuns DOM tree path to selected element and its ID and Class attributes
     *
     * @return sample output {@code [root, html, body]body#foo.bar}
     */
    public String getFocusedElement() {
        StringBuilder sb = new StringBuilder();
        TreePath tp = treeDOM().getSelectionPath();
        sb.append(tp.toString().replaceFirst("\\[#document, ", "["));// ignore root in path
        org.openide.nodes.Node node = Visualizer.findNode(treeDOM().getLastSelectedPathComponent());
        return sb.toString();
    }

    private TreePath getTreePathForNode(TreeNode node) {
        List<TreeNode> nodes = new ArrayList<>();

        do {
            nodes.add(node);
        } while ((node = node.getParent()) != null);

        Collections.reverse(nodes);
        return new TreePath(nodes.toArray(new Object[0]));
    }
}
