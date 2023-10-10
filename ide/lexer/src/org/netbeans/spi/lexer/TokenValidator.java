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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Token validator checks whether an existing token was affected
 * by just performed input source modification so that it needs to be relexed.
 * <br>
 * If the modification was limited to a single non-flyweight token and the token validator
 * exists for a particular token id then the token validation is attempted.
 * <br>
 * Token validator can refuse validation by returning null from its only method
 * if the modification affects the token or if the validation is unsure.
 *
 * <p>
 * Token validation is part of fine-tuning of the lexing
 * and should be considered for all tokens that may have significant length
 * such as whitespace or comments.
 * <br>
 * The advantage of validation is that compared to lexing
 * it typically only explores the modified characters and few adjacent characters.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenValidator<T extends TokenId> {
    
    /**
     * This method is invoked in mutable environments prior lexer invocation
     * to check whether token in which the text modification occurred
     * was only slightly modified by the performed modification and the lexer's
     * invocation is not necessary.
     * <br>
     * Typically the token can be validated by returning the token with the same
     * token id (just with different length that can be determined
     * by <code>tokenText.length()</code>).
     * <br>
     * But the validator can also return a token with different token id
     * (e.g. the identifier can become a keyword after the modification).
     *
     * @param token non-null token affected by the modification. The token's text
     *  is undefined and must not be retrieved from the token at this time.
     * @param factory non-null for producing of the new token to be returned.
     * @param tokenText non-null text of the token already affected by the modification.
     * @param modRelOffset &gt;0 offset of the text removal/insertion inside the token.
     * @param insertedLength &gt;0 length of the inserted text.
     * @return a new token instance produced by the token factory.
     *  <br>
     *  Null should be returned if the token must be relexed or if the validator
     *  is unsure whether it's able to resolve the situation properly.
     */
    Token<T> validateToken(Token<T> token,
    TokenFactory<T> factory,
    CharSequence tokenText, int modRelOffset,
    int removedLength, CharSequence removedText,
    int insertedLength, CharSequence insertedText);

}
