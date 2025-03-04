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
package org.netbeans.modules.jshell.parsing;

import org.junit.Test;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.jshell.model.JShellToken;

public class LexerTest {

    @Test
    public void testVarLexing() {
        TokenHierarchy<?> hi = TokenHierarchy.create("[1]-> var ", JShellToken.language());
        enumerateTokenSequence(hi.tokenSequence(), "", false);
    }

    @Test
    public void testVarLexing2() {
        TokenHierarchy<?> hi = TokenHierarchy.create("[1]-> var String = \"Hallo Welt\"", JShellToken.language());
        enumerateTokenSequence(hi.tokenSequence(), "", false);
    }

    private void enumerateTokenSequence(TokenSequence<?> ts, String indent, boolean debug) {
        ts.moveStart();
        while (ts.moveNext()) {
            Token t = ts.token();
            if (debug) {
                System.out.printf("%s%s/%s%n", indent, t.id().primaryCategory(), t.id().name());
            }
            if (t != null) {
                TokenSequence embeddedTs = ts.embedded();
                if (embeddedTs != null) {
                    enumerateTokenSequence(embeddedTs, indent + "  ", debug);
                }
            }
        }
    }
}
