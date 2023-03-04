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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.editor.TplMetaData;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.netbeans.modules.php.smarty.editor.utlis.TplUtils;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Martin Fousek
 */
public class TplTopLexer implements Lexer<TplTopTokenId> {

    private static final Logger LOG = Logger.getLogger(TplTopLexer.class.getName());

    private final TplTopColoringLexer scanner;
    private TokenFactory<TplTopTokenId> tokenFactory;
    private final InputAttributes inputAttributes;
    private final TplMetaData tplMetaData;

    private static class CompoundState {

        private State lexerState;
        private SubState lexerSubState;
        private int embeddingLevel;
        private TplTopTokenId lastState;
        private String stringDelimiter;

        public CompoundState(State lexerState, SubState lexerSubState, int embeddingLevel, TplTopTokenId lastState, String stringDelimiter) {
            this.lexerState = lexerState;
            this.lexerSubState = lexerSubState;
            this.embeddingLevel = embeddingLevel;
            this.lastState = lastState;
            this.stringDelimiter = stringDelimiter;
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
            if (this.lexerSubState != other.lexerSubState) {
                return false;
            }
            if (this.embeddingLevel != other.embeddingLevel) {
                return false;
            }
            if (this.lastState != other.lastState) {
                return false;
            }
            if (this.stringDelimiter == null ? other.stringDelimiter != null : !this.stringDelimiter.equals(other.stringDelimiter)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + lexerState.ordinal();
            hash = 17 * hash + lexerSubState.ordinal();
            hash = 17 * hash + embeddingLevel;
            hash = 17 * hash + lastState.ordinal();
            if (stringDelimiter != null) {
                hash = 17 * hash + stringDelimiter.hashCode();
            } else {
                hash = 17 * hash;
            }
            return hash;
        }

        @Override
        public String toString() {
            return "State(hash=" + hashCode() + ",s=" + lexerState + ",ss=" + lexerSubState + ",ls=" + lastState + ",sdel=" + stringDelimiter + ")"; //NOI18N
        }
    }

    private TplTopLexer(LexerRestartInfo<TplTopTokenId> info) {
        CompoundState state;
        if (info.state() == null) {
            state = new CompoundState(State.INIT, SubState.NO_SUB_STATE, 0, TplTopTokenId.T_HTML, null);
        } else {
            state = (CompoundState) info.state();
        }
        this.tokenFactory = info.tokenFactory();
        this.inputAttributes = info.inputAttributes();
        if (inputAttributes != null) {
            this.tplMetaData = (TplMetaData) inputAttributes.getValue(LanguagePath.get(TplTopTokenId.language()), TplMetaData.class);
        } else {
            this.tplMetaData = TplUtils.getProjectPropertiesForFileObject(null);
        }
        scanner = new TplTopColoringLexer(info, state, tplMetaData);
    }

    /**
     * Create new top lexer.
     *
     * @param info where was the parsing started
     * @return new lexer for additional parsing
     */
    public static synchronized TplTopLexer create(LexerRestartInfo<TplTopTokenId> info) {
        return new TplTopLexer(info);
    }

    @Override
    public Token<TplTopTokenId> nextToken() {
        TplTopTokenId tokenId = scanner.nextToken();
        Token<TplTopTokenId> token = null;
        if (tokenId != null) {
            token = tokenFactory.createToken(tokenId);
        }
        return token;
    }

    @Override
    public Object state() {
        return scanner.getState();
    }

    @Override
    public void release() {
    }

    private enum State {

        INIT,
        OUTER,
        AFTER_DELIMITER, // after any custom or default Smarty delimiter
        OPEN_DELIMITER,
        CLOSE_DELIMITER,
        IN_COMMENT,
        IN_SMARTY,
        IN_STRING,
        IN_PHP,
        AFTER_SUBSTATE,
        IN_LITERAL
    }

    private enum SubState {

        NO_SUB_STATE,
        PHP_CODE,
        LITERAL
    }

    private static class TplTopColoringLexer {

        private final LexerInput input;
        private final TplMetaData metadata;
        // state variables
        private State state;
        private SubState subState;
        private int embeddingLevel;
        private TplTopTokenId lastState;
        private String stringDelimiter;

        public TplTopColoringLexer(LexerRestartInfo<TplTopTokenId> info, CompoundState state, TplMetaData metadata) {
            this.input = info.input();
            this.state = state.lexerState;
            this.subState = state.lexerSubState;
            this.embeddingLevel = state.embeddingLevel;
            this.lastState = state.lastState;
            this.stringDelimiter = state.stringDelimiter;
            if (metadata != null) {
                this.metadata = metadata;
            } else {
                this.metadata = TplUtils.getProjectPropertiesForFileObject(null);
            }
        }

        public TplTopTokenId nextToken() {
            int c = input.read();
            CharSequence text;
            int textLength;
            int openDelimiterLength = getOpenDelimiterLength();
            int closeDelimiterLength = getCloseDelimiterLength();
            if (c == LexerInput.EOF) {
                return null;
            }
            while (c != LexerInput.EOF) {
                char cc = (char) c;
                text = input.readText();
                textLength = text.length();
                switch (state) {
                    case INIT:
                    case OUTER:
                        if (isSmartyOpenDelimiter(text)) {
                            c = input.read();
                            input.backup(1);
                            if ((!LexerUtils.isWS(c) && getSmartyVersion() == SmartyFramework.Version.SMARTY3)
                                    || getSmartyVersion() == SmartyFramework.Version.SMARTY2) {
                                state = State.OPEN_DELIMITER;
                                input.backup(openDelimiterLength);
                                if (textLength > openDelimiterLength) {
                                    return TplTopTokenId.T_HTML;
                                }
                            }
                        }
                        break;

                    case OPEN_DELIMITER:
                        if (textLength < openDelimiterLength) {
                            break;
                        }
                        state = State.AFTER_DELIMITER;
                        if (subState == SubState.NO_SUB_STATE) {
                            if (isSmartyOpenDelimiter(SmartyFramework.OPEN_DELIMITER)) {
                                c = input.read();
                                input.backup(1);
                                if (c == LexerInput.EOF) {
                                    return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                                } else {
                                    if (LexerUtils.isWS(c)
                                            && c != LexerInput.EOF
                                            && getSmartyVersion() == SmartyFramework.Version.SMARTY3) {
                                        if (lastState == TplTopTokenId.T_SMARTY) {
                                            state = State.IN_SMARTY;
                                        } else {
                                            state = State.OUTER;
                                        }
                                        return lastState;
                                    } else {
                                        embeddingLevel++;
                                        lastState = TplTopTokenId.T_SMARTY;
                                        return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                                    }
                                }
                            } else {
                                return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                            }
                        } else {
                            if (input.readLength() > openDelimiterLength) {
                                input.backup(input.readLength() - openDelimiterLength);
                                if (subState == SubState.LITERAL) {
                                    return TplTopTokenId.T_HTML;
                                } else {
                                    return TplTopTokenId.T_PHP;
                                }
                            }
                            break;
                        }

                    case AFTER_DELIMITER:
                        if (LexerUtils.isWS(c)) {
                            if (subState == SubState.NO_SUB_STATE) {
                                lastState = TplTopTokenId.T_SMARTY;
                                return TplTopTokenId.T_SMARTY;
                            } else {
                                break;
                            }
                        } else {
                            String lastWord = readNextWord(c);
                            switch (subState) {
                                case LITERAL:
                                    if (lastWord.startsWith("/literal")) {
                                        subState = SubState.NO_SUB_STATE;
                                        state = State.OPEN_DELIMITER;
                                        input.backup(input.readLength());
                                        break;
                                    } else {
                                        input.backup(input.readLength() - 1);
                                        state = State.IN_LITERAL;
                                    }
                                    return TplTopTokenId.T_HTML;
                                case PHP_CODE:
                                    if (lastWord.startsWith("/php")) {
                                        subState = SubState.NO_SUB_STATE;
                                        state = State.OPEN_DELIMITER;
                                        input.backup(input.readLength());
                                        break;
                                    } else {
                                        state = State.IN_PHP;
                                    }
                                    return TplTopTokenId.T_PHP;
                                default:
                                    if (lastWord.charAt(0) == '*') {
                                        state = State.IN_COMMENT;
                                        input.backup(lastWord.length() - 1);
                                        return TplTopTokenId.T_COMMENT;
                                    } else if (lastWord.startsWith("literal" + metadata.getCloseDelimiter())) {
                                        subState = SubState.LITERAL;
                                        state = State.AFTER_SUBSTATE;
                                        input.backup(lastWord.length() - 7);
                                        return TplTopTokenId.T_SMARTY;
                                    } else if (lastWord.startsWith("php" + metadata.getCloseDelimiter())) {
                                        subState = SubState.PHP_CODE;
                                        state = State.AFTER_SUBSTATE;
                                        input.backup(lastWord.length() - 3);
                                        return TplTopTokenId.T_SMARTY;
                                    } else {
                                        state = State.IN_SMARTY;
                                        input.backup(lastWord.length());
                                    }
                            }
                        }
                        break;

                    case IN_COMMENT:
                        if (cc == '*') {
                            int nextChar = input.read();
                            while (input.readLength() <= getCloseDelimiterLength() && nextChar != LexerInput.EOF) {
                                nextChar = input.read();
                            }
                            if (nextChar != LexerInput.EOF) {
                                if (isSmartyCloseDelimiter(input.readText())) {
                                    input.backup(input.readLength() - 1);
                                    state = State.AFTER_SUBSTATE;
                                    return TplTopTokenId.T_COMMENT;
                                } else {
                                    input.backup(1);
                                    return TplTopTokenId.T_COMMENT;
                                }
                            } else {
                                input.backup(input.readLength() - 1);
                            }
                        }
                        return TplTopTokenId.T_COMMENT;

                    case AFTER_SUBSTATE:
                        if (LexerUtils.isWS(c)) {
                            return TplTopTokenId.T_SMARTY;
                        } else if (isSmartyCloseDelimiter(text)) {
                            state = State.CLOSE_DELIMITER;
                            input.backup(closeDelimiterLength);
                            break;
                        } else {
                            break;
                        }

                    case CLOSE_DELIMITER:
                        if (textLength < closeDelimiterLength) {
                            break;
                        }
                        if (subState == SubState.NO_SUB_STATE) {
                            c = input.read();
                            input.backup(1);
                            if (c == LexerInput.EOF) {
                                embeddingLevel--;
                                state = getStateFromEmbeddingLevel(embeddingLevel);
                                return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                            } else {
                                embeddingLevel--;
                                state = getStateFromEmbeddingLevel(embeddingLevel);
                                if (state == State.IN_SMARTY) {
                                    lastState = TplTopTokenId.T_SMARTY;
                                } else {
                                    lastState = TplTopTokenId.T_HTML;
                                }
                                return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                            }
                        } else {
                            switch (subState) {
                                case LITERAL:
                                    state = State.IN_LITERAL;
                                    break;
                                case PHP_CODE:
                                    state = State.IN_PHP;
                                    break;
                                default:
                                    break;
                            }
                            lastState = TplTopTokenId.T_HTML;
                            embeddingLevel--;
                            return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                        }

                    case IN_PHP:
                        if (isSmartyOpenDelimiter(text)) {
                            state = State.OPEN_DELIMITER;
                            input.backup(openDelimiterLength);
                            if (input.readLength() > 0) {
                                return TplTopTokenId.T_PHP;
                            }
                        }
                        if (input.readLength() > 1) {
                            return TplTopTokenId.T_PHP;
                        }
                        break;

                    case IN_LITERAL:
                        while (!isSmartyOpenDelimiterWithFinalizingChar(text)) {
                            c = input.read();
                            if (c == LexerInput.EOF) {
                                if (input.readLength() > 0) {
                                    return TplTopTokenId.T_HTML;
                                }
                            }
                            text = input.readText();
                        }
                        state = State.OPEN_DELIMITER;
                        input.backup(openDelimiterLength + 1);
                        if (input.readLength() > 0) {
                            return TplTopTokenId.T_HTML;
                        }
                        break;

                    case IN_STRING:
                        String currentStringDelimiter = endingStringDelimiter(text);
                        if (currentStringDelimiter != null
                                && stringDelimiter.equals(currentStringDelimiter)
                                && (!CharSequenceUtilities.endsWith(text, '\\' + currentStringDelimiter))) { //NOI18N
                            state = State.IN_SMARTY;
                            stringDelimiter = null;
                        }
                        break;

                    case IN_SMARTY:
                        if (isSmartyCloseDelimiter(text)) {
                            if (textLength == closeDelimiterLength) {
                                embeddingLevel--;
                                state = getStateFromEmbeddingLevel(embeddingLevel);
                                return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                            } else {
                                state = State.CLOSE_DELIMITER;
                                input.backup(closeDelimiterLength);
                                if (input.readLength() != 0) {
                                    return TplTopTokenId.T_SMARTY;
                                }
                            }
                        } else if (isSmartyOpenDelimiter(text)) {
                            state = State.OPEN_DELIMITER;
                            input.backup(openDelimiterLength);
                            if (textLength > openDelimiterLength) {
                                return TplTopTokenId.T_SMARTY;
                            }
                        } else if ((stringDelimiter = endingStringDelimiter(text)) != null) {
                            state = State.IN_STRING;
                        }
                        switch (c) {
                            case LexerInput.EOF:
                                return TplTopTokenId.T_SMARTY;
                        }
                        break;
                }
                c = input.read();
            }

            return getTokenId(state);
        }

        private State getStateFromEmbeddingLevel(int embeddingLevel) {
            if (embeddingLevel > 0) {
                return State.IN_SMARTY;
            } else {
                return State.OUTER;
            }
        }

        private TplTopTokenId getTokenId(State state) {
            switch (state) {
                case IN_SMARTY:
                    return TplTopTokenId.T_SMARTY;
                case OPEN_DELIMITER:
                    return TplTopTokenId.T_SMARTY_OPEN_DELIMITER;
                case CLOSE_DELIMITER:
                    return TplTopTokenId.T_SMARTY_CLOSE_DELIMITER;
                default:
                    return TplTopTokenId.T_HTML;
            }
        }

        Object getState() {
            return new CompoundState(state, subState, embeddingLevel, lastState, stringDelimiter);
        }

        private String endingStringDelimiter(CharSequence text) {
            if (CharSequenceUtilities.endsWith(text, "\"")) {       //NOI18N
                return "\"";                                        //NOI18N
            } else if (CharSequenceUtilities.endsWith(text, "'")) { //NOI18N
                return "'";                                         //NOI18N
            } else {
                return null;
            }
        }

        private boolean isSmartyOpenDelimiter(CharSequence text) {
            return CharSequenceUtilities.endsWith(text, metadata.getOpenDelimiter());
        }

        private boolean isSmartyOpenDelimiterWithFinalizingChar(CharSequence text) {
            return CharSequenceUtilities.endsWith(text, metadata.getOpenDelimiter() + "/");
        }

        private boolean isSmartyCloseDelimiter(CharSequence text) {
            return CharSequenceUtilities.endsWith(text, metadata.getCloseDelimiter());
        }

        private int getOpenDelimiterLength() {
            return metadata.getOpenDelimiter().length();
        }

        private int getCloseDelimiterLength() {
            return metadata.getCloseDelimiter().length();
        }

        private SmartyFramework.Version getSmartyVersion() {
            return metadata.getSmartyVersion();
        }

        private String readNextWord(int lastChar) {
            StringBuilder sb = new StringBuilder();
            sb.append((char) lastChar);
            int c;
            while (!LexerUtils.isWS(c = input.read()) && c != LexerInput.EOF) {
                sb.append((char) c);
            }
            input.backup(1);
            return sb.toString();
        }
    }
}
