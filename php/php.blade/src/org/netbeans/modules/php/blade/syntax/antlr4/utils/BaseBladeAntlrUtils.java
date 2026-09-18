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
package org.netbeans.modules.php.blade.syntax.antlr4.utils;

import java.util.Set;
import org.antlr.v4.runtime.Token;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 *
 * @author bogdan
 */
public class BaseBladeAntlrUtils {

    public static Token getToken(AntlrTokenSequence tokens, int offset) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(offset);

        if (!tokens.hasNext()) {
            return null;
        }

        Token token = tokens.next().get();

        //need to move back
        if (token != null && tokens.hasPrevious() && token.getStartIndex() > offset && token.getStopIndex() > offset) {
            token = tokens.previous().get();
        }

        return token;
    }

    public static Token findForward(AntlrTokenSequence tokens, int startOffset,
            Set<String> stopTokensText, Set<String> openTokensText) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        tokens.seekTo(startOffset);

        return findForward(tokens, stopTokensText, openTokensText);
    }

    private static Token findForward(AntlrTokenSequence tokens,
            Set<String> stopTokenText, Set<String> openTokensText) {

        int openTokenBalance = 0;

        while (tokens.hasNext()) {
            Token nt = tokens.next().get();
            if (nt == null) {
                continue;
            }

            String tokenText = nt.getText().trim();

            if (openTokensText.contains(tokenText)) {
                openTokenBalance++;
                continue;
            }
            if (stopTokenText.contains(tokenText)) {
                if (openTokenBalance > 0) {
                    openTokenBalance--;
                } else {
                    return nt;
                }
            }
        }
        return null;
    }

    public static Token findBackward(AntlrTokenSequence tokens, int offset,
            Set<String> stopTokenText, Set<String> balanceTokensText) {

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(offset);

        return findBackward(tokens, stopTokenText, balanceTokensText);
    }
    
    private static Token findBackward(AntlrTokenSequence tokens,
            Set<String> stopTokenText, Set<String> balanceTokensText) {

        int openTokenBalance = 0;

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            String tokenText = pt.getText().trim();

            if (balanceTokensText.contains(tokenText)) {
                openTokenBalance++;
                continue;
            }
            if (stopTokenText.contains(tokenText)) {
                if (openTokenBalance > 0) {
                    openTokenBalance--;
                } else {
                    return pt;
                }
            }
        }

        return null;
    }

}