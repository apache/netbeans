/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsMethodHintTest extends NbTestCase {

    public EqualsMethodHintTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings("2:19-2:25:verifier:ENC");

    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(String s) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testSimple3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return o instanceof Test;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testSimple4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        return o.getClass() == Test.class;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void test134255() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o);\n" +
                       "}\n", false)
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }

    public void testAnnotations() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"a\") public boolean equals(Object o) {\n" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings("2:42-2:48:verifier:ENC");

    }
    
    public void testClassIsInstance216498() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public boolean equals(Object o) {\n" +
                       "        if (!getClass().isInstance(o)) return false;" +
                       "        return true;" +
                       "    }" +
                       "}\n")
                .run(EqualsMethodHint.class)
                .assertWarnings();

    }
}
