/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        return new TreePath(nodes.toArray(new Object[nodes.size()]));
    }
}
