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
package org.netbeans.modules.editor.java;

import java.nio.file.Paths;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 */
public class JavaBracesMatcherTest extends NbTestCase {

    private volatile int testNumber = 0;

    public JavaBracesMatcherTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[]{});
        SourceUtilsTestUtil2.disableArtificalParameterNames();
    }
    
    @Test
    public void testBracketInStringAndOutsideString() throws Exception {
        assertHaveNoMatches("String[] x = {\"^{test\",\"test2\"}");
        assertHaveNoMatches("char[] x = {'^{'}");
    }

    @Test
    public void testBracketsInDifferentStrings() throws Exception {
        assertHaveNoMatches("String[] x = {\"^{test\",\"test2}\"}");
        assertHaveNoMatches("String[] x = {\"{test\",\"test2^}\"}");
    }

    @Test
    public void testBracketsInSameString() throws Exception {
        assertMatches2("String x = \"aaaa^{^}\"");
    }

    @Test
    public void testStringTemplateBrackets() throws Exception {
        assertMatches2("\"\\^{test^}\"");
    }

    @Test
    public void testMultilineStringBrackets() throws Exception {
        assertMatches2(
            "\"\"\"\n"
            + "^(\n"
            + "^)\n"
            + "\"\"\"");
    }

    @Test
    public void testAngleBrackets() throws Exception {
        perfomAngleBracketsTest("Map x = new HashMap^<String, List<String>^>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, List^<String^>>()");
        perfomAngleBracketsTest("Map x = new HashMap^<String, Map<Integer,List<String>>^>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, Map^<Integer,List<String>^>>()");
        perfomAngleBracketsTest("Map x = new HashMap^<String, Map<Integer,List<String> > ^>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, Map^<Integer,List<String> ^> >()");
        perfomAngleBracketsTest("Map x = new HashMap^<String, Map<Integer,List<String> > ^>()");
        perfomAngleBracketsTest("Map x = new HashMap^<String, Map<Integer,Map<Integer,Map<Integer,List<String>>>>^>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, Map^<Integer,Map<Integer,Map<Integer,List<String>>>^>>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, Map<Integer,Map^<Integer,Map<Integer,List<String>>^>>>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, Map<Integer,Map<Integer,Map^<Integer,List<String>^>>>>()");
        perfomAngleBracketsTest("Map<String, Map<Integer,Map<Integer,Map<Integer,List<String>>>>> x = new HashMap^<^>()");
        perfomAngleBracketsTest("Map x = new HashMap<String, List^<String^>");
        perfomAngleBracketsTest("Map^<List<List<List<String>>>, Integer^> x = new HashMap<>()");
        perfomAngleBracketsTest("Map<List^<List<List<String>>^>, Integer> x = new HashMap<>()");
        perfomAngleBracketsTest("Map^<List<List<String>>, Integer^> x = new HashMap<>()");
        perfomAngleBracketsTest("""
            Map<Integer,List<String>> x = new HashMap<Integer,List<String>>()
            Object a = x.^<HashMap<Integer,List<String>>^>get(0)
        """);
        perfomAngleBracketsTest("class Test implements List^<String^> {}");
        perfomAngleBracketsTest("class Test implements Map^<String,List<String>^> {}");
        perfomAngleBracketsTest("class Test implements Map<String,List^<String^>> {}");
        perfomAngleBracketsTest("public <T> class Test implements Map<String,List^<T^>> {}");
        perfomAngleBracketsTest("public <T> class Test implements Map^<String,List<T>^> {}");
        perfomAngleBracketsTest("public ^<T^> class Test implements Map<String,List<T>> {}");
        // Check that non-generics are not reported as potential braces
        assertHasNoOrigin("int n1 = 10 ^>> 1;");
        assertHasNoOrigin("int n1 = 10 >^> 1;");
        assertHasNoOrigin("int n1 = 10 >>^ 1;");
        assertHasNoOrigin("int n1 = 10^>> 1;");
        assertHasNoOrigin("int n1 = 10>^> 1;");
        assertHasNoOrigin("int n1 = 10>>^ 1;");
        assertHasNoOrigin("if(b1 ^< 10) {}");
        assertHasNoOrigin("if(b1 ^> 10) {}");
        assertHasNoOrigin("""
            List<String> x = new ArrayList<>();
            for (int i = 0; i < 10; i++) x.forEach(x -^> {})
        """);
        // check javadoc
        assertHasNoOrigin("/** ^<p> */");
        assertHasNoOrigin("/** <p^> */");
    }

    private String makeTestClass(String angleStr) {
        String ret = "package text;\n"
            + "import java.util.List;\n"
            + "import java.util.Map;\n"
            + "import java.util.HashMap;\n"
            + "public class Test" + testNumber + " {\n"
            + "public void test() {\n"
            + angleStr + ";\n"
            + "}\n"
            + "}\n";

        return ret;
    }
    
    private TestAux prepareJavaFile(String angleStr,boolean findMatchingCaret) throws Exception {
        testNumber++;
        String srcTmp = makeTestClass(angleStr);
        int caretPos = srcTmp.indexOf('^');
        String sourceCode = srcTmp.substring(0, caretPos) + srcTmp.substring(caretPos + 1);
        int matchingCaretPos = findMatchingCaret 
                ? sourceCode.indexOf('^')
                : -1;
        sourceCode = findMatchingCaret 
                ? sourceCode.substring(0, matchingCaretPos) + sourceCode.substring(matchingCaretPos + 1)
                : sourceCode;
        assert(findMatchingCaret == false || matchingCaretPos > 0);
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject sourceDir = FileUtil.createFolder(wd, "src");
        FileObject buildDir = FileUtil.createFolder(wd, "build");
        FileObject cacheFolder = FileUtil.createFolder(wd, "cache");
        Paths.get(cacheFolder.toURI()).toFile().mkdirs();
        FileObject testFO = FileUtil.createData(sourceDir, "test/Test" + testNumber + ".java");
        TestUtilities.copyStringToFile(testFO, sourceCode);
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheFolder);
        JavaSource source = JavaSource.forFileObject(testFO);
        assertNotNull(source);
        DataObject od = DataObject.find(testFO);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", JavaKit.JAVA_MIME_TYPE);
        return new TestAux(caretPos,matchingCaretPos,doc);
    }

    private void perfomAngleBracketsTest(String angleStr) throws Exception {
        TestAux testAux = prepareJavaFile(angleStr,true);
        computeAndAssertMatches(testAux.doc(), testAux.caretPos(), false, testAux.matchingCaretPos());
        computeAndAssertMatches(testAux.doc(), testAux.caretPos() + 1, true, testAux.matchingCaretPos());
        computeAndAssertMatches(testAux.doc(), testAux.matchingCaretPos(), false, testAux.caretPos());
        computeAndAssertMatches(testAux.doc(), testAux.matchingCaretPos() + 1, true, testAux.caretPos());
    }

    private void assertHaveNoMatches(String angleStr) throws Exception {
        TestAux testAux = prepareJavaFile(angleStr,false);
        BracesMatcherFactory factory = new JavaBracesMatcher();
        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(testAux.doc(), testAux.caretPos(), false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = matcher.findOrigin();
        assertNotNull(origin);
        if (origin.length > 0) {
            assertNull(matcher.findMatches());
        }
    }    
    
    //from CslTestBase:
    protected void assertMatches2(String original) throws Exception {
        int caretPos = original.indexOf('^');
        original = original.substring(0, caretPos) + original.substring(caretPos + 1);

        int matchingCaretPos = original.indexOf('^');

        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos + 1);

        BaseDocument doc = getDocument(original);

        computeAndAssertMatches(doc, caretPos, false, matchingCaretPos);
        computeAndAssertMatches(doc, caretPos + 1, true, matchingCaretPos);
        computeAndAssertMatches(doc, matchingCaretPos, false, caretPos);
        computeAndAssertMatches(doc, matchingCaretPos + 1, true, caretPos);
    }

    private void computeAndAssertMatches(Document doc, int pos, boolean backwards, int matchingPos) throws BadLocationException, InterruptedException {
        BracesMatcherFactory factory = new JavaBracesMatcher();
        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, pos, backwards, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = matcher.findOrigin();
        int[] matches = matcher.findMatches();

        assertNotNull("Did not find origin for " + " position " + pos, origin);
        assertNotNull("Did not find matches for " + " position " + pos, matches);

        int expectedPos = backwards ? pos - 1 : pos;

        assertEquals("Incorrect origin", expectedPos, origin[0]);
        assertEquals("Incorrect origin", expectedPos + 1, origin[1]);
        assertEquals("Incorrect matches", matchingPos, matches[0]);
        assertEquals("Incorrect matches", matchingPos + 1, matches[1]);
    }
    
/**
     * Pass a method body as {@code angleStr}, where the caret position is
     * marked by {@code ^}. The method checks, that the
     * JavaBracesMatcher#findOrigin returns null.
     *
     * @param angleStr
     * @throws Exception 
     */
    private void assertHasNoOrigin(String angleStr) throws Exception {
        TestAux testAux = prepareJavaFile(angleStr,false);
        BracesMatcherFactory factory = new JavaBracesMatcher();
        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(testAux.doc(), testAux.caretPos(), false, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        assertNull(matcher.findOrigin());
    }    

    private BaseDocument getDocument(String content) throws Exception {
        BaseDocument doc = new BaseDocument(true, JavaKit.JAVA_MIME_TYPE) {
        };
        doc.putProperty(org.netbeans.api.lexer.Language.class, JavaTokenId.language());
        doc.insertString(0, content, null);
        return doc;
    }
    
    static record TestAux(int caretPos, int matchingCaretPos, Document doc) {}
        

}
