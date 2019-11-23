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

package org.netbeans.modules.java.editor.javadoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.modules.java.editor.base.javadoc.JavadocTestSupport;

/**
 *
 * @author Jan Pokorsky
 */
public class JavadocCompletionQueryTest extends JavadocTestSupport {

    public JavadocCompletionQueryTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(JavadocCompletionQueryTest.class);
//        suite.addTest(new JavadocCompletionQueryTest("testFindJavadoc"));
        return suite;
    }
    
    public void testParamNameCompletionForMethod() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @param p|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "p1:", "p2:");
    }
    
    public void testParamNameCompletionForMethod2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @param |\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "p1:", "p2:");
    }
    
    public void testParamNameCompletionForMethod3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @param" +
                "            p|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "p1:", "p2:");
    }
    
    public void testParamNameCompletionForMethod4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @param p1 tada\n" +
                "     * @param |\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "p2:");
    }
    
    public void testParamNameCompletionForConstructor() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @param p|\n" +
                "     */\n" +
                "    Clazz(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "p1:", "p2:");
    }
    
    public void XtestParamNameCompletionForClass() throws Exception {
        //XXX: ???
        String code =
                "package p;\n" +
                "/**\n" +
                " * @param P|\n" +
                " */\n" +
                "class Clazz<P1,P2> {\n" +
                "}\n";
        
        performCompletionTest(code, "P1:", "P2:");
    }
    
    public void testLink1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@link |\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "String", "Clazz");
    }
    
    public void testLink2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@link Str|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "String");
    }
    
    public void testLink3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@link String#|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "public final native Class getClass()");
    }
    
    public void testLink4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@link CharSequence#le|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "public abstract int length()");
    }
    
    public void testSee1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @see |\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "String", "Clazz");
    }
    
    public void testSee2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @see Str|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "String");
    }
    
    public void testSee3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @see String#|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "public final native Class getClass()");
    }
    
    public void testSee4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @see CharSequence#le|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "public abstract int length()");
    }
    
    public void testValue1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@value |\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "Math");
    }
    
    public void testValue2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@value Mat|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "Math");
    }
    
    public void testValue3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@value Math#|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "public static final double PI");
    }
    
    public void testValue4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@value Math#P|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "public static final double PI");
    }
    
    public void testThrows1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @throws |\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, null, "Exception", "NullPointerException");
    }
    
    public void testThrows2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @throws Class|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "ClassCircularityError", "ClassFormatError", "ClassCastException", "ClassNotFoundException");
    }
    
    private static String stripHTML(String from) {
        StringBuilder result = new StringBuilder();
        boolean inHTMLTag = false;
        
        for (char c : from.toCharArray()) {
            switch (c) {
                case '<': inHTMLTag = true; break;
                case '>': inHTMLTag = false; break;
                default:
                    if (!inHTMLTag) result.append(c);
                    break;
            }
        }
        
        return result.toString();
    }
    
    private void performCompletionTest(String code, String... golden) throws Exception {
        int caret = code.indexOf('|');
        prepareTest(code.replace("|", ""));
        
        List<CompletionItem> resultObj = JavadocCompletionQuery.runCompletionQuery(CompletionProvider.COMPLETION_QUERY_TYPE, doc, caret);
        List<String> resultStrings = new ArrayList<String>(resultObj.size());
        
        for (CompletionItem ci : resultObj) {
            if (ci instanceof JavadocCompletionItem) {
                JavadocCompletionItem jci = (JavadocCompletionItem) ci;
                resultStrings.add(stripHTML(jci.getLeftHtmlText() + ":" + (jci.getRightHtmlText() != null ? jci.getRightHtmlText() : "")));
            } else {
                resultStrings.add(stripHTML(ci.toString()));
            }
        }
        List<String> goldenList = new ArrayList<String>(Arrays.asList(golden));
        
        if (goldenList.contains(null)) {
            goldenList.remove(null);
            assertTrue(resultStrings.toString(), resultStrings.containsAll(goldenList));
        } else {
            Collections.sort(goldenList);
            Collections.sort(resultStrings);
            assertEquals(goldenList, resultStrings);
        }
    }
}
