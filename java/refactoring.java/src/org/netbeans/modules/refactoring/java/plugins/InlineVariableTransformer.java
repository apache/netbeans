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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineVariableTransformer extends RefactoringVisitor {

    private final FileObject sourceFile;

    public InlineVariableTransformer(TreePathHandle treePathHandle) {
        this.sourceFile = treePathHandle.getFileObject();
    }

    
    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        SourceUtils.forceSource(workingCopy, sourceFile);
        super.setWorkingCopy(workingCopy);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        replaceUsageIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        replaceUsageIfMatch(getCurrentPath(), node, p);
        return super.visitMemberSelect(node, p);
    }

    private void replaceUsageIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path)) {
            return;
        }
        Trees trees = workingCopy.getTrees();
        Element el = workingCopy.getTrees().getElement(path);
        if (el == null) {
            path = path.getParentPath();
            if (path != null && path.getLeaf().getKind() == Tree.Kind.IMPORT) {
                ImportTree impTree = (ImportTree) path.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Tree.Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                if (id == null || id.contentEquals("*")) { // NOI18N
                    // skip import static java.lang.Math.*
                    return;
                }
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                path = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(path);
                if (el == null) {
                    return;
                }
                Iterator<? extends Element> iter = workingCopy.getElementUtilities().getMembers(el.asType(), new ElementUtilities.ElementAcceptor() {

                    @Override
                    public boolean accept(Element e, TypeMirror type) {
                        return id.equals(e.getSimpleName());
                    }
                }).iterator();
                if (iter.hasNext()) {
                    el = iter.next();
                }
                if (iter.hasNext()) {
                    return;
                }
            } else {
                return;
            }
        }
        if (el.equals(elementToFind)) {
            GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
            TreePath resolvedPath = trees.getPath(elementToFind);
            VariableTree varTree = (VariableTree)resolvedPath.getLeaf();
            varTree = genUtils.importComments(varTree, resolvedPath.getCompilationUnit());
            ExpressionTree body = varTree.getInitializer();

            boolean parenthesize = OperatorPrecedence.needsParentheses(path, elementToFind, varTree.getInitializer(), workingCopy);
            if (parenthesize) {
                body = make.Parenthesized((ExpressionTree) body);
            }

            genUtils.copyComments(varTree, body, true);
            rewrite(tree, body);
        }
    }

    @Override
    public Tree visitVariable(VariableTree node, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (p.equals(el)) {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            switch(el.getKind()) {
                case LOCAL_VARIABLE:
                    Tree newOne = null;
                    if(parent.getKind() == Tree.Kind.CASE) {
                        newOne = make.removeCaseStatement((CaseTree) parent, node);
                    } else {
                        newOne = make.removeBlockStatement((BlockTree) parent, node);
                    }
                    if (newOne != null) {
                        rewrite(parent, newOne);
                    }
                    break;
                case FIELD:
                    ClassTree removeClassMember = make.removeClassMember((ClassTree)parent, node);
                    if (removeClassMember != null) {
                        rewrite(parent, removeClassMember);
                    }
                    break;
            }
        }
        return super.visitVariable(node, p);
    }
}
