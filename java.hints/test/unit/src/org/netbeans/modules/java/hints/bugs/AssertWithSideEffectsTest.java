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
public class AssertWithSideEffectsTest extends NbTestCase {

    public AssertWithSideEffectsTest(String name) {
        super(name);
    }
    
    public void testDirectAssigns() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    int field;\n" +
                "    void test1() {\n" +
                "        int var = 2;\n" +
                "        assert (var = 1) > 0 : \"ble\";\n" +
                "        assert (var += 2) > 1 : \"fuj\";\n" +
                "        assert (field = 1) > 0 : \"cune\";\n" +
                "        assert (field-- > 0) : \"nemehlo\";\n" +
                "        assert (Math.min(var = var +3, 7)) > 0 : \"truhlik\";\n" +
                "    }\n" +
                "}"
                )
                .run(AssertWithSideEffects.class).
                assertWarnings(
                    "5:16-5:19:verifier:Assert condition produces side effects", 
                    "6:16-6:19:verifier:Assert condition produces side effects", 
                    "7:16-7:21:verifier:Assert condition produces side effects", 
                    "8:16-8:23:verifier:Assert condition produces side effects",
                    "9:25-9:28:verifier:Assert condition produces side effects"
                );
    }
    
    public void testAssignFromSelfMethod() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    int field;\n" +
                "    void test1() {\n" +
                "        int var = 2;\n" +
                "        assert m1() : \"ble\";\n" +
                "        assert m2() : \"fuj\";\n" +
                "        assert m3() : \"cune\"; \n" +
                "        assert m4() : \"ok\";\n" +
                "        assert m5() : \"eek\";\n" +
                "        assert m6() : \"ook\";\n" +
                "    } \n" +
                "    boolean m1() {\n" +
                "        field = 2;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m2() { \n" +
                "        field--;\n" +
                "        return true;\n" +
                "        \n" +
                "    }\n" +
                "    boolean m3() {\n" +
                "        field -= 2;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m4() {\n" +
                "        int var = 1;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m5() {\n" +
                "        class Local {\n" +
                "            void m() {\n" +
                "                field = 3;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "   }\n" +
                "    boolean m6() {\n" +
                "        class Local {\n" +
                "            int field;\n" +
                "            void m() {\n" +
                "                field = 3;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "    }\n" +
                "}"
                )
                .run(AssertWithSideEffects.class).
                assertWarnings(
                    "5:15-5:19:verifier:Assert condition produces side effects",
                    "6:15-6:19:verifier:Assert condition produces side effects", 
                    "7:15-7:19:verifier:Assert condition produces side effects", 
                    "9:15-9:19:verifier:Assert condition produces side effects"
                );
    }

}
