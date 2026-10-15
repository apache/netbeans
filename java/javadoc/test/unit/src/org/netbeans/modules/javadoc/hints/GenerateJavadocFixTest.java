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

package org.netbeans.modules.javadoc.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import static org.netbeans.modules.javadoc.hints.JavadocHint.AVAILABILITY_KEY;
import static org.netbeans.modules.javadoc.hints.JavadocHint.SCOPE_KEY;

/**
 *
 * @author Jan Pokorsky
 * @author Ralph Benjamin Ruijs
 */
public class GenerateJavadocFixTest extends NbTestCase {

    public GenerateJavadocFixTest(String name) {
        super(name);
    }

    public void testGenerateMethodJavadoc() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "import java.io.IOException;\n" +
                "public class Test {\n" +
                "    @Deprecated public <T> int le|den(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n")
                .run(JavadocHint.class)
                .findWarning("3:31-3:36:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for leden")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.io.IOException;\n" +
                "public class Test {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @param <T>\n" +
                "     * @param param1\n" +
                "     * @param param2\n" +
                "     * @param param3\n" +
                "     * @return\n" +
                "     * @throws IOException\n" +
                "     * @throws IllegalArgumentException\n" +
                "     * @throws FileNotFoundException\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated public <T> int leden(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");    
    }
    
    public void testGenerateConstructorJavadoc() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "import java.io.IOException;\n" +
                "class Test {\n" +
                "    @Deprecated <T> Te|st(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:20-3:24:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "import java.io.FileNotFoundException;\n" +
                "import java.io.IOException;\n" +
                "class Test {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @param <T>\n" +
                "     * @param param1\n" +
                "     * @param param2\n" +
                "     * @param param3\n" +
                "     * @throws IOException\n" +
                "     * @throws IllegalArgumentException\n" +
                "     * @throws FileNotFoundException\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated <T> Test(int param1, int param2, T param3) throws IOException, IllegalArgumentException, java.io.FileNotFoundException {\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testGenerateClassJavadoc() throws Exception {
        System.setProperty("user.name", "Alois");
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "@Deprecated class Zi|ma<P,Q> {\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("1:18-1:22:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for Zima")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "/**\n" +
                " *\n" +
                " * @author Alois\n" +
                " * @param <P>\n" +
                " * @param <Q>\n" +
                " * @deprecated\n" +
                " */\n" +
                "@Deprecated class Zima<P,Q> {\n" +
                "}\n");
    }
    
    public void testGenerateFieldJavadoc() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int le|den;\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:8-3:13:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for leden")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    @Deprecated\n" +
                "    int leden;\n" +
                "}\n");
    }
    
    public void testGenerateFieldGroupJavadoc() throws Exception { //#213499
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int leden, un|or;\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:15-3:19:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for unor")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    @Deprecated\n" +
                "    int leden,\n" +
                "    /**\n" +
                "     *\n" +
                "     * @deprecated\n" +
                "     */\n" +
                "    unor;\n" +
                "}\n");
    }
    
    public void testGenerateEnumConstantJavadoc_124114() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "enum Zima {LE|DEN, UNOR}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("1:11-1:16:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for LEDEN")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "enum Zima {\n" +
                "    /**\n" +
                "     *\n" +
                "     */\n" +
                "    LEDEN, UNOR}\n");
    }
    
    public void testGenerateEnumConstantJavadoc_124114b() throws Exception {
       HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                "enum Zima {LEDEN, UN|OR}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("1:18-1:22:hint:Missing javadoc.")
                .applyFix("Create missing javadoc for UNOR")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "enum Zima {LEDEN,\n" +
                "    /**\n" +
                "     *\n" +
                "     */\n" +
                "    UNOR}\n");
    }

    public void testMarkdown1() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       import java.io.IOException;
                       /// Test
                       public class Test {
                           public int map(int p1, int p2) throws IOException {
                               return -1;
                           }
                       }
                       """)
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertWarnings("4:15-4:18:hint:Missing javadoc.")
                .findWarning("4:15-4:18:hint:Missing javadoc.")
                .applyFix()
                .assertCompilable()
                .assertOutput("""
                              package test;
                              import java.io.IOException;
                              /// Test
                              public class Test {

                                  ///
                                  /// @param p1
                                  /// @param p2
                                  /// @return
                                  /// @throws IOException
                                  public int map(int p1, int p2) throws IOException {
                                      return -1;
                                  }
                              }
                              """);
    }

    public void testMarkdown2() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       import java.io.IOException;
                       /// Test
                       public class Test {

                           ///
                           /// @param p1
                           /// @param p2
                           /// @return
                           /// @throws IOException
                           public int map(int p1, int p2) throws IOException {
                               return -1;
                           }
                       }
                       """)
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertWarnings();
    }

    public void testMarkdown3() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       import java.io.IOException;
                       /** Not markdown */
                       public class Test {
                           public int map(int p1, int p2) throws IOException {
                               return -1;
                           }
                           ///markdown
                           public void test() {}
                       }
                       """)
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertWarnings("4:15-4:18:hint:Missing javadoc.")
                .findWarning("4:15-4:18:hint:Missing javadoc.")
                .applyFix()
                .assertCompilable()
                .assertOutput("""
                              package test;
                              import java.io.IOException;
                              /** Not markdown */
                              public class Test {

                                  ///
                                  /// @param p1
                                  /// @param p2
                                  /// @return
                                  /// @throws IOException
                                  public int map(int p1, int p2) throws IOException {
                                      return -1;
                                  }
                                  ///markdown
                                  public void test() {}
                              }
                              """);
    }
}
