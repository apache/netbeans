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
package org.netbeans.spi.lexer.antlr4;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Predicate;
import org.antlr.v4.runtime.Lexer;
import static org.antlr.v4.runtime.Recognizer.EOF;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/**
 * This class provides sequential cursor based access to ANTLR Lexer tokens.
 * The cursor can be sought in the input stream.
 *
 * @author lkishalmi
 */
public final class AntlrTokenSequence {

    private final TokenSource tokens;
    private boolean eofRead = false;
    private int readIndex;
    private int cursorOffset;
    private ListIterator<Token> cursor;
    
    private final ArrayList<Token> tokenList = new ArrayList<>(200);

    /** Predicate constant to select tokens on the DEFAULT_CHANNEL */
    public static final Predicate<Token> DEFAULT_CHANNEL = new ChannelFilter(Lexer.DEFAULT_TOKEN_CHANNEL);

    /** Predicate class to filter tokens by a selected channel */
    public static final class ChannelFilter implements Predicate<Token> {
        private final int channel;

        public ChannelFilter(int channel) {
            this.channel = channel;
        }

        @Override
        public boolean test(Token t) {
            return channel == t.getChannel();
        }
    }

    /**
     * Creates a AntlrTokenSequence over the provided TokenSource (Lexer)
     *
     * @param tokens the token source to be read.
     */
    public AntlrTokenSequence(TokenSource tokens) {
        this.tokens = tokens;
        this.cursor = tokenList.listIterator();
    }

    /**
     * Seeks the cursor to an offset in the input stream. If the offset is
     * inside a token, then the {@linkplain #next()} method would return the
     * token which contains the offset.
     *
     * @param offset the position in the lexer input stream to seek to.
     */
    public void seekTo(int offset) {
        if (offset > readIndex) {
            if (cursor.hasNext()) {
                //replace the cursor if it is not at the end of the list.
                cursor = tokenList.listIterator(tokenList.size());
            }
            Token t = read();
            while ((t != null) && (t.getStopIndex() + 1 < offset)) {
                t = read();
            }
            if (t != null && (offset < t.getStopIndex() + 1)) {
                cursor.previous();
                cursorOffset = t.getStartIndex();
            } else {
                cursorOffset = readIndex;
            }
        } else {
            if (offset > getOffset()) {
                next((t) -> t.getStopIndex() > offset);
                if (cursor.hasPrevious()) {
                    cursor.previous();
                }
                
            } else {
                previous((t) -> t.getStartIndex() < offset);
            }
        }
        
    }

    /**
     * Returns {@code true} if the lexer stream is empty.
     *
     * @return {@code true} if the lexer stream is empty.
     */
    public boolean isEmpty() {
        return tokenList.isEmpty() && !hasNext();
    }
    
    /**
     * Returns {@code true} if there is a token to read after the cursor.
     *
     * @return {@code true} if there is a token to read after the cursor.
     */
    public boolean hasNext() {
        if (!eofRead && (cursorOffset == readIndex) && !cursor.hasNext()) {
            Token t = read();
            if (t != null) {
                cursor.previous();
            }
        }
        return !(eofRead && !cursor.hasNext());
    }
    
    /**
     * Returns {@code true} if there is a token to read before the cursor.
     *
     * @return {@code true} if there is a token to read before the cursor.
     */
    public boolean hasPrevious() {
        return cursor.hasPrevious();
    }

    /**
     * Returns the offset of the cursor. It always returns index on token
     * boundary.
     *
     * @return the offset of the cursor.
     */
    public int getOffset() {
        return cursorOffset;
    }

    /**
     * Returns the token before the cursor if there is one. It also moves the
     * cursor left by one.
     *
     * @return the token before the cursor.
     */
    public Optional<Token> previous() {
        Optional<Token> ret = cursor.hasPrevious() ? Optional.of(cursor.previous()) : Optional.empty();
        cursorOffset = cursor.hasPrevious() ? ret.get().getStartIndex() : 0;
        return ret;
        
    }
    
    /**
     * Returns the token before the cursor that satisfies the provided predicate
     * condition. It also moves the cursor at the start of the returned token.
     *
     * @param filter the predicate to satisfy.
     * @return the token before the cursor.
     */
    public Optional<Token> previous(Predicate<Token> filter) {
        Optional<Token> ot = previous();
        while (ot.isPresent() && !filter.test(ot.get())) {
            ot = previous();
        }
        return ot;
    }
    
    /**
     * Returns the token before the cursor that matches the provided token type.
     * It also moves the cursor at the start of the returned token.
     *
     * @param tokenType the searched token type
     * @return the token before the cursor.
     */
    public Optional<Token> previous(int tokenType){
        return previous((Token t) -> t.getType() == tokenType);
    }
    
    /**
     * Returns the token after the cursor if there is one. It also moves the
     * cursor right by one.
     *
     * @return the token after the cursor.
     */
    public Optional<Token> next() {
        if (hasNext()) {
            Token t = cursor.next();
            cursorOffset = t.getStopIndex() + 1;
            return Optional.of(t);
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Returns the token after the cursor that satisfies the provided predicate
     * condition. It also moves the cursor at the end of the returned token.
     *
     * @param filter the predicate to satisfy.
     * @return the token after the cursor.
     */
    public Optional<Token> next(Predicate<Token> filter) {
        Optional<Token> ot = next();
        while (ot.isPresent() && !filter.test(ot.get())) {
            ot = next();
        }
        return ot;
    }
    
    /**
     * Returns the token after the cursor that matches the provided token type.
     * It also moves the cursor at the end of the returned token.
     *
     * @param tokenType the searched token type
     * @return the token after the cursor.
     */
    public Optional<Token> next(int tokenType){
        return next((Token t) -> t.getType() == tokenType);
    }

    private Token read() {
        if (eofRead) {
            return null;
        }
        Token t = tokens.nextToken();
        if (t.getType() != EOF) {
            cursor.add(t);
            readIndex = t.getStopIndex() + 1;
            return t;
        } else {
            eofRead = true;
            return null;
        }
    }
    
}
