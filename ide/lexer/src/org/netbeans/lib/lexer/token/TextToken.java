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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.WrapTokenId;

/**
 * Token with an explicit text - either serving a flyweight token
 * or a non-flyweight replacement for a flyweight token.
 * <br/>
 * The represented text should be the same like the original content
 * of the recognized text input portion.
 *
 * <p>
 * The text token can act as a flyweight token by calling
 * {@link AbstractToken.makeFlyweight()}. In such case a single token
 * instance is shared for all the occurrences of the token.
 * <br/>
 * The rawOffset is -1 and tokenList reference is null.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TextToken<T extends TokenId> extends AbstractToken<T> {
    
    private final CharSequence text; // 24 bytes (20-super + 4)

    /**
     * Create text token. The token's text
     * is expected to correspond to the recognized input portion
     * (i.e. the text is not custom).
     * <br/>
     * The token can be made flyweight by using <code>makeFlyweight()</code>.
     *
     * @param wid non-null identification of the token.
     * @param text non-null text of the token.
     */
    public TextToken(WrapTokenId<T> wid, CharSequence text) {
        super(wid);
        assert (text != null);
        this.text = text;
    }
    
    private TextToken(WrapTokenId<T> wid, int rawOffset, CharSequence text) {
        super(wid, rawOffset);
        assert (text != null);
        this.text = text;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public final CharSequence text() {
        return text;
    }

    public final TextToken<T> createCopy(TokenList<T> tokenList, int rawOffset) {
        TextToken<T> token = new TextToken<T>(wid(), rawOffset, text);
        token.setTokenList(tokenList);
        return token;
    }
    
    @Override
    protected String dumpInfoTokenType() {
        return isFlyweight() ? "FlyT" : "TexT"; // NOI18N "TextToken" or "FlyToken"
    }

    @Override
    public String toString() {
        return text.toString();
    }

}
