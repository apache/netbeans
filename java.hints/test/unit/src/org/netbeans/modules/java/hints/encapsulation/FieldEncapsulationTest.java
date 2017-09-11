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
 * @author tom
 */
public class FieldEncapsulationTest extends NbTestCase {

    public FieldEncapsulationTest(final String name) {
        super(name);
    }

    public void testPublic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:15-2:16:verifier:Public Field");
    }

    public void testProtected() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:18-2:19:verifier:Protected Field");
    }

    public void testPackage() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:8-2:9:verifier:Package Field");
    }

    public void testPrivate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPublicStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:22-2:23:verifier:Public Field");
    }

    public void testProtectedStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected static int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:25-2:26:verifier:Protected Field");
    }

    public void testPackageStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    static int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:15-2:16:verifier:Package Field");
    }

    public void testPrivateStatic() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPublicFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testProtectedFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPackageFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPrivateFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPublicStaticFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testProtectedStaticFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected static final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPackageStaticFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    static final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPrivateStaticFinal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testLocalField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void foo(int a) {\n" +
                       "        int b;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public interface Test {\n" +
                       "    int a = 10;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testEnum() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testFieldGroup() throws Exception {
        HintTest
                .create()
                .input("test/Galois.java",
                       "package test;\n" +
                       "public class Galois {\n" +
                       "    public int a, b, c;\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("2:15-2:16:verifier:Public Field");
    }

    public void testOtherPriviteField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class Inner {\n" +
                       "        private int a = 10;\n" +
                       "    }\n" +
                       "    public void test() {\n" +
                       "        new Inner().a = 10;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("6:20-6:21:verifier:Access of Private Field of Another Object");
    }

    public void testOtherPublicField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class Inner {\n" +
                       "        public int a = 10;\n" +
                       "    }\n" +
                       "    public void test() {\n" +
                       "        new Inner().a = 10;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("3:19-3:20:verifier:Public Field");
    }

    public void testOtherPriviteMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class Inner {\n" +
                       "        private Object a = null;\n" +
                       "    }\n" +
                       "    public void test() {\n" +
                       "        new Inner().a.hashCode();\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("6:20-6:21:verifier:Access of Private Field of Another Object");
    }

    public void testOtherPublicMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class Inner {\n" +
                       "        public Object a = null;\n" +
                       "    }\n" +
                       "    public void test() {\n" +
                       "        new Inner().a.hashCode();\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("3:22-3:23:verifier:Public Field");
    }

    public void testThisField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int a = 10;\n" +
                       "    public void test() {\n" +
                       "        this.a = 10;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testOutherThisField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int a = 10;\n" +
                       "    public class Inner {\n" +
                       "        public void test() {\n" +
                       "            Test.this.a = 10;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPrivateStaticField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static class Inner {\n" +
                       "        private static int a = 10;\n" +
                       "    }\n" +
                       "    public void test() {\n" +
                       "        Inner.a = 10;\n" +
                       "        new Inner().a = 10;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testPrivateCrossField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Inner inner = new Inner();\n" +
                       "    private static class Inner {\n" +
                       "        private int a;\n" +
                       "    }\n" +
                       "    private class Friend {\n" +
                       "        public void test () {\n" +
                       "            Test.this.inner.a = 10;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings("8:28-8:29:verifier:Access of Private Field of Another Object");
    }

    public void testLimitByEnclosing194543() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final class A {\n" +
                       "        public static int I;\n" +
                       "    }\n" +
                       "}")
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }

    public void testEnumIgnore() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public java.lang.annotation.RetentionPolicy r = null;\n" +
                       "}")
                .preference(FieldEncapsulation.ALLOW_ENUMS_KEY, true)
                .run(FieldEncapsulation.class)
                .assertWarnings();
    }
}
