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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;

/**
 * Lexer should delegate all the token instances creation to this class.
 * <br/>
 * It's not allowed to create empty tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenFactory<T extends TokenId> {

    /**
     * Token instance that the token creation methods in this class produce
     * if there is an active filtering of certain token ids
     * and the just recognized token-id should be skipped.
     * Normally lexers do not need to check for this except some specific cases
     * in which the {@link #isSkipToken(Token)} is a better typed alternative
     * to this field.
     *
     * @deprecated Use {@link #isSkipToken(Token)} instead.
     */
    @Deprecated
    public static final Token SKIP_TOKEN = LexerUtilsConstants.SKIP_TOKEN;
    
    private final LexerInputOperation<T> operation;
    
    TokenFactory(LexerInputOperation<T> operation) {
        this.operation = operation;
    }

    /**
     * Create token with token length corresponding
     * to the number of characters read from the lexer input.
     *
     * @see #createToken(TokenId, int)
     */
    public Token<T> createToken(T id) {
        return createToken(id, operation.readLength());
    }

    /**
     * Create regular token instance with an explicit length.
     *
     * @param id non-null token id recognized by the lexer.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @return non-null regular token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createToken(T id, int length) {
        return operation.createToken(id, length);
    }

    /**
     * Create regular token instance with an explicit length and part type.
     * <br/>
     * This is suitable e.g. for unfinished block comment when a COMMENT token
     * and PartType.START arguments would be used.
     *
     * @param id non-null token id recognized by the lexer.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @param partType whether this token is complete token or a part of a complete token.
     * @return non-null regular token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createToken(T id, int length, PartType partType) {
        return operation.createToken(id, length, partType);
    }

    /**
     * Get flyweight token for the given arguments.
     * <br/>
     * <b>Note:</b> The returned token will not be flyweight under certain
     * conditions - see return value description.
     *
     * @param id non-null token id.
     * @param text non-null text that the flyweight token should carry.
     * @return non-null flyweight token instance.
     *  <br/>
     *  For performance reasons there is a limit for number of successive
     *  flyweight tokens. If this limit would be exceeded a single non-flyweight
     *  token gets created instead of flyweight one.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> getFlyweightToken(T id, String text) {
        return operation.getFlyweightToken(id, text);
    }
    
    /**
     * Create complete token with properties.
     *
     * @param id non-null token id.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @param propertyProvider token property provider or null if there are no extra properties.
     *  See {@link TokenPropertyProvider} for examples how this parameter may be used.
     * @return non-null property token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createPropertyToken(T id, int length, TokenPropertyProvider<T> propertyProvider) {
        return operation.createPropertyToken(id, length, propertyProvider, PartType.COMPLETE);
    }

    /**
     * Create token with properties.
     *
     * @param id non-null token id.
     * @param length >=0 length of the token to be created. The length must not
     *  exceed the number of characters read from the lexer input.
     * @param propertyProvider token property provider or null if there are no extra properties.
     *  See {@link TokenPropertyProvider} for examples how this parameter may be used.
     * @param partType whether this token is complete or just a part of complete token.
     *  Null may be passed which implies {@link PartType#COMPLETE}.
     * @return non-null property token instance.
     *  <br/>
     *  {@link #SKIP_TOKEN} will be returned
     *  if tokens for the given token id should be skipped
     *  because of token id filter.
     */
    public Token<T> createPropertyToken(T id, int length,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        return operation.createPropertyToken(id, length, propertyProvider, partType);
    }

    /**
     * Create token with a custom text that possibly differs in length and content
     * from the text represented by the token in the input text.
     * <br/>
     * <b>Note: This method should not be used. It is planned to be removed completely.</b>
     * The custom text tokens no longer
     * save space by not refrencing the original characters (when read e.g. from a Reader).
     * <br/>
     * Having token's text to always match the input's text is more systematic
     * and simplifies the lexer module's design.
     * <br/>
     * Therefore the only benefit of custom text tokens would be if certain tools
     * e.g. parsers would require a different text than the one present naturally
     * in the token. In such case the token should have a property
     * (the key can be e.g. a CharSequence.class) that will return a char sequence
     * with the desired text. If the text is a sub sequence of original token's text
     * the token property provider can even be made flyweight:
     * <pre>
     * StripFirstAndLastCharTokenPropertyProvider implements TokenPropertyProvider {
     *     public TokenPropertyProvider INSTANCE = new StripFirstAndLastCharTokenPropertyProvider();
     *     public Object getValue(Token token, Object key) {
     *         if (key == CharSequence.class) {
     *             return token.text().subSequence(1, token.length() - 1);
     *         }
     *         return null;
     *     }
     * }
     * </pre>
     * 
     * <p>
     * </p>
     * 
     * @param id non-null token id of the token being created.
     * @param text non-null custom text assigned to the token.
     * @param length recognized characters corresponding to the token being created.
     * @param partType should always be null otherwise this method would throw
     *  an exception.
     * @deprecated This method is deprecated without replacement - see description
     *  how a similar effect can be obtained.
     */
    @Deprecated
    public Token<T> createCustomTextToken(T id, CharSequence text, int length, PartType partType) {
        if (partType != null) {
            throw new IllegalArgumentException("This method is deprecated and it should" + 
                    " only be used with partType==null (see its javadoc).");
        }
        return operation.createCustomTextToken(id, length, text);
    }

    /**
     * Check whether a token (produced by one of the token creation methods)
     * is a special flyweight token used in cases
     * when there is an active filtering of certain token ids (e.g. comments and whitespace)
     * and the just recognized token-id should be skipped.
     *
     * @param token non-null token.
     * @return true if the token is a skip-token.
     */
    public boolean isSkipToken(Token<T> token) {
        return token == SKIP_TOKEN;
    }

}
