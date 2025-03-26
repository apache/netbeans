/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Don't check "never" type because it may be used in older versions as a class
 * name.
 */
public final class PHP81UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP81UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP81UnhandledError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null
                && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            TokenSequence<PHPTokenId> ts = phpParseResult.getSnapshot().getTokenHierarchy().tokenSequence(PHPTokenId.language());
            assert ts != null;
            errors.addAll(checkVisitor.getErrors(ts));
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_81);
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors(TokenSequence<PHPTokenId> ts) {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkConstantDeclaration(node);
            super.visit(node);
        }

        @Override
        public void visit(ConstantVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // e.g.
            // const CONSTANT = new Example();
            // CONSTANT->field;
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(IntersectionType node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(StaticStatement node) {
            // static $a = new A();
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            for (Expression expression : node.getExpressions()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (expression instanceof Assignment) {
                    Assignment assignment = (Assignment) expression;
                    if (assignment.getOperator() == Assignment.Type.EQUAL) {
                        checkNewInInitializer(assignment.getRightHandSide());
                    }
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(FormalParameter node) {
            // function func($param = new A()) {}
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNewInInitializer(node.getDefaultValue());
            super.visit(node);
        }

        @Override
        public void visit(AttributeDeclaration attributeDeclaration) {
            // #[AnAttribute(new A())]
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            List<Expression> parameters = attributeDeclaration.getParameters();
            // #[MyAttribute] this case is null
            if (parameters != null) {
                for (Expression parameter : parameters) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    checkNewInInitializer(parameter);
                }
            }
            super.visit(attributeDeclaration);
        }

        @Override
        public void visit(EnumDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(CaseDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(FirstClassCallableArg node) {
            // e.g. strlen(...)
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
            super.visit(node);
        }

        @Override
        public void visit(Scalar scalar) {
            // Check explicit octal integer literal notation
            // e.g. 0o16, 0O16, 0o1_6
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkExplicitOctalNotation(scalar);
            super.visit(scalar);
        }

        private void checkConstantDeclaration(ConstantDeclaration constantDeclaration) {
            if (BodyDeclaration.Modifier.isFinal(constantDeclaration.getModifier())) {
                for (Identifier name : constantDeclaration.getNames()) {
                    createError(name);
                }
            }
            // New in initializer
            // const CONSTANT = new Constant();
            if (constantDeclaration.isGlobal()) {
                for (Expression initializer : constantDeclaration.getInitializers()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    checkNewInInitializer(initializer);
                }
            }
        }

        private void checkNewInInitializer(Expression node) {
            if (node instanceof ClassInstanceCreation) {
                createError(node);
            }
        }

        private void checkExplicitOctalNotation(Scalar scalar) {
            if (isNumber(scalar) && isOctal(scalar)) {
                createError(scalar);
            }
        }

        private boolean isNumber(Scalar scalar) {
            return scalar.getScalarType() == Scalar.Type.INT
                    || scalar.getScalarType() == Scalar.Type.FLOAT
                    || scalar.getScalarType() == Scalar.Type.REAL;
        }

        private boolean isOctal(Scalar scalar) {
            return scalar.getStringValue().startsWith("0o") // NOI18N
                    || scalar.getStringValue().startsWith("0O"); // NOI18N
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP81VersionError(fileObject, startOffset, endOffset));
        }
    }

    private static final class PHP81VersionError extends VerificationError {

        private static final String KEY = "Php.Version.81"; // NOI18N

        private PHP81VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP81VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP81VersionError_displayName();
        }

        @NbBundle.Messages("PHP81VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP81VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

}
