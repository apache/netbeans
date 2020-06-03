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
package org.netbeans.modules.cnd.modelimpl.parser;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 */
final class CPPStraightParserEx extends CPPParserEx {
    protected CPPStraightParserEx(TokenStream stream, CppParserActionEx callback, int initialBufferCapacity) {
        super(stream, callback, initialBufferCapacity);
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
    }

    // Number of active markers
    private int nMarkers = 0;

    @Override
    public int mark() {
        nMarkers++;
        return super.mark();
    }

    @Override
    public void rewind(int pos) {
        nMarkers--;
        super.rewind(pos);
    }

    @Override
    public int LA(int i) {
        final int newIndex = skipIncludeTokensIfNeeded(i);
        int LA = super.LA(newIndex);
        assert !isIncludeToken(LA) : super.LT(newIndex) + " not expected";
        return LA;
    }

    @Override
    public Token LT(int i) {
        Token LT = super.LT(skipIncludeTokensIfNeeded(i));
        assert !isIncludeToken(LT.getType()) : LT + " not expected ";
        return LT;
    }

    @Override
    public void consume() {
        assert !isIncludeToken(super.LA(1)) : super.LT(1) + " not expected ";
        super.consume();
        // consume following includes as well
        while (isIncludeToken(super.LA(1))) {
            Token t = super.LT(1);
            onIncludeToken(t);
            super.consume();
        }
    }
    
    private int skipIncludeTokensIfNeeded(int i) {
        if (i == 0) {
            assert !isIncludeToken(super.LA(0)) : super.LT(0) + " not expected ";
            return 0;
        }
        int superIndex = 0;
        int nonIncludeTokens = 0;
        do {
            superIndex++;
            int LA = super.LA(superIndex);
            assert LA == super.LA(superIndex) : "how can LA be different?";
            if (isIncludeToken(LA)) {
                if (nMarkers == 0 && superIndex == 1 && guessing == 0) {
                    // consume if the first an no markers
                    Token t = super.LT(1);
                    assert isIncludeToken(t.getType()) : t + " not expected ";
                    onIncludeToken(t);
                    assert super.LT(1) == t : t + " have to be the same as " + super.LT(1);
                    super.consume();
                    superIndex = 0;
                }
            } else {
                nonIncludeTokens++;
            }
        } while (nonIncludeTokens < i);
        assert (superIndex >= i) && nonIncludeTokens == i : "LA(" + i + ") => LA(" + superIndex + ") " + nonIncludeTokens + ")" + super.LT(superIndex);
        return superIndex;
    }

    private static boolean isIncludeToken(int LA) {
        return LA == APTTokenTypes.INCLUDE || LA == APTTokenTypes.INCLUDE_NEXT;
    }
    
    private void onIncludeToken(Token t) {
        if (t instanceof APTToken) {
            APTToken aptToken = (APTToken) t;
            Boolean preInclude = (Boolean) aptToken.getProperty(Boolean.class);
            CsmFile inclFile = (CsmFile) aptToken.getProperty(CsmFile.class);
            if (inclFile != null) {
                if (preInclude == Boolean.TRUE) {
                    action.pushFile(inclFile);
                    assert inclFile instanceof FileImpl;
                } else {
                    CsmFile popFile = action.popFile();
                    assert popFile == inclFile;
                }
            }
        }
    }    
}
