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
