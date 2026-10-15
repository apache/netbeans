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
public class AddTagFixTest extends NbTestCase {

    public AddTagFixTest(String name) {
        super(name);
    }
    
    public void testInherited() throws Exception {
        HintTest.create()
                .input("package test;\n"
                    + "import java.io.IOException;\n"
                    + "public class Test implements CustomFileReader {\n"
                    + "    /**\n"
                    + "     * {@inheritDoc}\n"
                    + "     */\n"
                    + "    @Override\n"
                    + "    public String readFile(String path) throws IOException {\n"
                    + "\n"
                    + "        System.out.println(\"Fake reading file [\" + path + \"]\");\n"
                    + "        return \"\";\n"
                    + "    }\n"
                    + "}\n")
                .input("test/CustomFileReader.java", "package test;\n"
                    + "import java.io.IOException;\n"
                    + "\n"
                    + "public interface CustomFileReader {\n"
                    + "\n"
                    + "    /**\n"
                    + "     * Experimental operation to test Javadoc hints in NetBeans-8.0-RC1\n"
                    + "     * @param path file path\n"
                    + "     * @throws IOException when file cannot be read\n"
                    + "     * @return String the file contents\n"
                    + "     */\n"
                    + "    public String readFile(String path) throws IOException;\n"
                    + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .assertNotContainsWarnings("Missing @throws tag for java.io.IOException");
    }

    public void testAddReturnTagFixInEmptyJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:4-5:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddReturnTagFix() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * bla\n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:4-5:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * bla\n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddReturnTagFix2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla\n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:4-4:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla\n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void DISABLE_testAddReturnTagFixInEmpty1LineJavadoc() throws Exception { //JDK-8312093
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /***/\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:4-3:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:4-3:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla\n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /** @since 1.1 */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:4-3:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @return \n"
                + "     * @since 1.1 */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddReturnTagFixIn1LineJavadoc3() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla {@link nekam} */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:4-3:7:warning:Missing @return tag.")
                .applyFix("Add @return tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /** bla {@link nekam}\n"
                + "     * @return \n"
                + "     */\n"
                + "    int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddParamTagFixInEmptyJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    void leden(int prvniho) {\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:15-5:26:warning:Missing @param tag for prvniho")
                .applyFix("Add @param prvniho tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param prvniho \n"
                + "     */\n"
                + "    void leden(int prvniho) {\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddParamTagFixWithReturn() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @return bla\n"
                + "     */\n"
                + "    int leden(int prvniho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:14-5:25:warning:Missing @param tag for prvniho")
                .applyFix("Add @param prvniho tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param prvniho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    int leden(int prvniho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddParamTagFixWithReturn_115974() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @return bla */\n"
                + "    int leden(int prvniho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:14-4:25:warning:Missing @param tag for prvniho")
                .applyFix("Add @param prvniho tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param prvniho \n"
                + "     * @return bla */\n"
                + "    int leden(int prvniho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddParamTagFixAndParamOrder() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param prvniho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    int leden(int prvniho, int druheho, int tretiho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("7:27-7:38:warning:Missing @param tag for druheho")
                .applyFix("Add @param druheho tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param prvniho \n"
                + "     * @param druheho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    int leden(int prvniho, int druheho, int tretiho) {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddTypeParamTagFixInEmptyJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    <T> void leden() {\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:5-5:6:warning:Missing @param tag for <T>")
                .applyFix("Add @param <T> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param <T> \n"
                + "     */\n"
                + "    <T> void leden() {\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddTypeParamTagFixInEmptyClassJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "/**\n"
                + " * \n"
                + " */\n"
                + "class Zima<T> {\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:11-4:12:warning:Missing @param tag for <T>")
                .applyFix("Add @param <T> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "/**\n"
                + " * \n"
                + " * @param <T> \n"
                + " */\n"
                + "class Zima<T> {\n"
                + "}\n");
    }

    public void testAddTypeParamTagFixInClassJavadoc() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "/**\n"
                + " * @param <Q> \n"
                + " */\n"
                + "class Zima<P,Q> {\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:11-4:12:warning:Missing @param tag for <P>")
                .applyFix("Add @param <P> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "/**\n"
                + " * @param <P> \n"
                + " * @param <Q> \n"
                + " */\n"
                + "class Zima<P,Q> {\n"
                + "}\n");
    }

    public void testAddTypeParamTagFixWithReturn() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T> int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:5-5:6:warning:Missing @param tag for <T>")
                .applyFix("Add @param <T> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param <T> \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T> int leden() {\n"
                + "        return 0;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddTypeParamTagFixAndParamOrder() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param prvniho \n"
                + "     * @param druheho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T> T leden(int prvniho, int druheho, T tretiho) {\n"
                + "        return tretiho;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("8:5-8:6:warning:Missing @param tag for <T>")
                .applyFix("Add @param <T> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param <T> \n"
                + "     * @param prvniho \n"
                + "     * @param druheho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T> T leden(int prvniho, int druheho, T tretiho) {\n"
                + "        return tretiho;\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testAddTypeParamTagFixClashAndParamOrder() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param <T> \n"
                + "     * @param prvniho \n"
                + "     * @param druheho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T,S> T leden(int prvniho, int druheho, T tretiho) {\n"
                + "        return tretiho;\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("9:7-9:8:warning:Missing @param tag for <S>")
                .applyFix("Add @param <S> tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * @param <T> \n"
                + "     * @param <S> \n"
                + "     * @param prvniho \n"
                + "     * @param druheho \n"
                + "     * @param tretiho \n"
                + "     * @return bla\n"
                + "     */\n"
                + "    <T,S> T leden(int prvniho, int druheho, T tretiho) {\n"
                + "        return tretiho;\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddThrowsTagFix() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    void leden() throws java.io.IOException {\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:24-5:43:warning:Missing @throws tag for java.io.IOException")
                .applyFix("Add @throws java.io.IOException tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @throws java.io.IOException \n"
                + "     */\n"
                + "    void leden() throws java.io.IOException {\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddThrowsTagFix2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "import java.io.IOException;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    void leden() throws IOException {\n"
                + "    }\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("6:24-6:35:warning:Missing @throws tag for java.io.IOException")
                .applyFix("Add @throws java.io.IOException tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "import java.io.IOException;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @throws java.io.IOException \n"
                + "     */\n"
                + "    void leden() throws IOException {\n"
                + "    }\n"
                + "}\n");
    }

    public void testAddThrowsTagFix_NETBEANS_1615() throws Exception {
        // issue NETBEANS-1615
        HintTest.create()
                .input(
                "package test;\n"
                + "interface Zima {\n"
                + "    /**\n"
                + "     */\n"
                + "    <X extends Exception> void leden() throws X;\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:46-4:47:warning:Missing @throws tag for X")
                .applyFix("Add @throws X tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "interface Zima {\n"
                + "    /**\n"
                + "     * @throws X\n"
                + "     */\n"
                + "    <X extends Exception> void leden() throws X;\n"
                + "}\n");
    }

    public void testAddThrowsTagFix_NestedClass_160414() throws Exception {
        // issue 160414
        HintTest.create()
                .input(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     */\n"
                + "    void leden() throws MEx {\n"
                + "    }\n"
                + "    public static class MEx extends Exception {}\n"
                + "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:24-5:27:warning:Missing @throws tag for test.Zima.MEx")
                .applyFix("Add @throws test.Zima.MEx tag")
                .assertCompilable()
                .assertOutput(
                "package test;\n"
                + "class Zima {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @throws test.Zima.MEx \n"
                + "     */\n"
                + "    void leden() throws MEx {\n"
                + "    }\n"
                + "    public static class MEx extends Exception {}\n"
                + "}\n");
    }

    public void testAddTagMarkdown() throws Exception {
        // issue 160414
        HintTest.create()
                .input("""
                       package test;
                       public class Test {
                           ///
                           /// @param p1 param1
                           ///
                           public void leden(int p1, int p2) {
                           }
                       }
                       """)
                .sourceLevel("23")
                .run(JavadocHint.class)
                .findWarning("5:30-5:36:warning:Missing @param tag for p2") //TODO: test branding
                .applyFix("Add @param p2 tag")
                .assertCompilable()
                .assertOutput("""
                              package test;
                              public class Test {
                                  ///
                                  /// @param p1 param1
                                  /// @param p2
                                  ///
                                  public void leden(int p1, int p2) {
                                  }
                              }
                              """);
    }
}
