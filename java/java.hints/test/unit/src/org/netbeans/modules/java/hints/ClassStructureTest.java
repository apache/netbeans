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
package org.netbeans.modules.java.hints;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Dusan Balek
 */
public class ClassStructureTest extends NbTestCase {

    public ClassStructureTest(String name) {
        super(name);
    }

    @Test
    public void testFinalClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("1:19-1:23:verifier:Class Test is declared final")
                .applyFix("Remove final modifier from the Test class declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "}");
    }

    @Test
    public void testFinalClassSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"FinalClass\")" +
                       "public final class Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Class Test is declared final");
    }

    @Test
    public void testFinalMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("2:22-2:26:verifier:Method test is declared final")
                .applyFix("Remove final modifier from the test method declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test() {\n" +
                              "    }\n" +
                              "}");
    }

    @Test
    public void testFinalMethodSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"FinalMethod\")" +
                       "    public final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Method test is declared final");
    }

    @Test
    public void testFinalPrivateMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("2:23-2:27:verifier:Private method test is declared final")
                .applyFix("Remove final modifier from the test method declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "    }\n" +
                              "}");
    }

    @Test
    public void testFinalPrivateMethodSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"FinalPrivateMethod\")" +
                       "    private final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Private method test is declared final");
    }

    @Test
    public void testFinalStaticMethodSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"FinalStaticMethod\")" +
                       "    static final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Static method test is declared final");
    }

    @Test
    public void testFinalMethodInFinalClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "    public final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("2:22-2:26:verifier:Method test is declared final in final class")
                .applyFix("Remove final modifier from the test method declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public final class Test {\n" +
                              "    public void test() {\n" +
                              "    }\n" +
                              "}");
    }

    @Test
    public void testFinalMethodInFinalClassSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "    @SuppressWarnings(\"FinalMethodInFinalClass\")" +
                       "    public final void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Method test is declared final in final class");
    }

    @Test
    public void testNoopMethodInAbstractClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertContainsWarnings("2:16-2:20:verifier:No-op method test should be made abstract");
    }

    @Test
    public void testNoopMethodInAbstractClassSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    @SuppressWarnings(\"NoopMethodInAbstractClass\")" +
                       "    public void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("No-op method test should be made abstract");
    }

    @Test
    public void testPublicConstructorInNonPublicClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public Test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("2:11-2:15:verifier:Constructor is declared public in non-public class")
                .applyFix("Remove public modifier from the constructor declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "class Test {\n" +
                              "    Test() {\n" +
                              "    }\n" +
                              "}");
    }

    @Test
    public void testPublicConstructorInNonPublicClassSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    @SuppressWarnings(\"PublicConstructorInNonPublicClass\")" +
                       "    public Test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Constructor is declared public in non-public class");
    }

    @Test
    public void testProtectedMemberInFinalClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "    protected void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("2:19-2:23:verifier:Method test is declared protected in final class")
                .applyFix("Remove protected modifier from the test method declaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public final class Test {\n" +
                              "    void test() {\n" +
                              "    }\n" +
                              "}");
    }

    @Test
    public void testProtectedMemberInFinalClassSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "    @SuppressWarnings(\"ProtectedMemberInFinalClass\")" +
                       "    protected void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Method test is declared protected in final class");
    }

    @Test
    public void testProtectedMemberInFinalClass181723() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"FinalClass\") public final class Test extends A {\n" +
                       "    protected void test() {\n" +
                       "    }\n" +
                       "}\n" +
                       "@SuppressWarnings(\"MultipleTopLevelClassesInFile\") class A {\n" +
                       "    protected void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertWarnings();
    }

    @Test
    public void testMarkerInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public interface Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertContainsWarnings("1:17-1:21:verifier:Marker interface Test");
    }

    @Test
    public void testMarkerInterfaceSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"MarkerInterface\")" +
                       "public interface Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Marker interface Test");
    }

    @Test
    public void testClassMayBeInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .findWarning("1:13-1:17:verifier:Class Test may be interface")
                .applyFix("Convert class Test to interface")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public interface Test {\n" +
                              "}");
    }

    @Test
    public void testClassMayBeInterfaceSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"ClassMayBeInterface\")" +
                       "public class Test {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Class Test may be interface");
    }

    @Test
    public void testClassMayBeInterfaceComplexCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public static final int i = 10;\n" +
                       "    public abstract void test();\n" +
                       "}")
                .run(ClassStructure.class)
                .assertContainsWarnings("1:22-1:26:verifier:Class Test may be interface");
    }

    @Test
    public void testClassMayBeInterfaceComplexCase2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public int i = 10;\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Class Test may be interface");
    }

    @Test
    public void testClassMayBeInterfaceComplexCase3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public void test() {\n" +
                       "    }\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Class Test may be interface");
    }

    @Test
    public void testMultipleTopLevelClassesInFile() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}\n" +
                       "class Second {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertContainsWarnings("3:6-3:12:verifier:Multiple top-level classes in file");
    }

    @Test
    public void testMultipleTopLevelClassesInFileSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}\n" +
                       "@SuppressWarnings(\"MultipleTopLevelClassesInFile\")" +
                       "class Second {\n" +
                       "}")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Multiple top-level classes in file");
    }

    @Test
    public void testMultipleTopLevelClassesInFileExtraSemiColon200476() throws Exception {
        HintTest
                .create()
                .input("package test;\n;\n" +
                       "public class Test {\n" +
                       "}\n")
                .run(ClassStructure.class)
                .assertNotContainsWarnings("Multiple top-level classes in file");
    }
}