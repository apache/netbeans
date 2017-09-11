/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
