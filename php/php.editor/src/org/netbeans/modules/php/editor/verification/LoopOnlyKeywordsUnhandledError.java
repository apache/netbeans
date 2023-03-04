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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BreakStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LoopOnlyKeywordsUnhandledError extends UnhandledErrorRule {

    @Override
    public void invoke(PHPRuleContext context, List<org.netbeans.modules.csl.api.Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            LoopOnlyKeywordsUnhandledError.CheckVisitor checkVisitor = new LoopOnlyKeywordsUnhandledError.CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private static class CheckVisitor extends DefaultTreePathVisitor {
        private final FileObject fileObject;
        private static final String CONTINUE = "continue"; //NOI18N
        private static final String BREAK = "break"; //NOI18N
        private final List<VerificationError> errors = new ArrayList<>();

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(final ContinueStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkProperPath(node);
            super.visit(node);
        }

        @Override
        public void visit(final BreakStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkProperPath(node);
            super.visit(node);
        }

        private void checkProperPath(final ASTNode node) {
            if (!isInProperControlStructure()) {
                createError(node);
            }
        }

        private boolean isInProperControlStructure() {
            boolean result = false;
            for (ASTNode node : getPath()) {
                if (isProperStructure(node)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        private static boolean isProperStructure(final ASTNode node) {
            return node instanceof ForStatement
                    || node instanceof ForEachStatement
                    || node instanceof WhileStatement
                    || node instanceof DoStatement
                    || node instanceof SwitchStatement;
        }

        private void createError(final ASTNode node) {
            errors.add(new LoopOnlyKeywordsError(fileObject, node.getStartOffset(), node.getEndOffset(), extractKeyword(node)));
        }

        private String extractKeyword(final ASTNode node) {
            String result = ""; //NOI18N
            if (node instanceof ContinueStatement) {
                result = CONTINUE;
            } else if (node instanceof BreakStatement) {
                result = BREAK;
            }
            return result;
        }

    }

    private static class LoopOnlyKeywordsError extends VerificationError {

        private static final String KEY = "Loop.Only.Keywors.Error"; //NOI18N
        private final String nameOfKeyword;

        public LoopOnlyKeywordsError(final FileObject fileObject, final int startOffset, final int endOffset, final String nameOfKeyword) {
            super(fileObject, startOffset, endOffset);
            this.nameOfKeyword = nameOfKeyword;
        }

        @Override
        @Messages({
            "# {0} - name of keyword",
            "LoopOnlyKeywordsDisp={0} outside of for, foreach, while, do-while or switch statement."
        })
        public String getDisplayName() {
            return Bundle.LoopOnlyKeywordsDisp(nameOfKeyword);
        }

        @Override
        @Messages("LoopOnlyKeywordsDesc=Checks whether the keyword is used in a proper control structure.")
        public String getDescription() {
            return Bundle.LoopOnlyKeywordsDesc();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

    @Override
    @Messages("LoopOnlyKeywordsDispName=Keyword outside of for, foreach, while, do-while or switch statement.")
    public String getDisplayName() {
        return Bundle.LoopOnlyKeywordsDispName();
    }

}
