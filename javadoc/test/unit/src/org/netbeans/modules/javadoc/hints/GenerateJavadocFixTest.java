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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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

}
