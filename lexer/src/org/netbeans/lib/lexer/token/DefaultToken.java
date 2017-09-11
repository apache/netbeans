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

package org.netbeans.lib.lexer.token;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.StackElementArray;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.WrapTokenId;

/**
 * Default token which by default obtains text from its background storage.
 * <br/>
 * It is non-flyweight and it does not contain custom text.
 *
 * <p>
 * Once the token gets removed from a token list
 * (because of a text modification) the token
 * returns <code>null</code> from {@link #text()} because the text
 * that it would represent could no longer exist in the document.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DefaultToken<T extends TokenId> extends AbstractToken<T> {

    // -J-Dorg.netbeans.lib.lexer.token.DefaultToken.level=FINE
    private static final Logger LOG = Logger.getLogger(DefaultToken.class.getName());
    
    private static final boolean LOG_TOKEN_TEXT_TO_STRING;

    private static final int TOKEN_TEXT_TO_STRING_STACK_LENGTH;

    private static final Set<StackElementArray> toStringStacks;

    static {
        int val;
        try {
            // "-J-Dorg.netbeans.lexer.token.text.to.string=4" means to check 4 items on stack (excluding first two)
            val = Integer.parseInt(System.getProperty("org.netbeans.lexer.token.text.to.string")); // NOI18N
        } catch (NumberFormatException ex) {
            val = 0;
        }
        LOG_TOKEN_TEXT_TO_STRING = (val > 0);
        TOKEN_TEXT_TO_STRING_STACK_LENGTH = val;
        toStringStacks = LOG_TOKEN_TEXT_TO_STRING ? StackElementArray.createSet() : null;
    }
    
    int tokenLength; // 24 bytes (20-super + 4)
    
    /**
     * Construct new default token.
     */
    public DefaultToken(WrapTokenId<T> wid, int length) {
        super(wid);
        assert (length > 0) : "Token length=" + length + " <= 0"; // NOI18N
        this.tokenLength = length;
    }
    
    /**
     * Construct a special zero-length token.
     */
    public DefaultToken(WrapTokenId<T> wid) {
        super(wid);
        this.tokenLength = 0;
    }

    @Override
    public int length() {
        return tokenLength;
    }

    @Override
    protected String dumpInfoTokenType() {
        return "DefT"; // NOI18N "TextToken" or "FlyToken"
    }
    
    private static boolean textFailureLogged;
    /**
     * Get text represented by this token.
     */
    @Override
    public CharSequence text() {
        CharSequence text;
        TokenList<T> tList = tokenList;
        if (!isRemoved()) {
            TokenList<?> rootTokenList = tList.rootTokenList();
            synchronized (rootTokenList) {
                // Create subsequence of input source text
                CharSequence inputSourceText = tList.inputSourceText();
                int tokenOffset = tList.tokenOffset(this);
                int start = tokenOffset;
                int end = tokenOffset + length();
                if (LOG_TOKEN_TEXT_TO_STRING) {
                    CharSequenceUtilities.checkIndexesValid(inputSourceText, start, end);
                    text = new InputSourceSubsequence(this, inputSourceText, start, end);
                } else {
                    // Temporary fix for issue #225394
                    try {
                        return inputSourceText.subSequence(start, end);
                    } catch (IndexOutOfBoundsException ex) {
                        // Log that the IOOBE occurred.
                        if (!textFailureLogged) {
                            textFailureLogged = true;
                            LOG.log(Level.INFO, "Obtaining of token text failed.", ex); // NOI18N
                            LOG.info("Error-token@" + Integer.toHexString(System.identityHashCode(this)) + ", rawOffset=" + rawOffset() + // NOI18N
                                    ", tokenLength=" + tokenLength + ", start=" + start + ", end=" + end); // NOI18N
                            LOG.info("Errorneous token hierarchy:\n" + rootTokenList.tokenHierarchyOperation().toString()); // NOI18N
                        }
                        return "";
                    }
                }
            }

        } else { // Token is removed or flyweight
            text = null;
        }
        return text;
    }
    
    private static final class InputSourceSubsequence implements CharSequence {
        
        private final DefaultToken<?> token; // (8-super + 4) = 12 bytes
        
        private final CharSequence inputSourceText; // 16 bytes
        
        private final int start; // 20 bytes
        
        private final int end; // 24 bytes
        
        public InputSourceSubsequence(DefaultToken token, CharSequence text, int start, int end) {
            this.token = token;
            this.inputSourceText = text;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public int length() {
            return end - start;
        }
        
        @Override
        public char charAt(int index) {
            CharSequenceUtilities.checkIndexValid(index, length());
            try {
                return inputSourceText.charAt(start + index);
            } catch (IndexOutOfBoundsException ex) {
                StringBuilder sb = new StringBuilder(200);
                sb.append("Internal lexer error: index=").append(index). // NOI18N
                        append(", length()=").append(length()). // NOI18N
                        append("\n  start=").append(start).append(", end=").append(end). // NOI18N
                        append("\n  tokenOffset=").append(token.offset(null)). // NOI18N
                        append(", tokenLength=").append(token.length()). // NOI18N
                        append(", inputSourceLength=").append(inputSourceText.length()). // NOI18N
                        append('\n');
                org.netbeans.lib.lexer.TokenList tokenList = token.tokenList();
                org.netbeans.lib.lexer.TokenHierarchyOperation op;
                Object inputSource;
                if (tokenList != null &&
                        (op = tokenList.tokenHierarchyOperation()) != null &&
                        (inputSource = op.inputSource()) != null)
                {
                    sb.append("  inputSource: ").append(inputSource.getClass());
                    if (inputSource instanceof Document) {
                        Document doc = (Document) inputSource;
                        sb.append("  document-locked: "). // NOI18N
                                append(org.netbeans.lib.editor.util.swing.DocumentUtilities.
                                            isReadLocked(doc));
                    }
                    sb.append('\n');
                }
                throw new IndexOutOfBoundsException(sb.toString());
            }
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            CharSequenceUtilities.checkIndexesValid(this, start, end);
            return new InputSourceSubsequence(token, inputSourceText,
                    this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            if (LOG_TOKEN_TEXT_TO_STRING) {
                if (StackElementArray.addStackIfNew(toStringStacks, TOKEN_TEXT_TO_STRING_STACK_LENGTH)) {
                    LOG.log(Level.INFO, "Token.text().toString() called", new Exception());
                }
            }
            return inputSourceText.subSequence(start, end).toString();
        }

    }
    
}
