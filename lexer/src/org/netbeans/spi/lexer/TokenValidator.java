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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Token validator checks whether an existing token was affected
 * by just performed input source modification so that it needs to be relexed.
 * <br/>
 * If the modification was limited to a single non-flyweight token and the token validator
 * exists for a particular token id then the token validation is attempted.
 * <br/>
 * Token validator can refuse validation by returning null from its only method
 * if the modification affects the token or if the validation is unsure.
 *
 * <p>
 * Token validation is part of fine-tuning of the lexing
 * and should be considered for all tokens that may have significant length
 * such as whitespace or comments.
 * <br/>
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
     * <br/>
     * Typically the token can be validated by returning the token with the same
     * token id (just with different length that can be determined
     * by <code>tokenText.length()</code>).
     * <br/>
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
     *  <br/>
     *  Null should be returned if the token must be relexed or if the validator
     *  is unsure whether it's able to resolve the situation properly.
     */
    Token<T> validateToken(Token<T> token,
    TokenFactory<T> factory,
    CharSequence tokenText, int modRelOffset,
    int removedLength, CharSequence removedText,
    int insertedLength, CharSequence insertedText);

}
