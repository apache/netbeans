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

package org.netbeans.modules.php.editor.codegen.ui;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TreeElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.codegen.CGSGenerator;
import org.netbeans.modules.php.editor.codegen.CGSInfo;
import org.netbeans.modules.php.editor.codegen.MethodProperty;
import org.netbeans.modules.php.editor.codegen.Property;
import org.netbeans.modules.php.editor.codegen.ui.CheckNode.CGSClassNode;

/**
 * @author Tomas Mysik
 */
public class MethodPanel extends ConstructorPanel {

    public MethodPanel(CGSInfo cgsInfo) {
        super(CGSGenerator.GenType.METHODS, cgsInfo);
    }

    @Override
    protected MutableTreeNode getRootNode() {
        // get the enclosing type
        TreeElement<TypeElement> enclosingType = null;
        for (Property property : properties) {
            MethodProperty methodProperty = (MethodProperty) property;
            enclosingType = methodProperty.getEnclosingType();
            break;
        }

        // init tree
        CheckNode root = new CheckNode.CGSClassNode(className);

        Set<TypeElement> endlessLoopDetection = new HashSet<>();
        LinkedList<TreeElement<TypeElement>> queue = new LinkedList<>();
        if (enclosingType != null) {
            endlessLoopDetection.add(enclosingType.getElement());
        }
        queue.offer(enclosingType);
        while (!queue.isEmpty()) {
            TreeElement<TypeElement> type = queue.poll();
            final String nodeText = String.format("<html><b>%s</b> %s</html>", //NOI18N
                    type.getElement().getName(), type.getElement().asString(TypeElement.PrintAs.SuperTypes));
            final CGSClassNode classNode = new CheckNode.CGSClassNode(nodeText);
            for (Property property : properties) {
                MethodProperty methodProperty = (MethodProperty) property;
                if (!ElementFilter.forEqualTypes(type.getElement()).filter(methodProperty.getMethod().getType()).isEmpty()) {
                    classNode.add(new CheckNode.MethodPropertyNode(methodProperty));
                }
            }
            if (!classNode.isLeaf()) {
                root.add(classNode);
            }

            for (TreeElement<TypeElement> e : type.children()) {
                if (endlessLoopDetection.add(e.getElement())) {
                    queue.offer(e);
                }
            }
        }

        return root;
    }

    @Override
    protected void initTree(JTree tree) {
        tree.setRootVisible(false);
        expandAll(tree);
    }

    private static void expandAll(JTree tree) {
        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }
    }

}
