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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LexerInput;

/**
 * Used when branching a token with preprocessed text.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PreprocessedTextLexerInputOperation<T extends TokenId> extends TextLexerInputOperation<T> {

    private final PreprocessedTextStorage preprocessedText;

    private int prepStartIndex;

    private int prepEndIndex;

    private int tokenStartRawLengthShift;
    
    private int lastRawLengthShift;
    
    private int tokenEndRawLengthShift;
    
    private int tokenStartIndex; // Extra added to compile

    public PreprocessedTextLexerInputOperation(TokenList<T> tokenList, int tokenIndex,
    Object lexerRestartState, PreprocessedTextStorage prepText, int prepTextStartOffset,
    int startOffset, int endOffset) {
        super(tokenList, tokenIndex, lexerRestartState, startOffset, endOffset);
        this.preprocessedText = prepText;
        int index = startOffset - prepTextStartOffset;
        if (index > 0) {
            tokenStartRawLengthShift = preprocessedText.rawLengthShift(index);
            lastRawLengthShift = tokenStartRawLengthShift;
        }
    }

    public int read(int index) { // index >= 0 is guaranteed by contract
        index += tokenStartIndex;
        if (index < readEndIndex()) {
            // Check whether the char is preprocessed
            int rls = preprocessedText.rawLengthShift(index);
            if (rls != lastRawLengthShift) { // Preprocessed
                lastRawLengthShift = rls;
                if (prepStartIndex >= index) { // prepStartIndex already inited
                    prepStartIndex = index;
                }
                prepEndIndex = index + 1;
            }
            return preprocessedText.charAt(index);
        } else { // must read next or return EOF
            return LexerInput.EOF;
        }
    }

    public void assignTokenLength(int tokenLength) {
        tokenEndRawLengthShift = preprocessedText.rawLengthShift(
                tokenStartIndex + tokenLength - 1);
    }
    
    protected void tokenApproved() {
        // Increase base raw length shift by the token's last-char shift
        tokenStartRawLengthShift += tokenEndRawLengthShift;

        if (prepStartIndex != Integer.MAX_VALUE) { // some prep chars (may be after token length)
            if (prepStartIndex < tokenLength()) { // prep chars before token end
                if (prepEndIndex <= tokenLength()) { // no preprocessed chars past token end
                    prepStartIndex = Integer.MAX_VALUE; // signal no preprocessed chars
                } else { // prepEndIndex > tokenLength => initial prep chars in the next token
                    prepStartIndex = 0;
                    prepEndIndex -= tokenLength();
                }

            } else { // prepStartIndex >= tokenLength
                prepStartIndex -= tokenLength();
                prepEndIndex -= tokenLength();
            }
        }
    }
    
    public void collectExtraPreprocessedChars(CharProvider.ExtraPreprocessedChars epc,
    int prepStartIndex, int prepEndIndex, int topPrepEndIndex) {
        if (prepStartIndex < tokenLength()) { // Some preprocessed characters
            // Check for any pre-prepChars
            int preCount = Math.max(prepStartIndex - this.prepStartIndex, 0);
            // Check for post-prepChars
            int postCount;
            if (this.prepEndIndex > tokenLength()) {
                postCount = tokenLength() - prepEndIndex;
                if (postCount > 0) {
                    int i = tokenLength() - 2;
                    // Optimize the case when there are lookahead chars
                    // for the present token and the ending chars could possibly
                    // be non-preprocessed (prepEndIndex > tokenLength)
                    while (--i >= prepStartIndex && postCount > 0
                            && preprocessedText.rawLengthShift(i + tokenStartIndex) == tokenEndRawLengthShift
                    ) { // not preprocessed
                        postCount--;
                    }
                } else // postCount <= 0
                    postCount = 0;
                
            } else { // this.prepEndIndex <= tokenLength
                postCount = this.prepEndIndex - prepEndIndex;
            }
            
            assert (preCount >= 0 && postCount >= 0);
            epc.ensureExtraLength(preCount + postCount);
            while (--preCount >= 0) {
//                epc.insert(readExisting(prepStartIndex - 1), deepRawLength(prepStartIndex) - prepStartIndex);
                prepStartIndex--;
            }
            while (--postCount >= 0) {
//                epc.append(readExisting(prepEndIndex), deepRawLength(prepEndIndex) - topPrepEndIndex);
                prepEndIndex++;
                topPrepEndIndex++;
            }
        }
    }

}
