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
    public static final String PUG_LANG = "pug"; //NOI18N
    public static final String JADE_LANG = "jade"; //NOI18N
    
    public static final String SCSS_LANG = "scss"; //NOI18N
    public static final String LESS_LANG = "less"; //NOI18N

    public VueLexer(LexerRestartInfo<VueTokenId> info) {
        super(info, VueAntlrColoringLexer::new);
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    @Override
    protected Token<VueTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {

        return switch (antlrToken.getType()) {
            case VueAntlrColoringLexer.HTML -> groupToken(HTML, VueAntlrColoringLexer.HTML);
            case VueAntlrColoringLexer.VUE_DIRECTIVE -> token(VUE_DIRECTIVE);
            case VueAntlrColoringLexer.QUOTE_ATTR -> token(QUOTE_ATTR);
            case VueAntlrColoringLexer.VAR_TAG -> token(VAR_TAG);
            case VueAntlrColoringLexer.JAVASCRIPT_ATTR -> groupToken(JAVASCRIPT_ATTR, VueAntlrColoringLexer.JAVASCRIPT_ATTR);
            case VueAntlrColoringLexer.JAVASCRIPT_INTERP -> groupToken(JAVASCRIPT_INTERP, VueAntlrColoringLexer.JAVASCRIPT_INTERP);   
            case VueAntlrColoringLexer.JAVASCRIPT -> assignScriptLexerToken(JAVASCRIPT, VueAntlrColoringLexer.JAVASCRIPT);
            case VueAntlrColoringLexer.CSS -> assignStyleLexerToken(CSS, VueAntlrColoringLexer.CSS);
            default -> groupToken(HTML, VueAntlrColoringLexer.HTML);
        };
    }

    private Token<VueTokenId> assignScriptLexerToken(VueTokenId id, int antlrTokenType) {
        State currentState = (State) state();
        String scriptLang = currentState.getScriptLanguageState();
        if (scriptLang == null) {
            return groupToken(id, antlrTokenType);
        }
        return switch (scriptLang) {
            case JADE_LANG, PUG_LANG ->
                groupToken(JAVASCRIPT_PUG, antlrTokenType);
             default ->
                groupToken(id, antlrTokenType);
        };
    }

    private Token<VueTokenId> assignStyleLexerToken(VueTokenId id, int antlrTokenType) {
        State currentState = (State) state();
        String styleLang = currentState.getStyleLanguageState();
        if (styleLang == null) {
            return groupToken(id, antlrTokenType);
        }
        return switch (styleLang) {
            case SCSS_LANG ->
                groupToken(STYLE_SCSS, antlrTokenType);
            case LESS_LANG ->
                groupToken(STYLE_LESS, antlrTokenType);
            default ->
                groupToken(id, antlrTokenType);
        };
    }

    private static class State extends AbstractAntlrLexerBridge.LexerState<VueAntlrColoringLexer> {

        final boolean attrQuoteOpened;
        final boolean varInterpolationOpened;
        final String  scriptLanguage;
        final String  styleLanguage;

        public State(VueAntlrColoringLexer lexer) {
            super(lexer);
            this.attrQuoteOpened = lexer.getAttrQuoteState();
            this.varInterpolationOpened = lexer.isVarInterpolationOpened();
            this.scriptLanguage = lexer.getScriptLanguage();
            this.styleLanguage = lexer.getStyleLanguage();
        }

        @Override
        public void restore(VueAntlrColoringLexer lexer) {
            super.restore(lexer);
            lexer.setAttrQuoteState(attrQuoteOpened);
            lexer.setVarInterpolationOpened(varInterpolationOpened);
            lexer.setScriptLanguage(scriptLanguage);
            lexer.setStyleLanguage(styleLanguage);
        }

        public String getScriptLanguageState() {
            return this.scriptLanguage;
        }

        public String getStyleLanguageState() {
            return this.styleLanguage;
        }
    }

}
