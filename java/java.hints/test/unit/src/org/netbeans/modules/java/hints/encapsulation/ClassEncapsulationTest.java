/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
