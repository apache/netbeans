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
public class RemoveTagFixTest extends NbTestCase {

    public RemoveTagFixTest(String name) {
        super(name);
    }
    
    public void testRemoveReturnTagFixFirstSentence() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * It is always cold in winter.\n" +
                "     * \n" +
                "     * @return \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:7-5:14:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * It is always cold in winter.\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveReturnTagFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:14:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testRemoveReturnTagFixInLine() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /** @return bla */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("2:8-2:19:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /** */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveReturnTagFixHeaderLine() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /** @return bla\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("2:8-2:19:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /** \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveReturnTagFixTailLine() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return bla */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:18:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveReturnTagAfterParamFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     * @return \n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:14:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveMultilineReturnTagAfterParamFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     * @return bla bla\n" +
                "     *         bla bla bla bla\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-5:30:warning:@return tag cannot be used in method with void return type.")
                .applyFix("Remove @return tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * @param a param a\n" +
                "     */\n" +
                "    void leden(int a) {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveThrowsTagFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @throws java.io.IOException bla\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:38:warning:Unknown throwable: @throws java.io.IOException")
                .applyFix("Remove @throws tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testRemoveFromClass() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "/**\n" +
                " * \n" +
                " * @param bla\n" +
                " */\n" +
                "class Zima {\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("3:3-3:13:warning:@param tag cannot be used on CLASS.")
                .applyFix("Remove @param tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "/**\n" +
                " * \n" +
                " */\n" +
                "class Zima {\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testRemoveDuplicateThrowsTagFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @throws java.io.IOException bla\n" +
                "     * @throws java.io.IOException bla\n" +
                "     */\n" +
                "    void leden() throws java.io.IOException {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:7-5:38:warning:Duplicate @throws tag: java.io.IOException")
                .applyFix("Remove @throws tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @throws java.io.IOException bla\n" +
                "     */\n" +
                "    void leden() throws java.io.IOException {\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testRemoveDuplicateParamTagFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     * @param p1 description\n" +
                "     */\n" +
                "    void leden(int p1) {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("5:7-5:28:warning:Duplicate @param name: p1")
                .applyFix("Remove @param tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     */\n" +
                "    void leden(int p1) {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveParamTagFix() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:28:warning:Unknown @param: p1")
                .applyFix("Remove @param tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     */\n" +
                "    void leden() {\n" +
                "    }\n" +
                "}\n");
    }

    public void testRemoveParamTagFix_124353() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @param p1 description\n" +
                "     * @return int\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n")
                .preference(AVAILABILITY_KEY + true, true)
                .preference(SCOPE_KEY, "private")
                .run(JavadocHint.class)
                .findWarning("4:7-4:28:warning:Unknown @param: p1")
                .applyFix("Remove @param tag")
                .assertCompilable()
                .assertOutput("package test;\n" +
                "class Zima {\n" +
                "    /**\n" +
                "     * \n" +
                "     * @return int\n" +
                "     */\n" +
                "    int leden() {\n" +
                "        return 0;\n" +
                "    }\n" +
                "}\n");
    }

}
