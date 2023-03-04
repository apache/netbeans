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
package org.netbeans.modules.php.smarty.editor.lexer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for FUSE tpl templates
 * 
 * @author Martin Fousek
 */
public class TplLexer implements Lexer<TplTokenId> {

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private String keyword;
    private boolean argValue;
    private boolean endingTag;
    private final TokenFactory<TplTokenId> tokenFactory;
    private final InputAttributes inputAttributes;
    private int lexerState = INIT;

    private static class CompoundState {

        private int lexerState;
        private boolean isArgumentValue;
        private boolean isEndingTag;
        private String keyword;

        public CompoundState(int lexerState, boolean isArgumentValue, boolean isEndingTag, String keyword) {
            this.lexerState = lexerState;
            this.isArgumentValue = isArgumentValue;
            this.isEndingTag = isEndingTag;
            this.keyword = keyword;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CompoundState other = (CompoundState) obj;
            if (this.lexerState != other.lexerState) {
                return false;
            }
            if (this.isArgumentValue != other.isArgumentValue) {
                return false;
            }
            if (this.isEndingTag != other.isEndingTag) {
                return false;
            }
            if (this.keyword != other.keyword && (this.keyword == null || !this.keyword.equals(other.keyword))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + this.lexerState;
            hash = 17 * hash + (this.isArgumentValue? 1 : 0);
            hash = 17 * hash + (this.isEndingTag? 1 : 0);
            hash = 17 * hash + (this.keyword != null ? this.keyword.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "State(hash=" + hashCode() + ",s=" + lexerState + ",iAV=" +
                    isArgumentValue + ",iET=" + isEndingTag + ",keyword=" +
                    keyword + ")"; //NOI18N
        }
    }
    
    // Internal states
    private static final int INIT = 0;
    private static final int ISI_TEXT = 1;          // Plain text
    private static final int ISI_ERROR = 2;         // Syntax error in TPL syntax
    private static final int ISA_DOLLAR = 3;        // After dollar char - "$_"
    private static final int ISI_VAR_PHP = 4;       // PHP-like variables - "$v_" "$va_"
    private static final int ISA_WS = 9;            // Is after whitespace - " _"
    private static final int ISA_HASH = 10;         // Is after hash - "#_"
    private static final int ISI_QUOT = 12;         // Is in quot - "'_" "'asd_"
    private static final int ISI_DQUOT = 13;        // Is in double quot - "\"_" "\"asdfasd_"
    static final Set<String> VARIABLE_MODIFIERS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.modifiers.php
        VARIABLE_MODIFIERS.add("capitalize"); // NOI18N
        VARIABLE_MODIFIERS.add("cat"); // NOI18N
        VARIABLE_MODIFIERS.add("count_characters"); // NOI18N
        VARIABLE_MODIFIERS.add("count_paragraphs"); // NOI18N
        VARIABLE_MODIFIERS.add("count_sentences"); // NOI18N
        VARIABLE_MODIFIERS.add("count_words"); // NOI18N
        VARIABLE_MODIFIERS.add("date_format"); // NOI18N
        VARIABLE_MODIFIERS.add("default"); // NOI18N
        VARIABLE_MODIFIERS.add("escape"); // NOI18N
        VARIABLE_MODIFIERS.add("from_charset"); // NOI18N
        VARIABLE_MODIFIERS.add("indent"); // NOI18N
        VARIABLE_MODIFIERS.add("lower"); // NOI18N
        VARIABLE_MODIFIERS.add("nl2br"); // NOI18N
        VARIABLE_MODIFIERS.add("regex_replace"); // NOI18N
        VARIABLE_MODIFIERS.add("replace"); // NOI18N
        VARIABLE_MODIFIERS.add("spacify"); // NOI18N
        VARIABLE_MODIFIERS.add("string_format"); // NOI18N
        VARIABLE_MODIFIERS.add("strip"); // NOI18N
        VARIABLE_MODIFIERS.add("strip_tags"); // NOI18N
        VARIABLE_MODIFIERS.add("to_charset"); // NOI18N
        VARIABLE_MODIFIERS.add("truncate"); // NOI18N
        VARIABLE_MODIFIERS.add("unescape"); // NOI18N
        VARIABLE_MODIFIERS.add("upper"); // NOI18N
        VARIABLE_MODIFIERS.add("wordwrap"); // NOI18N
    }

    static final Set<String> OPERATORS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.function.if.php
        OPERATORS.add("as"); // NOI18N
        OPERATORS.add("div"); // NOI18N
        OPERATORS.add("by"); // NOI18N
        OPERATORS.add("even"); // NOI18N
        OPERATORS.add("is"); // NOI18N
        OPERATORS.add("not"); // NOI18N
        OPERATORS.add("odd"); // NOI18N
        OPERATORS.add("eq"); // NOI18N
        OPERATORS.add("ge"); // NOI18N
        OPERATORS.add("gt"); // NOI18N
        OPERATORS.add("gte"); // NOI18N
        OPERATORS.add("le"); // NOI18N
        OPERATORS.add("lt"); // NOI18N
        OPERATORS.add("lte"); // NOI18N
        OPERATORS.add("mod"); // NOI18N
        OPERATORS.add("ne"); // NOI18N
        OPERATORS.add("neq"); // NOI18N
        OPERATORS.add("not"); // NOI18N
        OPERATORS.add("or"); // NOI18N
        OPERATORS.add("and"); // NOI18N
    }

    static final Set<String> FUNCTIONS = new HashSet<String>();
    static {
        // See http://www.smarty.net/manual/en/language.builtin.functions.php,
        //     http://www.smarty.net/manual/en/language.custom.functions.php
        FUNCTIONS.add("append"); // NOI18N
        FUNCTIONS.add("assign"); // NOI18N
        FUNCTIONS.add("block"); // NOI18N
        FUNCTIONS.add("call"); // NOI18N
        FUNCTIONS.add("capture"); // NOI18N
        FUNCTIONS.add("config_load"); // NOI18N
        FUNCTIONS.add("counter"); // NOI18N
        FUNCTIONS.add("cycle"); // NOI18N
        FUNCTIONS.add("debug"); // NOI18N
        FUNCTIONS.add("else"); // NOI18N
        FUNCTIONS.add("elseif"); // NOI18N,
        FUNCTIONS.add("eval"); // NOI18N
        FUNCTIONS.add("extends"); // NOI18N
        FUNCTIONS.add("fetch"); // NOI18N
        FUNCTIONS.add("for"); // NOI18N
        FUNCTIONS.add("foreach"); // NOI18N,
        FUNCTIONS.add("foreachelse"); // NOI18N
        FUNCTIONS.add("function"); // NOI18N
        FUNCTIONS.add("html_checkboxes"); // NOI18N
        FUNCTIONS.add("html_image"); // NOI18N
        FUNCTIONS.add("html_options"); // NOI18N
        FUNCTIONS.add("html_radios"); // NOI18N
        FUNCTIONS.add("html_select_date"); // NOI18N
        FUNCTIONS.add("html_select_time"); // NOI18N
        FUNCTIONS.add("html_table"); // NOI18N
        FUNCTIONS.add("if"); // NOI18N,
        FUNCTIONS.add("include"); // NOI18N
        FUNCTIONS.add("include_php"); // NOI18N
        FUNCTIONS.add("insert"); // NOI18N
        FUNCTIONS.add("ldelim"); // NOI18N,
        FUNCTIONS.add("literal"); // NOI18N
        FUNCTIONS.add("mailto"); // NOI18N
        FUNCTIONS.add("math"); // NOI18N
        FUNCTIONS.add("nocache"); // NOI18N
        FUNCTIONS.add("php"); // NOI18N
        FUNCTIONS.add("popup"); // NOI18N
        FUNCTIONS.add("popup_init"); // NOI18N
        FUNCTIONS.add("rdelim"); // NOI18N
        FUNCTIONS.add("section"); // NOI18N,
        FUNCTIONS.add("sectionelse"); // NOI18N
        FUNCTIONS.add("setfilter"); // NOI18N
        FUNCTIONS.add("strip"); // NOI18N
        FUNCTIONS.add("textformat"); // NOI18N
        FUNCTIONS.add("while"); // NOI18N
    }

    /**
     * Create new TplLexer.
     * @param info from which place it should start again
     */
    public TplLexer(LexerRestartInfo<TplTokenId> info) {
        this.input = info.input();
        this.inputAttributes = info.inputAttributes();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            this.keyword = "";
            this.argValue = false;
            this.endingTag = false;
            this.lexerState = INIT;
        } else {
            CompoundState cs = (CompoundState) info.state();
            lexerState = cs.lexerState;
            argValue = cs.isArgumentValue;
            endingTag = cs.isEndingTag;
            keyword = cs.keyword;
        }
    }

    public Object state() {
        return new CompoundState(lexerState, argValue, endingTag, keyword);
    }

    public Token<TplTokenId> nextToken() {
        int actChar;

        while (true) {
            actChar = input.read();

            if (actChar == EOF) {
                if (input.readLengthEOF() == 1) {
                    return null; //just EOL is read
                } else {
                    if (lexerState == INIT) {
                        return token(TplTokenId.OTHER);
                    }
                }
            }
            switch (lexerState) {
                case INIT:
                    switch (actChar) {
                        case '$':           // Dollar, e.g. $
                            lexerState = ISA_DOLLAR;
                            break;
                        case '#':           // Hash, e.g. #
                            lexerState = ISA_HASH;
                            break;
                        case '\'':
                            lexerState = ISI_QUOT;
                            break;
                        case '/':
                            endingTag = true;
                            break;
                        case '"':
                            lexerState = ISI_DQUOT;
                            break;
                        case '=':
                            argValue = true;
                            return token(TplTokenId.OTHER);
                        case '|':           // Pipe, e.g. $var|, ''|
                            if (input.read() == '|') {
                                return token(TplTokenId.OTHER);
                            } else {
                                input.backup(1);
                                return token(TplTokenId.PIPE);
                            }
                        case '\n':
                        case ' ':
                        case '\r':
                        case '\t':
                            argValue = false;
                            lexerState = ISA_WS;
                            return token(TplTokenId.WHITESPACE);
                        case EOF:
                            return null;
                        default:
                            input.backup(1);
                            lexerState = ISI_TEXT;
                            break;
                    }
                    break;

                case ISA_DOLLAR:            // '$_'
                    argValue = false;
                    if (Character.isJavaIdentifierStart(actChar)) {
                        lexerState = ISI_VAR_PHP;
                    } else {
                        input.backup(1);
                        lexerState = ISI_ERROR;
                    }
                    break;

                case ISA_HASH:            // '#_'
                    argValue = false;
                    if (Character.isJavaIdentifierPart(actChar)) {
                        break;
                    } else if (actChar == '#' && input.readLength() > 2) {
                        lexerState = INIT;
                        return token(TplTokenId.CONFIG_VARIABLE);
                    } else {
                        return token(TplTokenId.ERROR);
                    }

                case ISI_QUOT:            // ''_', '' afssd_'
                    argValue = false;
                    if (actChar == '\'' || actChar == EOF) {
                        lexerState = INIT;
                        return token(TplTokenId.STRING);
                    } else if (actChar == '\\') {
                        if (input.read() == '\'') {
                            return token(TplTokenId.STRING);
                        } else {
                            input.backup(1);
                        }
                    } else {
                        return token(TplTokenId.STRING);
                    }
                    break;

                case ISI_DQUOT:            // '"_', '" afssd_'
                    argValue = false;
                    if (actChar == '"' || actChar == EOF) {
                        lexerState = INIT;
                        return token(TplTokenId.STRING);
                    } else if (actChar == '\\') {
                        if (input.read() == '"') {
                            return token(TplTokenId.STRING);
                        } else {
                            input.backup(1);
                        }
                    } else {
                        return token(TplTokenId.STRING);
                    }
                    break;

                case ISI_VAR_PHP:           // '$a_'
                    if (LexerUtils.isVariablePart(actChar)) {
                        break;    
                    }
                    lexerState = INIT;
                    if (input.readLength() > 1) { 
                        input.backup(1);
                        return token(TplTokenId.PHP_VARIABLE);
                    }
                    break;

                case ISA_WS:         // '$var _', '$var?|'
                    if (LexerUtils.isWS(actChar)) {
                        return token(TplTokenId.WHITESPACE);
                    }
                    input.backup(1);
                    lexerState = INIT;
                    break;

                case ISI_ERROR:
                    lexerState = INIT;
                    return token(TplTokenId.ERROR);

                case ISI_TEXT:
                    if (LexerUtils.isVariablePart(actChar)) {
                        keyword += Character.toString((char) actChar);
                        break;
                    } else if (input.readLengthEOF() == 1) {
                        lexerState = INIT;
                        if (LexerUtils.isWS(actChar)) {
                            return token(TplTokenId.WHITESPACE);
                        } else {
                            return token(TplTokenId.OTHER);
                        }
                    }
                    input.backup(1);
                    TplTokenId tokenId = resolveStringToken(keyword);
                    keyword = "";
                    lexerState = INIT;
                    return token(tokenId);

                default:
                    return token(TplTokenId.OTHER);
            } // end of switch (c)
        } // end of while(true)
    }

    public void release() {
    }

    private Token<TplTokenId> token(TplTokenId tplTokenId) {
        return tokenFactory.createToken(tplTokenId);
    }

    private TplTokenId resolveStringToken(String keyword) {
        // check variable modifiers 
        if (isVariableModifier(keyword)) {
            return TplTokenId.VARIABLE_MODIFIER;
        }

        // check operators
        if (isVariableOperator(keyword)) {
            return TplTokenId.OPERATOR;
        }

        // check functions
        if (isSmartyFunction(keyword)) {
            if (input.read() != '.') {
                input.backup(1);
                endingTag = false;
                return TplTokenId.FUNCTION;
            } else {
                input.backup(1);
                return TplTokenId.OTHER;
            }
        }

        // check if it's argument, its value or another text
        if (argValue) {
            return TplTokenId.ARGUMENT_VALUE;
        } else {
            int readChars = 1;
            int c = input.read();
            while (LexerUtils.isWS(c)) {
                readChars++;
                c = input.read();
            }
            if (c == '=') {
                readChars++;
                c = input.read();
                input.backup(readChars);
                if (c == '=') {
                    return TplTokenId.OTHER;
                } else {
                    return TplTokenId.ARGUMENT;
                }
            } else {
                input.backup(readChars);
                return TplTokenId.OTHER;
            }
        }
    }

    private boolean isVariableModifier(String keyword) {
        return VARIABLE_MODIFIERS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }

    private boolean isVariableOperator(String keyword) {
        return OPERATORS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }

    private boolean isSmartyFunction(String keyword) {
        return FUNCTIONS.contains(keyword.toString().toLowerCase(Locale.ENGLISH));
    }
}
