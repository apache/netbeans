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
package org.netbeans.api.search;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhavlin
 */
public class RegexpUtilTest {

    @Test
    public void testMakeMultiRegexp() {
        assertEquals("", makeMultiRegexp(""));
        assertEquals("a", makeMultiRegexp("a"));
        assertEquals("ab", makeMultiRegexp("ab"));
        assertEquals("abc", makeMultiRegexp("abc"));
        assertEquals("a.", makeMultiRegexp("a?"));
        assertEquals("a.*", makeMultiRegexp("a*"));
        assertEquals(".a", makeMultiRegexp("?a"));
        assertEquals(".*a", makeMultiRegexp("*a"));
        assertEquals("a.b", makeMultiRegexp("a?b"));
        assertEquals(".a.", makeMultiRegexp("?a?"));
        assertEquals("a.*b.c", makeMultiRegexp("a*b?c"));
        assertEquals("a...*b", makeMultiRegexp("a?*?b"));
        assertEquals("a..*b", makeMultiRegexp("a*?*b"));

        assertEquals("a|b", makeMultiRegexp("a b"));
        assertEquals("a\\!b", makeMultiRegexp("a!b"));
        assertEquals("a\\\"b", makeMultiRegexp("a\"b"));
        assertEquals("a\\#b", makeMultiRegexp("a#b"));
        assertEquals("a\\$b", makeMultiRegexp("a$b"));
        assertEquals("a\\%b", makeMultiRegexp("a%b"));
        assertEquals("a\\&b", makeMultiRegexp("a&b"));
        assertEquals("a\\'b", makeMultiRegexp("a'b"));
        assertEquals("a\\(b", makeMultiRegexp("a(b"));
        assertEquals("a\\)b", makeMultiRegexp("a)b"));
        assertEquals("a\\+b", makeMultiRegexp("a+b"));
        assertEquals("a|b", makeMultiRegexp("a,b"));
        assertEquals("a\\-b", makeMultiRegexp("a-b"));
        assertEquals("a\\.b", makeMultiRegexp("a.b"));
        assertEquals("a\\/b", makeMultiRegexp("a/b"));

        assertEquals("a0b", makeMultiRegexp("a0b"));
        assertEquals("a1b", makeMultiRegexp("a1b"));
        assertEquals("a2b", makeMultiRegexp("a2b"));
        assertEquals("a3b", makeMultiRegexp("a3b"));
        assertEquals("a4b", makeMultiRegexp("a4b"));
        assertEquals("a5b", makeMultiRegexp("a5b"));
        assertEquals("a6b", makeMultiRegexp("a6b"));
        assertEquals("a7b", makeMultiRegexp("a7b"));
        assertEquals("a8b", makeMultiRegexp("a8b"));
        assertEquals("a9b", makeMultiRegexp("a9b"));

        assertEquals("a\\:b", makeMultiRegexp("a:b"));
        assertEquals("a\\;b", makeMultiRegexp("a;b"));
        assertEquals("a\\<b", makeMultiRegexp("a<b"));
        assertEquals("a\\=b", makeMultiRegexp("a=b"));
        assertEquals("a\\>b", makeMultiRegexp("a>b"));
        assertEquals("a\\@b", makeMultiRegexp("a@b"));
        assertEquals("a\\[b", makeMultiRegexp("a[b"));
        assertEquals("ab", makeMultiRegexp("a\\b"));
        assertEquals("a\\]b", makeMultiRegexp("a]b"));
        assertEquals("a\\^b", makeMultiRegexp("a^b"));
        assertEquals("a\\_b", makeMultiRegexp("a_b"));
        assertEquals("a\\`b", makeMultiRegexp("a`b"));
        assertEquals("a\\{b", makeMultiRegexp("a{b"));
        assertEquals("a\\|b", makeMultiRegexp("a|b"));
        assertEquals("a\\}b", makeMultiRegexp("a}b"));
        assertEquals("a\\~b", makeMultiRegexp("a~b"));
        assertEquals("a\\\u007fb", makeMultiRegexp("a\u007fb"));

        assertEquals("a\u0080b", makeMultiRegexp("a\u0080b"));
        assertEquals("a\u00c1b", makeMultiRegexp("a\u00c1b"));

        assertEquals("abc\\\\", makeMultiRegexp("abc\\"));

        assertEquals("", makeMultiRegexp(""));
        assertEquals("", makeMultiRegexp(" "));
        assertEquals("", makeMultiRegexp(","));
        assertEquals("", makeMultiRegexp(", "));
        assertEquals("", makeMultiRegexp(" ,"));
        assertEquals("a", makeMultiRegexp("a,"));
        assertEquals("a", makeMultiRegexp("a "));
        assertEquals("a", makeMultiRegexp("a, "));
        assertEquals("a", makeMultiRegexp("a ,"));
        assertEquals("a", makeMultiRegexp(",a"));
        assertEquals("a", makeMultiRegexp(" a"));
        assertEquals("a", makeMultiRegexp(", a"));
        assertEquals("a", makeMultiRegexp(" ,a"));
        assertEquals("a|b", makeMultiRegexp("a b"));
        assertEquals("a|b", makeMultiRegexp("a,b"));
        assertEquals("a|b", makeMultiRegexp("a, b"));
        assertEquals("a|b", makeMultiRegexp("a ,b"));
        assertEquals(" ", makeMultiRegexp("\\ "));
        assertEquals("\\,", makeMultiRegexp("\\,"));
        assertEquals("\\,", makeMultiRegexp("\\, "));
        assertEquals(" ", makeMultiRegexp(",\\ "));
        assertEquals("\\, ", makeMultiRegexp("\\,\\ "));
        assertEquals(" ", makeMultiRegexp("\\ ,"));
        assertEquals("\\,", makeMultiRegexp(" \\,"));
        assertEquals(" \\,", makeMultiRegexp("\\ \\,"));
        assertEquals("a", makeMultiRegexp("\\a,"));
        assertEquals("a\\,", makeMultiRegexp("a\\,"));
        assertEquals("a\\,", makeMultiRegexp("\\a\\,"));
        assertEquals("a", makeMultiRegexp("\\a "));
        assertEquals("a ", makeMultiRegexp("a\\ "));
        assertEquals("a ", makeMultiRegexp("\\a\\ "));
        assertEquals("a|\\\\", makeMultiRegexp("a, \\"));
        assertEquals("a| ", makeMultiRegexp("a,\\ "));
        assertEquals("a| \\\\", makeMultiRegexp("a,\\ \\"));
        assertEquals("a\\,", makeMultiRegexp("a\\, "));
        assertEquals("a\\,|\\\\", makeMultiRegexp("a\\, \\"));
        assertEquals("a\\, ", makeMultiRegexp("a\\,\\ "));
        assertEquals("a\\, \\\\", makeMultiRegexp("a\\,\\ \\"));
        assertEquals("a", makeMultiRegexp("\\a, "));
        assertEquals("a|\\\\", makeMultiRegexp("\\a, \\"));
        assertEquals("a| ", makeMultiRegexp("\\a,\\ "));
        assertEquals("a| \\\\", makeMultiRegexp("\\a,\\ \\"));
        assertEquals("a\\,", makeMultiRegexp("\\a\\, "));
        assertEquals("a\\,|\\\\", makeMultiRegexp("\\a\\, \\"));
        assertEquals("a\\, ", makeMultiRegexp("\\a\\,\\ "));
        assertEquals("a\\, \\\\", makeMultiRegexp("\\a\\,\\ \\"));
        assertEquals("a|\\\\", makeMultiRegexp("a ,\\"));
        assertEquals("a|\\,", makeMultiRegexp("a \\,"));
        assertEquals("a|\\,\\\\", makeMultiRegexp("a \\,\\"));
        assertEquals("a ", makeMultiRegexp("a\\ ,"));
        assertEquals("a |\\\\", makeMultiRegexp("a\\ ,\\"));
        assertEquals("a \\,", makeMultiRegexp("a\\ \\,"));
        assertEquals("a \\,\\\\", makeMultiRegexp("a\\ \\,\\"));
        assertEquals("a", makeMultiRegexp("\\a ,"));
        assertEquals("a|\\\\", makeMultiRegexp("\\a ,\\"));
        assertEquals("a|\\,", makeMultiRegexp("\\a \\,"));
        assertEquals("a|\\,\\\\", makeMultiRegexp("\\a \\,\\"));
        assertEquals("a ", makeMultiRegexp("\\a\\ ,"));
        assertEquals("a |\\\\", makeMultiRegexp("\\a\\ ,\\"));
        assertEquals("a \\,", makeMultiRegexp("\\a\\ \\,"));
        assertEquals("a \\,\\\\", makeMultiRegexp("\\a\\ \\,\\"));

        assertEquals("a|b", makeMultiRegexp("a, b"));
        assertEquals("a|.*b.", makeMultiRegexp("a,*b?"));
        assertEquals("a|\\*b.", makeMultiRegexp("a,\\*b?"));
        assertEquals("a|.*b\\?", makeMultiRegexp("a,*b\\?"));
    }

    private String makeMultiRegexp(String string) {
        return RegexpUtil.makeFileNamePattern(
                SearchScopeOptions.create(string, false)).pattern();
    }
}