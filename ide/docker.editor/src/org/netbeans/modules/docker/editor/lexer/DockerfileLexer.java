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
package org.netbeans.modules.docker.editor.lexer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import static org.netbeans.spi.lexer.LexerInput.EOF;

/**
 *
 * @author Tomas Zezula
 */
final class DockerfileLexer implements Lexer<DockerfileTokenId> {
    private static final int STATE_NEW_LINE = 0;
    private static final int STATE_CONT_LINE = STATE_NEW_LINE + 1;
    private static final int STATE_ONBUILD = STATE_CONT_LINE + 1;
    private static final int STATE_REST = STATE_ONBUILD + 1;
    private static final int STATE_ESCAPE = STATE_REST + 1;

    private static final Map<String, DockerfileTokenId> KW_TO_TKN;
    private static final Set<DockerfileTokenId> KW_ON_BUILD;
    static {
        final Map<String, DockerfileTokenId> m = new HashMap<>();
        final Set<DockerfileTokenId> s = EnumSet.noneOf(DockerfileTokenId.class);
        for (DockerfileTokenId id : DockerfileTokenId.values()) {
            if (id.fixedText() != null && DockerfileTokenId.language().tokenCategories(id).contains("keyword")) { //NOI18N
                m.put(id.fixedText(), id);
                if (id.isOnBuildSupported()) {
                    s.add(id);
                }
            }
        }
        KW_TO_TKN = Collections.unmodifiableMap(m);
        KW_ON_BUILD = Collections.unmodifiableSet(s);
    }

    private final LexerInput input;
    private final TokenFactory<DockerfileTokenId> tokenFactory;
    private Integer state = null;
    private int previousLength = -1;
    private int currentLength = -1;

    DockerfileLexer(@NonNull final LexerRestartInfo<DockerfileTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        Object restoredState = info.state();
        if (restoredState instanceof Integer) {
            state = (Integer) restoredState;
        }
    }

    @Override
    @CheckForNull
    @SuppressWarnings("fallthrough")
    public Token<DockerfileTokenId> nextToken() {
        if (state == null) {
            state = STATE_NEW_LINE;
        }
        int currentState = state;
        final int[] newStateHolder = {state == STATE_ESCAPE ? STATE_REST : state};
        int c = nextChar();
        try {
            switch (c) {
                case '\r':
                case '\n':
                    newStateHolder[0] =
                            currentState == STATE_ESCAPE ?
                            STATE_CONT_LINE :
                            STATE_NEW_LINE;
                case '\t':
                case 0x0b:
                case '\f':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    return finishWhitespace(currentState, newStateHolder);
                case ' ':
                    c = nextChar();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        backup(1);
                        return   input.readLength() == 1
                               ? tokenFactory.getFlyweightToken(DockerfileTokenId.WHITESPACE, " ") //NOI18N
                               : tokenFactory.createToken(DockerfileTokenId.WHITESPACE);
                    }
                    backup(1);
                    return finishWhitespace(currentState, newStateHolder);
                case EOF:
                    return null;
                case '[':   //NOI18N
                    newStateHolder[0] = STATE_REST;
                    return token(DockerfileTokenId.LBRACKET);
                case ']':   //NOI18N
                    newStateHolder[0] = STATE_REST;
                    return token(DockerfileTokenId.RBRACKET);
                case ',':   //NOI18N
                    newStateHolder[0] = STATE_REST;
                    return token(DockerfileTokenId.COMMA);
                case '#':   //NOI18N
                    if (currentState == STATE_NEW_LINE ||
                        currentState == STATE_CONT_LINE) {
                        while (true) {
                            switch (nextChar()) {
                                case '\r': consumeNewline();
                                case '\n':
                                case EOF:
                                    return token(DockerfileTokenId.LINE_COMMENT);
                            }
                        }
                    }
                    newStateHolder[0] = STATE_REST;
                    return token(DockerfileTokenId.IDENTIFIER);
                case '\'':  //NOI18N
                    newStateHolder[0] = STATE_REST;
                    if (currentState != STATE_ESCAPE) {
                        return finishString('\'', '"', newStateHolder); //NOI18N
                    }
                    return token(DockerfileTokenId.IDENTIFIER);
                case '"':   //NOI18N
                    newStateHolder[0] = STATE_REST;
                    if (currentState != STATE_ESCAPE) {
                        return finishString('"','\'', newStateHolder);  //NOI18N
                    }
                    return token(DockerfileTokenId.IDENTIFIER);
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    newStateHolder[0] = STATE_REST;
                    return finishNumber(nextChar());
                case '\\':
                    newStateHolder[0] = STATE_ESCAPE;
                    return token(DockerfileTokenId.ESCAPE);
                default:
                    final Token<DockerfileTokenId> t;
                    if (isIdentifierStart(c, currentState)) {
                        while ((c = nextChar()) != EOF && isIdentifierPart(c, currentState));
                        backup(1);
                        t = keywordOrIdentifier(currentState);
                    } else {
                        t = token(DockerfileTokenId.IDENTIFIER);
                    }
                    newStateHolder[0] = t.id() == DockerfileTokenId.ONBUILD ?
                            STATE_ONBUILD :
                            STATE_REST;
                    return t;
            }
        } finally {
            state = newStateHolder[0];
        }
    }

    @Override
    public Object state() {
        return state;
    }

    @Override
    public void release() {
    }

    @NonNull
    private Token<DockerfileTokenId> token(DockerfileTokenId id) {
        final String fixedText = id.fixedText();
        return (fixedText != null && fixedText.length() == input.readLength())
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id);
    }

    private int nextChar() {
        previousLength = currentLength;
        int c = input.read();
        currentLength = 1;
        return c;
    }

    public void backup(int howMany) {
        switch (howMany) {
            case 1:
                assert currentLength != (-1);
                input.backup(currentLength);
                currentLength = previousLength;
                previousLength = (-1);
                break;
            case 2:
                assert currentLength != (-1) && previousLength != (-1);
                input.backup(currentLength + previousLength);
                currentLength = previousLength = (-1);
                break;
            default:
                assert false : howMany;
        }
    }

    private void consumeNewline() {
        if (nextChar() != '\n') backup(1);
    }

    private Token<DockerfileTokenId> finishWhitespace(
            int currentState,
            int[] stateHolder) {
        while (true) {
            int c = nextChar();
            switch (c) {
                case '\r':
                case '\n':
                    stateHolder[0] = currentState == STATE_ESCAPE ?
                            STATE_CONT_LINE:
                            STATE_NEW_LINE;
                    break;
                case '\t':
                case 0x0b:
                case '\f':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                case ' ':
                    break;
                case EOF:
                default:
                    backup(1);
                    return tokenFactory.createToken(DockerfileTokenId.WHITESPACE);
            }
        }
    }

    @NonNull
    private Token<DockerfileTokenId> finishString(
            final char closingChar,
            final char otherStringChar,
            int[] stateHolder) {
        boolean inEscapeBlock = false,
                escapeBlockAllowed = true;
        while (true) {
            int c = nextChar();
            if (c == '\r') {    //NOI18N
                stateHolder[0] = STATE_NEW_LINE;
                consumeNewline();
                return tokenFactory.createToken(
                            DockerfileTokenId.STRING_LITERAL,
                            input.readLength(),
                            PartType.START);
            } else if (c == '\n') { //NOI18N
                stateHolder[0] = STATE_NEW_LINE;
                return tokenFactory.createToken(
                            DockerfileTokenId.STRING_LITERAL,
                            input.readLength(),
                            PartType.START);
            } else if (c == EOF) {
                return tokenFactory.createToken(
                            DockerfileTokenId.STRING_LITERAL,
                            input.readLength(),
                            PartType.START);
            } else if (c == closingChar) {
                if (!inEscapeBlock) {
                    return token(DockerfileTokenId.STRING_LITERAL);
                }
            } else if (c == otherStringChar && escapeBlockAllowed) {
                inEscapeBlock = !inEscapeBlock;
            } else if (c == '#') {    //NOI18N
                //Special handling for sh comment which may be followd by nonclosing otherStringChar
                escapeBlockAllowed = false;
            }else if (c == '\\') {    //NOI18N
                boolean followedBySpace = false;
                for (c = nextChar(); isSpaceOnLine(c); c = nextChar()) {
                    followedBySpace = true;
                }
                if (followedBySpace) {
                    switch (c) {
                        case '\r':  //NOI18N
                        case '\n':  //NOI18N
                            break;
                        default:
                            backup(1);
                    }
                }
            }
        }
    }

    @NonNull
    private Token<DockerfileTokenId> finishNumber(int c) {
        while (true) {
            switch (c) {
                case '0': case '1': case '2': case '3': case '4':   //NOI18N
                case '5': case '6': case '7': case '8': case '9':   //NOI18N
                case 'a': case 'b': case 'c': case 'd': case 'e':   //NOI18N
                case 'f': case 'A': case 'B': case 'C': case 'D':   //NOI18N
                case 'E': case 'F':
                    break;
                default:
                    backup(1);
                    return token(DockerfileTokenId.NUMBER_LITERAL);
            }
            c = nextChar();
        }
    }

    @NonNull
    private Token<DockerfileTokenId> keywordOrIdentifier(final int currentState) {
        DockerfileTokenId id = filter(KW_TO_TKN.get(input.readText().toString().toUpperCase()), currentState);
        if (id == null) {
            id = DockerfileTokenId.IDENTIFIER;
        }
        return token(id);
    }

    @CheckForNull
    private static DockerfileTokenId filter(@NullAllowed DockerfileTokenId id, final int currentState) {
        switch (currentState) {
            case STATE_NEW_LINE:
                return id;
            case STATE_ONBUILD:
                return KW_ON_BUILD.contains(id) ? id : null;
            default:
                return null;
        }
    }

    private static boolean isIdentifierStart(final int c, final int currentState) {
        return Character.isJavaIdentifierStart(c) ||
            (c == '#' && currentState != STATE_NEW_LINE && currentState != STATE_CONT_LINE);   //NOI18N
    }

    private static boolean isIdentifierPart(final int c, int currentState) {
        return Character.isJavaIdentifierPart(c) ||
                c == '.' || //NOI18N
                c == '-' || //NOI18N
                c == '_' || //NOI18N
                (c == '#' && currentState != STATE_NEW_LINE && currentState != STATE_CONT_LINE);   //NOI18N
    }

    private static boolean isSpaceOnLine(int c) {
        switch(c) {
            case ' ':   //NOI18N
            case '\t':  //NOI18N
                return  true;
            default:
                return false;
        }
    }

}
