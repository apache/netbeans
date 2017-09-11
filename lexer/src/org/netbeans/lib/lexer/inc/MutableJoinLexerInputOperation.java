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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.JoinTokenList;

/**
 * Lexer input operation over multiple joined sections (embedded token lists).
 * <br/>
 * It produces regular tokens (to be added directly into ETL represented by
 * {@link #activeTokenList()} and also special {@link #JoinToken} instances
 * in case a token spans boundaries of multiple ETLs.
 * <br/>
 * It can either work over JoinTokenList directly or, during a modification,
 * it simulates that certain token lists are already removed/added to underlying token list.
 * <br/>
 * 
 * {@link #recognizedTokenLastInTokenList()} gives information whether the lastly
 * produced token ends right at boundary of the activeTokenList.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class MutableJoinLexerInputOperation<T extends TokenId> extends JoinLexerInputOperation<T> {
    
    private final TokenListListUpdate<T> tokenListListUpdate;

    MutableJoinLexerInputOperation(JoinTokenList<T> joinTokenList, int relexJoinIndex, Object lexerRestartState,
            int activeTokenListIndex, int relexOffset, TokenListListUpdate<T> tokenListListUpdate
    ) {
        super(joinTokenList, relexJoinIndex, lexerRestartState, activeTokenListIndex, relexOffset);
        this.tokenListListUpdate = tokenListListUpdate;
    }

    @Override
    public EmbeddedTokenList<?,T> tokenList(int tokenListIndex) {
        return tokenListListUpdate.afterUpdateTokenList((JoinTokenList<T>) tokenList, tokenListIndex);
    }

    @Override
    protected int tokenListCount() {
        return tokenListListUpdate.afterUpdateTokenListCount((JoinTokenList<T>) tokenList);
    }

    @Override
    public String toString() {
        return super.toString() + ", tokenListListUpdate: " + tokenListListUpdate; // NOI18N
    }

}
