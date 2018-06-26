/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Check "void" return type.
 *
 */
public class ReturnTypeHintError extends HintErrorRule {

    private FileObject fileObject;

    @NbBundle.Messages("ReturnTypeHintErrorDisplayName=Invalid Return Type")
    @Override
    public String getDisplayName() {
        return Bundle.ReturnTypeHintErrorDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            Map<ASTNode, Set<ReturnStatement>> returnStatements = checkVisitor.getReturnStatements();
            checkReturnType(returnStatements, hints);
        }
    }

    protected PhpVersion getPhpVersion(@NullAllowed FileObject file) {
        if (file == null) {
            return PhpVersion.getDefault();
        }
        return CodeUtils.getPhpVersion(file);
    }

    private boolean appliesTo(FileObject file) {
        return getPhpVersion(file).compareTo(PhpVersion.PHP_71) >= 0;
    }

    // support only "void"
    // XXX support types?
    private void checkReturnType(Map<ASTNode, Set<ReturnStatement>> returnStatements, List<Hint> hints) {
        for (Entry<ASTNode, Set<ReturnStatement>> entry : returnStatements.entrySet()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ASTNode node = entry.getKey();
            Expression returnType = null;
            if (node instanceof FunctionDeclaration) {
                returnType = ((FunctionDeclaration) node).getReturnType();
            } else if (node instanceof LambdaFunctionDeclaration) {
                returnType = ((LambdaFunctionDeclaration) node).getReturnType();
            }

            if (returnType == null) {
                continue;
            }
            Set<ReturnStatement> statements = entry.getValue();
            if (returnType instanceof NamespaceName) {
                NamespaceName namespaceName = (NamespaceName) returnType;
                String name = CodeUtils.extractUnqualifiedName(namespaceName);
                checkVoidReturnStatements(statements, name, hints);
            } else if (returnType instanceof NullableType) {
                Expression type = ((NullableType) returnType).getType();
                if (type instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) type;
                    String name = CodeUtils.extractUnqualifiedName(namespaceName);
                    checkInvalidVoidReturnType(type, name, hints);
                }
            }

        }
    }

    @NbBundle.Messages({
        "ReturnTypeHintErrorVoidDesc=\"void\" cannot return anything"
    })
    private void checkVoidReturnStatements(Set<ReturnStatement> statements, String name, List<Hint> hints) {
        if (Type.VOID.equals(name)) {
            // check empty return statement
            statements.forEach((statement) -> {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                Expression expression = statement.getExpression();
                if (expression != null) {
                    addHint(statement, Bundle.ReturnTypeHintErrorVoidDesc(), hints);
                }
            });
        }
    }

    @NbBundle.Messages({
        "ReturnTypeHintErrorInvalidVoidDesc=\"void\" cannot be used with \"?\""
    })
    private void checkInvalidVoidReturnType(Expression returnType, String name, List<Hint> hints) {
        if (Type.VOID.equals(name)) {
            addHint(returnType, Bundle.ReturnTypeHintErrorInvalidVoidDesc(), hints);
        }
    }

    private void addHint(ASTNode node, String description, List<Hint> hints) {
        hints.add(new Hint(this,
                description,
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                Collections.<HintFix>emptyList(),
                500
        ));
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final Map<ASTNode, Set<ReturnStatement>> returnStatements = new HashMap<>();
        private final LinkedList<ASTNode> path = new LinkedList<>();

        public Map<ASTNode, Set<ReturnStatement>> getReturnStatements() {
            return Collections.unmodifiableMap(returnStatements);
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            path.addFirst(node);
            super.visit(node);
            // if there is no returnstatment, just add empty set
            addReturnStatement(node, null);
            path.removeFirst();
        }

        @Override
        public void visit(LambdaFunctionDeclaration declaration) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            path.addFirst(declaration);
            super.visit(declaration);
            // if there is no returnstatment, just add empty set
            addReturnStatement(declaration, null);
            path.removeFirst();
        }

        @Override
        public void visit(ReturnStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!path.isEmpty()) {
                addReturnStatement(path.get(0), node);
            }
        }

        private void addReturnStatement(ASTNode node, ReturnStatement returnStatement) {
            if (node != null) {
                Set<ReturnStatement> returns = returnStatements.get(node);
                if (returns == null) {
                    HashSet<ReturnStatement> statements = new HashSet<>();
                    if (returnStatement != null) {
                        statements.add(returnStatement);
                    }
                    returnStatements.put(node, statements);
                } else {
                    if (returnStatement != null) {
                        returns.add(returnStatement);
                    }
                }
            }
        }

    }
}
