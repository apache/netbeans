/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
