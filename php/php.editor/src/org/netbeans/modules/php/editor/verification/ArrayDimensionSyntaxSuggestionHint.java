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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension;
import org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Suggest using brackets for accessing arrays instead of curly braces. {} (e.g.
 * {@code $array{0}}) syntax is deprecated since PHP 7.4.
 *
 * @see https://wiki.php.net/rfc/deprecate_curly_braces_array_access
 */
public class ArrayDimensionSyntaxSuggestionHint extends HintRule {

    private static final String HINT_ID = "Array.Dimension.Syntax.Suggestion.Hint"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(ArrayDimensionSyntaxSuggestionHint.class.getName());

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("ArrayDimensionSyntaxSuggestion.Description=Curly brace syntax(\"{}\") for accessing array elements is deprecated since PHP 7.4. Instead, suggest using bracket syntax(\"[]\").")
    public String getDescription() {
        return Bundle.ArrayDimensionSyntaxSuggestion_Description();
    }

    @Override
    @NbBundle.Messages("ArrayDimensionSyntaxSuggestion.DisplayName=Array Dimension Syntax")
    public String getDisplayName() {
        return Bundle.ArrayDimensionSyntaxSuggestion_DisplayName();
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

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
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null) {
            CheckVisitor checkVisitor = new CheckVisitor(fileObject, this, doc);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            result.addAll(checkVisitor.getHints());
        }
    }

    // for unit tests
    boolean isFixAllEnabled() {
        return true;
    }

    //~ Innser classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final ArrayDimensionSyntaxSuggestionHint suggestion;
        private final BaseDocument document;
        private final List<FixInfo> fixInfos = new ArrayList<>();

        public CheckVisitor(FileObject fileObject, ArrayDimensionSyntaxSuggestionHint suggestion, BaseDocument document) {
            this.fileObject = fileObject;
            this.suggestion = suggestion;
            this.document = document;
        }

        @NbBundle.Messages("ArrayDimensionSyntaxSuggestion.Hint.Description=Curly brace syntax(\"{}\") is deprecated since PHP 7.4")
        public List<Hint> getHints() {
            List<Hint> hints = new ArrayList<>();
            int originalLineIndex = 0;
            for (FixInfo fixInfo : fixInfos) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                if (document.getLength() > fixInfo.getOffsetRange().getStart()) {
                    if (suggestion.isFixAllEnabled()) {
                        try {
                            // all
                            int lineIndex = LineDocumentUtils.getLineIndex(document, fixInfo.getOffsetRange().getStart());
                            if (originalLineIndex != lineIndex) {
                                int lineStart = LineDocumentUtils.getLineStart(document, fixInfo.getOffsetRange().getStart());
                                int lineEnd = LineDocumentUtils.getLineEnd(document, fixInfo.getOffsetRange().getStart());
                                addHint(hints, new OffsetRange(lineStart, lineEnd), createAllFixes(fixInfos));
                                originalLineIndex = lineIndex;
                            }
                        } catch (BadLocationException ex) {
                            String filePath = fileObject == null ? "no file" : FileUtil.toFile(fileObject).getAbsolutePath(); // NOI18N
                            LOGGER.log(Level.WARNING, "Invalid offset: {0} {1}", new Object[]{ex.offsetRequested(), filePath}); // NOI18N
                        }
                    }
                    addHint(hints, fixInfo.getOffsetRange(), createFixes(fixInfo));
                }
            }
            return hints;
        }

        private void addHint(List<Hint> hints, OffsetRange offsetRange, List<HintFix> fixes) {
            hints.add(new Hint(
                    suggestion,
                    Bundle.ArrayDimensionSyntaxSuggestion_Hint_Description(),
                    fileObject,
                    offsetRange,
                    fixes,
                    500
            ));
        }

        private List<HintFix> createFixes(FixInfo fixInfo) {
            List<HintFix> hintFixes = new ArrayList<>();
            hintFixes.add(fixInfo.createFix(document));
            return hintFixes;
        }

        private List<HintFix> createAllFixes(List<FixInfo> fixInfos) {
            List<HintFix> hintFixes = new ArrayList<>();
            ArrayAccessingSyntaxSuggestionFix fix = new ArrayAccessingSyntaxSuggestionFix(fixInfos, document, true);
            hintFixes.add(fix);
            return hintFixes;
        }

        @Override
        public void visit(ArrayAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArrayDimension(node.getDimension());
            super.visit(node);
        }

        @Override
        public void visit(DereferencedArrayAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArrayDimension(node.getDimension());
            super.visit(node);
        }

        @Override
        public void visit(ExpressionArrayAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArrayDimension(node.getDimension());
            super.visit(node);
        }

        private void processArrayDimension(ArrayDimension arrayDimension) {
            if (arrayDimension.getType() == ArrayDimension.Type.VARIABLE_HASHTABLE) {
                int startOffset = arrayDimension.getStartOffset();
                int endOffset = arrayDimension.getEndOffset();
                OffsetRange headRange = new OffsetRange(startOffset, startOffset + 1);
                OffsetRange tailRange = new OffsetRange(endOffset - 1, endOffset);
                fixInfos.add(new FixInfo(headRange, tailRange, new OffsetRange(startOffset, endOffset)));
            }
        }

    }

    private static final class FixInfo {

        private final OffsetRange headRange;
        private final OffsetRange tailRange;
        private final OffsetRange offsetRange;

        public FixInfo(OffsetRange headRange, OffsetRange tailRange, OffsetRange offsetRange) {
            this.headRange = headRange;
            this.tailRange = tailRange;
            this.offsetRange = offsetRange;
        }

        public OffsetRange getHeadRange() {
            return headRange;
        }

        public OffsetRange getTailRange() {
            return tailRange;
        }

        public OffsetRange getOffsetRange() {
            return offsetRange;
        }

        private HintFix createFix(BaseDocument document) {
            return new ArrayAccessingSyntaxSuggestionFix(Collections.singletonList(this), document);
        }

    }

    private static final class ArrayAccessingSyntaxSuggestionFix implements HintFix {

        private final List<FixInfo> fixInfos;
        private final BaseDocument document;
        private final boolean isAll;

        private ArrayAccessingSyntaxSuggestionFix(List<FixInfo> fixInfos, BaseDocument document) {
            this(fixInfos, document, false);
        }

        private ArrayAccessingSyntaxSuggestionFix(List<FixInfo> fixInfos, BaseDocument document, boolean isAll) {
            this.fixInfos = fixInfos;
            this.document = document;
            this.isAll = isAll;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - array dimension",
            "ArrayDimensionSyntaxSuggestion.Fix.Description=Use Bracket Syntax ({0})",
            "ArrayDimensionSyntaxSuggestion.Fix.All.Description=Use Bracket Syntax (All)"
        })
        public String getDescription() {
            if (isAll) {
                return Bundle.ArrayDimensionSyntaxSuggestion_Fix_All_Description();
            }
            assert !fixInfos.isEmpty();
            FixInfo fixInfo = fixInfos.get(0);
            String arrayDimension = ""; // NOI18N
            if (document.getLength() >= fixInfo.getOffsetRange().getEnd()) {
                try {
                    arrayDimension = document.getText(fixInfo.getOffsetRange().getStart(), fixInfo.getOffsetRange().getLength());
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested()); // NOI18N
                }
            }
            assert !arrayDimension.isEmpty();
            return Bundle.ArrayDimensionSyntaxSuggestion_Fix_Description(arrayDimension);
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            for (FixInfo fixInfo : fixInfos) {
                // don't format because there is a problem in the following case
                // whitespace is added behind "]"
                // $array{1}{2}{$test}; -> $array{1}[2] {$test};
                OffsetRange headRange = fixInfo.getHeadRange();
                edits.replace(headRange.getStart(), headRange.getLength(), "[", false, 0); // NOI18N
                OffsetRange tailRange = fixInfo.getTailRange();
                edits.replace(tailRange.getStart(), tailRange.getLength(), "]", false, 0); // NOI18N
            }
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
}
