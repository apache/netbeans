/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 * Splits the input source to tokens.
 * 
 * Internally it uses nb lexer.
 * 
 * XXX possibly expose the lexer's token instead of the wrappers
 *
 * @author mfukala@netbeans.org
 */
public final class Tokenizer {
    
    private List<Token> tokens;
    private int currentToken;
    
    private Tokenizer(List<Token> tokens, CharSequence input) {
        this.tokens = tokens;
    }
    
    public Tokenizer(CharSequence input) {
        this(tokenize(input), input);
        reset();
    }
    
    public List<Token> tokensList() {
        return tokens;
    }
    
    public int tokenIndex() {
        return currentToken;
    }
    
    public int tokensCount() {
        return tokens.size();
    }
    
    public void move(int tokenIndex) {
        currentToken = tokenIndex;
    }
    
    public void reset() {
        currentToken = -1;
    }
    
    public Token token() {
        if(currentToken == -1) {
            currentToken = 0; //position at the beginning
        }
        if(currentToken >= tokens.size()) {
            return null;
        }
        
        return tokens.get(currentToken);
    }
    
    public boolean moveNext() {
        if(currentToken < tokens.size() - 1) {
            currentToken++;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean movePrevious() {
        if(currentToken >= 0) {
            currentToken--;
            return currentToken != -1;
        } else {
            return false;
        }
    }

    private static List<Token> tokenize(CharSequence input) {
        List<Token> stack = new LinkedList<>();
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();
        while(ts.moveNext()) {
            org.netbeans.api.lexer.Token<CssTokenId> t = ts.token();
            switch(t.id()) {
                case WS:
                case NL:
                    continue; //ignore WS
            }
            stack.add(new Token(t.id(), ts.offset(), t.length(), input));
        }
        return stack;
    }
    
}
