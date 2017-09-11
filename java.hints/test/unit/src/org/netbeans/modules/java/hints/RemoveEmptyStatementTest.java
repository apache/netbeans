/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree;
import org.junit.Test;
import static org.netbeans.modules.java.hints.EmptyStatements.getDisplayName;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author markiewb
 */
public class RemoveEmptyStatementTest {

    @Test
    public void testRemoveEmptyBLOCK() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"hello world\");;\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:42-3:43:verifier:" + getDisplayName(Tree.Kind.BLOCK))
                .applyFix(Bundle.ERR_EmptyBLOCK())
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"hello world\");\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testRemoveEmptyWHILE_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        while(true);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:20:verifier:" + getDisplayName(Tree.Kind.WHILE_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyFOR_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        for(int i=0;i<10;i++);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:30:verifier:" + getDisplayName(Tree.Kind.FOR_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyENHANCED_FOR_LOOP() throws Exception {

        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        java.util.List<String> list = new java.util.ArrayList<String>();"
                        + "        for(String s:list);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:80-3:99:verifier:" + getDisplayName(Tree.Kind.ENHANCED_FOR_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyDO_WHILE_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        do;while(true);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:23:verifier:" + getDisplayName(Tree.Kind.DO_WHILE_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyIF() throws Exception {
        final String ifWarn = "3:8-4:13:verifier:" + getDisplayName(Tree.Kind.IF);
        final String elseWarn = "3:8-4:13:verifier:" + getDisplayName(Tree.Kind.IF);
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if (\"1\".equals(\"1\")); \n"
                        + "        else;"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .assertWarnings(ifWarn, elseWarn)
                .findWarning(ifWarn)
                .assertFixes();

        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if (\"1\".equals(\"1\")); \n"
                        + "        else;"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .assertWarnings(ifWarn, elseWarn)
                .findWarning(elseWarn)
                .assertFixes();
    }

}
