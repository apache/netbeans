/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter.FilterToken;

/**
 *
 */
final class APTFortranEOSFilter implements APTLanguageFilter {
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    public APTFortranEOSFilter() {
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

    private static final class FilterStream implements TokenStream {
        private TokenStream orig;
        private Token currentToken;
        boolean newLine = false;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            if(newLine) {
                newLine = false;
                return currentToken;
            }
            Token newToken = orig.nextToken();
            if (currentToken != null) {
                if (currentToken.getType() == APTTokenTypes.EOF) {
                    assert newToken.getType() == APTTokenTypes.EOF;
                    return currentToken;
                } else if (newToken.getType() == APTTokenTypes.EOF || newToken.getLine() != currentToken.getLine()) {
                    Token eos = new EOSToken((APTToken) currentToken);
                    currentToken = newToken;
                    newLine = true;
                    return eos;
                }
            }
            if (newToken.getType() == APTTokenTypes.SEMICOLON) {
                currentToken = newToken;
                return new FilterToken((APTToken)currentToken, APTTokenTypes.T_EOS);
            }
            currentToken = newToken;
            return currentToken;
        }
    }

    static final class EOSToken implements APTToken {

        int offset;
        int endOffset;
        int column;
        int endColumn;
        int line;
        int endLine;
        String fileName;

        EOSToken(APTToken token) {
            offset = token.getEndOffset();
            endOffset = token.getEndOffset();
            column = token.getEndColumn();
            endColumn = token.getEndColumn();
            line = token.getEndLine();
            endLine = token.getEndLine();
            fileName = token.getFilename();
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public void setOffset(int o) {
            offset = o;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndOffset(int o) {
            endOffset = o;
        }

        @Override
        public int getEndColumn() {
            return endColumn;
        }

        @Override
        public void setEndColumn(int c) {
            endColumn = c;
        }

        @Override
        public int getEndLine() {
            return endLine;
        }

        @Override
        public void setEndLine(int l) {
            endLine = l;
        }

        @Override
        public String getText() {
            return "<EOS>"; // NOI18N
        }

        @Override
        public CharSequence getTextID() {
            return "<EOS>"; // NOI18N
        }

        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public void setColumn(int c) {
            column = c;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public void setLine(int l) {
            line = l;
        }

        @Override
        public String getFilename() {
            return fileName;
        }

        @Override
        public void setFilename(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void setText(String t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getType() {
            return APTTokenTypes.T_EOS;
        }

        @Override
        public void setType(int t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Object getProperty(Object key) {
            return null;
        }
    }
}
