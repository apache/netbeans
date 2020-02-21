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

package org.netbeans.modules.cnd.script.lexer;

import org.netbeans.modules.cnd.api.script.ShTokenId;
import org.junit.Test;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import static org.netbeans.modules.cnd.api.script.ShTokenId.*;
import static org.netbeans.lib.lexer.test.LexerTestUtilities.assertNextTokenEquals;

/**
 */
public class ShLexerTest extends NbTestCase {

    public ShLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    @Test
    public void testSimple() {
        TokenSequence<ShTokenId> ts = getShellTokenSequence(
                "#!/bin/sh\n\n" +
                "for f in foo.tar foo.bar; do\n" +
                "\techo if for do $f \\\"asd\\\" \"fasdf\" >/dev/null 2>&1\n" +
                "done\n\n" +
                "tar xf foo.tar\n");

        assertNextTokenEquals(ts, COMMENT, "#!/bin/sh\n");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, KEYWORD, "for");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "f");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, KEYWORD, "in");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "tar");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "bar");
        assertNextTokenEquals(ts, OPERATOR, ";");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, KEYWORD, "do");
        assertNextTokenEquals(ts, WHITESPACE, "\n\t");
        assertNextTokenEquals(ts, COMMAND, "echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "if");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "for");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "do");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, "$");
        assertNextTokenEquals(ts, IDENTIFIER, "f");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, "\\\"");
        assertNextTokenEquals(ts, IDENTIFIER, "asd");
        assertNextTokenEquals(ts, OPERATOR, "\\\"");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, STRING, "\"fasdf\"");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, OPERATOR, ">");
        assertNextTokenEquals(ts, OPERATOR, "/");
        assertNextTokenEquals(ts, IDENTIFIER, "dev");
        assertNextTokenEquals(ts, OPERATOR, "/");
        assertNextTokenEquals(ts, IDENTIFIER, "null");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, NUMBER, "2");
        assertNextTokenEquals(ts, OPERATOR, ">");
        assertNextTokenEquals(ts, OPERATOR, "&");
        assertNextTokenEquals(ts, NUMBER, "1");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, KEYWORD, "done");
        assertNextTokenEquals(ts, WHITESPACE, "\n\n");
        assertNextTokenEquals(ts, COMMAND, "tar");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "xf");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, ".");
        assertNextTokenEquals(ts, IDENTIFIER, "tar");
        assertNextTokenEquals(ts, WHITESPACE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    @Test
    public void testEscapedLine() {
        TokenSequence<ShTokenId> ts = getShellTokenSequence("\\\necho foo\\\necho bar");

        assertNextTokenEquals(ts, OPERATOR, "\\\n");
        assertNextTokenEquals(ts, COMMAND, "echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, OPERATOR, "\\\n");
        assertNextTokenEquals(ts, IDENTIFIER, "echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "bar");

        assertFalse("No more tokens", ts.moveNext());
    }

    @Test
    public void testCaseSensitivity() {
        TokenSequence<ShTokenId> ts = getShellTokenSequence("ECHO foo\necho foo\nEcho foo\n");

        assertNextTokenEquals(ts, IDENTIFIER, "ECHO");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, COMMAND, "echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, WHITESPACE, "\n");
        assertNextTokenEquals(ts, IDENTIFIER, "Echo");
        assertNextTokenEquals(ts, WHITESPACE, " ");
        assertNextTokenEquals(ts, IDENTIFIER, "foo");
        assertNextTokenEquals(ts, WHITESPACE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }

    private static TokenSequence<ShTokenId> getShellTokenSequence(String text) {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, ShTokenId.language());
        return hi.tokenSequence(ShTokenId.language());
    }
}
