/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.encapsulation;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Tomas Zezula
 */
public class ClassEncapsulationTest extends NbTestCase {

    public ClassEncapsulationTest(final String name) {
        super(name);
    }

    public void testPublic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:17-2:22:verifier:Public Inner Class");
    }

    public void testProtected() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:20-2:25:verifier:Protected Inner Class");
    }

    public void testPackage() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:10-2:15:verifier:Package Visible Inner Class");
    }

    public void testPrivate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void testPublicStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:24-2:29:verifier:Public Inner Class");
    }

    public void testProtectedStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected static class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:27-2:32:verifier:Protected Inner Class");
    }

    public void testPackageStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    static class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings("2:17-2:22:verifier:Package Visible Inner Class");
    }

    public void testPrivateStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static class Inner {}\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void testOuther() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}\n" +
                       "class Outher {}\n")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void testLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void foo() {\n" +
                       "        class Local {};\n" +
                       "    }\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void testLimitByEnclosing194543() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final class A {\n" +
                       "        class E {}\n" +
                       "    }\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void test197590() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private enum A {\n" +
                       "        E\n" +
                       "    }\n" +
                       "}")
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }

    public void testEnumIgnore() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public enum E {A}\n" +
                       "}")
                .preference(ClassEncapsulation.ALLOW_ENUMS_KEY, true)
                .run(ClassEncapsulation.class)
                .assertWarnings();
    }
}
