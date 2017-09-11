/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
