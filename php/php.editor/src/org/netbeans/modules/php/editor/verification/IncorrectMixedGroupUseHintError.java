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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handles cases when incorrect mixed group uses are defined. For example:
 * <pre>
 * use const \A\ { function fnc };
 * </pre>
 * @see https://wiki.php.net/rfc/group_use_declarations#mixed_use_declarations
 */
public final class IncorrectMixedGroupUseHintError extends UnhandledErrorRule {

    @NbBundle.Messages("IncorrectMixedGroupUseHintError.displayName=Syntax error, unexpected 'const' or 'function', expecting identifier.")
    @Override
    public String getDisplayName() {
        return Bundle.IncorrectMixedGroupUseHintError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<org.netbeans.modules.csl.api.Error> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getErrors());
            }
        }
    }

    //~ Inner classes

    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        private UseStatement currentUseStatement;
        private GroupUseStatementPart currentGroupUseStatementPart;


        CheckVisitor(FileObject fileObject) {
            assert fileObject != null;
            this.fileObject = fileObject;
        }

        List<VerificationError> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        @Override
        public void visit(UseStatement statement) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentUseStatement = statement;
            super.visit(statement);
            currentUseStatement = null;
        }

        @Override
        public void visit(GroupUseStatementPart statementPart) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            currentGroupUseStatementPart = statementPart;
            super.visit(statementPart);
            currentGroupUseStatementPart = null;
        }

        @Override
        public void visit(SingleUseStatementPart statementPart) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkSingleUseStatement(statementPart);
            super.visit(statementPart);
        }

        private void checkSingleUseStatement(SingleUseStatementPart statementPart) {
            if (currentUseStatement == null
                    || currentGroupUseStatementPart == null) {
                return;
            }
            UseStatement.Type type = statementPart.getType();
            if (type == null) {
                return;
            }
            if (currentUseStatement.getType() != UseStatement.Type.TYPE) {
                createError(statementPart);
            }
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new IncorrectMixedGroupUse(fileObject, startOffset, endOffset));
        }

    }

    private static final class IncorrectMixedGroupUse extends VerificationError {

        private static final String KEY = "Php.Group.Use.Mixed"; // NOI18N


        IncorrectMixedGroupUse(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("IncorrectMixedGroupUse.displayName=Syntax error, unexpected 'const' or 'function', expecting identifier.")
        @Override
        public String getDisplayName() {
            return Bundle.IncorrectMixedGroupUse_displayName();
        }

        @NbBundle.Messages("IncorrectMixedGroupUse.description=Mixed use declarations cannot be used for 'const' or 'function' declarations.")
        @Override
        public String getDescription() {
            return Bundle.IncorrectMixedGroupUse_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

}
