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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnnecessaryClosingDelimiterHint extends HintRule {
    private static final String HINT_ID = "unnecessary.closing.delimiter.hint";
    private FileObject fileObject;
    private BaseDocument baseDocument;

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            baseDocument = context.doc;
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                TokenHierarchy<?> th = phpParseResult.getSnapshot().getTokenHierarchy();
                if (th != null) {
                    int startOffset = phpParseResult.getProgram().getStartOffset();
                    TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(th, startOffset);
                    if (ts != null) {
                        ts.move(startOffset);
                        CloseTagWrapper closeTagWrapper = createCloseTagWrapper(ts);
                        if (closeTagWrapper.shouldBeRemoved()) {
                            closeTagWrapper.createHint(result);
                        }
                    }
                }
            }
        }
    }

    private CloseTagWrapper createCloseTagWrapper(TokenSequence<PHPTokenId> ts) {
        CloseTagWrapper result = CloseTagWrapper.NONE;
        boolean inOpenTagWithEcho = false;
        while (ts.moveNext()) {
            Token<PHPTokenId> token = ts.token();
            if (token != null) {
                PHPTokenId id = token.id();
                switch (id) {
                    case T_OPEN_TAG_WITH_ECHO:
                        inOpenTagWithEcho = true;
                        break;
                    case PHP_CLOSETAG:
                        result = new CloseTagWrapperImpl(ts.offset(), inOpenTagWithEcho);
                        break;
                    case T_INLINE_HTML:
                        result.setHtmlPart(token);
                        break;
                    default:
                        result = CloseTagWrapper.NONE;
                }
            }
        }
        return result;
    }

    private interface CloseTagWrapper {
        CloseTagWrapper NONE = new CloseTagWrapper() {

            @Override
            public void setHtmlPart(Token<PHPTokenId> inlineHtmlTag) {
            }

            @Override
            public boolean shouldBeRemoved() {
                return false;
            }

            @Override
            public void createHint(List<Hint> result) {
            }
        };

        void setHtmlPart(Token<PHPTokenId> inlineHtmlTag);
        boolean shouldBeRemoved();
        void createHint(List<Hint> result);
    }

    private final class CloseTagWrapperImpl implements CloseTagWrapper {
        private static final String CLOSING_TAG = "?>"; //NOI18N
        private final int closeTagOffset;
        private final boolean startsWithOpenTagWithEcho;
        private Token<PHPTokenId> inlineHtmlTag;

        private CloseTagWrapperImpl(int closeTagOffset, boolean startsWithOpenTagWithEcho) {
            this.closeTagOffset = closeTagOffset;
            this.startsWithOpenTagWithEcho = startsWithOpenTagWithEcho;
        }

        @Override
        public void setHtmlPart(Token<PHPTokenId> inlineHtmlTag) {
            this.inlineHtmlTag = inlineHtmlTag;
        }

        @Override
        public boolean shouldBeRemoved() {
            return !startsWithOpenTagWithEcho && (inlineHtmlTag == null || TokenUtilities.trim(inlineHtmlTag.text()).length() == 0);
        }

        @NbBundle.Messages("UnnecessaryClosingDelimiterHintText=Unnecessary Closing Delimiter")
        @Override
        public void createHint(List<Hint> result) {
            OffsetRange hintRange = new OffsetRange(closeTagOffset, closeTagOffset + CLOSING_TAG.length());
            result.add(new Hint(
                    UnnecessaryClosingDelimiterHint.this,
                    Bundle.UnnecessaryClosingDelimiterHintText(),
                    fileObject,
                    hintRange,
                    Collections.<HintFix>singletonList(new Fix(hintRange, baseDocument)),
                    500));
        }

    }

    private static final class Fix implements HintFix {
        private final OffsetRange hintRange;
        private final BaseDocument baseDocument;

        public Fix(OffsetRange hintRange, BaseDocument baseDocument) {
            this.hintRange = hintRange;
            this.baseDocument = baseDocument;
        }

        @Override
        @NbBundle.Messages("UnnecessaryClosingDelimiterHintFix=Remove Closing Delimiter")
        public String getDescription() {
            return Bundle.UnnecessaryClosingDelimiterHintFix();
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(baseDocument);
            editList.replace(hintRange.getStart(), hintRange.getLength(), "", true, 0);
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

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("UnnecessaryClosingDelimiterHintDesc=It is a good practise to omit closing PHP delimiter "
            + "at the end of file. It's just a source of \"Headers already sent\" errors.")
    public String getDescription() {
        return Bundle.UnnecessaryClosingDelimiterHintDesc();
    }

    @Override
    @NbBundle.Messages("UnnecessaryClosingDelimiterHintDisp=Unnecessary Closing Delimiter")
    public String getDisplayName() {
        return Bundle.UnnecessaryClosingDelimiterHintDisp();
    }

}
