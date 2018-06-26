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

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class EmptyStatementHint extends HintRule {
    private static final String HINT_ID = "Empty.Statement.Hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.addAll(checkVisitor.getHints());
            }
        }
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;

        public CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return hints;
        }

        @Override
        @NbBundle.Messages("EmptyStatementHintText=Empty Statement")
        public void visit(EmptyStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            super.visit(node);
            if (isSemicolon(node)) {
                createHint(node);
            }
        }

        @Override
        public void visit(DeclareStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Statement body = node.getBody();
            // #259026 ignore declare();
            if (!(body instanceof EmptyStatement)) {
                super.visit(node);
            }
        }

        private void createHint(EmptyStatement node) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        EmptyStatementHint.this,
                        Bundle.EmptyStatementHintText(),
                        fileObject,
                        offsetRange,
                        Collections.<HintFix>singletonList(new Fix(node, baseDocument)),
                        500));
            }
        }

        private boolean isSemicolon(EmptyStatement node) {
            return (node.getEndOffset() - node.getStartOffset()) == 1;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("EmptyStatementHintDesc=Empty statements should be removed.")
    public String getDescription() {
        return Bundle.EmptyStatementHintDesc();
    }

    @Override
    @NbBundle.Messages("EmptyStatementHintDisp=Empty Statement")
    public String getDisplayName() {
        return Bundle.EmptyStatementHintDisp();
    }

    private static final class Fix implements HintFix {
        private final EmptyStatement node;
        private final BaseDocument baseDocument;

        private Fix(EmptyStatement node, BaseDocument baseDocument) {
            this.node = node;
            this.baseDocument = baseDocument;
        }

        @Override
        @NbBundle.Messages("EmptyStatementHintFix=Remove Empty Statement")
        public String getDescription() {
            return Bundle.EmptyStatementHintFix();
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(baseDocument);
            int removeLength = node.getEndOffset() - node.getStartOffset();
            editList.replace(node.getStartOffset(), removeLength, "", true, 0); //NOI18N
            editList.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

}
