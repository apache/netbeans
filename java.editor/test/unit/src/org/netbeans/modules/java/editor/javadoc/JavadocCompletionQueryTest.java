/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
