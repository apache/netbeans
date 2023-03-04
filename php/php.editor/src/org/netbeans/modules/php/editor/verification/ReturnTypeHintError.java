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
 * Check "void" and "never" return type.
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

    // support only "void" and "never"
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
            // void type can never be part of a union type
            // handle that in UnusableTypesUnhandledError
            if (returnType instanceof NamespaceName) {
                NamespaceName namespaceName = (NamespaceName) returnType;
                String name = CodeUtils.extractUnqualifiedName(namespaceName);
                checkVoidAndNeverReturnStatements(statements, name, hints);
            } else if (returnType instanceof NullableType) {
                Expression type = ((NullableType) returnType).getType();
                if (type instanceof NamespaceName) {
                    NamespaceName namespaceName = (NamespaceName) type;
                    String name = CodeUtils.extractUnqualifiedName(namespaceName);
                    checkInvalidVoidAndNeverReturnType(type, name, hints);
                }
            }

        }
    }

    @NbBundle.Messages({
        "# {0} - type",
        "ReturnTypeHintErrorVoidDesc=\"{0}\" cannot return anything"
    })
    private void checkVoidAndNeverReturnStatements(Set<ReturnStatement> statements, String name, List<Hint> hints) {
        if (Type.VOID.equals(name) || isNeverType(name)) {
            // check empty return statement
            statements.forEach((statement) -> {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                Expression expression = statement.getExpression();
                if (expression != null || isNeverType(name)) {
                    addHint(statement, Bundle.ReturnTypeHintErrorVoidDesc(name), hints);
                }
            });
        }
    }

    @NbBundle.Messages({
        "# {0} - type",
        "ReturnTypeHintErrorInvalidVoidDesc=\"{0}\" cannot be used with \"?\""
    })
    private void checkInvalidVoidAndNeverReturnType(Expression returnType, String name, List<Hint> hints) {
        if (Type.VOID.equals(name)
                || isNeverType(name)) {
            addHint(returnType, Bundle.ReturnTypeHintErrorInvalidVoidDesc(name), hints);
        }
    }

    private boolean isNeverType(String name) {
        return getPhpVersion(fileObject).hasNeverType()
                && Type.NEVER.equals(name);
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
                    returns = new HashSet<>();
                    returnStatements.put(node, returns);
                }
                if (returnStatement != null) {
                    returns.add(returnStatement);
                }
            }
        }

    }
}
