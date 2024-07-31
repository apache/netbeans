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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrParser.*;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 *
 * @author bogdan
 */
public class BladeAntlrUtils {

    public static AntlrTokenSequence getTokens(Document doc) {

        try {
            String text = doc.getText(0, doc.getLength());
            return new AntlrTokenSequence(new BladeAntlrLexer(CharStreams.fromString(text)));
        } catch (BadLocationException ex) {

        }
        return null;
    }

    public static Token getToken(Document doc, int offset) {
        AntlrTokenSequence tokens = getTokens(doc);
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(offset);

        if (!tokens.hasNext()) {
            return null;
        }

        Token token = tokens.next().get();

        //need to move back
        if ( token != null && tokens.hasPrevious() && token.getStartIndex() > offset && token.getStopIndex() > offset){
            token = tokens.previous().get();
        }
        
        return token;
    }

    public static Token findForward(Document doc, Token start,
            List<String> stopTokenText, List<String> openTokensText) {

        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStopIndex() + 1);

        int openTokenBalance = 0;

        while (tokens.hasNext()) {
            Token nt = tokens.next().get();
            if (nt == null) {
                continue;
            }

            String tokenText = nt.getText();

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

    public static Token findForward(AntlrTokenSequence tokens,
            List<Integer> tokensMatch, List<Integer> tokensStop) {

        while (tokens.hasNext()) {
            Token pt = tokens.next().get();
            if (pt == null) {
                continue;
            }

            if (tokensMatch.contains(pt.getType())) {
                return pt;
            }

            if (tokensStop.contains(pt.getType())) {
                return null;
            }
        }

        return null;
    }

    public static Token findBackward(Document doc, Token start,
            List<String> stopTokenText, List<String> openTokensText) {

        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStartIndex() - 1);

        int openTokenBalance = 0;

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            String tokenText = pt.getText();

            if (openTokensText.contains(tokenText)) {
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

    public static Token findBackward(AntlrTokenSequence tokens,
            List<Integer> tokensMatch, List<Integer> tokensStop) {

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            if (tokensMatch.contains(pt.getType())) {
                return pt;
            }

            if (tokensStop.contains(pt.getType())) {
                return null;
            }
        }

        return null;
    }

    public static Token findForward(Document doc, Token start,
            int tokensMatch, List<Integer> skipableTokens) {
        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStopIndex() + 1);

        while (tokens.hasNext()) {
            Token pt = tokens.next().get();
            if (pt == null) {
                continue;
            }

            if (pt.getType() == tokensMatch) {
                return pt;
            }

            if (skipableTokens.contains(pt.getType())) {
                continue;
            }

            return null;
        }

        return null;

    }
    
    public static Token findForwardWithStop(Document doc, Token start,
            int tokensMatch, List<Integer> stopTokens) {
        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStopIndex() + 1);

        while (tokens.hasNext()) {
            Token pt = tokens.next().get();
            if (pt == null) {
                continue;
            }

            if (pt.getType() == tokensMatch) {
                return pt;
            }

            if (stopTokens.contains(pt.getType())) {
                return null;
            }
        }

        return null;

    }

    public static Token findBackward(Document doc, Token start,
            int tokensMatch, List<Integer> skipableTokens) {
        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStartIndex() - 1);

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            if (pt.getType() == tokensMatch) {
                return pt;
            }

            if (skipableTokens.contains(pt.getType())) {
                continue;
            }

            return null;
        }

        return null;

    }
    
    public static Token findBackwardWithStop(Document doc, Token start,
            int tokensMatch, List<Integer> stopTokens) {
        AntlrTokenSequence tokens = getTokens(doc);

        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        tokens.seekTo(start.getStartIndex() - 1);

        while (tokens.hasPrevious()) {
            Token pt = tokens.previous().get();
            if (pt == null) {
                continue;
            }

            if (pt.getType() == tokensMatch) {
                return pt;
            }

            if (stopTokens.contains(pt.getType())) {
                return null;
            }
        }

        return null;

    }

    public static int getTagPairTokenType(int tokenType) {
        switch (tokenType) {
            case CONTENT_TAG_OPEN:
                return CONTENT_TAG_CLOSE;
            case CONTENT_TAG_CLOSE:
                return CONTENT_TAG_OPEN;
            case RAW_TAG_OPEN:
                return RAW_TAG_CLOSE;
            case RAW_TAG_CLOSE:
                return RAW_TAG_OPEN;
            default:
                return -1;
        }
    }
}
