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
package org.netbeans.modules.testng.ui.actions;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
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
    }

    public TestClassInfoTaskTest(String testName) {
        super(testName);
    }

    @BeforeSuite
    public void setupClass() throws Exception {
        super.setUp();
    }

    public void testCursorInMethod() throws Exception {
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(70);
        src.runUserActionTask(task, true);
        assertEquals("method", task.getMethodName());
        assertEquals("sample.pkg", task.getPackageName());
        assertEquals("Test", task.getClassName());
    }

    public void testCursorInClass() throws Exception {
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(42);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("Test", task.getClassName());
        assertEquals("sample.pkg", task.getPackageName());
    }

    public void testCursorInClass2() throws Exception {
        JavaSource src = JavaSource.forFileObject(getTestFO());
        TestClassInfoTask task = new TestClassInfoTask(0);
        src.runUserActionTask(task, true);
        assertNull(task.getMethodName());
        assertEquals("Test", task.getClassName());
        assertEquals("sample.pkg", task.getPackageName());
    }

    public void testCursorInClass3() throws Exception {
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
}
