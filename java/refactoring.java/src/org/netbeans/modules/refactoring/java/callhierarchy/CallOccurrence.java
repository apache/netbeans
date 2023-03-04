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

package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.swing.Icon;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.openide.text.PositionBounds;

/**
 *
 * @author Jan Pokorsky
 */
final class CallOccurrence implements CallDescriptor {
    
    private String displayName;
    private String htmlDisplayName;
    private PositionBounds selectionBounds;
    private TreePathHandle occurrence;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void open() {
        if(occurrence != null) {
            Call.doOpen(occurrence.getFileObject(), selectionBounds);
        }
    }

    public PositionBounds getSelectionBounds() {
        return selectionBounds;
    }

    public static CallOccurrence createOccurrence(
            CompilationInfo javac, TreePath selection, Call parent) {
        WhereUsedElement wue = WhereUsedElement.create(javac, selection, false);
        CallOccurrence c = new CallOccurrence();
        if(JavaPluginUtils.isSyntheticPath(javac, selection)) {
            selection = getEnclosingTree(selection);
            if (JavaPluginUtils.isSyntheticPath(javac, selection)) {
                selection = getEnclosingTree(selection.getParentPath());
            }
        }
        c.occurrence = TreePathHandle.create(selection, javac);
        c.displayName = selection.getLeaf().toString();
        c.htmlDisplayName = String.format("<html>%s</html>", wue.getDisplayText());
        c.selectionBounds = wue.getPosition();
        return c;
    }
    
    private static TreePath getEnclosingTree(TreePath tp) {
        while(tp != null) {
            Tree tree = tp.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind()) || tree.getKind() == Tree.Kind.METHOD || tree.getKind() == Tree.Kind.IMPORT || tree.getKind() == tree.getKind().VARIABLE) {
                return tp;
            } 
            tp = tp.getParentPath();
        }
        return null;
    }
}
