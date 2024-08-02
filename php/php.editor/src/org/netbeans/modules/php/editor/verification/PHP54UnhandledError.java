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
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP54UnhandledError extends UnhandledErrorRule {

    @Override
    public void invoke(PHPRuleContext context, List<org.netbeans.modules.csl.api.Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            PHP54UnhandledError.CheckVisitor checkVisitor = new PHP54UnhandledError.CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    public static  boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_54);
    }

    private static class CheckVisitor extends DefaultVisitor {
        private static final String BINARY_PREFIX = "0b"; //NOI18N
        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;
        private boolean checkAnonymousObjectVariable;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Identifier name = node.getName();
            if (name != null) {
                createError(name);
            } else {
                createError(node);
            }
        }

        @Override
        public void visit(UseTraitStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
        }

        @Override
        public void visit(MethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkAnonymousObjectVariable = true;
            super.visit(node);
            checkAnonymousObjectVariable = false;
        }

        @Override
        public void visit(FieldAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkAnonymousObjectVariable = true;
            super.visit(node);
            checkAnonymousObjectVariable = false;
        }

        @Override
        public void visit(AnonymousObjectVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (checkAnonymousObjectVariable) {
                createError(node);
            }
        }

        @Override
        public void visit(DereferencedArrayAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
        }

        @Override
        public void visit(Scalar node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if ((node.getScalarType().equals(Scalar.Type.FLOAT) || node.getScalarType().equals(Scalar.Type.REAL)) && node.getStringValue().startsWith(BINARY_PREFIX)) {
                createError(node);
            }
            if (node.getScalarType().equals(Scalar.Type.SYSTEM) && "__TRAIT__".equals(node.getStringValue())) { // NOI18N
                createError(node);
            }
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression name = node.getMethod().getFunctionName().getName();
            if (name instanceof ReflectionVariable) {
                createError(name);
            }
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isStatic()) {
                createError(node);
            } else {
                checkCallableType(node.getFormalParameters());
            }
        }

        @Override
        public void visit(ArrayCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ArrayCreation.Type type = node.getType();
            if (type == ArrayCreation.Type.NEW) {
                createError(node);
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!checkCallableType(node.getFormalParameters())) {
                super.visit(node);
            }
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!checkCallableType(node.getFunction().getFormalParameters())) {
                super.visit(node);
            }
        }

        private boolean checkCallableType(List<FormalParameter> formalParameters) {
            for (FormalParameter formalParameter : formalParameters) {
                // nullable types are checked in PHP71UnhandledError, so just ignore "?"
                String typeName = CodeUtils.extractUnqualifiedTypeName(formalParameter);
                if (Type.CALLABLE.equals(typeName)) {
                    createError(formalParameter);
                    return true;
                }
            }
            return false;
        }

        private  void createError(int startOffset, int endOffset) {
            VerificationError error = new PHP54VersionError(fileObject, startOffset, endOffset);
            errors.add(error);
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
            super.visit(node);
        }

    }

    private static final class PHP54VersionError extends VerificationError {

        private static final String KEY = "Php.Version.54"; //NOI18N

        private PHP54VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @Override
        @Messages("CheckPHP54VerDisp=Language feature not compatible with PHP version indicated in project settings")
        public String getDisplayName() {
            return Bundle.CheckPHP54VerDisp();
        }

        @Override
        @Messages("CheckPHP54VerDesc=Detect language features not compatible with PHP version indicated in project settings")
        public String getDescription() {
            return Bundle.CheckPHP54VerDesc();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

    @Override
    @Messages("PHP54VersionErrorHintDispName=Language feature not compatible with PHP version indicated in project settings")
    public String getDisplayName() {
        return Bundle.PHP54VersionErrorHintDispName();
    }

}
