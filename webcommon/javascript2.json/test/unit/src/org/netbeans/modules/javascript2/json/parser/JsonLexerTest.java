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
package org.netbeans.modules.javascript2.json.parser;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class JsonLexerTest extends NbTestCase {
    public JsonLexerTest(@NonNull final String name) {
        super(name);
    }

    public void testLineCommentLex() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 //comment\n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ","}");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testLineCommentLexEOF() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1} //comment");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1","}"," ");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testCommentLex() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 /*comment*/\n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ","\n","}");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testCommentLexEOF() throws Exception {
        final ANTLRInputStream in = new ANTLRInputStream("{\"a\":1 /*comment \n}");  //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","1"," ");   //NOI18N
        final JsonLexer lexer = new JsonLexer(in, false);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    public void testMultiLineString() throws Exception {
        final String testCase = "{\"a\":\"1\n2\"}"; //NOI18N
        final ANTLRInputStream in = new ANTLRInputStream(testCase);
        final JsonLexer lexer = new JsonLexer(in, false, false);
        final MockErrorListener ml = new MockErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(ml);
        lexer.getAllTokens();
        ml.assertErrorCount(2);
    }

    public void testMultiLineString2() throws Exception {
        final String testCase = "{\"a\":\"1\\g2\"}"; //NOI18N
        final List<String> expected = Arrays.asList("{","\"a\"",":","}");   //NOI18N
        final ANTLRInputStream in = new ANTLRInputStream(testCase);
        final JsonLexer lexer = new JsonLexer(in, false, false);
        final MockErrorListener ml = new MockErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(ml);
        final List<String> result = lexer.getAllTokens().stream().map((t)->t.getText()).collect(Collectors.toList());
        ml.assertErrorCount(1);
        assertEquals(expected, result);
    }

    private static final class MockErrorListener extends BaseErrorListener {
        private final Queue<RecognitionException> errors;

        MockErrorListener() {
            errors = new ArrayDeque<>();
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> rcgnzr,
                Object o,
                int i,
                int i1,
                String string,
                RecognitionException re) {
            errors.offer(re);
        }

        void assertErrorCount(int expectedCount) {
            final int errCount = errors.size();
            errors.clear();
            assertEquals(expectedCount, errCount);
        }
    }
}
