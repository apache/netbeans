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
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class PHP74UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP74UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP74UnhandledError_displayName();
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
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_74);
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkArrowFunction(node);
            super.visit(node);
        }

        @Override
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNullCoalescingAssignment(node);
            super.visit(node);
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypedProperties(node);
            super.visit(node);
        }

        @Override
        public void visit(UnpackableArrayElement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkUnpackableArrayElement(node);
            super.visit(node);
        }

        @Override
        public void visit(Scalar scalar) {
            // Numeric Literal Separator
            // https://wiki.php.net/rfc/numeric_literal_separator
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNumericLiteralSeparator(scalar);
            super.visit(scalar);
        }

        private void checkNullCoalescingAssignment(Assignment node) {
            if (node.getOperator() == Assignment.Type.COALESCE_EQUAL) { // ??=
                createError(node);
            }
        }

        private void checkTypedProperties(FieldsDeclaration node) {
            if (node.getFieldType() != null) {
                createError(node);
            }
        }

        private void checkUnpackableArrayElement(UnpackableArrayElement node) {
            createError(node);
        }

        private void checkNumericLiteralSeparator(Scalar node) {
            if (node.getScalarType() == Scalar.Type.INT
                    || node.getScalarType() == Scalar.Type.REAL) {
                if (node.getStringValue().contains("_")) { // NOI18N
                    createError(node);
                }
            }
        }

        private void checkArrowFunction(ArrowFunctionDeclaration node) {
            createError(node);
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP74VersionError(fileObject, startOffset, endOffset));
        }
    }

    private static final class PHP74VersionError extends VerificationError {

        private static final String KEY = "Php.Version.74"; // NOI18N

        private PHP74VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP74VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP74VersionError_displayName();
        }

        @NbBundle.Messages("PHP74VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP74VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

}
