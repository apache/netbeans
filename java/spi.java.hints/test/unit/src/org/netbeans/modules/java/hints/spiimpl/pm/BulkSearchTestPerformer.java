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

import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.spiimpl.pm.BulkSearch.EncodingContext;

import com.sun.source.util.TreePath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author lahvac
 */
public abstract class BulkSearchTestPerformer extends NbTestCase {

    public BulkSearchTestPerformer(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        SourceUtilsTestUtil2.disableConfinementTest();
    }

//    public static TestSuite suite() {
//        NbTestSuite s = new NbTestSuite();
//
//        s.addTestSuite(NFABasedBulkSearchTest.class);
//
//        return s;
//    }

    public void testSimple1() throws Exception {
        performTest("package test; public class Test { private void test() { System.err./**/println(\"\");}}",
                    Collections.singletonMap("System.err.println(\"\")", Arrays.asList("System.err./**/println(\"\")")),
                    Arrays.asList("System.err.println(\"\" + \"\")"));
    }

    public void testDontCare() throws Exception {
        performTest("package test; public class Test { private void test() { System.err./**/println(\"\" + \"\");}}",
                    Collections.singletonMap("System.err.println($1)", Arrays.asList("System.err./**/println(\"\" + \"\")")),
                    Collections.<String>emptyList());
    }

    public void testMemberSelectAndIdentifier() throws Exception {
        performTest("package test; public class Test { private static void test() { test();}}",
                    Collections.singletonMap("test.Test.test()", Arrays.asList("test()")),
                    Collections.<String>emptyList());
    }

    public void testUnpureMemberSelect() throws Exception {
        performTest("package test; public class Test { private static void test() { new StringBuilder().append(\"\");}}",
                    Collections.<String, List<String>>emptyMap(),
                    Arrays.asList("test.append(\"\")"));
    }

    public void testMemberSelectWithVariables1() throws Exception {
        performTest("package test; public class Test { private static void test() { new StringBuilder().append(\"\");}}",
                    Collections.singletonMap("$0.append(\"\")", Arrays.asList("new StringBuilder().append(\"\")")),
                    Collections.<String>emptyList());
    }

    public void testMemberSelectWithVariables2() throws Exception {
        performTest("package test; public class Test { private void append(char c) { append(\"\");}}",
                    Collections.singletonMap("$0.append(\"\")", Arrays.asList("append(\"\")")),
                    Collections.<String>emptyList());
    }

    public void testLocalVariables() throws Exception {
        performTest("package test; public class Test { private void test() { { int y; y = 1; } }}",
                    Collections.singletonMap("{ int $1; $1 = 1; }", Arrays.asList("{ int y; y = 1; }")),
                    Collections.<String>emptyList());
    }

    public void testAssert() throws Exception {
        performTest("package test; public class Test { private void test() { assert true : \"\"; }}",
                    Collections.singletonMap("assert $1 : $2;", Arrays.asList("assert true : \"\";")),
                    Collections.<String>emptyList());
    }

    public void testStatementAndSingleBlockStatementAreSame1() throws Exception {
        performTest("package test; public class Test { private void test() { { int y; { y = 1; } } }}",
                    Collections.singletonMap("{ int $1; $1 = 1; }", Arrays.asList("{ int y; { y = 1; } }")),
                    Collections.<String>emptyList());
    }

    public void testStatementAndSingleBlockStatementAreSame2() throws Exception {
        performTest("package test; public class Test { private void test() { { int y; y = 1; } }}",
                    Collections.singletonMap("{ int $1; { $1 = 1; } }", Arrays.asList("{ int y; y = 1; }")),
                    Collections.<String>emptyList());
    }

    public void testStatementVariables1() throws Exception {
        performTest("package test; public class Test { public int test1() { if (true) return 1; else return 2; } }",
                    Collections.singletonMap("if ($1) $2; else $3;", Arrays.asList("if (true) return 1; else return 2;")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariables1() throws Exception {
        performTest("package test; public class Test { public int test1(int i) { System.err.println(i); System.err.println(i); i = 3; System.err.println(i); System.err.println(i); return i; } }",
                    Collections.singletonMap("{ $s1$; i = 3; $s2$; return i; }", Arrays.asList("{ System.err.println(i); System.err.println(i); i = 3; System.err.println(i); System.err.println(i); return i; }")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariables2() throws Exception {
        performTest("package test; public class Test { public int test1(int i) { i = 3; return i; } }",
                    Collections.singletonMap("{ $s1$; i = 3; $s2$; return i; }", Arrays.asList("{ i = 3; return i; }")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariablesAndBlocks1() throws Exception {
        performTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                    Collections.singletonMap("if ($c) {$s1$; System.err.println(); $s2$; }", Arrays.asList("if (true) System.err.println();")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariablesAndBlocks2() throws Exception {
        performTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                    Collections.singletonMap("if ($c) {$s1$; System.err.println(); }", Arrays.asList("if (true) System.err.println();")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariablesAndBlocks3() throws Exception {
        performTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                    Collections.singletonMap("if ($c) {System.err.println(); $s2$; }", Arrays.asList("if (true) System.err.println();")),
                    Collections.<String>emptyList());
    }

    public void testMultiStatementVariablesAndBlocks4() throws Exception {
        performTest("package test; public class Test { public void test1() { if (true) System.err.println(); } }",
                    Collections.singletonMap("{ $s1$; System.err.println(); $s2$; }", Arrays.asList("System.err.println();")),
                    Collections.<String>emptyList());
    }

    public void testTwoPatterns() throws Exception {
        Map<String, List<String>> contained = new HashMap<String, List<String>>();

        contained.put("if ($a) $ret = $b; else $ret = $c;", Arrays.asList("if (b) q = 2; else q = 3;"));
        contained.put("{ $p$; $T $v; if($a) $v = $b; else $v = $c; $q$; }", Arrays.asList("{ int q; if (b) q = 2; else q = 3; }"));

        performTest("package test; public class Test { public void test1(boolean b) { int q; if (b) q = 2; else q = 3; } }",
                    contained,
                    Collections.<String>emptyList());
    }

    public void testEffectiveNewClass() throws Exception {
        performTest("package test; import javax.swing.ImageIcon; public class Test { public void test1(java.awt.Image i) { new ImageIcon(i); new String(i); } }",
                    Collections.singletonMap("new javax.swing.ImageIcon($1)", Arrays.asList("new ImageIcon(i)")),
                    Collections.<String>emptyList());
    }

    public void testSynchronizedAndMultiStatementVariables() throws Exception {
        performTest("package test; public class Test {public void test() { Object o = null; int i = 0; synchronized (o) {} } }",
                    Collections.singletonMap("synchronized($var) {$stmts$;}", Arrays.asList("synchronized (o) {}")),
                    Collections.<String>emptyList());
    }

    public void testJackpot30_2() throws Exception {
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private void m() {\n" +
                      "        a(c.i().getFileObject());\n" +
                      "        if (span != null && span[0] != (-1) && span[1] != (-1));\n" +
                      "    }\n" +
                      "}\n";

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    Arrays.asList("$0.getFileObject($1)"));
    }

    public void testIdentifierInPureMemberSelect() throws Exception {
        String code = "package test;\n" +
                       "public class Test {\n" +
                       "     public Test test;\n" +
                       "     public String name;\n" +
                       "     private void test() {\n" +
                       "         Test t = null;\n" +
                       "         String s = t.test.name;\n" +
                       "     }\n" +
                       "}\n";

        performTest(code,
                    Collections.singletonMap("$Test.test", Arrays.asList("test", "t.test")),
                    Collections.<String>emptyList());
    }

    @RandomlyFails
    public void testNoExponentialTimeComplexity() throws Exception {
        try {
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private void test() {\n" +
                      "        Object o;\n" +
                      "        if(o == null) {\n" +
                      "            f(\"\");\n" +
                      "        }|\n" +
                      "    }\n" +
                      "}";
        String pattern = "{ $p$; $T $v; if($a) $v = $b; else $v = $c; }";

        measure(code, "\na(\"\");", 5, pattern); //to load needed classes, etc.

        int rep = 1;
        long baseline;

        while (true) {
            baseline = measure(code, "\na(\"\");", rep, pattern);

            if (baseline >= 2000) {
                break;
            }

            rep *= 2;
        }

        long doubleSize = measure(code, "\na(\"\");", 2 * rep, pattern);

        assertTrue("baseline=" + baseline + ", actual=" + String.valueOf(doubleSize), doubleSize <= 4 * baseline);
        } catch (OutOfMemoryError oome) {
            //OK
        }
    }

    public void testMultiParameter1() throws Exception {
        performTest("package test; public class Test { { java.util.Arrays.asList(\"a\", \"b\", \"c\"); } }",
                    Collections.singletonMap("java.util.Arrays.asList($params$)", Arrays.asList("java.util.Arrays.asList(\"a\", \"b\", \"c\")")),
                    Collections.<String>emptyList());
    }

    public void testMultiParameter2() throws Exception {
        performTest("package test; public class Test { { java.util.Arrays.asList(); } }",
                    Collections.singletonMap("java.util.Arrays.asList($params$)", Arrays.asList("java.util.Arrays.asList()")),
                    Collections.<String>emptyList());
    }

    public void testTypeParameter() throws Exception {
        performTest("package test; public class Test { { java.util.Arrays.<String>asList(); } }",
                    Collections.singletonMap("java.util.Arrays.<$1>asList($params$)", Arrays.asList("java.util.Arrays.<String>asList()")),
                    Collections.<String>emptyList());
    }

    public void testField1() throws Exception {
        String code = "package test;\n" +
                       "public class Test {\n" +
                       "     String name = null;\n" +
                       "}\n";

        performTest(code,
                    Collections.singletonMap("$modifiers$ java.lang.String $name = $initializer;", Arrays.asList("String name = null;")),
                    Collections.<String>emptyList());
    }

    public void testField2() throws Exception {
        String code = "package test;\n" +
                       "public class Test {\n" +
                       "     private String name = null;\n" +
                       "}\n";

        performTest(code,
                    Collections.singletonMap("$modifiers$ java.lang.String $name = $initializer;", Arrays.asList("private String name = null;")),
                    Collections.<String>emptyList());
    }

    public void testMemberSelectWithVariable() throws Exception {
        String code = "package test;\n" +
                      "import java.util.Arrays;\n" +
                      "public class Test {" +
                      "     {\n" +
                      "          foo.bar(0, 3, 4);\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.singletonMap("$foo.$bar($p1, $p2$)", Arrays.asList("foo.bar(0, 3, 4)")),
                    Collections.<String>emptyList());
    }

    public void testCheckIdentifiers1() throws Exception {
        String code = "package test;\n" +
                      "import static java.util.Arrays.*;\n" +
                      "public class Test {" +
                      "     {\n" +
                      "          toString(new int[] {0, 3, 4});\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.singletonMap("java.util.Arrays.toString($x)", Arrays.asList("toString(new int[] {0, 3, 4})")),
                    Collections.<String>emptyList());
    }

    public void testCheckIdentifiers2() throws Exception {
        String code = "package test;\n" +
                      "public class Test {" +
                      "     {\n" +
                      "          toString(new int[] {0, 3, 4});\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    Collections.singletonList("java.util.Arrays.toString($x)"));
    }

    public void testCheckIdentifiers3() throws Exception {
        String code = "package test;\n" +
                      "import static java.util.Arrays.*;\n" +
                      "public class Test {" +
                      "     {\n" +
                      "          Foo.toString(new int[] {0, 3, 4});\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    Collections.singletonList("java.util.Arrays.toString($x)"));
    }

    public void testCheckIdentifiers4() throws Exception {
        String code = "package test;\n" +
                      "public class Test {" +
                      "     {\n" +
                      "          java.util.Arrays.toString(new int[] {0, 3, 4});\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.singletonMap("Arrays", Arrays.asList("java.util.Arrays")), //could be imported in the input pattern
                    Collections.<String>emptyList());
    }

    public void testLambdaInput() throws Exception {
        String code = "package test; public class Test {public void test() { new java.io.FilenameFilter() { public boolean accept(java.io.File dir, String name) { return true; } }; } }";

        performTest(code,
                    Collections.singletonMap("new $type() {public $retType $name($params$) { $body$; } }", Arrays.asList("new java.io.FilenameFilter() { public boolean accept(java.io.File dir, String name) { return true; } }")),

                    Collections.<String>emptyList());
    }

    public void testDoubleCheckedLockingWithVariable() throws Exception {
        String dcl =  "if (o == null) {\n" +
                      "              Object o1 = new Object();\n" +
                      "              synchronized (Test.class) {\n" +
                      "                  if (o == null) {\n" +
                      "                      o = o1;\n" +
                      "                  }\n" +
                      "              }\n" +
                      "          }";
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "     private Object o;\n" +
                      "     private void t() {\n" +
                      "          " + dcl + "\n" +
                      "     }\n" +
                      "}\n";

        performTest(code,
                    Collections.singletonMap("if ($var == null) {$pref$; synchronized ($lock) { if ($var == null) { $init$; } } }", Arrays.asList(dcl)),
                    Collections.<String>emptyList());
    }

    public void testMethodName1() throws Exception {
        String code = "package test; public class Test {public void test() { clone(); } }";

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    Collections.<String>singletonList("public void clone() {$stmts$;}"));
    }

    public void testMethodName2() throws Exception {
        String code = "package test; public class Test {public void test() { clone(); } }";

        performTest(code,
                    Collections.singletonMap("public void test() {$stmts$;}", Arrays.asList("public void test() { clone(); }")),
                    Collections.<String>emptyList());
    }
    
    public void testBooleanLiterals() throws Exception {
        String code = "package test; public class Test { public void test() { if (false) { System.err.println(\"false\"); } if (true) { System.err.println(\"true\"); } } }";

        performTest(code,
                    Collections.singletonMap("if (true) $then; else $else$;", Arrays.asList("if (true) { System.err.println(\"true\"); }")),
                    Collections.<String>emptyList());
    }
    
    public void testEfficientMultiMatching() throws Exception {
        String code = "package test; public class Test { private void m() {} }";

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    Collections.singletonList("$mods$ class $name implements $i$ { }"));
    }

    private long measure(String baseCode, String toInsert, int repetitions, String pattern) throws Exception {
        int pos = baseCode.indexOf('|');

        assertTrue(pos != (-1));

        baseCode = baseCode.replaceAll(Pattern.quote("|"), "");
        
        StringBuilder code = new StringBuilder(baseCode.length() + repetitions * toInsert.length());

        code.append(baseCode);
        
        while (repetitions-- > 0) {
            code.insert(pos, toInsert);
        }

        long startTime = System.currentTimeMillis();

        performTest(code.toString(),
                    Collections.<String, List<String>>emptyMap(),
                    Arrays.asList(pattern));

        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    public void XtestMeasureTime() throws Exception {
        String code = TestUtilities.copyFileToString(new File("/usr/local/home/lahvac/src/nb//outgoing/java.editor/src/org/netbeans/modules/editor/java/JavaCompletionProvider.java"));
        List<String> patterns = new LinkedList<String>();

        for (int cntr = 0; cntr < 1000; cntr++) {
            patterns.add("System.err.println($1)");
        }

        performTest(code,
                    Collections.<String, List<String>>emptyMap(),
                    patterns);
    }

    public void testMatches1() throws Exception {
        performMatchesTest("package test; public class Test { private void test() { f.isDirectory(); } }", Arrays.asList("$1.isDirectory()", "new ImageIcon($1)"), true);
    }

    public void testSerialization() throws Exception {
        String text = "package test; public class Test { public void test1(boolean b) { int q; if (b) q = 2; else q = 3; } }";

        prepareTest("test/Test.java", text);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncodingContext ec = new EncodingContext(out, false);

        createSearch().encode(info.getCompilationUnit(), ec, new AtomicBoolean());
        
        boolean matches = createSearch().matches(new ByteArrayInputStream(out.toByteArray()), new AtomicBoolean(), createSearch().create(info, new AtomicBoolean(), "{ $p$; $T $v; if($a) $v = $b; else $v = $c; $q$; }"));

        assertTrue(matches);
    }

    public void testFrequencies() throws Exception {
        String text = "package test; public class Test { public void test1(boolean b) { java.io.File f = null; f.isDirectory(); f.isDirectory(); new javax.swing.ImageIcon(null); } }";

        prepareTest("test/Test.java", text);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncodingContext ec = new EncodingContext(out, false);

        createSearch().encode(info.getCompilationUnit(), ec, new AtomicBoolean());
        
        Map<String, Integer> actual = createSearch().matchesWithFrequencies(new ByteArrayInputStream(out.toByteArray()), createSearch().create(info, new AtomicBoolean(), "$1.isDirectory()", "new ImageIcon($1)"), new AtomicBoolean());
        Map<String, Integer> golden = new HashMap<String, Integer>();

        golden.put("$1.isDirectory()", 2);
        golden.put("new ImageIcon($1)", 1);

        assertEquals(golden, actual);
    }

    public void testPatternEncodingAndIdentifiers() throws Exception {
        String text = "package test; public class Test { }";

        prepareTest("test/Test.java", text);

        BulkPattern bp = createSearch().create(info, new AtomicBoolean(), "$0.isDirectory()");

        assertEquals(Arrays.asList(new HashSet<String>(Arrays.asList("isDirectory"))), bp.getIdentifiers());
        //TODO: the actual code for kinds differs for NFABased search and REBased search:
//        assertEquals(Arrays.asList(new HashSet<String>(Arrays.asList(Kind.METHOD_INVOCATION.name()))), bp.getKinds());
    }
    
    public void testModifiersMultiVariable() throws Exception {
        String code = "package test;\n" +
                       "public class Test {\n" +
                       "     @Deprecated @Override public Test test;\n" +
                       "}\n";

        performTest(code,
                    Collections.singletonMap("$mods$ @Deprecated public $type $name = $init$;", Arrays.asList("@Deprecated @Override public Test test;")),
                    Collections.<String>emptyList());
    }

    protected abstract BulkSearch createSearch();
    
    private void performMatchesTest(String text, List<String> patterns, boolean golden) throws Exception {
        prepareTest("test/Test.java", text);

        BulkPattern p = createSearch().create(info, new AtomicBoolean(), patterns);

        boolean result = createSearch().matches(info, new AtomicBoolean(), new TreePath(info.getCompilationUnit()), p);

        assertEquals(golden, result);
    }
    
    private void performTest(String text, Map<String, List<String>> containedPatterns, Collection<String> notContainedPatterns) throws Exception {
        prepareTest("test/Test.java", text);

        List<String> patterns = new LinkedList<String>();

        patterns.addAll(containedPatterns.keySet());
        patterns.addAll(notContainedPatterns);

        long s1 = System.currentTimeMillis();
        BulkPattern p = createSearch().create(info, new AtomicBoolean(), patterns);
        long e1 = System.currentTimeMillis();

//        System.err.println("create: " + (e1 - s1));

        long s2 = System.currentTimeMillis();
        Map<String, Collection<TreePath>> result = createSearch().match(info, new AtomicBoolean(), new TreePath(info.getCompilationUnit()), p);
        long e2 = System.currentTimeMillis();

//        System.err.println("match: " + (e2 - s2));

        assertTrue(result.toString(), result.keySet().containsAll(containedPatterns.keySet()));

        for (Entry<String, Collection<TreePath>> e : result.entrySet()) {
            List<String> actual = new LinkedList<String>();

            for (TreePath tp : e.getValue()) {
                assertNotNull(TreePathHandle.create(tp, info).resolve(info));
                
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tp.getLeaf());
                int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tp.getLeaf());

                actual.add(info.getText().substring(start, end));
            }

            assertEquals(e.getKey(), containedPatterns.get(e.getKey()), actual);
        }


        Set<String> none = new HashSet<String>(result.keySet());

        none.retainAll(notContainedPatterns);

        assertTrue(none.isEmpty());

        if (!verifyIndexingData())
            return ;
        
        //ensure the returned identifiers/treeKinds are correct:
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        EncodingContext ec = new EncodingContext(data, false);
        
        createSearch().encode(info.getCompilationUnit(), ec, new AtomicBoolean());

        for (int i = 0; i < containedPatterns.size(); i++) {
            assertTrue("expected: " + p.getIdentifiers().get(i) + ", but exist only: " + ec.getIdentifiers(), ec.getIdentifiers().containsAll(p.getIdentifiers().get(i)));
            
            for (List<String> phrase : p.getRequiredContent().get(i)) {
                assertTrue("expected: " + phrase + ", but exist only: " + ec.getContent() + "(all phrases: " + p.getRequiredContent().get(i) + ")", Collections.indexOfSubList(ec.getContent(), phrase) != (-1));
            }
        }

        data.close();
        assertEquals(!containedPatterns.isEmpty(), createSearch().matches(new ByteArrayInputStream(data.toByteArray()), new AtomicBoolean(), p));
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();

        FileUtil.refreshFor(File.listRoots());

        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        workFO.refresh();

        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;

    protected boolean verifyIndexingData() {
        return true;
    }
}
