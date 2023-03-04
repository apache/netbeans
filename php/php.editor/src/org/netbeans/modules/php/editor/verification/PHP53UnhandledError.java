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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP53UnhandledError extends UnhandledErrorRule {

    @Override
    public void invoke(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_53);
    }

    private static class CheckVisitor extends DefaultVisitor {
        private final List<Error> errors = new ArrayList<>();
        private final ArrayDeque<ASTNode> parent = new ArrayDeque<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public List<Error> getErrors() {
            return errors;
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parent.push(node);
            super.visit(node);
            parent.pop();
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parent.push(node);
            super.visit(node);
            parent.pop();
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            final NamespaceName name = node.getName();
            if (name != null) {
                createError(node.getStartOffset(), name.getEndOffset());
            } else {
                createError(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(GotoLabel node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!parent.isEmpty()
                    && parent.peek() instanceof TypeDeclaration) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(GotoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(UseStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getDispatcher() instanceof Variable) {
                createError(node.getDispatcher());
            }
            super.visit(node);
        }

        @Override
        public void visit(StaticFieldAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getDispatcher() instanceof Variable) {
                createError(node.getDispatcher());
            }
            super.visit(node);
        }

        @Override
        public void visit(StaticConstantAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getDispatcher() instanceof Variable) {
                createError(node.getDispatcher());
            }
            super.visit(node);
        }

        @Override
        public void visit(NamespaceName node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            QualifiedName qname = QualifiedName.create(node);
            if (qname.getKind() != QualifiedNameKind.UNQUALIFIED) {
                createError(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(ConditionalExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (ConditionalExpression.OperatorType.ELVIS.equals(node.getOperator())) {
                createError(node);
            }
            super.visit(node);
        }

        private  void createError(int startOffset, int endOffset) {
            errors.add(new PhpVersionError(fileObject, startOffset, endOffset));
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

    }

    private static class PhpVersionError extends VerificationError {
        private static final String ERROR_KEY = "Php.Version.53"; //NOI18N

        public PhpVersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @Override
        @Messages("PhpVersionErrorDisp=Language feature not compatible with PHP version indicated in project settings")
        public String getDisplayName() {
            return Bundle.PhpVersionErrorDisp();
        }

        @Override
        @Messages("PhpVersionErrorDesc=Detect language features not compatible with PHP version indicated in project settings")
        public String getDescription() {
            return Bundle.PhpVersionErrorDesc();
        }

        @Override
        public String getKey() {
            return ERROR_KEY;
        }

    }

    @Override
    @Messages("PHP53VersionErrorHintDispName=Language feature not compatible with PHP version indicated in project settings")
    public String getDisplayName() {
        return Bundle.PHP53VersionErrorHintDispName();
    }

}
