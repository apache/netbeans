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
package org.netbeans.modules.java.hints.control;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class RedundantConditionalTest {
    
    @Test
    public void testSimpleRedundantConditional() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        return i == 0 ? true : false;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:15-3:36:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i == 0;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testRedundantConditionalNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        return i == 0 ? false : true;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:15-3:36:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i != 0;\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testSimpleRedundantConditionalVar() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        r = i == 0 ? true : false;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("4:12-4:33:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r;\n" +
                              "        r = i == 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testSimpleRedundantConditionalVarNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        r = i == 0 ? false : true;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("4:12-4:33:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r;\n" +
                              "        r = i != 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testSimpleRedundantConditionalVarInit() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r = i == 0 ? true : false;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:20-3:41:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r = i == 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testSimpleRedundantConditionalVarInitNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r = i == 0 ? false : true;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:20-3:41:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r = i != 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
}
