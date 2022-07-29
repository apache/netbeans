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

import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;

/**
 * Lexer reads input characters from {@link LexerInput} and groups
 * them into tokens.
 * <br>
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
 * <br>
 * Testing of newly written lexers can be performed in several ways.
 * The most simple way is to test batch lexing first
 * (see e.g.
 * <a href="https://github.com/apache/netbeans/tree/master/ide/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/SimpleLexerBatchTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerBatchTest</a> in lexer module tests).
 * <br>
 * Then an "incremental" behavior of the new lexer can be tested
 * (see e.g. <a href="https://github.com/apache/netbeans/tree/master/ide/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/SimpleLexerIncTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerIncTest</a>).
 * <br>
 * Finally the lexer can be tested by random tests that randomly insert and remove
 * characters from the document
 * (see e.g. <a href="https://github.com/apache/netbeans/tree/master/ide/lexer/test/unit/src/org/netbeans/lib/lexer/test/simple/SimpleLexerRandomTest.java">
 * org.netbeans.lib.lexer.test.simple.SimpleLexerRandomTest</a>).
 * <br>
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
     *  <br>
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
     * <br>
     * In mutable environment this method is called after each recognized token
     * and its result is paired (together with token's lookahead) with the token
     * for later use - when lexer needs to be restarted at the token boundary.
     *
     * <p>
     * If the lexer is in no extra state (it is in a default state)
     * it should return <code>null</code>. Most lexers are in the default state
     * only at all the time.
     * <br>
     * If possible the non-default lexer states should be expressed
     * as small non-negative integers.
     * <br>
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
     * <br>
     * If lexer instances are cached and reused later
     * then this method should first release all the references that might cause
     * memory leaks and then add this unused lexer to the cache.
     */
    void release();
    
}
