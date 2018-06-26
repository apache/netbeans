/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
