/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
