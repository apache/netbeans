/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;

/**
 * according to <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#javadocoptions">Javadoc Options</a>
 */
enum Access {
    PUBLIC, PROTECTED, PACKAGE, PRIVATE;

    /**
     * accept [public|protected|package|private], default(null or other) is protected
     */
    public static Access resolve(String s) {
        if (s != null) {
            s = s.trim().toLowerCase();
            if ("public".equals(s)) { // NOI18N
                return Access.PUBLIC;
            } else if ("protected".equals(s)) { // NOI18N
                return Access.PROTECTED;
            } else if ("private".equals(s)) { // NOI18N
                return Access.PRIVATE;
            } else if ("package".equals(s)) { // NOI18N
                return Access.PACKAGE;
            }
        }
        return Access.PROTECTED;
    }

    public boolean isAccessible(Set<Modifier> flags) {
        switch(this) {
        case PRIVATE:
            return true;
        case PACKAGE:
            return !flags.contains(Modifier.PRIVATE);
        case PROTECTED:
            return flags.contains(Modifier.PUBLIC) || flags.contains(Modifier.PROTECTED);
        case PUBLIC:
            return flags.contains(Modifier.PUBLIC);
        default:
            throw new IllegalStateException();
        }
    }

    /**
     * @param path path to check
     * @param alwaysAccessible true means to check if the path contains only class members;
     *                         false means to check besides class members also their modifiers
     * @return is accessible
     * @see #isAccessible(Set)
     */
    public boolean isAccessible(CompilationInfo javac, TreePath path, boolean alwaysAccessible) {
        TreePath parent = path.getParentPath();
        Tree leaf = path.getLeaf();
        if (parent != null) {
            Tree.Kind parentKind = parent.getLeaf().getKind();
            if (!TreeUtilities.CLASS_TREE_KINDS.contains(parentKind) && parentKind != Tree.Kind.COMPILATION_UNIT) {
                // not class member
                return false;
            }

            if (!isAccessible(javac, parent, alwaysAccessible)) {
                return false;
            }
        }

        Set<Modifier> flags;
        switch (leaf.getKind()) {
        case COMPILATION_UNIT: return true;
        case ANNOTATION_TYPE:case CLASS:
            case ENUM:
            case INTERFACE:
                flags = ((ClassTree) leaf).getModifiers().getFlags(); break;
        case METHOD: flags = ((MethodTree) leaf).getModifiers().getFlags(); break;
        case VARIABLE: flags = ((VariableTree) leaf).getModifiers().getFlags(); break;
        default: return false;
        }

        // all members of interface and annotation type are public by definition (JLS 9.1.5)
        return alwaysAccessible || isInterfaceMember(javac, path) || isAccessible(flags);
    }

    /**
     * @return is member of interface or annotatin type
     */
    private boolean isInterfaceMember(CompilationInfo javac, TreePath path) {
        TreePath parentPath = path.getParentPath();
        if (parentPath == null) {
            return false;
        }
        Tree parent = parentPath.getLeaf();
        TreeUtilities utils = javac.getTreeUtilities();
        return TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())
                && (utils.isInterface((ClassTree) parent) || utils.isAnnotation((ClassTree) parent));
    }
}
