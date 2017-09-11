/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineVariableTransformer extends RefactoringVisitor {

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
