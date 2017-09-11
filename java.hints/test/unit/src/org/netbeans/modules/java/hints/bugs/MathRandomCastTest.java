/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class MathRandomCastTest extends NbTestCase {

    public MathRandomCastTest(String name) {
        super(name);
    }

    public void testCastAndCompute() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = ((int)Math.random() + 3) * 4;\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:19-3:37:verifier:Math.int() immediately casted to int or long")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                ("package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int) ((Math.random() + 3) * 4);\n" +
                "    }\n" +
                "}\n"
                ).replaceAll("\\s+", " "));
    }
    
    /**
     * Checks the expression is correctly parenthesized in the
     * @throws Exception 
     */
    public void testCastMultiExpression() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int)Math.random() * 4 + 3;\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:18-3:36:verifier:Math.int() immediately casted to int or long")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                ("package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int) (Math.random() * 4 + 3);\n" +
                "    }\n" +
                "}\n"
                ).replaceAll("\\s+", " "));
    }
    
    /**
     * Checks that the warning is presented, but no fix is offered
     * @throws Exception 
     */
    public void testCastInAssignment() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int)Math.random();\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:18-3:36:verifier:Math.int() immediately casted to int or long")
                .assertFixes();
    }
}
