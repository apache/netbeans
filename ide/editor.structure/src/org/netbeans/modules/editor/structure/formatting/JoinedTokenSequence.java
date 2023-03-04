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
package org.netbeans.modules.editor.structure.formatting;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JoinedTokenSequence {

    private TokenSequence[] tokenSequences;
    private TextBounds[] tokenSequenceBounds;
    private int currentTokenSequence = -1;

    public JoinedTokenSequence(TokenSequence[] tokenSequences, TextBounds[] tokenSequenceBounds) {
        this.tokenSequences = tokenSequences;
        this.tokenSequenceBounds = tokenSequenceBounds;
    }

    public Token token() {
        return currentTokenSequence().token();
    }

    public TokenSequence currentTokenSequence() {
        return tokenSequences[currentTokenSequence];
    }

    public void moveStart() {
        currentTokenSequence = 0;
        currentTokenSequence().moveStart();
    }

    public boolean moveNext() {
        boolean moreTokens = currentTokenSequence().moveNext();

        if (!moreTokens) {
            if (currentTokenSequence + 1 < tokenSequences.length) {
                currentTokenSequence++;
                currentTokenSequence().moveStart();
                moveNext();
            } else {
                return false;
            }
        }

        return true;
    }

    public boolean movePrevious() {
        boolean moreTokens = currentTokenSequence().movePrevious();

        if (!moreTokens) {
            if (currentTokenSequence > 0) {
                currentTokenSequence--;
                currentTokenSequence().moveEnd();
                movePrevious();
            } else {
                return false;
            }
        }

        return true;
    }

    public int move(int offset) {
        for (int i = 0; i < tokenSequences.length; i++) {
            if (tokenSequenceBounds[i].getAbsoluteStart() <= offset && tokenSequenceBounds[i].getAbsoluteEnd() > offset) {

                currentTokenSequence = i;
                return currentTokenSequence().move(offset);
            }
        }

        return Integer.MIN_VALUE;
    }

    boolean isJustAfterGap(){
        boolean justAfterGap = !currentTokenSequence().movePrevious();

        if (justAfterGap){
            currentTokenSequence().moveStart();
        }
        
        currentTokenSequence().moveNext();
        return justAfterGap;
    }

    public int offset() {
        return currentTokenSequence().offset();
    }

    public TokenSequence embedded() {
        return currentTokenSequence().embedded();
    }
}
