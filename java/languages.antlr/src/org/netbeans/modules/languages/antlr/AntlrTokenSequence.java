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
package org.netbeans.modules.languages.antlr;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Predicate;
import org.antlr.v4.runtime.Lexer;
import static org.antlr.v4.runtime.Recognizer.EOF;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/**
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

    public static final Predicate<Token> DEFAULT_CHANNEL = new ChannelFilter(Lexer.DEFAULT_TOKEN_CHANNEL);
    
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
    
    public AntlrTokenSequence(TokenSource tokens) {
        this.tokens = tokens;
        this.cursor = tokenList.listIterator();
    }
    
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
    
    public boolean isEmpty() {
        return tokenList.isEmpty() && !hasNext();
    }
    
    public boolean hasNext() {
        if (!eofRead && (cursorOffset == readIndex) && !cursor.hasNext()) {
            Token t = read();
            if (t != null) {
                cursor.previous();
            }
        }
        return !(eofRead && !cursor.hasNext());
    }
    
    public boolean hasPrevious() {
        return cursor.hasPrevious();
    }
    
    public int getOffset() {
        return cursorOffset;
    }
    
    public Optional<Token> previous() {
        Optional<Token> ret = cursor.hasPrevious() ? Optional.of(cursor.previous()) : Optional.empty();
        cursorOffset = cursor.hasPrevious() ? ret.get().getStartIndex() : 0;
        return ret;
        
    }
    
    public Optional<Token> previous(Predicate<Token> filter) {
        Optional<Token> ot = previous();
        while (ot.isPresent() && !filter.test(ot.get())) {
            ot = previous();
        }
        return ot;
    }
    
    public Optional<Token> previous(int tokenType){
        return previous((Token t) -> t.getType() == tokenType);
    }
    
    public Optional<Token> next() {
        if (hasNext()) {
            Token t = cursor.next();
            cursorOffset = t.getStopIndex() + 1;
            return Optional.of(t);
        } else {
            return Optional.empty();
        }
    }
    
    public Optional<Token> next(Predicate<Token> filter) {
        Optional<Token> ot = next();
        while (ot.isPresent() && !filter.test(ot.get())) {
            ot = next();
        }
        return ot;
    }
    
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
