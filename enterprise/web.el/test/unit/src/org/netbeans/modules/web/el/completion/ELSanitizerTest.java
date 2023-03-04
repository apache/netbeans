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

package org.netbeans.modules.web.el.completion;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@code ELSanitizer}
 *
 * @author Erno Mononen
 */
public class ELSanitizerTest {

    public ELSanitizerTest() {
    }


    @Test
    public void testSanitizeDots() {
        String sanitized = ELSanitizer.sanitize("#{foo.}");
        assertEquals("#{foo.x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar.}");
        assertEquals("#{foo.bar.x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo[}");
        assertEquals("#{foo['x']}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo(}");
        assertEquals("#{foo()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{just[");
        assertEquals("#{just['x']}", sanitized);
    }

    @Test
    public void testSanitizeSpace() {
        String sanitized = ELSanitizer.sanitize("#{foo. }");
        assertEquals("#{foo.x }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.    }");
        assertEquals("#{foo.x    }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar. }");
        assertEquals("#{foo.bar.x }", sanitized);
    }

    @Test
    public void testSanitizeBrackets() {
        String sanitized = ELSanitizer.sanitize("#{foo[}");
        assertEquals("#{foo['x']}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo");
        assertEquals("#{foo}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo(}");
        assertEquals("#{foo()}", sanitized);
    }

    @Test
    public void testSanitizeOperators() {
        String sanitized = ELSanitizer.sanitize("#{foo +}");
        assertEquals("#{foo +x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo -}");
        assertEquals("#{foo -x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo /}");
        assertEquals("#{foo /x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo +");
        assertEquals("#{foo +x}", sanitized);
    }

    @Test
    public void testSanitizeTrailingSpaces() {
        String sanitized = ELSanitizer.sanitize("#{ }");
        assertEquals("#{ x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{ ");
        assertEquals("#{ x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{a + }");
        assertEquals("#{a + x}", sanitized);
    }

    @Test
    public void testSanitizeKeywords() {
        String sanitized = ELSanitizer.sanitize("#{a and}");
        assertEquals("#{a and x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo.bar gt");
        assertEquals("#{foo.bar gt x}", sanitized);
    }

    @Test
    public void testSanitizeEmpty() {
        String sanitized = ELSanitizer.sanitize("#{}");
        assertEquals("#{x}", sanitized);

        sanitized = ELSanitizer.sanitize("#{");
        assertEquals("#{x}", sanitized);
    }
    
    @Test
    public void testSanitizeUnclosedExpressionInHTML() {
        String sanitized = ELSanitizer.sanitize("#{<body><div>hello", 2);
        assertEquals("#{x}", sanitized);
        
        sanitized = ELSanitizer.sanitize("#{   <body><div>hello", 2);
        assertEquals("#{x}", sanitized);
        
        sanitized = ELSanitizer.sanitize("#{   <body>\n<div>\nhello", 2);
        assertEquals("#{x}", sanitized);

    }

    @Test
    public void testSanitizeFunctions() {
        String sanitized = ELSanitizer.sanitize("#{foo:}");
        assertEquals("#{foo:x()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo: ");
        assertEquals("#{foo:x() }", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo:te");
        assertEquals("#{foo:te()}", sanitized);

        sanitized = ELSanitizer.sanitize("#{foo:te ");
        assertEquals("#{foo:te() }", sanitized);
    }

    @Test
    public void testFindLastNonWhiteSpace() {
        assertEquals(2, ELSanitizer.findLastNonWhiteSpace("foo "));
        assertEquals(2, ELSanitizer.findLastNonWhiteSpace("foo     "));
        assertEquals(6, ELSanitizer.findLastNonWhiteSpace("foo bar"));
        assertEquals(6, ELSanitizer.findLastNonWhiteSpace("foo bar "));
    }

    @Test
    public void testIssue234865() {
        String sanitized = ELSanitizer.sanitize("#{['word', 4].stream().peek(i->)}");
        assertEquals("#{['word', 4].stream().peek(i->x)}", sanitized);
    }
}