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
package org.netbeans.modules.docker;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class IgnorePatternTest extends NbTestCase {

    private static final String[] COMPILABLE_PATTERNS = new String[]{
        "abc",
        "*",
        "*c",
        "a*",
        "a*",
        "a*",
        "a*/b",
        "a*/b",
        "a*b*c*d*e*/f",
        "a*b*c*d*e*/f",
        "a*b*c*d*e*/f",
        "a*b*c*d*e*/f",
        "a*b?c*x",
        "a*b?c*x",
        "ab[c]",
        "ab[b-d]",
        "ab[e-g]",
        "ab[^c]",
        "ab[^b-d]",
        "ab[^e-g]",
        "a\\*b",
        "a\\*b",
        "a?b",
        "a[^a]b",
        "a???b",
        "a[^a][^a][^a]b",
        "[a-ζ]*",
        "*[a-ζ]",
        "a?b",
        "a*b",
        "[\\]a]",
        "[\\-]",
        "[x\\-]",
        "[x\\-]",
        "[x\\-]",
        "[\\-x]",
        "[\\-x]",
        "[\\-x]"
    };

    private static final String[] UNCOMPILABLE_PATTERNS = new String[]{
        "[]a]",
        "[-]",
        "[x-]",
        "[x-]",
        "[x-]",
        "[-x]",
        "[-x]",
        "[-x]",
        "\\",
        "[a-b-c]",
        "[",
        "[^",
        "[^bc",
        "a["
    };

    private static final String[][] MATCH_INPUTS = new String[][]{
        {"abc", "abc"},
        {"*", "abc"},
        {"*c", "abc"},
        {"a*", "a"},
        {"a*", "abc"},
        {"a*/b", "abc/b"},
        {"a*b*c*d*e*/f", "axbxcxdxe/f"},
        {"a*b*c*d*e*/f", "axbxcxdxexxx/f"},
        {"a*b?c*x", "abxbbxdbxebxczzx"},
        {"ab[c]", "abc"},
        {"ab[b-d]", "abc"},
        {"ab[^e-g]", "abc"},
        {"a\\*b", "a*b"},
        {"a?b", "a☺b"},
        {"a[^a]b", "a☺b"},
        {"[a-ζ]*", "α"},
        {"[\\]a]", "]"},
        {"[\\-]", "-"},
        {"[x\\-]", "x"},
        {"[x\\-]", "-"},
        {"[\\-x]", "x"},
        {"[\\-x]", "-"},
        {"*x", "xxx"}
    };

    private static final String[][] NO_MATCH_INPUTS = new String[][]{
        {"a*", "ab/c"},
        {"a*/b", "a/c/b"},
        {"a*b*c*d*e*/f", "axbxcxdxe/xxx/f"},
        {"a*b*c*d*e*/f", "axbxcxdxexxx/fff"},
        {"a*b?c*x", "abxbbxdbxebxczzy"},
        {"ab[e-g]", "abc"},
        {"ab[^c]", "abc"},
        {"ab[^b-d]", "abc"},
        {"a\\*b", "ab"},
        {"a???b", "a☺b"},
        {"a[^a][^a][^a]b", "a☺b"},
        {"*[a-ζ]", "A"},
        {"a?b", "a/b"},
        {"a*b", "a/b"},
        {"[x\\-]", "z"},
        {"[\\-x]", "a"},
        {"a[", "a"}};

    private static final String[][] PREPROCESS = new String[][]{
        // Already clean
        {"abc", "abc"},
        {"abc/def", "abc/def"},
        {"a/b/c", "a/b/c"},
        {".", "."},
        {"..", ".."},
        {"../..", "../.."},
        {"../../abc", "../../abc"},
        {"/abc", "/abc"},
        {"/", "/"},
        // Empty is current dir
        {"", "."},
        // Remove trailing slash
        {"abc/", "abc"},
        {"abc/def/", "abc/def"},
        {"a/b/c/", "a/b/c"},
        {"./", "."},
        {"../", ".."},
        {"../../", "../.."},
        {"/abc/", "/abc"},
        // Remove doubled slash
        {"abc//def//ghi", "abc/def/ghi"},
        {"//abc", "/abc"},
        {"///abc", "/abc"},
        {"//abc//", "/abc"},
        {"abc//", "abc"},
        // Remove . elements
        {"abc/./def", "abc/def"},
        {"/./abc/def", "/abc/def"},
        {"abc/.", "abc"},
        // Remove .. elements
        {"abc/def/ghi/../jkl", "abc/def/jkl"},
        {"abc/def/../ghi/../jkl", "abc/jkl"},
        {"abc/def/..", "abc"},
        {"abc/def/../..", "."},
        {"/abc/def/../..", "/"},
        {"abc/def/../../..", ".."},
        {"/abc/def/../../..", "/"},
        {"abc/def/../../../ghi/jkl/../../../mno", "../../mno"},
        {"/../abc", "/abc"},
        // Combinations
        {"abc/./../def", "def"},
        {"abc//./../def", "def"},
        {"abc/../../././../def", "../../def"}
    };

    private static final String[][] PREPROCESS_WIN = {
        {"c:", "c:."},
        {"c:\\", "c:\\"},
        {"c:\\abc", "c:\\abc"},
        {"c:abc\\..\\..\\.\\.\\..\\def", "c:..\\..\\def"},
        {"c:\\abc\\def\\..\\..", "c:\\"},
        {"c:\\..\\abc", "c:\\abc"},
        {"c:..\\abc", "c:..\\abc"},
        {"\\", "\\"},
        {"/", "\\"},
        {"..\\abc", "..\\abc"},
        {"\\..\\abc", "\\abc"}
//        {"\\\\i\\..\\c$", "\\c$"},
//        {"\\\\i\\..\\i\\c$", "\\i\\c$"},
//        {"\\\\i\\..\\I\\c$", "\\I\\c$"},
//        {"\\\\host\\share\\foo\\..\\bar", "\\\\host\\share\\bar"},
//        {"//host/share/foo/../baz", "\\\\host\\share\\baz"},
//        {"\\\\a\\b\\..\\c", "\\\\a\\b\\c"},
//        {"\\\\a\\b", "\\\\a\\b"}
    };

    public IgnorePatternTest(String name) {
        super(name);
    }

    public void testCompile() {
        for (String s : COMPILABLE_PATTERNS) {
            IgnorePattern pattern = IgnorePattern.compilePattern(s, '/', false);
            assertFalse(s, pattern.isError());
        }

        for (String s : UNCOMPILABLE_PATTERNS) {
            IgnorePattern pattern = IgnorePattern.compilePattern(s, '/', false);
            assertTrue(s, pattern.isError());
        }
    }

    public void testMatch() {
        for (String[] item : MATCH_INPUTS) {
            try {
                IgnorePattern pattern = IgnorePattern.compilePattern(item[0], '/', false);
                assertTrue(item[0] + ":" + item[1], pattern.matches(item[1]));
            } catch (IllegalStateException ex) {
                fail(item[0] + ":" + item[1]);
            }
        }

        for (String[] item : NO_MATCH_INPUTS) {
            try {
                IgnorePattern pattern = IgnorePattern.compilePattern(item[0], '/', false);
                assertFalse(item[0] + ":" + item[1], pattern.matches(item[1]));
            } catch (IllegalStateException ex) {
                fail(item[0] + ":" + item[1]);
            }
        }
    }

    public void testPreprocess() {
        for (String[] item : PREPROCESS) {
            assertEquals(item[1], IgnorePattern.preprocess(item[0], '/'));
        }

        for (String[] item : PREPROCESS_WIN) {
            assertEquals(item[1], IgnorePattern.preprocess(item[0], '\\'));
        }
    }
}
