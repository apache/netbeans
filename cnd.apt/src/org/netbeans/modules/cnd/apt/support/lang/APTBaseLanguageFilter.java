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

package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.cnd.apt.impl.support.APTLiteLiteralToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public abstract class APTBaseLanguageFilter implements APTLanguageFilter {
    private final Map<CharSequence,Integer/*ttype*/> filter;
    private final Set<Integer/*ttype*/> keywords = new HashSet<Integer>();

    // #268301 - ArrayIndexOutOfBoundsException: 308
    // use own cache of Integers
    private static final int BUFFERED_COUNT = 1024;
    private static final Integer[] INT_TO_INTEGER_CACHE;
    static {
        INT_TO_INTEGER_CACHE = new Integer[BUFFERED_COUNT];
        for (int i = 0; i < BUFFERED_COUNT; i++) {
            INT_TO_INTEGER_CACHE[i] = Integer.valueOf(i);
        }
    }

    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    protected APTBaseLanguageFilter(boolean caseInsensitive) {
        if (caseInsensitive) {
            filter = new TreeMap<CharSequence,Integer>(CharSequenceUtils.ComparatorIgnoreCase);
        } else {
            filter = new HashMap<CharSequence,Integer>();
        }
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

//    // do necessary initializations in derived classes by calling
//    // filter() method to fill up the filter
//    protected abstract void initialize();

    /**
     * add token's key to be filtered
     * the token stream returned from getFilteredStream
     * will change the type of original token to new token type
     * if original token has the filtered textKey value
     */
    protected void filter(String text, int ttype) {
        filter.put(CharSequences.create(text), INT_TO_INTEGER_CACHE[ttype]);
        keywords.add(INT_TO_INTEGER_CACHE[ttype]);
    }

    protected Token onID(Token token) {
        // literal token has new type inside already
        if (token instanceof APTLiteLiteralToken) {
            APTLiteLiteralToken literalToken = (APTLiteLiteralToken)token;
            final Integer literalTypeAsInteger = INT_TO_INTEGER_CACHE[literalToken.getLiteralType()];
            if (keywords.contains(literalTypeAsInteger)) {
                assert literalTypeAsInteger == filter.get(((APTToken)token).getTextID());
                return new FilterLiteralToken((APTLiteLiteralToken)token);
            } else {
                return token;
            }
        }
        return defaultWrap(token);
    }
    
    protected final Token defaultWrap(Token token) {
        Integer newType = filter.get(((APTToken)token).getTextID());
        if (newType != null) {
            int ttype = newType.intValue();
            token = new FilterToken((APTToken)token, ttype);
        }
        return token;
    }

    protected Token onToken(Token token) {
        if (token.getType() == APTTokenTypes.IDENT) {
            token = onID(token);
        }
        return token;
    }

    private final class FilterStream implements TokenStream {
        private TokenStream orig;
        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            Token token = orig.nextToken();
            token = onToken(token);
            return token;
        }
    }

    public boolean isKeyword(Token token) {
        if (token.getType() == APTTokenTypes.IDENT) {
            Integer newType = filter.get(((APTToken)token).getTextID());
            if (newType != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isKeyword(int ttype) {
        return keywords.contains(Integer.valueOf(ttype));
    }

    public static final class FilterTextToken extends FilterToken {
        private final String text;
        private final int shift;

        public FilterTextToken(APTToken origToken, int type, String text, int shift) {
            super(origToken, type);
            this.text = text;
            this.shift = shift;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public CharSequence getTextID() {
            return text;
        }

        @Override
        public int getColumn() {
            return super.getColumn()+shift;
        }

        @Override
        public int getEndColumn() {
            return super.getEndColumn()+shift;
        }

        @Override
        public int getOffset() {
            return super.getOffset()+shift;
        }
    }

    /**
     * A wrapper token that changes original token type
     * and delegates the rest of the methods to original token.
     */
    public static class FilterToken implements APTToken {

        private final APTToken origToken;
        private int type;

        public FilterToken(APTToken origToken, int type) {
            this.origToken = origToken;
            this.type = type;
        }

        public APTToken getOriginalToken() {
            return origToken;
        }

        @Override
        public int getOffset() {
            return origToken.getOffset();
        }

        @Override
        public void setOffset(int o) {
            origToken.setOffset(o);
        }

        @Override
        public int getEndColumn() {
            return origToken.getEndColumn();
        }

        @Override
        public void setEndColumn(int c) {
            origToken.setEndColumn(c);
        }

        @Override
        public int getEndLine() {
            return origToken.getEndLine();
        }

        @Override
        public void setEndLine(int l) {
            origToken.setEndLine(l);
        }

        @Override
        public int getEndOffset() {
            return origToken.getEndOffset();
        }

        @Override
        public void setEndOffset(int o) {
            origToken.setEndOffset(o);
        }

        @Override
        public CharSequence getTextID() {
            return origToken.getTextID();
        }

        @Override
        public void setTextID(CharSequence id) {
            origToken.setTextID(id);
        }

        @Override
        public int getColumn() {
            return origToken.getColumn();
        }

        @Override
        public void setColumn(int c) {
            origToken.setColumn(c);
        }

        @Override
        public int getLine() {
            return origToken.getLine();
        }

        @Override
        public void setLine(int l) {
            origToken.setLine(l);
        }

        @Override
        public String getFilename() {
            return origToken.getFilename();
        }

        @Override
        public void setFilename(String name) {
            origToken.setFilename(name);
        }

        @Override
        public String getText() {
            return origToken.getText();
        }

        @Override
        public void setText(String t) {
            origToken.setText(t);
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public void setType(int t) {
            this.type = t;
        }

        @Override
        public String toString() {
            return "FilterToken: " + APTUtils.getAPTTokenName(type) + ((origToken == null) ? "null" : origToken.toString()); // NOI18N
        }
        
        @Override
        public Object getProperty(Object key) {
            return null;
        }        
    }
    
    /**
     * Special wrapper for literal tokens, they have new type inside already 
     * so there is no need to have field for a new type
     */
    private static class FilterLiteralToken implements APTToken {
        private final APTLiteLiteralToken origToken;

        public FilterLiteralToken(APTLiteLiteralToken origToken) {
            this.origToken = origToken;
        }

        public int getOffset() {
            return origToken.getOffset();
        }

        public void setOffset(int o) {
            origToken.setOffset(o);
        }

        public int getEndOffset() {
            return origToken.getEndOffset();
        }

        public void setEndOffset(int o) {
            origToken.setEndOffset(o);
        }

        public int getEndColumn() {
            return origToken.getEndColumn();
        }

        public void setEndColumn(int c) {
            origToken.setEndColumn(c);
        }

        public int getEndLine() {
            return origToken.getEndLine();
        }

        public void setEndLine(int l) {
            origToken.setEndLine(l);
        }

        public String getText() {
            return origToken.getText();
        }

        public CharSequence getTextID() {
            return origToken.getTextID();
        }

        public void setTextID(CharSequence id) {
            origToken.setTextID(id);
        }

        public Object getProperty(Object key) {
            return null;
        }

        public int getColumn() {
            return origToken.getColumn();
        }

        public void setColumn(int c) {
            origToken.setColumn(c);
        }

        public int getLine() {
            return origToken.getLine();
        }

        public void setLine(int l) {
            origToken.setLine(l);
        }

        public String getFilename() {
            return origToken.getFilename();
        }

        public void setFilename(String name) {
            origToken.setFilename(name);
        }

        public void setText(String t) {
            origToken.setText(t);
        }

        public int getType() {
            return origToken.getLiteralType();
        }

        public void setType(int t) {
            throw new IllegalStateException("Not supported"); //NOI18N
        }
        
        @Override
        public String toString() {
            return "FilterToken: " + APTUtils.getAPTTokenName(getType()) + ((origToken == null) ? "null" : origToken.toString()); // NOI18N
        }
    }
}
