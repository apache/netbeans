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

import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;

/**
 * Lexer reads input characters from {@link LexerInput} and groups
 * them into tokens.
 * <br/>
 * The lexer delegates token creation
 * to {@link TokenFactory#createToken(TokenId)}.
 * Token factory instance should be given to the lexer in its constructor.
 *
 * <p>
 * The lexer must be able to express its internal lexing
 * state at token boundaries and it must be able
 * to restart lexing from such state.
 * <br>
 * It is expected that if the input characters following the restart point
 * would not change then the lexer will return the same tokens
 * regardless whether it was restarted at the restart point
 * or run from the input begining as a batch lexer.
 * </p>
 *
 * <p>
 * <b>Testing of the lexers</b>:
 * <br/>
 * Testing of newly written lexers can be performed in several ways.
 * The most simple way is to test batch lexing first
 * (see e.g.
 * <a href="http://www.netbeans.org/source/browse/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/Attic/SimpleLexerBatchTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerBatchTest</a> in lexer module tests).
 * <br/>
 * Then an "incremental" behavior of the new lexer can be tested
 * (see e.g. <a href="http://www.netbeans.org/source/browse/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/Attic/SimpleLexerIncTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerIncTest</a>).
 * <br/>
 * Finally the lexer can be tested by random tests that randomly insert and remove
 * characters from the document
 * (see e.g. <a href="http://www.netbeans.org/source/browse/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/Attic/SimpleLexerRandomTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerRandomTest</a>).
 * <br/>
 * Once these tests pass the lexer can be considered stable.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface Lexer<T extends TokenId> {

    /**
     * Return a token based on characters of the input
     * and possibly additional input properties.
     * <br>
     * Characters can be read by using
     * {@link LexerInput#read()} method. Once the lexer
     * knows that it has read enough characters to recognize
     * a token it calls
     * {@link TokenFactory#createToken(TokenId)}
     * to obtain an instance of a {@link Token} and then returns it.
     *
     * <p>
     * <b>Note:</B>&nbsp;Lexer must *not* return any other <code>Token</code> instances than
     * those obtained from the TokenFactory.
     * </p>
     *
     * <p>
     * The lexer is required to tokenize all the characters (except EOF)
     * provided by the {@link LexerInput} prior to returning null
     * from this method. Not doing so is treated
     * as malfunctioning of the lexer.
     * </p>
     *
     * @return token recognized by the lexer
     *  or null if there are no more characters (available in the input) to be tokenized.
     *  <br/>
     *  Return {@link TokenFactory#SKIP_TOKEN}
     *  if the token should be skipped because of a token filter.
     *
     * @throws IllegalStateException if the token instance created by the lexer
     *  was not created by the methods of TokenFactory (there is a common superclass
     *  for those token implementations).
     * @throws IllegalStateException if this method returns null but not all
     *  the characters of the lexer input were tokenized.
     */
    Token<T> nextToken();
    
    /**
     * This method is called by lexer's infrastructure
     * to return present lexer's state
     * once the lexer has recognized and returned a token.
     * <br/>
     * In mutable environment this method is called after each recognized token
     * and its result is paired (together with token's lookahead) with the token
     * for later use - when lexer needs to be restarted at the token boundary.
     *
     * <p>
     * If the lexer is in no extra state (it is in a default state)
     * it should return <code>null</code>. Most lexers are in the default state
     * only at all the time.
     * <br/>
     * If possible the non-default lexer states should be expressed
     * as small non-negative integers.
     * <br/>
     * There is an optimization that shrinks the storage costs for small
     * <code>java.lang.Integer</code>s to single bytes.
     * </p>
     *
     * <p>
     * The returned value should not be tied to this particular lexer instance in any way.
     * Another lexer instance may be restarted from this state later.
     * </p>
     *
     * @return valid state object or null if the lexer is in a default state.
     */
    Object state();

    /**
     * Infrastructure calls this method when it no longer needs this lexer for lexing
     * so it becomes unused.
     * <br/>
     * If lexer instances are cached and reused later
     * then this method should first release all the references that might cause
     * memory leaks and then add this unused lexer to the cache.
     */
    void release();
    
}
