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
package org.netbeans.modules.javascript2.vue.editor.lexer;

import org.netbeans.api.lexer.Token;
import static org.netbeans.modules.javascript2.vue.editor.lexer.VueTokenId.*;
import org.netbeans.modules.javascript2.vue.grammar.antlr4.coloring.VueAntlrColoringLexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 * @author bogdan.haidu
 */
public class VueLexer extends AbstractAntlrLexerBridge<VueAntlrColoringLexer, VueTokenId> {

    public VueLexer(LexerRestartInfo<VueTokenId> info) {
        super(info, VueAntlrColoringLexer::new);
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    @Override
    protected Token<VueTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {

        switch (antlrToken.getType()) {
            case VueAntlrColoringLexer.HTML:
                return groupToken(HTML, VueAntlrColoringLexer.HTML);
            case VueAntlrColoringLexer.JAVASCRIPT_ATTR:
                return groupToken(JAVASCRIPT_ATTR, VueAntlrColoringLexer.JAVASCRIPT_ATTR);
            case VueAntlrColoringLexer.VUE_DIRECTIVE:
                return token(VUE_DIRECTIVE);
            case VueAntlrColoringLexer.QUOTE_ATTR:
                return token(QUOTE_ATTR);
            case VueAntlrColoringLexer.VAR_TAG:
                return token(VAR_TAG);
            case VueAntlrColoringLexer.JAVASCRIPT:
                return groupToken(JAVASCRIPT, VueAntlrColoringLexer.JAVASCRIPT);
            case VueAntlrColoringLexer.CSS:
                return groupToken(CSS, VueAntlrColoringLexer.CSS);
            default:
                return groupToken(HTML, VueAntlrColoringLexer.HTML);
        }
    }

    private static class State extends AbstractAntlrLexerBridge.LexerState<VueAntlrColoringLexer> {

        final boolean insideTemplateTag;
        final boolean insideStyleTag;
        final boolean attrQuoteOpened;
        final boolean varInterpolationOpened;

        public State(VueAntlrColoringLexer lexer) {
            super(lexer);
            this.insideTemplateTag = lexer.isInsideTemplateTag();
            this.insideStyleTag = lexer.isInsideStyleTag();
            this.attrQuoteOpened = lexer.getAttrQuoteState();
            this.varInterpolationOpened = lexer.isVarInterpolationOpened();
        }

        @Override
        public void restore(VueAntlrColoringLexer lexer) {
            super.restore(lexer);
            lexer.setInsideTemplateTag(insideTemplateTag);
            lexer.setInsideStyleTag(insideStyleTag);
            lexer.setAttrQuoteState(attrQuoteOpened);
            lexer.setVarInterpolationOpened(varInterpolationOpened);
        }
    }

}
