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
package org.netbeans.spi.java.hints.matching;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.MatchingTestAccessor;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.introduce.IntroduceMethodFix;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompilerUtilities;
import org.netbeans.modules.java.source.matching.CopyFinder;
import org.netbeans.modules.java.source.matching.CopyFinder.Cancel;
import org.netbeans.modules.java.source.matching.CopyFinder.Options;
import org.netbeans.modules.java.source.matching.CopyFinder.VariableAssignments;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CopyFinderTest extends NbTestCase {

    public CopyFinderTest(String testName) {
        super(testName);
    }

//    public static TestSuite suite() {
//        NbTestSuite nb = new NbTestSuite();
//
//        nb.addTest(new CopyFinderTest("testCorrectSite3"));
//
//        return nb;
//    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testSimple1() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }

//    public void testSimple2() throws Exception {
//        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
//    }

    public void testSimple3() throws Exception {
        performTest("package test; public class Test {public void test() {int i = System.currentTimeMillis(); y = System.currentTimeMillis();}}", 83 - 22, 109 - 22, 115 - 22, 141 - 22);
    }

    public void testSimple4() throws Exception {
        performTest("package test; import java.util.ArrayList; public class Test {public void test() {Object o = new ArrayList<String>();o = new ArrayList<String>();}}", 114 - 22, 137- 22, 142 - 22, 165 - 22);
    }

    public void testSimple5() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; String s = (String) o; s = (String) o; s = (String) null; o = (Object) o;}}", 103 - 22, 113 - 22, 119 - 22, 129 - 22);
    }

    public void testSimple6() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i + i; y = i + i;} public void test2() {int i = 0; y = i + i; y = i + i;}}", 90 - 22, 95 - 22, 101 - 22, 106 - 22);
    }

    public void testSimple7() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; y = i != 0 ? i + i : i * i; y = i != 0 ? i + i : i * i; y = i != 1 ? i + i : i * i; y = i == 0 ? i + i : i * i; y = i != 0 ? i * i : i * i; y = i != 0 ? i + i : i + i; y = i != 0 ? i + i : i * 1;}}", 90 - 22, 112 - 22, 118 - 22, 140 - 22);
    }

    public void testSimple8() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = -i; y = -i; y = +i; y = +y;}}", 94 - 22, 96 - 22, 102 - 22, 104 - 22);
    }

    public void testSimple9() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = i *= 9; y = i *= 9; y = i /= 9; y = i *= 8; y = y *= 9;}}", 94 - 22, 100 - 22, 106 - 22, 112 - 22);
    }

    public void testSimple10() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = null; int y = i[1]; y = i[1]; y = i[y]; y = i[0];}}", 99 - 22, 103 - 22, 109 - 22, 113 - 22);
    }

    public void testSimple11() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[0]; i = new int[0]; i = new int[1];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }

    public void testSimple12() throws Exception {
        performTest("package test; public class Test {public void test() {int[] i = new int[1]; i = new int[1]; i = new int[0];}}", 85 - 22, 95 - 22, 101 - 22, 111 - 22);
    }

    public void testSimple13() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; int y = (i); y = (i); y = i;}}", 94 - 22, 97 - 22, 103 - 22, 106 - 22);
    }

    public void testSimple14() throws Exception {
        performTest("package test; public class Test {public void test() {Object o = null; boolean b = o instanceof String; b = o instanceof String; b = o instanceof Object;}}", 104 - 22, 123 - 22, 129 - 22, 148 - 22);
    }

    public void testSimple15() throws Exception {
        performTest("package test; public class Test {private int x = 1; private int y = 1; public void test() {int x = 1; int y = 1;}}", 90 - 22, 91 - 22, 71 - 22, 72 - 22, 121 - 22, 122 - 22, 132 - 22, 133 - 22);
    }

    public void testSimple16() throws Exception {
        performTest("package test; public class Test {public void test(int i) {int y = \"\".length(); test(\"\".length());} }", 88 - 22, 99 - 22, 106 - 22, 117 - 22);
    }

    public void testSimple17() throws Exception {
        performTest("package test; public class Test {public void test2() {int a = test(test(test(1))); a = test(test(test(1))); a = test(test(test(1)));} public int test(int i) {return 0;} }", 94 - 22, 101 - 22, 119 - 22, 126 - 22, 144 - 22, 151 - 22);
    }

    public void testMemberSelectAndIdentifierAreSame() throws Exception {
        performTest("package test; import static java.lang.String.*; public class Test {public void test1() {|String.valueOf(2)|; |valueOf(2)|;} }");
    }

    public void testVariables1() throws Exception {
        performVariablesTest("package test; import static java.lang.String.*; public class Test {public void test1() {String.valueOf(2+4);} }",
                             "java.lang.String.valueOf($1)",
                             new Pair[] {new Pair<String, int[]>("$1", new int[] {134 - 31, 137 - 31})},
                             new Pair[0]);
    }
    
    /**
     * Checks that static method call on constrained variable does not match className.staticMethod(), but only
     * instance.staticMethod(). Ensures the instance's type is bound to the variable.
     * @throws Exception 
     */
    public void testStaticRefDoesNotMatchVariable241261() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() {Thread t = new Thread(); t.dumpStack(); Thread.dumpStack();} }",
                             "$1{java.lang.Thread}.dumpStack()",
                             new Pair[] { new Pair<String, int[]>("$1", new int[] { 79, 80}) },
                             new Pair[0]);
    }

    public void testAssert1() throws Exception {
        performTest("package test; public class Test {public void test() {int i = 0; |assert i == 1;| |assert i == 1;|}}");
    }

    public void testReturn1() throws Exception {
        performTest("package test; public class Test {public int test1() {|return 1;|} public int test2() {|return 1;|}}");
    }

    public void testIf1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; int j; |if (i == 0) {j = 1;} else {j = 2;}| |if (i == 0) {j = 1;} else {j = 2;}| } }");
    }

    public void testExpressionStatement1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; |i = 1;| |i = 1;| } }");
    }

    public void testBlock1() throws Exception {
        performTest("package test; public class Test {public void test() { int i = 0; |{i = 1;}| |{i = 1;}| } }");
    }

    public void testSynchronized1() throws Exception {
        performTest("package test; public class Test {public void test() { Object o = null; int i = 0; |synchronized (o) {i = 1;}| |synchronized (o) {i = 1;}| } }");
    }

//    public void testEnhancedForLoop() throws Exception {
//        performTest("package test; public class Test {public void test(Iterable<String> i) { |for (String s : i) { System.err.println(); }| |for (String s : i) { System.err.println(); }| }");
//    }

//    public void testConstants() throws Exception {
//        performTest("package test; public class Test {public static final int A = 3; public void test() { int i = |3|; i = |test.Test.A|; } }");
//    }

    public void testOverridingImplementing1() throws Exception {
        performVariablesTest("package test; public class Test implements Runnable { { this.run(); } public void run() { } } }",
                             "$0{java.lang.Runnable}.run()",
                             new Pair[] {new Pair<String, int[]>("$0", new int[] {56, 60})},
                             new Pair[0]);
    }

    public void testMemberSelectCCE() throws Exception {
        //should not throw a CCE
        //(selected regions are not duplicates)
        performTest("package test; public class Test {public static class T extends Test { public void test() { |Test.test|(); |System.err.println|(); } } }", false);
    }

    public void testLocalVariable() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int y; y = 1; } int z; { int y; z = 1; } } }",
                             "{ int $1; $1 = 1; }",
                             null,
                             new Pair[] {new Pair<String, String>("$1", "y")});
    }

    public void testStatementAndSingleBlockStatementAreSame1() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int x; { x = 1; } } } }",
                             "{ int $1; $1 = 1; }",
                             null,
                             new Pair[] {new Pair<String, String>("$1", "x")});
    }

    public void testStatementAndSingleBlockStatementAreSame2() throws Exception {
        performVariablesTest("package test; public class Test {public void test1() { { int x; x = 1; } } }",
                             "{ int $1; { $1 = 1; } }",
                             null,
                             new Pair[] {new Pair<String, String>("$1", "x")});
    }

    public void testStatementVariables() throws Exception {
        performVariablesTest("package test; public class Test {public int test1() { if (true) return 1; else return 2; } }",
                             "if ($1) $2; else $3;",
                             new Pair[] {
                                  new Pair<String, int[]>("$1", new int[] {89 - 31, 93 - 31}),
                                  new Pair<String, int[]>("$2", new int[] {95 - 31, 104 - 31}),
                                  new Pair<String, int[]>("$3", new int[] {110 - 31, 119 - 31})
                             },
                             new Pair[0]);
    }

    public void testThrowStatement() throws Exception {
        performVariablesTest("package test; public class Test {public void test() { throw new NullPointerException(); throw new IllegalStateException();} }",
                             "throw new NullPointerException()",
                             new Pair[0],
                             new Pair[0]);
    }

    public void testMultiStatementVariables1() throws Exception {
        performVariablesTest("package test; public class Test { public int test1() { System.err.println(); System.err.println(); int i = 3; System.err.println(i); System.err.println(i); return i; } }",
                             "{ $s1$; int $i = 3; $s2$; return $i; }",
                             null,
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {55, 76, 77, 98}),
                                  new Pair<String, int[]>("$s2$", new int[] {110, 132, 133, 155})
                             },
                             new Pair[] {new Pair<String, String>("$i", "i")});
    }

    public void testMultiStatementVariables2() throws Exception {
        performVariablesTest("package test; public class Test { public int test1() { int i = 3; return i; } }",
                             "{ $s1$; int $i = 3; $s2$; return $i; }",
                             null,
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[] {new Pair<String, String>("$i", "i")});
    }

    public void testMultiStatementVariablesAndBlocks1() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {$s1$; System.err.println(); $s2$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks2() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {$s1$; System.err.println(); }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s1$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks3() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) {System.err.println(); $s2$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s2$", new int[] {}),
                             },
                             new Pair[0]);
    }

    public void testMultiStatementVariablesAndBlocks4() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                             "if ($c) { $s$; }",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {60, 64})},
                             new Pair[] {
                                  new Pair<String, int[]>("$s$", new int[] {66, 87}),
                             },
                             new Pair[0]);
    }

    public void testVariableVerification() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a, String[] b) { for (int c = 0; c < a.length; c++) { String s = b[c]; System.err.println(s); } } }",
                             "for(int $i = 0; $i < $array.length; $i++) { $T $var = $array[$i]; $stmts$; }",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testFor() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { for (int c = 0; c < a.length; c++) { String s = a[c]; System.err.println(s); } } }",
                             "for(int $i = 0; $i < $array.length; $i++) { $T $var = $array[$i]; $stmts$; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {117 - 31, 118 - 31}),
                                  new Pair<String, int[]>("$array$1", new int[] {145 - 31, 146 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {134 - 31, 140 - 31}),
                                  new Pair<String, int[]>("$var", new int[] {103, 119}),
                                  new Pair<String, int[]>("$i", new int[] {71, 80}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {151 - 31, 173 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$i", "c"),
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testEnhancedFor() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { for (String s : a) { System.err.println(s); } } }",
                             "for($T $var : $array) { $stmts$; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {113 - 31, 114 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {102 - 31, 108 - 31}),
                                  new Pair<String, int[]>("$var", new int[] {71, 79}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {118 - 31, 140 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testWhile() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { int c = 0; while  (c < a.length) { String s = a[c]; System.err.println(s); c++; } } }",
                             "while ($i < $array.length) { $T $var = $array[$i]; $stmts$; $i++; }",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {120 - 31, 121 - 31}),
                                  new Pair<String, int[]>("$array$1", new int[] {143 - 31, 144 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {132 - 31, 138 - 31}),
                                  new Pair<String, int[]>("$i", new int[] {116 - 31, 117 - 31}),
                                  new Pair<String, int[]>("$i$2", new int[] {145 - 31, 146 - 31}),
                                  new Pair<String, int[]>("$i$3", new int[] {172 - 31, 173 - 31}),
                                  new Pair<String, int[]>("$var", new int[] {101, 117}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {149 - 31, 171 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testDoWhile() throws Exception {
        performVariablesTest("package test; public class Test { public void test1(String[] a) { int c = 0; do { String s = a[c]; System.err.println(s); c++; } while  (c < a.length); } }",
                             "do { $T $var = $array[$i]; $stmts$; $i++; } while ($i < $array.length);",
                             new Pair[] {
                                  new Pair<String, int[]>("$array", new int[] {124 - 31, 125 - 31}),
                                  new Pair<String, int[]>("$array$3", new int[] {172 - 31, 173 - 31}),
                                  new Pair<String, int[]>("$T", new int[] {113 - 31, 119 - 31}),
                                  new Pair<String, int[]>("$i", new int[] {126 - 31, 127 - 31}),
                                  new Pair<String, int[]>("$i$1", new int[] {153 - 31, 154 - 31}),
                                  new Pair<String, int[]>("$i$2", new int[] {168 - 31, 169 - 31}),
                                  new Pair<String, int[]>("$var", new int[] {82, 98}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {130 - 31, 152 - 31}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$var", "s"),
                             });
    }

    public void testArrayType() throws Exception {
        performVariablesTest("package test; public class Test { public void test1() { int[][] a; } }",
                             "$T[]",
                             new Pair[] {
                                  new Pair<String, int[]>("$T", new int[] {87 - 31, /*92*//*XXX:*/94 - 31}),
                             },
                             new Pair[0],
                             new Pair[0]);
    }

    public void testSemiMatchPackage() throws Exception {
        performVariablesTest("package test; import javax.lang.model.type.TypeMirror; public class Test { }",
                             "$T{java.lang.Object}.type",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testNullType() throws Exception {
        performVariablesTest("package javax.lang.model.type; public class Test { }",
                             "$T{java.lang.Object}.type",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testTryCatch() throws Exception {
        performVariablesTest("package test; import java.io.*; public class Test { public void test() { InputStream ins = null; try { ins = new FileInputStream(\"\"); } catch (IOException e) { e.printStackTrace(); } finally {ins.close();} } }",
                             "try {$stmts$;} catch (java.io.IOException $e) {$e.printStackTrace();} finally {$finally$;}",
                             new Pair[] {
                                   new Pair<String, int[]>("$e", new int[] {176 - 31 - 2, 189 - 31 - 2}),
                             },
                             new Pair[] {
                                  new Pair<String, int[]>("$stmts$", new int[] {134 - 31, 166 - 31 - 2}),
                                  new Pair<String, int[]>("$finally$", new int[] {225 - 31 - 2, 237 - 31 - 2}),
                             },
                             new Pair[] {
                                  new Pair<String, String>("$e", "e"),
                             });
    }

    public void testMultiParameters1() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(\"a\", \"b\", \"c\"); }",
                             "java.util.Arrays.asList($1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {60, 63, 65, 68, 70, 73}),
                             },
                             new Pair[] {
                             });
    }

    public void testMultiParameters2() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(new String(\"a\"), \"b\", \"c\"); }",
                             "java.util.Arrays.asList(new String(\"a\"), $1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {77, 80, 82, 85}),
                             },
                             new Pair[] {
                             });
    }

    public void testMultiParameters3() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.asList(); }",
                             "java.util.Arrays.asList($1$)",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$1$", new int[] {}),
                             },
                             new Pair[] {
                             });
    }

    public void testTypeParameters() throws Exception {
        performVariablesTest("package test; public class Test { { java.util.Arrays.<String>asList(\"a\", \"b\"); }",
                             "java.util.Arrays.<$1>asList($1$)",
                             new Pair[] {
                                   new Pair<String, int[]>("$1", new int[] {85 - 31, 91 - 31}),
                             },
                             new Pair[] {
                                   new Pair<String, int[]>("$1$", new int[] {68, 71, 73, 76 }),
                             },
                             new Pair[] {
                             });
    }

    public void testModifiers() throws Exception {
        performVariablesTest("package test; public class Test { private String s; }",
                             "$mods$ java.lang.String $name;",
                             new Pair[] {
                                 new Pair<String, int[]>("$name", new int[] {65 - 31, 82 - 31}),
                                 new Pair<String, int[]>("$mods$", new int[] {65 - 31, 72 - 31}), //XXX: shouldn't this be a multi-variable?
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                                  new Pair<String, String>("$name", "s"),
                             });
    }

    public void testVariableIsFullPattern1() throws Exception {
        performVariablesTest("package test; public class Test { private int a; {System.err.println(a);} }",
                             "$0{int}",
                             new Pair[] {
                                 new Pair<String, int[]>("$0", new int[] {100 - 31, 101 - 31}),
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             });
    }

    public void testVariableIsFullPattern2() throws Exception {
        performVariablesTest("package test; public class Test { private int a; {System.err.println(a);} }",
                             "$0{int}",
                             new Pair[] {
                                 new Pair<String, int[]>("$0", new int[] {100 - 31, 101 - 31}),
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             false,
                             true);
    }

    public void testNoCCEForVariableName() throws Exception {
        performVariablesTest("package test; public class Test { { int[] arr = null; int a; arr[a] = 0;} }",
                             "int $a; $a = 0;",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTrees1() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); } else { System.err.println(); System.err.println(); } } }",
                             "if ($c) $s; else $s;",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables1() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(); } else { System.err.println(); System.err.println(); System.err.println(); } } }",
                             "if ($c) { $s$;} else { $s$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables2() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(1); System.err.println(); } else System.err.println(1); } }",
                             "if ($c) { System.err.println(1); $s2$; } else { System.err.println(1); $s2$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables3() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(1); } else System.err.println(1); } }",
                             "if ($c) { $s1$; System.err.println(1); } else { $s1$; System.err.println(1); }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void XtestVerifySameTreesMultiVariables4() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(); System.err.println(1); System.err.println(); } else System.err.println(1); } }",
                             "if ($c) { $s1$; System.err.println(1); $s2$; } else { $s1$; System.err.println(1); $s2$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testVerifySameTreesMultiVariables5() throws Exception {
        performVariablesTest("package test; public class Test { { if (true) { System.err.println(1); } else System.err.println(2); } }",
                             "if ($c) { $s$; } else { $s$; }",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testSimpleRemapping1() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int a = 0;\n" +
                             "        |System.err.println(a);|\n" +
                             "    }\n" +
                             "}\n",
                             "i",
                             Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
    }

    public void testSimpleRemapping2() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);\n" +
                             "         int i2 = 0;\n" +
                             "         System.err.println(i2);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int a = 0;\n" +
                             "        |System.err.println(a);\n" +
                             "         int a2 = 0;\n" +
                             "         System.err.println(a2);|\n" +
                             "    }\n" +
                             "}\n",
                             "i",
                             Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
    }

    public void testSimpleRemapping3() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        |int i = 0;\n" +
                             "         System.err.println(i);\n" +
                             "         int i2 = 0;\n" +
                             "         System.err.println(i2);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        |int a = 0;\n" +
                             "         System.err.println(a);\n" +
                             "         int a2 = 0;\n" +
                             "         System.err.println(a2);|\n" +
                             "    }\n" +
                             "}\n",
                             "",
                             Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
    }

    public void testSimpleRemapping4() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        int i = 0;\n" +
                             "        |System.err.println(i);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        int[] a = {0};\n" +
                             "        |System.err.println(a[0]);|\n" +
                             "    }\n" +
                             "}\n",
                             "i",
                             Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
    }

    public void testPreventRemapOnExpressions1() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        Throwable t = null;\n" +
                             "        |System.err.println(t);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        Throwable t = null;\n" +
                             "        |System.err.println(t.getCause());|\n" +
                             "    }\n" +
                             "}\n",
                             "t",
                             Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
    }

    public void testPreventRemapOnExpressions2() throws Exception {
        performRemappingTest("package test;\n" +
                             "public class Test {\n" +
                             "    void t1() {\n" +
                             "        Throwable t = null;\n" +
                             "        |System.err.println(t);|\n" +
                             "    }\n" +
                             "    void t2() {\n" +
                             "        Throwable t = null;\n" +
                             "        System.err.println(t.getCause());\n" +
                             "    }\n" +
                             "}\n",
                             "t");
    }

    public void testVariableMemberSelect() throws Exception {
        performVariablesTest("package test; public class Test {public void test(String str) { str.length(); str.length(); } public void test1(String str) { str.length(); str.isEmpty(); } }",
                             "{ $str.$method(); $str.$method(); }",
                             null,
                             new Pair[] {new Pair<String, String>("$method", "length")});
    }

    public void testCorrectSite1() throws Exception {
        performVariablesTest("package test; public class Test { public void test(Object o) { o.wait(); } }",
                             "$s{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testCorrectSite2() throws Exception {
        performVariablesTest("package test; public class Test { public void test(Object o) { wait(); } }",
                             "$s{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testCorrectSite3() throws Exception {
        performVariablesTest("package test; public abstract class Test implements java.util.concurrent.locks.Condition { public void test() { new Runnable() { public void run() { wait(); } } } }",
                             "$0{java.util.concurrent.locks.Condition}.wait()",
                             new Pair[0],// {new Pair<String, int[]>("$s", new int[] {-1, -1})},
                             new Pair[0],
                             new Pair[0]);
    }

    public void testCorrectSite4() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { foo.stop(); } }",
                             "$0{java.lang.Thread}.stop()",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testDotClassForSameClass() throws Exception {
        performTest("package test; public class Test { {Class c = |Test.class|; c = |Test.class|; c = String.class; } }");
    }

    public void testTryCatchVariable() throws Exception {
        performVariablesTest("package test; public class Test { { try { throw new java.io.IOException(); } catch (java.io.IOException ex) { } } }",
                             "try { $stmts$; } catch $catches$",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$stmts$", new int[] {42, 74}),
                                new Pair<String, int[]>("$catches$", new int[] {77, 111}),
                             },
                             new Pair[] {
                             },
                             false,
                             true);
    }

    public void testMatchInterfaceNoFQN() throws Exception {
        performTest("package test; import java.util.*; public class Test { public void test() { |List| l1; |java.util.List| l2;} }");
    }

    public void testUnresolvableNonMatchingConstraint() throws Exception {
        performVariablesTest("package test; public class Test { private Object a; {System.err.println(a);} }",
                             "System.err.println($v{does.not.Exist}",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true);
    }

    public void testIndexOutOfBoundsInMultiList() throws Exception {
        performVariablesTest("package test;" +
                             "public class Test {" +
                             "    public void test() {" +
                             "        int i = 0;" +
                             "        int j = 0;" +
                             "        i++;" +
                             "        j++;" +
                             "    }" +
                             "}",
                             "{$type $i = $init; $stms$; $i++;}",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             false);
    }

    public void testCorrectSite192812() throws Exception {
        performVariablesTest("package test; public class Test { private int i; public void test(Test t) { t.i = i - 10; } }",
                             "$t = $t - $v",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             true);
    }

    public void testCorrectSite183367() throws Exception {
        performVariablesTest("package test; public class Test { public void test(java.util.List l) { l.subList(0, 0).remove(0); } }",
                             "$l{java.util.Collection}.remove($o{java.lang.Object})",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             true);
    }

    public void testDisableVariablesWhenVerifyingDuplicates1() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { int $i = 1, $j = 2; int k = $i + $i; } }",
                             "$i + $i",
                             new Pair[] {new Pair<String, int[]>("$i", new int[] {83, 85}),
                                         new Pair<String, int[]>("$i$1", new int[] {88, 90})},
                             new Pair[0],
                             new Pair[0],
                             false,
                             true);
    }

    public void testDisableVariablesWhenVerifyingDuplicates2() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { int $i = 1, $j = 2; int k = $i + $i; } }",
                             "$i + $i",
                             new Pair[] {new Pair<String, int[]>("$i", new int[] {83, 85}),
                                         new Pair<String, int[]>("$i$1", new int[] {88, 90})},
                             new Pair[0],
                             new Pair[0],
                             false,
                             false);
    }

    public void testMethodMatchingMoreParams() throws Exception {
        performVariablesTest("package test; public class Test {public void test(String s1, String s2) { } }",
                             "public void test($params$) { }",
                             new Pair[0],
                             new Pair[] {new Pair<String, int[]>("$params$", new int[] {50, 59, 61, 70})},
                             new Pair[0],
                             false,
                             true);
    }

    public void testLambdaInput1() throws Exception {
        performVariablesTest("package test; public class Test {public void test() { new java.io.FilenameFilter() { public boolean accept(File dir, String name) { } }; } }",
                             "new $type() {public $retType $name($params$) { $body$; } }",
                             null,
                             new Pair[] {
                                 new Pair<String, int[]>("$params$", new int[] { 107, 115, 117, 128 }),
                                 new Pair<String, int[]>("$body$", new int[] { })
                             },
                             new Pair[] {new Pair<String, String>("$name", "accept")},
                             false,
                             false);
    }

    public void testLambdaInput2() throws Exception {
        performVariablesTest("package test; public class Test {public void test() { new java.io.FilenameFilter() { public boolean accept(File dir, String name) { } }; } }",
                             "new $type() { $mods$ $retType $name($params$) { $body$; } }",
                             null,
                             new Pair[] {
                                 new Pair<String, int[]>("$params$", new int[] { 107, 115, 117, 128 }),
                                 new Pair<String, int[]>("$body$", new int[] { })
                             },
                             new Pair[] {new Pair<String, String>("$name", "accept")},
                             false,
                             true);
    }

    public void testSwitch1() throws Exception {
        performVariablesTest("package test;\n" +
                             "public class Test {\n" +
                             "     {\n" +
                             "         E e = null;\n" +
                             "         switch (e) {\n" +
                             "             case A: System.err.println(1); break;\n" +
                             "             case D: System.err.println(2); break;\n" +
                             "             case E: System.err.println(3); break;\n" +
                             "         }\n" +
                             "     }\n" +
                             "     public enum E {A, B, C, D, E, F;}\n" +
                             "}\n",
                             "switch ($0{test.Test.E}) { case $c1$ case D: $stmts$; case $c2$ }",
                             new Pair[] {new Pair<String, int[]>("$0", new int[] {79, 80})},
                             new Pair[] {
                                new Pair<String, int[]>("$stmts$", new int[] { 156, 178, 179, 185 }),
                                new Pair<String, int[]>("$c1$", new int[] { 97, 134 }),
                                new Pair<String, int[]>("$c2$", new int[] { 199, 236 }),
                             },
                             new Pair[0],
                             false,
                             false);
    }

    public void testWildcard1() throws Exception {
        performTest("package test; import java.util.*; public class Test { public void test() { |List<?>| l1; |List<?>| l2;} }");
    }

    public void testWildcard2() throws Exception {
        performTest("package test; import java.util.*; public class Test { public void test() { |List<? extends String>| l1; |List<? extends String>| l2;} }");
    }

    public void testWildcard3() throws Exception {
        performTest("package test; import java.util.*; public class Test { public void test() { |List<? super String>| l1; |List<? super String>| l2;} }");
    }

    public void testSingleVariableStrict() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { if (true) System.err.println(1); } }",
                             "if ($c) $then; else $else;",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             true);
    }

    public void testMultiVariableZeroOrOne1() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { if (true) System.err.println(1); } }",
                             "if ($c) $then; else $else$;",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {59, 63}),
                                         new Pair<String, int[]>("$then", new int[] {65, 87})},
                             new Pair[0],
                             new Pair[0],
                             false,
                             true);
    }

    public void testMultiVariableZeroOrOne2() throws Exception {
        performVariablesTest("package test; public class Test { public void test() { if (true) System.err.println(1); else System.err.println(2); } }",
                             "if ($c) $then; else $else$;",
                             new Pair[] {new Pair<String, int[]>("$c", new int[] {59, 63}),
                                         new Pair<String, int[]>("$then", new int[] {65, 87}),
                                         new Pair<String, int[]>("$else$", new int[] {93, 115})},
                             new Pair[0],
                             new Pair[0],
                             false,
                             true);
    }

    public void testNonResolvableType() throws Exception {
        performVariablesTest("package test; public class Test { { java.io.File f = null; boolean b = f.isDirectory(); } }",
                             "$1{can.not.Resolve}.$m($args$)",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             true);
    }

    public void testTryWithResources() throws Exception {
        performVariablesTest("package test; public class Test { { try (java.io.InputStream in = null) { System.err.println(1); } } }",
                             "try ($resources$) {$body$;}",
                             new Pair[] {
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$resources$", new int[] {41, 70}),
                                new Pair<String, int[]>("$body$", new int[] {74, 96}),
                             },
                             new Pair[] {
                             },
                             false,
                             true);
    }

    public void testIgnoreOtherKind() throws Exception {
        performVariablesTest("package test; public class Test { private java.util.Collection<String> x() { return java.util.Collections.emptySet(); } } }",
                             "$i{java.lang.Class}",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             true,
                             true);
    }

    public void testSearchPackageClause() throws Exception {
        performVariablesTest("package test.a; public class Test { }",
                             "test.$1",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                                 new Pair<String, String>("$1", "a"),
                             },
                             false,
                             true);
    }

    public void testPackageImport() throws Exception {
        performVariablesTest("package test; import java.util.*; public class Test { }",
                             "java.$1",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                                 new Pair<String, String>("$1", "util"),
                             },
                             false,
                             true);
    }
    
    public void testSubclassMatching() throws Exception {
        performVariablesTest("package test; import java.util.*; public abstract class Test { Map.Entry e; }",
                             "java.util.Map.$1",
                             new Pair[] {
                             },
                             new Pair[] {
                             },
                             new Pair[] {
                                 new Pair<String, String>("$1", "Entry"),
                             },
                             false,
                             true);
    }
    
    public void testMethodTypeParameters1() throws Exception {
        performVariablesTest("package test; public class Test { private void t() { } }",
                             "$mods$ <$tp$> $ret $name($args$) { $body$; }",
                             new Pair[] {
                                new Pair<String, int[]>("$ret", new int[] {42, 46}),
                                new Pair<String, int[]>("$mods$", new int[] {34, 41}),
                                new Pair<String, int[]>("$name", new int[] {34, 54}),
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$tp$", new int[] {}),
                                new Pair<String, int[]>("$args$", new int[] {}),
                                new Pair<String, int[]>("$body$", new int[] {}),
                             },
                             new Pair[] {
                                 new Pair<String, String>("$name", "t")
                             },
                             false,
                             false);
    }
    
    public void testMethodTypeParameters2() throws Exception {
        performVariablesTest("package test; public class Test { private <A, B> String aa(int a, int b) { a = b; b = a;} }",
                             "$mods$ <$tp$> $ret $name($args$) { $body$; }",
                             new Pair[] {
                                new Pair<String, int[]>("$ret", new int[] {49, 55}),
                                new Pair<String, int[]>("$mods$", new int[] {34, 41}),
                                new Pair<String, int[]>("$name", new int[] {34, 89}),
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$tp$", new int[] {43, 44, 46, 47}),
                                new Pair<String, int[]>("$args$", new int[] {59, 64, 66, 71}),
                                new Pair<String, int[]>("$body$", new int[] {75, 81, 82, 88}),
                             },
                             new Pair[] {
                                 new Pair<String, String>("$name", "aa")
                             },
                             false,
                             true);
    }
    
    public void testMethodTypeParameters3() throws Exception {
        performVariablesTest("package test; public class Test { private <A> String aa(int a, int b) { a = b; b = a;} }",
                             "$mods$ <$tp> $ret $name($args$) { $body$; }",
                             new Pair[] {
                                new Pair<String, int[]>("$ret", new int[] {46, 52}),
                                new Pair<String, int[]>("$mods$", new int[] {34, 41}),
                                new Pair<String, int[]>("$tp", new int[] {43, 44}),
                                new Pair<String, int[]>("$name", new int[] {34, 86}),
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$args$", new int[] {56, 61, 63, 68}),
                                new Pair<String, int[]>("$body$", new int[] {72, 78, 79, 85}),
                             },
                             new Pair[] {
                                 new Pair<String, String>("$name", "aa")
                             },
                             false,
                             true);
    }
    
    public void testTypeParameters1() throws Exception {
        performVariablesTest("package test; public class Test { private <A extends String> void aa() { } }",
                             "$mods$ <$tp extends $bound&$obounds$> $ret $name($args$) { $body$; }",
                             new Pair[] {
                                new Pair<String, int[]>("$ret", new int[] {61, 65}),
                                new Pair<String, int[]>("$mods$", new int[] {34, 41}),
                                new Pair<String, int[]>("$tp", new int[] {43, 59}),
                                new Pair<String, int[]>("$bound", new int[] {53, 59}),
                                new Pair<String, int[]>("$name", new int[] {34, 74}),
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$obounds$", new int[] {}),
                                new Pair<String, int[]>("$body$", new int[] {}),
                                new Pair<String, int[]>("$args$", new int[] {}),
                             },
                             new Pair[] {
                                 new Pair<String, String>("$name", "aa"),
                                 new Pair<String, String>("$tp", "A")
                             },
                             false,
                             true);
    }
    
    public void testPartialModifiers1() throws Exception {
        performVariablesTest("package test; public class Test { @Deprecated @Override private void aa() { } }",
                             "$mods$ @Deprecated private $ret $name() { $body$; }",
                             new Pair[] {
                                new Pair<String, int[]>("$ret", new int[] {64, 68}),
                                new Pair<String, int[]>("$mods$", new int[] {34, 63}),
                                new Pair<String, int[]>("$name", new int[] {34, 77}),
                             },
                             new Pair[] {
                                new Pair<String, int[]>("$body$", new int[] {}),
                             },
                             new Pair[] {
                                 new Pair<String, String>("$name", "aa"),
                             },
                             false,
                             true);
    }
    
    public void testPartialModifiers2() throws Exception {
        performVariablesTest("package test; public class Test { @Override private void aa() { } }",
                             "$mods$ @Deprecated private $ret $name() { $body$; }",
                             new Pair[0],
                             new Pair[0],
                             new Pair[0],
                             true,
                             true);
    }
    
    public void testNonStaticInnerClassesMatch() throws Exception {
        performVariablesTest("package test; import test.Test.Inner; public class Test { public class Inner { } } class Other { { Inner i = null; } }",
                             "test.Test.Inner $i = $init$;",
                             new Pair[] {
                                 new Pair<String, int[]>("$i", new int[] {99, 114}),
                                 new Pair<String, int[]>("$init$", new int[] {109, 113})
                             },
                             new Pair[0],
                             new Pair[] {
                                 new Pair<String, String>("$i", "i")
                             },
                             false,
                             true);
    }
    
    public void testNewClassTypeParams222066a() throws Exception {
        performVariablesTest("package test; public class Test { private Object aa() { return new java.util.ArrayList(1); } }",
                             "new java.util.ArrayList<$whatever$>($param)",
                             new Pair[] {
                                 new Pair<String, int[]>("$param", new int[] {87, 88})
                             },
                             new Pair[] {
                                 new Pair<String, int[]>("$whatever$", new int[0])
                             },
                             new Pair[0],
                             false,
                             false);
    }
    
    public void testNewClassTypeParams222066b() throws Exception {
        performVariablesTest("package test; import java.util.ArrayList; public class Test { private Object aa() { return new ArrayList(1); } }",
                             "new java.util.ArrayList<$whatever$>($param)",
                             new Pair[] {
                                 new Pair<String, int[]>("$param", new int[] {105, 106})
                             },
                             new Pair[] {
                                 new Pair<String, int[]>("$whatever$", new int[0])
                             },
                             new Pair[0],
                             false,
                             false);
    }
    
    public void testFindLambda() throws Exception {
        performVariablesTest("package test; import java.util.Comparator; public class Test { private void aa() { Comparator<String> c = (l, r) -> l.compareTo(r); } }",
                             "($args$) -> $expression",
                             new Pair[] {
                                 new Pair<String, int[]>("$expression", new int[] {116, 130})
                             },
                             new Pair[] {
                                 new Pair<String, int[]>("$args$", new int[] {107, 108, 110, 111})
                             },
                             new Pair[0],
                             false,
                             false);
    }
    
    public void testAnnotation1() throws Exception {
        performVariablesTest("package test; import test.Test.A; @A(i=1) public class Test { @interface A { public int i(); } }",
                             "@$annotation($args$)",
                             new Pair[] {
                                 new Pair<String, int[]>("$annotation", new int[] {35, 36})
                             },
                             new Pair[] {
                                 new Pair<String, int[]>("$args$", new int[] {37, 40})
                             },
                             new Pair[0],
                             false,
                             true);
    }
    
    public void testAnnotation2() throws Exception {
        performVariablesTest("package test; import test.Test.A; @A(i=1,b=true,l=2) public class Test { @interface A { public int i(); public boolean b(); public long l(); } }",
                             "@test.Test.A($prefix$, b=$value, $suffix$)",
                             new Pair[] {
                                 new Pair<String, int[]>("$value", new int[] {43, 47})
                             },
                             new Pair[] {
                                 new Pair<String, int[]>("$prefix$", new int[] {37, 40}),
                                 new Pair<String, int[]>("$suffix$", new int[] {48, 51})
                             },
                             new Pair[0],
                             false,
                             false);
    }


    public void testPatternMatchingInstanceOf() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            System.err.println("Skipping testPatternMatchingInstanceOf," +
                               "as SourceVersion.RELEASE_17 is not available.");
            return ;
        }
        sourceLevel = "17";
        performVariablesTest("package test;\n" +
                             "public class Test {\n" +
                             "    boolean test(Object o) {\n" +
                             "      return o instanceof String s;\n" +
                             "    }\n" +
                             "}\n",
                             "$expr instanceof $type $name",
                             new Pair[] {
                                 new Pair<String, int[]>("$expr", new int[] {76, 77}),
                                 new Pair<String, int[]>("$type", new int[] {89, 95}),
                                 new Pair<String, int[]>("$name", new int[] {89, 97}),
                             },
                             new Pair[0],
                             new Pair[] {
                                 new Pair<String, String >("$name", "s"),
                             },
                             false,
                             false);
    }

    public void testNotPatternMatchingInstanceOf() throws Exception {
        performVariablesTest("package test;\n" +
                             "public class Test {\n" +
                             "    boolean test(Object o) {\n" +
                             "      return o instanceof String;\n" +
                             "    }\n" +
                             "}\n",
                             "$expr instanceof $type",
                             new Pair[] {
                                 new Pair<String, int[]>("$expr", new int[] {76, 77}),
                                 new Pair<String, int[]>("$type", new int[] {89, 95})
                             },
                             new Pair[0],
                             new Pair[0],
                             false,
                             false);
    }

    public void testInsidePatternMatchingInstanceOf() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17");
        } catch (IllegalArgumentException ex) {
            System.err.println("Skipping testPatternMatchingInstanceOf," +
                               "as SourceVersion.RELEASE_17 is not available.");
            return ;
        }
        sourceLevel = "17";
        performVariablesTest("package test;\n" +
                             "public class Test {\n" +
                             "    boolean test(Object o) {\n" +
                             "      return o.toString() instanceof String s;\n" +
                             "    }\n" +
                             "}\n",
                             "$expr.toString()",
                             new Pair[] {
                                 new Pair<String, int[]>("$expr", new int[] {76, 77}),
                             },
                             new Pair[0],
                             new Pair[0],
                             false,
                             false);
    }

    public void testTargetTyping1() throws Exception {
        sourceLevel = "17";
        performVariablesTest("""
                             package test;
                             public class Test {
                                 boolean test() {
                                     long l = "";
                                 }
                             }
                             """,
                             "long $var = $1{java.lang.String};",
                             new Pair[] {
                                 new Pair<String, int[]>("$1", new int[] {72, 74}),
                                 new Pair<String, int[]>("$var", new int[] {63, 75}),
                             },
                             new Pair[0],
                             new Pair[] {
                                 new Pair<String, String>("$var", "l"),
                             },
                             false,
                             false);
    }

    public void testTargetTyping2() throws Exception {
        sourceLevel = "17";
        performVariablesTest("""
                             package test;
                             public class Test {
                                 boolean test() {
                                     long l;
                                     l = "";
                                 }
                             }
                             """,
                             "$0{long} = $1{java.lang.String};",
                             new Pair[] {
                                 new Pair<String, int[]>("$1", new int[] {83, 85}),
                                 new Pair<String, int[]>("$0", new int[] {79, 80}),
                             },
                             new Pair[0],
                             new Pair[0],
                             false,
                             false);
    }

    public void testKeepImplicitThis() throws Exception {
        prepareTest("package test; public class Test { void t() { toString(); } }", -1);

        Map<String, TypeMirror> constraints = new HashMap<>();
        String patternCode = PatternCompilerUtilities.parseOutTypesFromPattern(info, "$0{test.Test}.toString()", constraints);

        Pattern pattern = PatternCompiler.compile(info, patternCode, constraints, Collections.<String>emptyList());

        Collection<? extends Occurrence> occurrences = Matcher.create(info).setKeepSyntheticTrees().match(pattern);

        assertEquals(1, occurrences.size());
        Occurrence occ = occurrences.iterator().next();
        assertEquals("this", occ.getVariables().get("$0").getLeaf().toString());
    }

    public void testFieldCheckName() throws Exception {
        prepareTest("package test; public class Test { private int foo; private int foo2; }", -1);

        Map<String, TypeMirror> constraints = new HashMap<>();
        String patternCode = PatternCompilerUtilities.parseOutTypesFromPattern(info, "private int foo;", constraints);

        Pattern pattern = PatternCompiler.compile(info, patternCode, constraints, Collections.<String>emptyList());

        Collection<? extends Occurrence> occurrences = Matcher.create(info).match(pattern);

        assertEquals(1, occurrences.size());
        Occurrence occ = occurrences.iterator().next();
        assertEquals("private int foo", occ.getOccurrenceRoot().getLeaf().toString());
    }

    protected void prepareTest(String code) throws Exception {
        prepareTest(code, -1);
    }

    protected void prepareTest(String code, int testIndex) throws Exception {
        File workDirWithIndexFile = testIndex != (-1) ? new File(getWorkDir(), Integer.toString(testIndex)) : getWorkDir();
        FileObject workDirWithIndex = FileUtil.toFileObject(workDirWithIndexFile);

        if (workDirWithIndex != null) {
            workDirWithIndex.delete();
        }

        workDirWithIndex = FileUtil.createFolder(workDirWithIndexFile);

        assertNotNull(workDirWithIndexFile);

        FileObject sourceRoot = workDirWithIndex.createFolder("src");
        FileObject buildRoot  = workDirWithIndex.createFolder("build");
        FileObject cache = workDirWithIndex.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");

        TestUtilities.copyStringToFile(data, code);

        SourceUtilsTestUtil.setSourceLevel(data, sourceLevel);

        data.refresh();

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private static String findRegions(String code, List<int[]> regions) {
        String[] split = code.split("\\|");
        StringBuilder filtered = new StringBuilder();

        filtered.append(split[0]);

        int offset = split[0].length();

        for (int cntr = 1; cntr < split.length; cntr += 2) {
            int[] i = new int[] {
                offset,
                offset + split[cntr].length()
            };

            regions.add(i);

            filtered.append(split[cntr]);
            filtered.append(split[cntr + 1]);

            offset += split[cntr].length();
            offset += split[cntr + 1].length();
        }

        return filtered.toString();
    }

    protected CompilationInfo info;
    private Document doc;
    private String sourceLevel;

    private void performTest(String code) throws Exception {
        performTest(code, true);
    }

    private void performTest(String code, boolean verify) throws Exception {
        List<int[]> result = new LinkedList<int[]>();

        code = findRegions(code, result);

        int testIndex = 0;

        for (int[] i : result) {
            int[] duplicates = new int[2 * (result.size() - 1)];
            int cntr = 0;
            List<int[]> l = new LinkedList<int[]>(result);

            l.remove(i);

            for (int[] span : l) {
                duplicates[cntr++] = span[0];
                duplicates[cntr++] = span[1];
            }

            doPerformTest(code, i[0], i[1], testIndex++, verify, duplicates);
        }
    }

    protected void performTest(String code, int start, int end, int... duplicates) throws Exception {
        doPerformTest(code, start, end, -1, true, duplicates);
    }

    protected void doPerformTest(String code, int start, int end, int testIndex, int... duplicates) throws Exception {
        doPerformTest(code, start, end, testIndex, true, duplicates);
    }

    protected void doPerformTest(String code, int start, int end, int testIndex, boolean verify, int... duplicates) throws Exception {
        prepareTest(code, testIndex);

        TreePath path = info.getTreeUtilities().pathFor((start + end) / 2 + 1);

        while (path != null) {
            Tree t = path.getLeaf();
            SourcePositions sp = info.getTrees().getSourcePositions();

            if (   start == sp.getStartPosition(info.getCompilationUnit(), t)
                && end   == sp.getEndPosition(info.getCompilationUnit(), t)) {
                break;
            }

            path = path.getParentPath();
        }

        assertNotNull(path);

        Collection<TreePath> result = computeDuplicates(path);

        //        assertEquals(f.result.toString(), duplicates.length / 2, f.result.size());

        if (verify) {
            int[] dupes = new int[result.size() * 2];
            int   index = 0;

            for (TreePath tp : result) {
                dupes[index++] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
                dupes[index++] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
            }

            assertTrue("Was: " + Arrays.toString(dupes) + " should have been: " + Arrays.toString(duplicates), Arrays.equals(duplicates, dupes));
        }
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, String>[] duplicatesNames) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, new Pair[0], duplicatesNames);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, multiStatementPos, duplicatesNames, false);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames, boolean noOccurrences) throws Exception {
        performVariablesTest(code, pattern, duplicatesPos, multiStatementPos, duplicatesNames, noOccurrences, false);
    }

    protected void performVariablesTest(String code, String pattern, Pair<String, int[]>[] duplicatesPos, Pair<String, int[]>[] multiStatementPos, Pair<String, String>[] duplicatesNames, boolean noOccurrences, boolean useBulkSearch) throws Exception {
        prepareTest(code, -1);

        Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();
        String patternCode = PatternCompilerUtilities.parseOutTypesFromPattern(info, pattern, constraints);

        Pattern patternObj = PatternCompiler.compile(info, patternCode, constraints, Collections.<String>emptyList());
        TreePath patternPath = MatchingTestAccessor.getPattern(patternObj).iterator().next();
        Map<TreePath, VariableAssignments> result;

        if (useBulkSearch) {
            result = new HashMap<TreePath, VariableAssignments>();

            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, new AtomicBoolean(), patternCode);

            for (Entry<String, Collection<TreePath>> e : BulkSearch.getDefault().match(info, new AtomicBoolean(), new TreePath(info.getCompilationUnit()), bulkPattern).entrySet()) {
                for (TreePath tp : e.getValue()) {
                    VariableAssignments vars = computeVariables(info, patternPath, tp, new AtomicBoolean(), MatchingTestAccessor.getVariable2Type(patternObj));

                    if (vars != null) {
                        result.put(tp, vars);
                    }
                }
            }
        } else {
            result = computeDuplicates(info, patternPath, new TreePath( info.getCompilationUnit()), new AtomicBoolean(), MatchingTestAccessor.getVariable2Type(patternObj));
        }

        if (noOccurrences) {
            assertEquals(0, result.size());
            return ;
        }

        assertSame(1, result.size());

        Map<String, int[]> actual = new HashMap<String, int[]>();

        for (Entry<String, TreePath> e : result.values().iterator().next().variables.entrySet()) {
            int[] span = new int[] {
                (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), e.getValue().getLeaf()),
                (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), e.getValue().getLeaf())
            };

            actual.put(e.getKey(), span);
        }

        if (duplicatesPos != null) {
            for (Pair<String, int[]> dup : duplicatesPos) {
                int[] span = actual.remove(dup.getA());

                if (span == null) {
                    fail(dup.getA());
                }
                assertTrue(dup.getA() + ":" + Arrays.toString(span), Arrays.equals(span, dup.getB()));
            }
            if (!actual.isEmpty()) {
                Map<String, String> print = new HashMap<String, String>(actual.size());
                for (Entry<String, int[]> entry : actual.entrySet()) {
                    String s = entry.getKey();
                    int[] arr = entry.getValue();
                    print.put(s, Arrays.toString(arr));
                }
                assertTrue("Extra duplicates found: " + print, actual.isEmpty());
            }
        }
        
        Map<String, int[]> actualMulti = new HashMap<String, int[]>();

        for (Entry<String, Collection<? extends TreePath>> e : result.values().iterator().next().multiVariables.entrySet()) {
            int[] span = new int[2 * e.getValue().size()];
            int i = 0;

            for (TreePath tp : e.getValue()) {
                span[i++] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
                span[i++] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());
            }

            actualMulti.put(e.getKey(), span);
        }

        if (multiStatementPos != null) {
            for (Pair<String, int[]> dup : multiStatementPos) {
                int[] span = actualMulti.remove(dup.getA());

                if (span == null) {
                    fail(dup.getA());
                }
                assertTrue(dup.getA() + ":" + Arrays.toString(span), Arrays.equals(span, dup.getB()));
            }
            if (!actualMulti.isEmpty()) {
                Map<String, String> print = new HashMap<String, String>(actualMulti.size());
                for (Entry<String, int[]> entry : actualMulti.entrySet()) {
                    String s = entry.getKey();
                    int[] arr = entry.getValue();
                    print.put(s, Arrays.toString(arr));
                }
                assertTrue("Extra multi duplicates found: " + print, actualMulti.isEmpty());
            }
        }

        Map<String, String> golden = new HashMap<String, String>();

        for ( Pair<String, String> e : duplicatesNames) {
            golden.put(e.getA(), e.getB());
        }

        assertEquals(golden, result.values().iterator().next().variables2Names);
    }

    protected VariableAssignments computeVariables(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        Collection<VariableAssignments> values = CopyFinder.internalComputeDuplicates(info, Collections.singletonList(searchingFor), scope, null, null, new AtomicBooleanCancel(cancel), designedTypeHack, Options.ALLOW_VARIABLES_IN_PATTERN).values();

        if (values.iterator().hasNext()) {
            return values.iterator().next();
        } else {
            return null;
        }
    }

    protected Map<TreePath, VariableAssignments> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel, Map<String, TypeMirror> designedTypeHack) {
        return CopyFinder.internalComputeDuplicates(info, Collections.singletonList(searchingFor), scope, null, null, new AtomicBooleanCancel(cancel), designedTypeHack, Options.ALLOW_VARIABLES_IN_PATTERN, Options.ALLOW_GO_DEEPER);
    }

    private void performRemappingTest(String code, String remappableVariables, Options... options) throws Exception {
        List<int[]> regions = new LinkedList<int[]>();

        code = findRegions(code, regions);

        prepareTest(code, -1);

        int[] statements = new int[2];

        int[] currentRegion = regions.get(0);
        TreePathHandle tph = IntroduceMethodFix.validateSelectionForIntroduceMethod(info, currentRegion[0], currentRegion[1], statements);

        assertNotNull(tph);

        TreePath tp = tph.resolve(info);

        assertNotNull(tp);

        BlockTree bt = (BlockTree) tp.getParentPath().getLeaf();
        List<TreePath> searchFor = new LinkedList<TreePath>();

        for (StatementTree t : bt.getStatements().subList(statements[0], statements[1] + 1)) {
            searchFor.add(new TreePath(tp, t));
        }

        final Set<VariableElement> vars = new HashSet<VariableElement>();

        for (final String name : remappableVariables.split(",")) {
            if (name.isEmpty()) continue;
            new ErrorAwareTreePathScanner<Object, Object>() {
                @Override
                public Object visitVariable(VariableTree node, Object p) {
                    if (node.getName().contentEquals(name)) {
                        vars.add((VariableElement) info.getTrees().getElement(getCurrentPath()));
                    }

                    return super.visitVariable(node, p);
                }
            }.scan(info.getCompilationUnit(), null);
        }

        Set<Options> opts = EnumSet.of(Options.ALLOW_GO_DEEPER);

        opts.addAll(Arrays.asList(options));

        Map<TreePath, VariableAssignments> result = CopyFinder.internalComputeDuplicates(info, searchFor, new TreePath(info.getCompilationUnit()), null, vars, new AtomicBooleanCancel(), Collections.<String, TypeMirror>emptyMap(), opts.toArray(new Options[0]));
        Set<List<Integer>> realSpans = new HashSet<List<Integer>>();

        for (Entry<TreePath, VariableAssignments> e : result.entrySet()) {
            List<? extends StatementTree> parentStatements = CopyFinder.getStatements(e.getKey());
            int dupeStart = parentStatements.indexOf(e.getKey().getLeaf());
            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), parentStatements.get(dupeStart));
            int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), parentStatements.get(dupeStart + searchFor.size() - 1));

            realSpans.add(Arrays.asList(startPos, endPos));
        }

        Set<List<Integer>> goldenSpans = new HashSet<List<Integer>>();

        for (int[] region : regions) {
            if (region == currentRegion) continue;

            int[] stmts = new int[2];
            TreePathHandle gtph = IntroduceMethodFix.validateSelectionForIntroduceMethod(info, region[0], region[1], stmts);

            assertNotNull(gtph);

            TreePath gtp = gtph.resolve(info);

            assertNotNull(gtp);

            BlockTree b = (BlockTree) gtp.getParentPath().getLeaf();

            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), b.getStatements().get(stmts[0]));
            int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), b.getStatements().get(stmts[1]));

            goldenSpans.add(Arrays.asList(startPos, endPos));
        }

        assertEquals(goldenSpans, realSpans);
    }

    protected Collection<TreePath> computeDuplicates(TreePath path) {
        return CopyFinder.internalComputeDuplicates(info, Collections.singletonList(path), new TreePath(info.getCompilationUnit()), null, null, new AtomicBooleanCancel(), null, Options.ALLOW_GO_DEEPER).keySet();
    }

    public static final class Pair<A, B> {
        private final A a;
        private final B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

    }

    private static final class AtomicBooleanCancel implements Cancel {

        private final AtomicBoolean cancel;

        public AtomicBooleanCancel() {
            this(new AtomicBoolean());
        }

        public AtomicBooleanCancel(AtomicBoolean cancel) {
            this.cancel = cancel;
        }

        @Override
        public boolean isCancelled() {
            return cancel.get();
        }

    }
}
