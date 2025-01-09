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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreationVariable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class PHP84UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP84UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP84UnhandledError_displayName();
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
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_84);
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
        public void visit(ClassInstanceCreationVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // new without parentheses
            // e.g.
            // new A()->method();
            // new class() {}::staticMethod();
            createError(node);
            super.visit(node);
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(OffsetRange offsetRange) {
            errors.add(new PHP84VersionError(fileObject, offsetRange.getStart(), offsetRange.getEnd()));
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP84VersionError(fileObject, startOffset, endOffset));
        }
    }

    private static final class PHP84VersionError extends VerificationError {

        private static final String KEY = "Php.Version.84"; // NOI18N

        private PHP84VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP84VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP83VersionError_displayName();
        }

        @NbBundle.Messages("PHP84VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP83VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

}
