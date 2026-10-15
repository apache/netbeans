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
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.modules.java.editor.base.javadoc.JavadocTestSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

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
    
    public void testBlockTagsCompletion() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * |\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }
    
    public void testBlockTagsCompletion1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * |\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }
    
    public void testBlockTagsCompletion2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }
    
    public void testBlockTagsCompletion3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * @|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }
    
    public void testBlockTagsCompletion4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @p|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@param:");
    }
    
    public void testBlockTagsCompletion5() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * @p|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@param:");
    }
    
    public void testBlockTagsCompletion6() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * @p|aram\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@param:");
    }
    
    public void testBlockTagsCompletion7() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * @p|aram\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@param:");
    }
    
    public void testInlineTagsCompletion() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@code:", "@docRoot:", "@index:", "@inheritDoc:", "@link:", "@linkplain:", "@literal:", "@snippet:", "@summary:", "@systemProperty:");
    }
    
    public void testInlineTagsCompletion1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * {|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@code:", "@docRoot:", "@index:", "@inheritDoc:", "@link:", "@linkplain:", "@literal:", "@snippet:", "@summary:", "@systemProperty:");
    }

    public void testInlineTagsCompletion2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@code:", "@docRoot:", "@index:", "@inheritDoc:", "@link:", "@linkplain:", "@literal:", "@snippet:", "@summary:", "@systemProperty:");
    }
    
    public void testInlineTagsCompletion3() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * {@|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@code:", "@docRoot:", "@index:", "@inheritDoc:", "@link:", "@linkplain:", "@literal:", "@snippet:", "@summary:", "@systemProperty:");
    }

    public void testInlineTagsCompletion4() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@l|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@link:", "@linkplain:", "@literal:");
    }
    
    public void testInlineTagsCompletion5() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * {@l|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@link:", "@linkplain:", "@literal:");
    }

    public void testInlineTagsCompletion6() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@l|ink\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@link:", "@linkplain:", "@literal:");
    }
    
    public void testInlineTagsCompletion7() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * This is javadoc for method.\n" +
                "     * {@l|ink\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "}\n";
        
        performCompletionTest(code, "@link:", "@linkplain:", "@literal:");
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
    
    public void testParamNameCompletionForClass() throws Exception {
        String code =
                "package p;\n" +
                "/**\n" +
                " * @param <P|\n" +
                " */\n" +
                "class Clazz<P1,P2> {\n" +
                "}\n";
        
        performCompletionTest(code, "&lt;P1&gt;:", "&lt;P2&gt;:");
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
                "     * {@value Math|\n" +
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
    
    public void testSnippet1() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@snippet\n" +
                "     *  System.err.println(1); //@|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";

        performCompletionTest(code, "end:", "highlight:", "link:", "replace:", "start:");
    }

    public void testSnippet2() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@snippet\n" +
                "     * //@e|\n" +
                "     */\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";

        performCompletionTest(code, "end:");
    }
    
    public void testSummaryCompletionForMethod() throws Exception {
        String code =
                "package p;\n" +
                "class Clazz {\n" +
                "    /**\n" +
                "     * {@sum|\n" +
                "     */\n" +
                "    void method(int p1, int p2) {\n" +
                "    }\n" +
                "    Clazz() {\n" +
                "    }\n" +
                "}\n";
        performCompletionTest(code, "@summary:");
    }    

    public void testBlockTagsCompletionInMarkdown() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    /// |
                    ///
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testBlockTagsCompletionInMarkdown2() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    ///|
                    ///
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testBlockTagsCompletionInMarkdown3() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    ///|\s
                    ///
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testBlockTagsCompletionInMarkdownStart() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///|
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testSeeMarkdown1() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    /// @see CharSequence#le|
                    ///
                    Clazz() {
                    }
                }
                """;

        performCompletionTest(code, "public abstract int length()");
    }

    public void testSeeMarkdown2() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    /// @see |
                    ///
                    Clazz() {
                    }
                }
                """;

        performCompletionTest(code, null, "String", "Clazz");
    }

    public void testSeeMarkdown3() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///@param i i
                    ///@see |
                    ///
                    Clazz(int i) {
                    }
                }
                """;

        performCompletionTest(code, null, "String", "Clazz");
    }

    public void testParamMarkdown() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///
                    /// @param |
                    ///
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "p1:", "p2:");
    }

    public void testJavadocOldStart1() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    /**| */
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testJavadocOldStart2() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    /**@s| */
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@see:", "@serialData:", "@since:");
    }

    public void testJavadocOldStart3() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    /**@param | */
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "p1:", "p2:");
    }

    public void testJavadocOldStart4() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    /**@see | */
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, null, "String", "Clazz");
    }

    public void testJavadocMarkdownStart1() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///|
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@deprecated:", "@exception:", "@hidden:", "@param:", "@return:", "@see:", "@serialData:", "@since:", "@throws:");
    }

    public void testJavadocMarkdownStart2() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///@s|
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "@see:", "@serialData:", "@since:");
    }

    public void testJavadocMarkdownStart3() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///@param |
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, "p1:", "p2:");
    }

    public void testJavadocMarkdownStart4() throws Exception {
        String code =
                """
                package p;
                class Clazz {
                    ///@see |
                    void method(int p1, int p2) {
                    }
                }
                """;

        performCompletionTest(code, null, "String", "Clazz");
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

    @Override
    protected Object[] additionalServices() {
        return new Object[] {
            new SourceLevelQueryImplementation2() {

                @Override
                public Result getSourceLevel(FileObject javaFile) {
                    return new Result() {
                        @Override
                        public String getSourceLevel() {
                            return "23";
                        }

                        @Override
                        public void addChangeListener(ChangeListener listener) {
                        }

                        @Override
                        public void removeChangeListener(ChangeListener listener) {
                        }
                    };
                }
            }
        };
    }
}
