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

package org.netbeans.modules.java.hints.declarative.test;

import org.junit.Test;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;

import static org.junit.Assert.*;
import static org.netbeans.modules.java.hints.declarative.test.TestTokenId.*;
import static org.netbeans.modules.java.hints.declarative.TestUtils.*;

/**
 *
 * @author lahvac
 */
public class TestLexerTest {

    public TestLexerTest() {
    }

    @Test
    public void testSimple() {
        String text = "%%TestCase name\njava code\n%%=>\ntarget\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertNextTokenEquals(ts, METADATA, "%%TestCase name\n");
        assertNextTokenEquals(ts, JAVA_CODE, "java code\n");
        assertNextTokenEquals(ts, METADATA, "%%=>\n");
        assertNextTokenEquals(ts, JAVA_CODE, "target\n");

        assertFalse(ts.moveNext());
    }

}
