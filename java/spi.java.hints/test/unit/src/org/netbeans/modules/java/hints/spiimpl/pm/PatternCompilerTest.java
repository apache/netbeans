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

package org.netbeans.modules.java.hints.spiimpl.pm;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PatternCompilerTest extends TestBase {

    public PatternCompilerTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        performVariablesTest("package test; public class Test {public void test() {int i = |1 + 2|;}}", "$1+$2",
                             Pair.<String, String>of("$1", "1"),
                             Pair.<String, String>of("$2", "2"));
    }

    public void testTyped1() throws Exception {
        performVariablesTest("package test; public class Test {public void test() {|String.valueOf(\"t\")|;}}", "String.valueOf($1{String})",
                             Pair.<String, String>of("$1", "\"t\""));
    }

//    public void testTyped2() throws Exception {
//        performVariablesTest("package test; public class Test {public void test() {|String.valueOf(\"t\")|;}}", "$2{java.lang.String}.valueOf($1{String})",
//                             new Pair<String, String>("$1", "\"t\""),
//                             new Pair<String, String>("$2", "String"));
//    }

    public void testTyped3() throws Exception {
        performVariablesTest("package test; public class Test {public void test(String str) {|str.valueOf(\"t\")|;}}", "String.valueOf($1{String})",
                             Pair.<String, String>of("$1", "\"t\""));
    }

    public void testTyped4() throws Exception {
        performVariablesTest("package test; public class Test {public void test() {|Integer.bitCount(1)|;}}", "$2{java.lang.String}.valueOf($1{String})",
                             (Pair[]) null);
    }

    public void testTyped5() throws Exception {
        performVariablesTest("package test; public class Test {public void test() {java.io.File f = null; |f.toURI().toURL()|;}}", "$1{java.io.File}.toURL()",
                             (Pair[]) null);
    }

    public void testTypedPrimitiveType() throws Exception {
        performVariablesTest("package test; public class Test {public void test(int i) {|test(1)|;}}", "$0{test.Test}.test($1{int})",
                             Pair.<String, String>of("$1", "1"));
    }

    public void testMemberSelectVSIdentifier() throws Exception {
        performVariablesTest("package test; public class Test {void test1() {} void test2() {|test1()|;}}", "$1{test.Test}.test1()",
                             new Pair[0]);
    }

    public void testSubClass() throws Exception {
        performVariablesTest("package test; public class Test {void test() {String s = null; |s.toString()|;}}", "$1{java.lang.CharSequence}.toString()",
                             Pair.<String, String>of("$1", "s"));
    }

    public void testEquality1() throws Exception {
        performVariablesTest("package test; public class Test {void test() {|test()|;}}", "$1{test.Test}.test()",
                             new Pair[0]);
    }

    public void testEquality2() throws Exception {
        performVariablesTest("package test; public class Test {void test() {String s = null; |String.valueOf(1).charAt(0)|;}}", "$1{java.lang.String}.charAt(0)",
                             Pair.<String, String>of("$1", "String.valueOf(1)"));
    }

    public void testEquality3() throws Exception {
        performVariablesTest("package test; public class Test {void test() {String s = null; |s.charAt(0)|;}}", "java.lang.String.valueOf(1).charAt(0)",
                             (Pair[]) null);
    }

    public void testType1() throws Exception {
        performVariablesTest("package test; public class Test {void test() {|String| s;}}", "java.lang.String",
                             new Pair[0]);
    }

    public void testStatements1() throws Exception {
        performVariablesTest("package test; public class Test {void test() {|assert true : \"\";|}}", "assert $1{boolean} : $2{java.lang.Object};",
                             new Pair[0]);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, String>... duplicates) throws Exception {
        String[] split = code.split("\\|");

        assertEquals(Arrays.toString(split), 3, split.length);

        int      start = split[0].length();
        int      end   = start + split[1].length();

        code = split[0] + split[1] + split[2];

        prepareTest("test/Test.java", code);

        TreePath tp = info.getTreeUtilities().pathFor((start + end) / 2);

        while (tp != null) {
            Tree t = tp.getLeaf();
            SourcePositions sp = info.getTrees().getSourcePositions();

            if (   start == sp.getStartPosition(info.getCompilationUnit(), t)
                && end   == sp.getEndPosition(info.getCompilationUnit(), t)) {
                break;
            }

            tp = tp.getParentPath();
        }

        assertNotNull(tp);

        //XXX:
        Iterator<? extends Occurrence> vars = Matcher.create(info).setCancel(new AtomicBoolean()).setSearchRoot(tp).setTreeTopSearch().match(PatternCompilerUtilities.compile(info, pattern)).iterator();

        if (duplicates == null) {
            assertFalse(vars.hasNext());
            return ;
        }

        assertNotNull(vars);
        assertTrue(vars.hasNext());

        Map<String, String> actual = new HashMap<String, String>();

        for (Entry<String, TreePath> e : vars.next().getVariables().entrySet()) {
            int[] span = new int[] {
                (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), e.getValue().getLeaf()),
                (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), e.getValue().getLeaf())
            };

            actual.put(e.getKey(), info.getText().substring(span[0], span[1]));
        }

        assertFalse(vars.hasNext());

        for (Pair<String, String> dup : duplicates) {
            String span = actual.remove(dup.first());

            if (span == null) {
                fail(dup.first());
            }
            assertEquals(dup.first()+ ":" + span, span, dup.second());
        }
    }

}
