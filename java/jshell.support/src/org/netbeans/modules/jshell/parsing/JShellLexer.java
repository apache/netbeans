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
package org.netbeans.modules.jshell.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.PartType;
import org.netbeans.modules.jshell.model.JShellToken;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 *
 * @author sdedic
 */
public class JShellLexer implements Lexer<JShellToken>, TokenPropertyProvider<JShellToken> {
    private final TokenFactory<JShellToken> tokenFactory;
    private final LexerInput input;
    
    private S state = S.INITIAL;
    
    private static final String[] COMMANDS = {
        "list", "drop", "save",
        "open",
        "vars", "methods", "types", "imports",
        "exit", "reset", "reload",
        
        "classpath", "history",
        "debug",
        
        "help",
        "set",
        "?", "!"
    };
    
    private static final String[] COMMAND_STRINGS = {
        "l", "list", // NOI18N
        "dr", "drop", // NOI18N
        "sa", "save", // NOI18N
        "o", "open", // NOI18N
        "v", "vars", // NOI18N
        "m", "methods",  // NOI18N
        "cl", "types",  // NOI18N
        "im", "imports", // NOI18N
        "ex", "exit", // NOI18N
        "res", "reset",  // NOI18N
        "rel", "reload",  // NOI18N
        "c", "classpath",  // NOI18N
        "hi", "history",  // NOI18N
        "de", "debug",    // NOI18N
        "he", "?", "help",  // NOI18N
        "se", "set",        // NOI18N
        "!", "" // NOI18N
    };
    
    private static enum S {
        INITIAL, // next: PROMPT_INPUT
        PROMPT_INPUT,
        PROMPT_MESSAGE,
        MESSAGE,
        JAVA,
        COMMAND,
        START_MARKER
    }
    
    private static class LexState {
        S   s;
        S   prev;
    }
    
    private S prevState = S.INITIAL;
    
    private static final Map<String, String> commandUniquePrefixes = new HashMap<>();
    
    private static void initCommandPrefixes() {
        Set<String> seenPrefixes = new HashSet<>();
        for (String s : COMMANDS) {
            for (int i = 1; i <= s.length(); i++) {
                String x = s.substring(0, i);
                if (seenPrefixes.contains(x)) {
                    commandUniquePrefixes.remove(x);
                } else {
                    commandUniquePrefixes.put(x, s);
                    seenPrefixes.add(x);
                }
            }
        }
    }
    
    public static List<String>  getCommandsFromPrefix(String prefix) {
        List<String> commands = new ArrayList<>();
        for (String s : COMMANDS) {
            if (s.startsWith(prefix)) {
                commands.add(s);
            }
        }
        return commands;
    }
    
    static {
        initCommandPrefixes();
    }
    
    public JShellLexer(LexerRestartInfo<JShellToken> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        Object s = info.state();
        if (s != null) {
            LexState ls = (LexState)s;
            this.state = ls.s;
            this.prevState = ls.prev;
        }
    }
    
    private void setState(S ns, S ps) {
        this.prevState = ps;
        this.state = ns;
    }
    
    private void setState(S ns) {
        this.prevState = state;
        this.state = ns;
    }
    
    private Token<JShellToken> possibleSpace(JShellToken t, int spaces, S next, boolean flyweight) {
        int c;
        
        this.state = next;
        for (int s = spaces; s > 0; s--) {
            c = input.read();
            if (c != ' ' && c != '\n') {
                // not a part of the token:
                input.backup(1);
                return blockToken(t);
            }
        }
        if (flyweight) {
            return tokenFactory.getFlyweightToken(t, t.fixedText());
        } else {
            return tokenFactory.createToken(t);
        }
    }
    
    private Token<JShellToken> promptOrDefault(boolean continuation) {
        int c = input.read();
        if (c != '>') {
            return state == S.JAVA ? 
                eoln(JShellToken.JAVA) : eoln(JShellToken.OUTPUT);
        }
        return possibleSpace(continuation ?
            JShellToken.CONTINUATION_PROMPT : JShellToken.PROMPT, 1, S.PROMPT_INPUT, false);
    }
    
    private Token<JShellToken> blockToken(JShellToken id) {
        return tokenFactory.createPropertyToken(id, input.readLength(), this);
    }
    
    private Token<JShellToken> messageLine() {
        int c;
        int s = 0;
        int mark = -1;
        
        CYCLE: while (true) {
            c = input.read();
            switch (c) {
                case '\n':
                    setState(S.INITIAL, S.MESSAGE);
                    return blockToken(JShellToken.MESSAGE_TEXT);

                case ' ':
                    break;

                case '-':
                    // retain 1, otherwise count as non-whitespace.
                    if (s == 1 || s == 2) {
                        s = 2;
                    } else {
                        s = 10;
                    }
                    break;

                case '.':
                    // accept 3 consecutive dots
                    if (s >= 2 && ++s == 5) {
                        // read one more character to learn
                        int c2 = input.read();
                        if (c2 != '.') {
                            input.backup(1);
                            setState(S.PROMPT_MESSAGE);
                            return tokenFactory.createToken(JShellToken.ERROR_MARKER);
                        } else {
                            input.backup(1);
                        }
                    }
                    s = 10;
                    break;
                case '^':
                    if (s == 0) {
                        // only non-whitespaces at the beginning
                        mark = input.readLength();
                        s = 1;
                        if (input.readLength() > 1) { // something else preceding the ^ marker
                            input.backup(1);
                            setState(S.PROMPT_MESSAGE);
                            return blockToken(JShellToken.MESSAGE_TEXT);
                        }
                    } else if (s >= 1 && s < 3) {
                        // terminating ^ after initial ^
                        setState(S.PROMPT_MESSAGE);
                        return tokenFactory.createToken(JShellToken.ERROR_MARKER);
                    } else {
                        s = 10;
                    }
                    break;
                case LexerInput.EOF:
                    break CYCLE;
                default:
                    s = 10;
                    break;
            }
        }
        setState(S.INITIAL, S.MESSAGE);
        return blockToken(JShellToken.MESSAGE_TEXT);
    }
    
    private Token<JShellToken> eoln(JShellToken id) {
        return eoln(id, S.INITIAL);
    }
    
    private Token<JShellToken> eoln(JShellToken id, S nextState) {
        int c;
        
        do {
            c = input.read();
        } while (c != LexerInput.EOF && c != '\n');
        setState(nextState);
        return input.readLength() > 0 ? blockToken(id) : null;
    }
    
    private boolean eatWhitespace() {
        int c;
        boolean wh = true;
        if (input.readLength() > 0) {
            input.backup(1);
        }
        do {
            c = input.read();
        } while (c != LexerInput.EOF && c != '\n' && (wh = Character.isWhitespace(c)));
        return wh;
    }
    
    /**
     * Determines if the string is a command prefix.
     * Returns 0, if the string forms a whole command. Positive, if command prefix
     * and negative if no command is found.
     * @return 
     */
    private int startsCommand(String s) {
        int l = s.length();
        if (l == 0) {
            return 20;
        } 
        int idx = 0;
        char first = s.charAt(0);
        if (first == '-') {
            if (l == 1) {
                return 2;
            }
            first = s.charAt(1);
            idx++;
        }
        if (Character.isDigit(first)) {
            try {
                Integer.parseInt(s.substring(idx));
                return 0;
            } catch (NumberFormatException e) {
                // ignore
                return -1;
            }
        }
        String full = commandUniquePrefixes.get(s);
        if (full != null) {
            return full.length() - l;
        }
        return -1;
    }
    
    private Token<JShellToken> commandOrJava() {
        int c;
        
        c = input.read();
        if (c == LexerInput.EOF) {
            return null;
        }
        if (Character.isWhitespace(c) && eatWhitespace()) {
            // represent empty lines as java
            setState(S.JAVA);
            return blockToken(JShellToken.JAVA);
        }
        if (c != '/') {
            return eoln(JShellToken.JAVA, S.JAVA);
        }
        boolean cont;
        do {
            c = input.read();
        } while ((cont = (c != LexerInput.EOF && c != '\n')) && !Character.isWhitespace(c));
        // \n is a whitespace, but `end' now contains a flag that the command is terminated
        // and parameters are not expectex
        
        String cmd = input.readText().toString();
        // strip the slash
        cmd = cmd.substring(1).trim();
        setState(cont ? S.COMMAND : S.INITIAL);
        if ("".equals(cmd)) {
            return blockToken(JShellToken.ERR_COMMAND);
        }
        int status = startsCommand(cmd);
        if (status == -1) {
            return blockToken(JShellToken.ERR_COMMAND);
        }
        if (status > 0) {
            if (c == '\n') {
                input.backup(1);
            } else if (c != LexerInput.EOF) {
                // just a whitespace after partial command
                 return blockToken(JShellToken.ERR_COMMAND);
            }
        } else if (status == 0) {
            if (c != LexerInput.EOF) {
                if (!Character.isWhitespace(c)) {
                    do {
                        c = input.read();
                    } while (c != LexerInput.EOF && !Character.isWhitespace(c));
                    if (c != LexerInput.EOF) {
                        input.backup(1);
                    }
                    return tokenFactory.createToken(JShellToken.ERR_COMMAND);
                }
                input.backup(1);
            }
        }
//        setState(cont ? S.COMMAND : S.INITIAL);
        setState(S.COMMAND);
        return blockToken(JShellToken.COMMAND);
    }
    
    private Token<JShellToken> possibleNumberPrompt() {
        int c;
        c = input.read();
        while (Character.isDigit(c)) {
            c = input.read();
        }
        if (c != ']') {
            return null;
        }
        c = input.read();
        if (c == ' ') {
            c = input.read();
        }
        if (c == '-' || c == '>') {
            return promptOrDefault(c == '>');
        }
        
        return null;
    }
    
    private Token<JShellToken> createContinuationTokenForState(S s) {
        switch (s) {
            case INITIAL:
                return tokenFactory.createToken(JShellToken.WHITESPACE);
            case PROMPT_INPUT:
                return blockToken(JShellToken.JAVA);
            case PROMPT_MESSAGE:
                return blockToken(JShellToken.MESSAGE_TEXT);
            case MESSAGE:
                return blockToken(JShellToken.MESSAGE_TEXT);
            case JAVA:
                return blockToken(JShellToken.JAVA);
            case COMMAND:
                return blockToken(JShellToken.OUTPUT);
        }
        return null;
    }

    @Override
    public Token<JShellToken> nextToken() {
        Token<JShellToken> token = null;
        
        switch (state) {
            case JAVA:
            case INITIAL:
                int c = input.read();
                switch (c) {
                    case '[': 
                        if ((token = possibleNumberPrompt()) != null) {
                            return token;
                        }
                        break;
                        
                    case '|':
                        return possibleSpace(JShellToken.MESSAGE_MARK, 2, S.PROMPT_MESSAGE, true);
                        
                    case '-':
                        return promptOrDefault(false);
                        
                    case '>':
                        return promptOrDefault(true);
                        
                    case LexerInput.EOF:
                        return null;
                        
                    default:
                        input.backup(1);
                        break;
                }
                if (state == S.JAVA) {
                    return eoln(JShellToken.JAVA, S.JAVA);
                } else {
                    // if all whitespace, assign to the previous state
                    if (prevState != S.INITIAL && eatWhitespace()) {
                        return createContinuationTokenForState(prevState);
                    }
                    return eoln(JShellToken.OUTPUT, state);
                }
                
            case COMMAND:
                return commandLine();
                
            case MESSAGE:
                return eoln(JShellToken.MESSAGE_TEXT);
                
            case PROMPT_MESSAGE:
                // consume anything till end of the line, look for error marker
                return messageLine();
                
            case PROMPT_INPUT:
                return commandOrJava();

            default:
                throw new IllegalStateException(state.toString());
        }
    }
    
    private Token<JShellToken>  commandLine() {
        int c = input.read();
        if (c == LexerInput.EOF) {
            return null;
        }
        if (Character.isWhitespace(c)) {
            boolean endsNonWhite = eatWhitespace();
            if (!endsNonWhite) {
                input.backup(1);
            } else {
                setState(S.INITIAL);
            }
            return blockToken(JShellToken.WHITESPACE);
        }
        int quote = 0;
        if (c == '\'' || c == '"') {
            quote = c;
            c = input.read();
        }
        
        if (c == '-') {
            // dash-option
            return readParameter(JShellToken.COMMAND_OPTION, quote);
        } else {
            return readParameter(quote == 0  ? JShellToken.COMMAND_PARAM : JShellToken.COMMAND_STRING, quote);
        }
    }
    
    private Token<JShellToken> readParameter(JShellToken tokenId, int quote) {
        int c = input.read();
        boolean verbatim = false;
        
        while (true) {
            if (c == LexerInput.EOF) {
                // only partial token
                state = S.INITIAL;
                return tokenFactory.createToken(tokenId, input.readLength(), PartType.START);
            }
            if (c == '\\') { // NOI18N
                // next character verbatim
                verbatim = true;
                c = input.read();
                continue;
            }
            if (!verbatim && c == quote) {
                break;
            }
            if (!verbatim && quote == 0) {
                if (Character.isWhitespace(c)) {
                    input.backup(1);
                    break;
                }
            } else if (c == '\n') { // NOI18N
                // unterminated quote
                input.backup(1);
                state = S.INITIAL;
                return tokenFactory.createToken(tokenId, input.readLength(), PartType.START);
            }
            verbatim = false;
            c = input.read();
        }
        return tokenFactory.createToken(tokenId, input.readLength());
    }

    @Override
    public Object state() {
        LexState ls = new LexState();
        ls.prev = this.prevState;
        ls.s = this.state;
        return ls;
    }

    @Override
    public void release() {
        // no op
    }

    @Override
    public Object getValue(Token<JShellToken> token, Object key) {
        if ("highlight.block".equals(key)) { // NOI18N
            switch (token.id()) {
                case MESSAGE_TEXT:
                case JAVA:
                case COMMAND:
                case COMMAND_PARAM:
                case OUTPUT:
                    return true;
            }
        }
        return null;
    }
    
}
