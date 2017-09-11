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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public final class TreeUtils {
    private static final Set<JavaTokenId> WHITESPACES = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);

    static boolean isConstructor(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);
        return e != null && e.getKind() == ElementKind.CONSTRUCTOR;
    }

    public static boolean isParentOf(TreePath parent, TreePath path) {
        Tree parentLeaf = parent.getLeaf();
        while (path != null && path.getLeaf() != parentLeaf) {
            path = path.getParentPath();
        }
        return path != null;
    }

    static boolean isParentOf(TreePath parent, List<? extends TreePath> candidates) {
        for (TreePath tp : candidates) {
            if (!isParentOf(parent, tp)) {
                return false;
            }
        }
        return true;
    }

    public static TreePath findClass(TreePath path) {
        while (path != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind())) {
                return path;
            }
            path = path.getParentPath();
        }
        return null;
    }

    static List<TreePath> findConstructors(CompilationInfo info, TreePath method) {
        List<TreePath> result = new LinkedList<TreePath>();
        TreePath parent = method.getParentPath();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getLeaf().getKind())) {
            for (Tree t : ((ClassTree) parent.getLeaf()).getMembers()) {
                TreePath tp = new TreePath(parent, t);
                if (isConstructor(info, tp)) {
                    result.add(tp);
                }
            }
        }
        return result;
    }

    static boolean isInAnnotationType(CompilationInfo info, TreePath path) {
        Element e = info.getTrees().getElement(path);
        if (e != null) {
            e = e.getEnclosingElement();
            return e != null && e.getKind() == ElementKind.ANNOTATION_TYPE;
        }
        return false;
    }

    static int[] ignoreWhitespaces(CompilationInfo ci, int start, int end) {
        TokenSequence<JavaTokenId> ts = ci.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        if (ts == null) {
            return new int[]{start, end};
        }
        ts.move(start);
        if (ts.moveNext()) {
            boolean wasMoveNext = true;
            while (WHITESPACES.contains(ts.token().id()) && (wasMoveNext = ts.moveNext())) {
                ;
            }
            if (wasMoveNext && ts.offset() > start) {
                start = ts.offset();
            }
        }
        ts.move(end);
        while (ts.movePrevious() && WHITESPACES.contains(ts.token().id()) && ts.offset() < end) {
            end = ts.offset();
        }
        return new int[]{start, end};
    }

    static boolean isInsideClass(TreePath tp) {
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                return true;
            }
            tp = tp.getParentPath();
        }
        return false;
    }
    
    /**
     * Finds an enclosing statement which is a part of block or a body.
     * Unlike {@link #findStatement}, if the enclosing statement is not itself
     * directly a member of a block or case, returns a parent statement which is. In example
     * <code>if (cond) y = 1;</code>, expression statement <code>y + 1</code> will not be returned
     * because it's nested in <code>if</code> rather than in block/body
     * @param statementPath path
     * @return enclosing statement whose parent is a block, case, lambda or initializer.
     */
    static TreePath findStatementInBlock(TreePath statementPath) {
        return findBlockOrStatement(statementPath, false);
    }
    
    /**
     * Finds the enclosing statement.
     * @param statementPath path
     * @return the enclosing statement or null.
     */
    static TreePath findStatement(TreePath statementPath) {
        return findBlockOrStatement(statementPath, true);
    }

    static TreePath findBlockOrStatement(TreePath statementPath, boolean statement) {
        CYCLE: while (statementPath != null) {
            Tree leaf = statementPath.getLeaf();
            if (statement && StatementTree.class.isAssignableFrom(leaf.getKind().asInterface())) {
                break;
            }
            if (statementPath.getParentPath() != null) {
                switch (statementPath.getParentPath().getLeaf().getKind()) {
                    case BLOCK:
                    case CASE:
                    case LAMBDA_EXPRESSION:
                        break CYCLE;
                }
            }
            if (TreeUtilities.CLASS_TREE_KINDS.contains(statementPath.getLeaf().getKind())) {
                return null;
            }
            statementPath = statementPath.getParentPath();
        }
        return statementPath;
    }

    /**
     * Returns a path to the immediate enclosing method, lambda body or initializer block
     * @param path start of the search
     * @return path to the nearest enclosing executable or {@code null} in case of error.
     */
    static TreePath findMethod(TreePath path) {
        while (path != null) {
            Tree leaf = path.getLeaf();
            switch (leaf.getKind()) {
                case BLOCK:
                    if (path.getParentPath() != null && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                        return path;
                    }
                    break;
                case METHOD:
                case LAMBDA_EXPRESSION:
                    return path;
                default:
                    break;
            }
            path = path.getParentPath();
        }
        return null;
    }
    
}
