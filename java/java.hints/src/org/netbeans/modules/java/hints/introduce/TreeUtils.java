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
        return findMethod(path, false);
    }
    
    static TreePath findMethod(TreePath path, boolean methodOnly) {
        while (path != null) {
            Tree leaf = path.getLeaf();
            switch (leaf.getKind()) {
                case BLOCK:
                    if (path.getParentPath() != null && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                        return path;
                    }
                    break;
                case LAMBDA_EXPRESSION:
                    if (methodOnly) {
                        break;
                    }
                case METHOD:
                    return path;
                default:
                    break;
            }
            path = path.getParentPath();
        }
        return null;
    }
    
}
