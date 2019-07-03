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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle unusable types as errors here to avoid complicating the grammar(cup
 * file).
 *
 */
public class UnusableTypesUnhandledError extends UnhandledErrorRule {

    @Override
    @NbBundle.Messages("UnusableTypesUnhandledError.displayName=Unusable types.")
    public String getDisplayName() {
        return Bundle.UnusableTypesUnhandledError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject);
                phpParseResult.getProgram().accept(checkVisitor);
                result.addAll(checkVisitor.getErrors());
            }
        }
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        private CheckVisitor(FileObject fileObject) {
            assert fileObject != null;
            this.fileObject = fileObject;
        }

        private List<VerificationError> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression fieldType = node.getFieldType();
            if (fieldType != null) {
                checkFieldType(fieldType);
            }
            super.visit(node);
        }

        @Override
        public void visit(FormalParameter node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression parameterType = node.getParameterType();
            if (parameterType != null) {
                checkParameterType(parameterType);
            }
            super.visit(node);
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // PHP 7.4
            // fn(): void => $y;
            // arrow function returns something, so we would get the following
            // "Fatal error: A void function must not return a value"
            Expression returnType = node.getReturnType();
            if (returnType != null) {
                checkArrowFunctionReturnType(returnType);
            }
            super.visit(node);
        }

        private void checkFieldType(Expression fieldType) {
            // unusable types: void and callable PHP 7.4
            Expression type = fieldType;
            if (fieldType instanceof NullableType) {
                type = ((NullableType) fieldType).getType();
            }
            if (type == null) {
                return;
            }
            if (type instanceof Identifier) {
                if (isCallableType((Identifier) type)) {
                    createError(type, "callable", "property"); // NOI18N
                }
            } else if (type instanceof NamespaceName) {
                if (isVoidType((NamespaceName) type)) {
                    createError(type, "void", "property"); // NOI18N
                }
            }
        }

        private void checkParameterType(Expression parameterType) {
            // unusable type: void
            if (parameterType instanceof NamespaceName) {
                if (isVoidType((NamespaceName) parameterType)) {
                    createError(parameterType, "void", "parameter"); // NOI18N
                }
            }
        }

        private void checkArrowFunctionReturnType(Expression returnType) {
            // unusable type: void
            if (returnType instanceof NamespaceName) {
                if (isVoidType((NamespaceName) returnType)) {
                    createError(returnType, "void", "return"); // NOI18N
                }
            }
        }

        private void createError(ASTNode node, String type, String context) {
            createError(node.getStartOffset(), node.getEndOffset(), type, context);
        }

        private void createError(int startOffset, int endOffset, String type, String context) {
            errors.add(new UnusableType(fileObject, startOffset, endOffset, type, context));
        }

        private static boolean isCallableType(Identifier identifier) {
            return !identifier.isKeyword()
                    && "callable".equals(identifier.getName().toLowerCase()); // NOI18N
        }

        private static boolean isVoidType(NamespaceName namespaceName) {
            return "void".equals(CodeUtils.extractUnqualifiedName(namespaceName)); // NOI18N
        }
    }

    private static final class UnusableType extends VerificationError {

        private static final String KEY = "Php.Unusable.Type"; // NOI18N
        private final String type;
        private final String context;

        private UnusableType(FileObject fileObject, int startOffset, int endOffset, String type, String context) {
            super(fileObject, startOffset, endOffset);
            this.type = type;
            this.context = context;
        }

        @NbBundle.Messages({
            "# {0} - type",
            "# {1} - context",
            "UnusableType.displayName=Unusable type: \"{0}\" cannot be used as {1} type."
        })
        @Override
        public String getDisplayName() {
            return Bundle.UnusableType_displayName(type, context);
        }

        @NbBundle.Messages({
            "# {0} - type",
            "# {1} - context",
            "UnusableType.description=\"{0}\" cannot be used as {1} type."
        })
        @Override
        public String getDescription() {
            return Bundle.UnusableType_description(type, context);
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

}
