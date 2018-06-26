/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
