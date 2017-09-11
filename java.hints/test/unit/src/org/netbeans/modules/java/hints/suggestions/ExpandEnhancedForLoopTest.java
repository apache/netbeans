/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class ExpandEnhancedForLoopTest extends NbTestCase {

    public ExpandEnhancedForLoopTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        fo|r (String s : java.util.Arrays.asList(\"a\")) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        for (Iterator<String> it = java.util.Arrays.asList(\"a\").iterator(); it.hasNext();) {" +
                              "            String s = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNoBlock() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l)\n" +
                       "            System.err.println(c);\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "            System.err.println(c);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testEmptyStatement() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l);\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNegative() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        fo|r (String s : new Object()) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(ExpandEnhancedForLoop.class)
                .assertWarnings();
    }
}