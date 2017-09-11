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

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Lexer restart info contains all the necessary information for restarting
 * of a lexer mainly the lexer input, state and token factory.
 * 
 * <p>
 * When lexing embedded sections if {@link LanguageEmbedding#joinSections()}
 * returns true then the {@link #state()} will return state after
 * the last token of a corresponding previous section (with the same language path).
 * </p>
 *
 * @author Miloslav Metelka
 */

public final class LexerRestartInfo<T extends TokenId> {

    private final LexerInput input;
    
    private final TokenFactory<T> tokenFactory;
    
    private final Object state;
    
    private final LanguagePath languagePath;
    
    private final InputAttributes inputAttributes;
    
    LexerRestartInfo(LexerInput input,
    TokenFactory<T> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        this.input = input;
        this.tokenFactory = tokenFactory;
        this.state = state;
        this.languagePath = languagePath;
        this.inputAttributes = inputAttributes;
    }
    
    /**
     * Get lexer input from which the lexer should read characters.
     */
    public LexerInput input() {
        return input;
    }

    /**
     * Get token factory through which the lexer should produce tokens.
     */
    public TokenFactory<T> tokenFactory() {
        return tokenFactory;
    }
    
    /**
     * Get state from which the lexer should start lexing.
     */
    public Object state() {
        return state;
    }
    
    /**
     * Get language path at which the lexer operates.
     */
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Get supplementary information about particular input source
     * or null if there are no extra attributes.
     */
    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    /**
     * Get value of an attribute or null if the attribute is not set
     * or if there are no attributes at all.
     */
    public Object getAttributeValue(Object key) {
        return (inputAttributes != null)
                ? inputAttributes.getValue(languagePath, key)
                : null;
    }
    
}
