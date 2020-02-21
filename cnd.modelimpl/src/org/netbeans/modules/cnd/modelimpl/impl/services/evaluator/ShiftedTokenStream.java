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
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 */
public class ShiftedTokenStream implements TokenStream {
    
    private final TokenStream tokenStream;
    
    private final int offset;

    
    public ShiftedTokenStream(TokenStream tokenStream, int offset) {
        this.tokenStream = tokenStream;
        this.offset = offset;
    }

    @Override
    public Token nextToken() throws TokenStreamException {
        Token token = tokenStream.nextToken();               
        if (token instanceof APTToken) {
            token = new ShiftedToken((APTToken) token, offset);
        }
        return token;
    }
    
    
    private static class ShiftedToken implements APTToken {
        
        private final APTToken aptToken;
        
        private final int startOffset;
        
        private final int endOffset;

        
        public ShiftedToken(APTToken aptToken, int shiftOffset) {
            this.aptToken = aptToken;
            this.startOffset = aptToken.getOffset() + shiftOffset;
            this.endOffset = aptToken.getEndOffset() + shiftOffset;
        }
        
        @Override
        public int getOffset() {
            return startOffset;
        }

        @Override
        public void setOffset(int o) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndColumn() {
            return aptToken.getEndColumn();
        }

        @Override
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndLine() {
            return aptToken.getEndLine();
        }

        @Override
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public String getText() {
            return aptToken.getText();
        }

        @Override
        public CharSequence getTextID() {
            return aptToken.getTextID();
        }

        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public Object getProperty(Object key) {
            return aptToken.getProperty(key);
        }        

        @Override
        public int getColumn() {
            return aptToken.getColumn();
        }

        @Override
        public void setColumn(int c) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getLine() {
            return aptToken.getLine();
        }

        @Override
        public void setLine(int l) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public String getFilename() {
            return aptToken.getFilename();
        }

        @Override
        public void setFilename(String name) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public void setText(String t) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getType() {
            return aptToken.getType();
        }

        @Override
        public void setType(int t) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }                
    }
    
}
