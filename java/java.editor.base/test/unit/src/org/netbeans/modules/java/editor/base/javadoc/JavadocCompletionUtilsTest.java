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

package org.netbeans.modules.java.editor.base.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestSuite;

/**
 * XXX missing tests of unclosed javadoc cases
 * 
 * @author Jan Pokorsky
 */
public class JavadocCompletionUtilsTest extends JavadocTestSupport {

    public JavadocCompletionUtilsTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        System.setProperty("org.netbeans.modules.javadoc.completion.level", "0");
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(JavadocCompletionUtilsTest.class);
//        suite.addTest(new JavadocCompletionUtilsTest("testFindJavadoc"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsInlineTagStart"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsJavadocContext"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsJavadocContext_InEmptyJavadoc"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsLineBreak"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsLineBreak2"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsWhiteSpace"));
//        suite.addTest(new JavadocCompletionUtilsTest("testIsInvalidDocInstance"));
        return suite;
    }
    
    public void testIsJavadocContext() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
                "  inside indent   * Second line sentence.\n" +
                "     * @param m1 m1 description\n" +
                "     * \n" +
                "     * @return return description\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, code.indexOf("HUH"));
        jdts.moveStart();
        jdts.moveNext();
        
        String what = "HUH";
        int offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = " sentence";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "block comment";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "int m";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // test positions around '*'
        
        // empty content
        what = "     * \n";
        offset = code.indexOf(what) + what.length() - 1;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // '*' inside sentence is not considered as indent
        what = " inside indent   *";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // test position inside indent
        what = "   * HUH";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 1;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 2;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/**";
        offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        // last line of javadoc
        what = "*/";
        offset = code.indexOf(what);
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "*/";
        offset = code.indexOf(what) + 1;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "*/";
        offset = code.indexOf(what) + 2;
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
    }
    
    public void testIsJavadocContext_InEmptyJavadoc() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** */\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);

        String what = "/***/";
        int offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
        
        what = "/** */";
        offset = code.indexOf(what) + 3;
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isJavadocContext(doc, offset));
    }

    public void testFindJavadoc() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** */\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);
        
        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(1);
        
        String what = "/***/";
        int offset = code.indexOf(what) + 3;
        
        TreePath jdoc = JavadocCompletionUtils.findJavadoc(info, offset);
        assertEquals("Wrong Doc instance", fieldEl, info.getTrees().getElement(jdoc));
    }

    public void testFindJavadoc_147533() throws Exception {
        String code =
                "package p;\n" +
                "class C {\n" +
                "    /**jd1*/\n" +
                "    /**/\n" +
                "    int f1;\n" +
                "    /**jd2*/\n" +
                "    /*a*/\n" +
                "    int f2;\n" +
                "}\n";
        prepareTest(code);

        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(1);

        String what = "/**jd1*/";
        int offset = code.indexOf(what) + 3;

        TreePath jdoc = JavadocCompletionUtils.findJavadoc(info, offset);
        assertNull("Wrong Doc instance", jdoc);

        fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(2);
        what = "/**jd2*/";
        offset = code.indexOf(what) + 3;
        jdoc = JavadocCompletionUtils.findJavadoc(info, offset);
        assertEquals("Wrong Doc instance", fieldEl, info.getTrees().getElement(jdoc));
    }
    
    public void testResolveOtherText() throws Exception {
        // XXX obsolete, write new one
//        String code = 
//                "package p;\n" +
//                "class C {\n" +
//                "    /**\n" +
//                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
//                "  inside indent   * Second line sentence.\n" +
//                " * \n" +
//                "     * @param m1 m1 description\n" +
//                "     * \n" +
//                "     * @return return description\n" +
//                "     */\n" +
//                "    int m(int m1) {\n" +
//                "        /* block comment */\n" +
//                "        return 0;\n" +
//                "    }\n" +
//                "}\n";
//        prepareTest(code);
//        
//        String what = "     * \n";
//        int offset = code.indexOf(what) + what.length() - 1;
//        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
//        assertTrue(jdts.moveNext());
//        JavadocCompletionQuery.JavadocContext jdctx = new JavadocCompletionQuery.JavadocContext();
//        JavadocCompletionQuery.resolveOtherText(jdctx, jdts, offset);
//        assertFalse(what, jdctx.isInsideDesription);
//        
//        what = " * \n";
//        offset = code.indexOf(what) + what.length() - 1;
//        jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
//        assertTrue(jdts.moveNext());
//        jdctx = new JavadocCompletionQuery.JavadocContext();
//        JavadocCompletionQuery.resolveOtherText(jdctx, jdts, offset);
//        assertFalse(what, jdctx.isInsideDesription);
    }
    
    public void testIsLineBreak() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link String} GUG. Second  sentence. <code>true {@link St</code>\n" +
                "  \n" +
                "  * *\n" +
                "  * {*iii\n" +
                "  inside indent   * Second line sentence.\n" +
                " * \n" +
                "     * @param m1 m1 description\n" +
                "     * \n" +
                "     * @return return description{@link String } \n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "     * HUH";
        int offset = code.indexOf(what) + what.length() - 4;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts));
        offset += 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        // token is INDENT
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts));
        
        what = "  \n";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts, offset - jdts.offset()));
        
        what = "  * {*i";
        offset = code.indexOf(what) + what.length() - 3;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts));
        assertTrue(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts, offset - jdts.offset()));
        offset = code.indexOf(what);
        assertFalse(insertPointer(code, offset),
                JavadocCompletionUtils.isLineBreak(jdts, offset - jdts.offset()));
    }
    
    public void testIsLineBreak2() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * {@code String}\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "{@code";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        // test OTHER_TEXT('     * {|')
        assertTrue(jdts.moveNext());
        assertTrue(jdts.token().id() == JavadocTokenId.OTHER_TEXT);
        assertFalse(insertPointer(code, jdts.offset() + jdts.token().length()),
                JavadocCompletionUtils.isLineBreak(jdts));
        // test OTHER_TEXT('     * |{')
        assertTrue(insertPointer(code, jdts.offset() + jdts.token().length() - 1),
                JavadocCompletionUtils.isLineBreak(jdts, jdts.token().length() - 1));
    }
    
    public void testIsWhiteSpace() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link \t String} GUG. Second  sentence. \n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "     * HUH";
        int offset = code.indexOf(what) + what.length() - 4;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpaceLast(jdts.token()));
        
        what = "Second  sentence.";
        offset = code.indexOf(what) + "Second".length();
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpace(jdts.token()));
        
        what = "\t String}";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpace(jdts.token()));
    }
    
    public void testIsFirstWhiteSpaceAtFirstLine_131826() throws Exception {
        String code = 
                "/** * @author\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        
        prepareTest(code);
        
        String what = "* @author";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isFirstWhiteSpaceAtFirstLine(jdts.token()));
    }
    
    public void testIsInlineTagStart() throws Exception {
        String code = 
                "package p;\n" +
                "class C {\n" +
                "    /**\n" +
                "     * HUH {@link \t String} GUG{@link String}.\n" +
                "     */\n" +
                "    int m(int m1) {\n" +
                "        /* block comment */\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "HUH {@link";
        int offset = code.indexOf(what) + 3;
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isWhiteSpaceFirst(jdts.token()));
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInlineTagStart(jdts.token()));
        
        what = "GUG{@link";
        offset = code.indexOf(what) + 3;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInlineTagStart(jdts.token()));
    }
    
    public void testIsInsideIndent() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " *   \n" +
                "   line3\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);
        
        String what = " * line1";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = " * line1";
        offset = code.indexOf(what) + 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertTrue(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = " * line1";
        offset = code.indexOf(what) + 2;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = "   line3";
        offset = code.indexOf(what);
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        what = "line3";
        offset = code.indexOf(what) + 1;
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
        
        // issue #128963
        what = " * ";
        offset = code.indexOf(what) + what.length();
        jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        assertTrue(jdts.moveNext());
        assertFalse(insertPointer(code, offset), JavadocCompletionUtils.isInsideIndent(jdts.token(), offset - jdts.offset()));
    }
    
    public void testConcurrentModification_130709() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);
        
        String what = " * line1";
        int offset = code.indexOf(what);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, offset);
        
        doc.insertString(0, "\n", null);
        assertTrue(jdts.moveNext());
    }
    
    public void testFindJavadocTokenSequenceForElement() throws Exception {
        String code = 
                "/**\n" +
                " * line1\n" +
                " */\n" +
                "class C {\n" +
                "}\n";
        prepareTest(code);

        TypeElement clazzC = info.getTopLevelElements().iterator().next();
        assertNotNull(clazzC);

        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, clazzC);
        assertNotNull(jdts);
        assertTrue(jdts.moveNext());

        // test synthetic element #131157
        Element defConstructor = clazzC.getEnclosedElements().get(0);
        assertNotNull(defConstructor);
        assertTrue(info.getElementUtilities().isSynthetic(defConstructor));
        assertNull(JavadocCompletionUtils.findJavadocTokenSequence(info, null, defConstructor));
    }

    public void testIsInvalidDocInstance() throws Exception {
        String code =
                "package p;\n" +
                "class C {\n" +
                "    /***/\n" +
                "    int f1;\n" +
                "    /** \t */\n" +
                "    int f2;\n" +
                "    /**\t \n" +
                "     */\n" +
                "    int f3;\n" +
                "    /** javadoc\n" +
                "     */\n" +
                "    int f4;\n" +
                "    int f5;\n" +
                "    /**\n" +
                "     */\n" +
                "    int f6_issue_159352;\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    int f7;\n" +
                "    /**\n" +
                "\n" +
                "     */\n" +
                "    int f8;\n" +
                "    /**  **/\n" +
                "    int f9_issue_139147;\n" +
                "    /**  ***/\n" +
                "    int f10_issue_139147;\n" +
                "    /**  * */\n" +
                "    int f11_issue_139147;\n" +
                "    /*******\n" +
                "     ***/\n" +
                "    int f12_issue_183776;\n" +
                "}\n";
        prepareTest(code);

        doIsInvalidJavadoc(1, null, false);
        doIsTokenOfEmptyJavadoc(1, true);
        doIsInvalidJavadoc(2, null, false);
        doIsTokenOfEmptyJavadoc(2, true);
        doIsInvalidJavadoc(3, null, false);
        doIsTokenOfEmptyJavadoc(3, true);
        doIsInvalidJavadoc(4, null, false);

        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(4);
        assertNotNull(fieldEl);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, fieldEl);
        assertNotNull(jdts);
        doIsInvalidJavadoc(5, jdts, true);

        // issue 159352
        doIsInvalidJavadoc(6, null, false);
        doIsTokenOfEmptyJavadoc(6, true);
        doIsInvalidJavadoc(7, null, false);
        doIsTokenOfEmptyJavadoc(7, true);
        doIsInvalidJavadoc(8, null, false);
        doIsTokenOfEmptyJavadoc(8, true);
        doIsInvalidJavadoc(9, null, false);
        doIsTokenOfEmptyJavadoc(9, true);
        doIsInvalidJavadoc(10, null, false);
        doIsTokenOfEmptyJavadoc(10, true);
        doIsInvalidJavadoc(11, null, false);
        doIsTokenOfEmptyJavadoc(11, true);
        doIsInvalidJavadoc(12, null, false);
        doIsTokenOfEmptyJavadoc(11, true);
    }

    private void doIsInvalidJavadoc(int fieldIndex, TokenSequence<JavadocTokenId> jdts, boolean isInvalid) {
        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(fieldIndex);
        assertNotNull(fieldEl);
        DocCommentTree jdoc = info.getDocTrees().getDocCommentTree(fieldEl);
        jdts = jdts != null ? jdts : JavadocCompletionUtils.findJavadocTokenSequence(info, null, fieldEl);
        assertNotNull(jdts);
        assertEquals(fieldEl.getSimpleName().toString(), isInvalid, JavadocCompletionUtils.isInvalidDocInstance(jdoc, jdts));
    }

    private void doIsTokenOfEmptyJavadoc(int fieldIndex, boolean isEmpty) {
        Element fieldEl = info.getTopLevelElements().get(0).getEnclosedElements().get(fieldIndex);
        assertNotNull(fieldEl);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, null, fieldEl);
        assertNotNull(jdts);
        jdts.moveStart();
        if (jdts.moveNext()) {
            assertEquals(fieldEl.getSimpleName().toString(), isEmpty, JavadocCompletionUtils.isTokenOfEmptyJavadoc(jdts.token()));
        }
    }
    
}
