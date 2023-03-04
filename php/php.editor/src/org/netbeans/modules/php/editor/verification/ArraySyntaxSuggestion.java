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
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ArraySyntaxSuggestion extends SuggestionRule {
    private static final String HINT_ID = "Array.Syntax.Suggestion"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final BaseDocument doc = context.doc;
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null && isAtLeastPhp54(fileObject)) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, this, context.doc, lineBounds);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    protected boolean isAtLeastPhp54(FileObject fileObject) {
        return CodeUtils.isPhpVersionGreaterThan(fileObject, PhpVersion.PHP_53);
    }

    private static final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final ArraySyntaxSuggestion suggestion;
        private final BaseDocument document;
        private final OffsetRange lineRange;
        private final List<FixInfo> fixInfos = new ArrayList<>();

        public CheckVisitor(FileObject fileObject, ArraySyntaxSuggestion suggestion, BaseDocument document, OffsetRange lineRange) {
            this.fileObject = fileObject;
            this.suggestion = suggestion;
            this.document = document;
            this.lineRange = lineRange;
        }

        @NbBundle.Messages("ArraySyntaxDesc=You can use new shorter array creation syntax")
        public List<Hint> getHints() {
            List<Hint> hints = new ArrayList<>();
            for (FixInfo fixInfo : fixInfos) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                hints.add(new Hint(suggestion, Bundle.ArraySyntaxDesc(), fileObject, fixInfo.getLineRange(), createFixes(fixInfo), 500));
            }
            return hints;
        }

        private List<HintFix> createFixes(FixInfo fixInfo) {
            List<HintFix> hintFixes = new ArrayList<>();
            hintFixes.add(fixInfo.createFix(document));
            return hintFixes;
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineRange.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ArrayCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processArrayCreation(node);
            }
        }

        private void processArrayCreation(ArrayCreation node) {
            if (node.getType().equals(ArrayCreation.Type.OLD)) {
                int startOffset = node.getStartOffset();
                int endOffset = node.getEndOffset();
                List<ArrayElement> elements = node.getElements();
                int headEnd = elements.isEmpty() ? endOffset - 1 : getFirstElementStart(elements);
                OffsetRange headRange = new OffsetRange(startOffset, headEnd);
                OffsetRange tailRange = new OffsetRange(endOffset - 1, endOffset);
                fixInfos.add(new FixInfo(headRange, tailRange, lineRange));
            }
            scan(node.getElements());
        }

        private static int getFirstElementStart(List<ArrayElement> elements) {
            assert elements != null;
            assert !elements.isEmpty();
            return elements.get(0).getStartOffset();
        }

    }

    private static final class FixInfo {
        private final OffsetRange headRange;
        private final OffsetRange tailRange;
        private final OffsetRange lineRange;

        public FixInfo(OffsetRange headRange, OffsetRange tailRange, OffsetRange lineRange) {
            this.headRange = headRange;
            this.tailRange = tailRange;
            this.lineRange = lineRange;
        }

        public OffsetRange getHeadRange() {
            return headRange;
        }

        public OffsetRange getTailRange() {
            return tailRange;
        }

        private OffsetRange getLineRange() {
            return lineRange;
        }

        private HintFix createFix(BaseDocument document) {
            return new Fix(this, document);
        }

    }

    private static final class Fix implements HintFix {
        private final FixInfo fixInfo;
        private final BaseDocument document;

        private Fix(FixInfo fixInfo, BaseDocument document) {
            this.fixInfo = fixInfo;
            this.document = document;
        }

        @Override
        @NbBundle.Messages("FixDesc=Use New Array Creation Syntax")
        public String getDescription() {
            return Bundle.FixDesc();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            OffsetRange headRange = fixInfo.getHeadRange();
            edits.replace(headRange.getStart(), headRange.getLength(), "[", true, 0); //NOI18N
            OffsetRange tailRange = fixInfo.getTailRange();
            edits.replace(tailRange.getStart(), tailRange.getLength(), "]", true, 0); //NOI18N
            edits.apply();
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

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("ArraySyntaxDescName=Allows you to change old array syntax to new shorter one.")
    public String getDescription() {
        return Bundle.ArraySyntaxDescName();
    }

    @Override
    @NbBundle.Messages("ArraySyntaxDispName=Array Syntax")
    public String getDisplayName() {
        return Bundle.ArraySyntaxDispName();
    }

}
