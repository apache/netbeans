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
package org.netbeans.modules.php.latte.hints;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class DeprecatedMacrosHint extends HintRule {
    private FileObject fileObject;
    private BaseDocument baseDocument;
    private List<Hint> hints;

    @Override
    public void invoke(RuleContext context, List<Hint> result) {
        Snapshot snapshot = context.parserResult.getSnapshot();
        hints = result;
        fileObject = snapshot.getSource().getFileObject();
        baseDocument = context.doc;
        if (fileObject != null) {
            TokenSequence<LatteTopTokenId> topTs = snapshot.getTokenHierarchy().tokenSequence(LatteTopTokenId.language());
            if (topTs != null) {
                checkTopTokenSequence(topTs);
            }
        }
    }

    private void checkTopTokenSequence(TokenSequence<LatteTopTokenId> topTs) {
        topTs.moveStart();
        TokenSequence<LatteMarkupTokenId> ts;
        while (topTs.moveNext()) {
            ts = topTs.embeddedJoined(LatteMarkupTokenId.language());
            if (ts != null) {
                checkMarkupTokenSequence(ts);
            }
        }
    }

    private void checkMarkupTokenSequence(TokenSequence<LatteMarkupTokenId> ts) {
        ts.moveStart();
        Token<LatteMarkupTokenId> token;
        while (ts.moveNext()) {
            token = ts.token();
            if (isDeprecatedToken(token)) {
                createHint(ts.offset(), token);
            }
        }
    }

    @NbBundle.Messages("DeprecatedMacroHintText=Deprecated Macro")
    private void createHint(int startOffset, Token<LatteMarkupTokenId> token) {
        OffsetRange offsetRange = new OffsetRange(startOffset, startOffset + token.length());
        if (showHint(offsetRange, baseDocument)) {
            String replaceText = getReplaceText();
            List<HintFix> fixes = replaceText == null
                    ? Collections.<HintFix>emptyList()
                    : Collections.<HintFix>singletonList(new Fix(startOffset, token, baseDocument, replaceText));
            hints.add(new Hint(this, Bundle.DeprecatedMacroHintText(), fileObject, offsetRange, fixes, 500));
        }
    }

    protected abstract boolean isDeprecatedToken(Token<LatteMarkupTokenId> token);

    protected abstract String getReplaceText();

    public static final class WidgetMacroHint extends DeprecatedMacrosHint {
        private static final String HINT_ID = "latte.widget.macro.hint"; //NOI18N
        private static final String WIDGET_MACRO = "widget"; //NOI18N
        private static final String CONTROL_MACRO = "control"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("WidgetMacroHintDesc=Widget macro is deprecated, use 'control' macro instead.")
        public String getDescription() {
            return Bundle.WidgetMacroHintDesc();
        }

        @Override
        @NbBundle.Messages("WidgetMacroHintDisp=Widget Macro")
        public String getDisplayName() {
            return Bundle.WidgetMacroHintDisp();
        }

        @Override
        protected boolean isDeprecatedToken(Token<LatteMarkupTokenId> token) {
            return token != null && LatteMarkupTokenId.T_MACRO_START.equals(token.id()) && WIDGET_MACRO.equals(token.text().toString().trim());
        }

        @Override
        protected String getReplaceText() {
            return CONTROL_MACRO;
        }

    }

    public static final class IfCurrentMacroHint extends DeprecatedMacrosHint {
        private static final String HINT_ID = "latte.ifcurrent.macro.hint"; //NOI18N
        private static final String DEPRECATED_MACRO = "ifCurrent"; //NOI18N

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("IfCurrentMacroHintDesc=IfCurrent macro is deprecated, use 'n:class=\"$presenter->linkCurrent ? ...\"' instead.")
        public String getDescription() {
            return Bundle.IfCurrentMacroHintDesc();
        }

        @Override
        @NbBundle.Messages("IfCurrentMacroHintDisp=IfCurrent Macro")
        public String getDisplayName() {
            return Bundle.IfCurrentMacroHintDisp();
        }

        @Override
        protected boolean isDeprecatedToken(Token<LatteMarkupTokenId> token) {
            return token != null && LatteMarkupTokenId.T_MACRO_START.equals(token.id()) && DEPRECATED_MACRO.equals(token.text().toString().trim());
        }

        @Override
        protected String getReplaceText() {
            return null;
        }

    }

    private static final class Fix implements HintFix {
        private final int startOffset;
        private final Token<LatteMarkupTokenId> token;
        private final BaseDocument baseDocument;
        private final String replaceText;

        private Fix(int startOffset, Token<LatteMarkupTokenId> token, BaseDocument baseDocument, String replaceText) {
            this.startOffset = startOffset;
            this.token = token;
            this.baseDocument = baseDocument;
            this.replaceText = replaceText;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - text of replacement",
            "DeprecatedMacroHintFix=Replace with: {0}"
        })
        public String getDescription() {
            return Bundle.DeprecatedMacroHintFix(replaceText);
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(baseDocument);
            editList.replace(startOffset, token.length(), replaceText, true, 0);
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
