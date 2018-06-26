/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
