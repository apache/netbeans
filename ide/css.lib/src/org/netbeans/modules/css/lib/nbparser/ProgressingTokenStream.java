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
package org.netbeans.modules.css.lib.nbparser;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;

public class ProgressingTokenStream implements TokenStream {
    private final int maxReadCalls;
    private final TokenStream backingStream;

    public ProgressingTokenStream(int maxReadCalls, TokenStream backingStream) {
        this.maxReadCalls = maxReadCalls;
        this.backingStream = backingStream;
    }

    @Override
    public Token LT(int k) {
        Token t = backingStream.LT(k);
        limitReadCalls(t);
        return t;
    }

    @Override
    public Token get(int i) {
        Token t = backingStream.get(i);
        limitReadCalls(t);
        return t;
    }

    private int highestReachedIndex = 0;
    private int readCalls = 0;

    private void limitReadCalls(Token t) throws RuntimeException {
        int index = t.getTokenIndex();
        if (index > highestReachedIndex) {
            highestReachedIndex = index;
            readCalls = 0;
        }
        readCalls++;
        if (readCalls > maxReadCalls) {
            throw new ProgressingFailedException("Excessive read calls");
        }
    }

    @Override
    public int range() {
        return backingStream.range();
    }

    @Override
    public TokenSource getTokenSource() {
        return backingStream.getTokenSource();
    }

    @Override
    public String toString(int i, int i1) {
        return backingStream.toString(i, i1);
    }

    @Override
    public String toString(Token token, Token token1) {
        return backingStream.toString(token, token1);
    }

    @Override
    public void consume() {
        backingStream.consume();
    }

    @Override
    public int LA(int i) {
        return backingStream.LA(i);
    }

    @Override
    public int mark() {
        return backingStream.mark();
    }

    @Override
    public int index() {
        return backingStream.index();
    }

    @Override
    public void rewind(int i) {
        backingStream.rewind(i);
    }

    @Override
    public void rewind() {
        backingStream.rewind();
    }

    @Override
    public void release(int i) {
        backingStream.release(i);
    }

    @Override
    public void seek(int i) {
        backingStream.seek(i);
    }

    @Override
    public int size() {
        return backingStream.size();
    }

    @Override
    public String getSourceName() {
        return backingStream.getSourceName();
    }
}
