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
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector.UnusedOffsetRanges;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@Messages("UnsedUsesHintDisp=Unused Use Statement")
public class UnusedUsesHint extends HintRule {

    private static final String HINT_ID = "Unused.Uses.Hint"; //NOI18N
    private List<Hint> hints;
    private BaseDocument baseDocument;
    private FileObject fileObject;

    @Override
    public void invoke(PHPRuleContext context, List<Hint> allHints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null
                || CodeUtils.isPhpVersion(fileObject, PhpVersion.PHP_5)) {
            return;
        }
        hints = allHints;
        baseDocument = context.doc;
        for (UnusedOffsetRanges unusedOffsetRanges : new UnusedUsesCollector(phpParseResult).collect()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createHint(unusedOffsetRanges);
        }
    }

    private void createHint(UnusedOffsetRanges unusedOffsetRanges) {
        OffsetRange offsetRange = unusedOffsetRanges.getRangeToVisualise();
        if (showHint(offsetRange, baseDocument)) {
            hints.add(new Hint(
                    UnusedUsesHint.this,
                    Bundle.UnsedUsesHintDisp(),
                    fileObject,
                    offsetRange,
                    createHintFixes(baseDocument, unusedOffsetRanges),
                    500));
        }
    }

    private List<HintFix> createHintFixes(final BaseDocument baseDocument, final UnusedOffsetRanges unusedOffsetRanges) {
        return Collections.<HintFix>singletonList(new RemoveUnusedUseFix(baseDocument, unusedOffsetRanges));
    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("UnusedUsesHintDesc=Checks unused use statements.")
    public String getDescription() {
        return Bundle.UnusedUsesHintDesc();
    }

    @Override
    public String getDisplayName() {
        return Bundle.UnsedUsesHintDisp();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    private static class RemoveUnusedUseFix implements HintFix {
        private final BaseDocument baseDocument;
        private final UnusedOffsetRanges unusedOffsetRanges;

        public RemoveUnusedUseFix(final BaseDocument baseDocument, final UnusedOffsetRanges unusedOffsetRanges) {
            this.baseDocument = baseDocument;
            this.unusedOffsetRanges = unusedOffsetRanges;
        }

        @Override
        @Messages("RemoveUnusedUseFixDesc=Remove Unused Use Statement")
        public String getDescription() {
            return Bundle.RemoveUnusedUseFixDesc();
        }

        @Override
        public void implement() throws Exception {
            final EditList editList = new EditList(baseDocument);
            OffsetRange offsetRange = unusedOffsetRanges.getRangeToReplace();
            int startOffset = getOffsetWithoutLeadingWhitespaces(offsetRange.getStart());
            editList.replace(startOffset, offsetRange.getEnd() - startOffset, "", true, 0); //NOI18N
            editList.apply();
        }

        private int getOffsetWithoutLeadingWhitespaces(final int startOffset) {
            int result = startOffset;
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(baseDocument, startOffset);
            if (ts != null) {
                ts.move(startOffset);
                while (ts.movePrevious() && ts.token().id().equals(PHPTokenId.WHITESPACE)) {
                    result = ts.offset();
                }
                // don't skip WS after "use" and before first NamespaceName in multiple use statement
                if (ts.token().id().equals(PHPTokenId.PHP_USE)) {
                    result = startOffset;
                }
            }
            return result;
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
