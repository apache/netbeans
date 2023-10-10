/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.testng.ui.actions;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 *
 * @author lukas
 */
@Test
public class TestClassInfoTaskTest extends RetoucheTestBase {

    static {
        TestClassInfoTask.ANNOTATION = "java.lang.Deprecated";
        TestClassInfoTask.TESTNG_ANNOTATION_PACKAGE = "java.lang";
    }

    private static final String DEFAULT_TEST_DATA =
        "package sample.pkg;\n" +
        "\n" +
        "public class Test {\n" +
        "\n" +
        "    @Deprecated\n" +
        "    void method() {\n" +
        "    }\n" +
        "\n" +
        "}\n";

    public TestClassInfoTaskTest(String testName) {
        super(testName);
    }

    @BeforeSuite
    public void setupClass() throws Exception {
        super.setUp();
    }

    public void testCursorInMethod() throws Exception {
        TestUtilities.copyStringToFile(getTestFO(), DEFAULT_TEST_DATA);
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(70);
        src.runUserActionTask(task, true);
        assertEquals("method", task.getMethodName());
        assertEquals("sample.pkg", task.getPackageName());
        assertEquals("Test", task.getClassName());
    }

    public void testCursorInClass() throws Exception {
        TestUtilities.copyStringToFile(getTestFO(), DEFAULT_TEST_DATA);
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(42);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("Test", task.getClassName());
        assertEquals("sample.pkg", task.getPackageName());
    }

    public void testCursorInClass2() throws Exception {
        TestUtilities.copyStringToFile(getTestFO(), DEFAULT_TEST_DATA);
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(0);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("Test", task.getClassName());
        assertEquals("sample.pkg", task.getPackageName());
    }

    public void testCursorInClass3() throws Exception {
        TestUtilities.copyStringToFile(getTestFO(), DEFAULT_TEST_DATA);
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(87);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("Test", task.getClassName());
        assertEquals("sample.pkg", task.getPackageName());
    }

    public void testDefaultPackage() throws Exception {
        TestUtilities.copyStringToFile(getTestFO(),
                "public class Test {\n" +
                "\n" +
                "    @Deprecated\n" +
                "    void method() {\n" +
                "    }\n" +
                "\n" +
                "}\n");
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(20);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("", task.getPackageName());
        assertEquals("Test", task.getClassName());
    }

    public void testClassAnnotated1() throws Exception {
        String code = "package test;\n" +
                      "@Deprecated\n" +
                      "public class Test {\n" +
                      "    public void method1() {\n" +
                      "        //test1\n" +
                      "    }\n" +
                      "    @SuppressWarnings(\"\")\n" +
                      "    public void method2() {\n" +
                      "        //test2\n" +
                      "    }\n" +
                      "    void method3() {\n" +
                      "        //test3\n" +
                      "    }\n" +
                      "    public static void method4() {\n" +
                      "        //test4\n" +
                      "    }\n" +
                      "}\n";
        TestUtilities.copyStringToFile(getTestFO(), code);
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task1 = new TestClassInfoTask(code.indexOf("//test1"));
        src.runUserActionTask(task1, true);
        assertEquals("method1", task1.getMethodName());
        assertEquals("test", task1.getPackageName());
        assertEquals("Test", task1.getClassName());
        TestClassInfoTask task2 = new TestClassInfoTask(code.indexOf("//test2"));
        src.runUserActionTask(task2, true);
        assertNull(task2.getMethodName());
        assertEquals("test", task2.getPackageName());
        assertEquals("Test", task2.getClassName());
        TestClassInfoTask task3 = new TestClassInfoTask(code.indexOf("//test3"));
        src.runUserActionTask(task3, true);
        assertNull(task3.getMethodName());
        assertEquals("test", task3.getPackageName());
        assertEquals("Test", task3.getClassName());
        TestClassInfoTask task4 = new TestClassInfoTask(code.indexOf("//test4"));
        src.runUserActionTask(task4, true);
        assertEquals("method4", task4.getMethodName());
        assertEquals("test", task4.getPackageName());
        assertEquals("Test", task4.getClassName());
        src.runUserActionTask(cc -> {
            cc.toPhase(Phase.ELEMENTS_RESOLVED);
            assertEquals(2, TestClassInfoTask.computeTestMethods(cc, new AtomicBoolean(), -1).size());
        }, true);
    }
}
