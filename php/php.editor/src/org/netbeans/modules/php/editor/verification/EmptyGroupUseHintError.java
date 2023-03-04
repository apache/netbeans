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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class EmptyGroupUseHintError extends UnhandledErrorRule {

    private static final Logger LOGGER = Logger.getLogger(EmptyGroupUseHintError.class.getName());
    private static final Pattern GROUP_USE_PATTERN = Pattern.compile(".*\\{(.+)\\}", Pattern.DOTALL); // NOI18N

    @NbBundle.Messages("EmptyGroupUseHintError.displayName=Syntax error, syntax error, unexpected '}', expecting identifier.")
    @Override
    public String getDisplayName() {
        return Bundle.EmptyGroupUseHintError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                result.addAll(checkVisitor.getErrors());
            }
        }
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;
        private final BaseDocument baseDocument;

        CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            assert fileObject != null;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
        }

        List<VerificationError> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        @Override
        public void visit(UseStatement statement) {
            super.visit(statement);
        }

        @Override
        public void visit(GroupUseStatementPart statementPart) {
            List<SingleUseStatementPart> items = statementPart.getItems();
            if (items.isEmpty()) {
                // check whether there are some chars within the block
                int startOffset = statementPart.getStartOffset();
                int endOffset = statementPart.getEndOffset();
                int length = endOffset - startOffset;
                if (length > 0) {
                    try {
                        String statementText = baseDocument.getText(startOffset, length);
                        Matcher matcher = GROUP_USE_PATTERN.matcher(statementText);
                        if (matcher.matches()) {
                            String blockText = matcher.group(1);
                            String trimed = blockText.trim();
                            if (trimed.length() > 0) {
                                return;
                            }
                        }
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                }
                createError(statementPart);
            }
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new EmptyGroupUse(fileObject, startOffset, endOffset));
        }

    }

    private static final class EmptyGroupUse extends VerificationError {

        private static final String KEY = "Php.Empty.Group.Use"; // NOI18N

        EmptyGroupUse(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("EmptyGroupUse.displayName=syntax error, unexpected '}', expecting identifier.")
        @Override
        public String getDisplayName() {
            return Bundle.EmptyGroupUse_displayName();
        }

        @NbBundle.Messages("EmptyGroupUse.description=Group use declarations cannot be empty.")
        @Override
        public String getDescription() {
            return Bundle.EmptyGroupUse_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

}
