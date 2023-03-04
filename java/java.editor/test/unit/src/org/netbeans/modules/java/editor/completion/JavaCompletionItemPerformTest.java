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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionItemPerformTest extends CompletionTestBase {

    public JavaCompletionItemPerformTest(String testName) {
        super(testName);
    }

    public void testFieldInitializer() throws Exception {
        performTest("FieldSet", 91, " = new HashS", "HashSet", "testFieldInitializer.pass2");
    }

    public void testAnnotation() throws Exception {
        performTest("Annotation", 27, "Se", "Set", "testAnnotation.pass2");
    }

    public void testCast1() throws Exception {
        performTest("Empty", 0,
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private t(Object o) {\n" +
                    "        if (o instanceof String) {\n" +
                    "            o.len",
                    "length",
                    "testCast1.pass");
    }

    public void testCast2() throws Exception {
        performTest("Empty", 0,
                    "package test;\n" +
                    "public class Test {\n" +
                    "    private t(Number n) {\n" +
                    "        if (n instanceof Integer) {\n" +
                    "            n.in",
                    "intValue",
                    "testCast2.pass");
    }

    public void testCast3() throws Exception {
        //calling an overridden method
        performTest("Empty", 0,
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public interface Base {\n" +
                    "         public void test() { }\n" +
                    "    }\n" +
                    "    public interface Int extends Base {\n" +
                    "    }\n" +
                    "    public static class Impl implements Int {\n" +
                    "         public void test() { }\n" +
                    "    }\n" +
                    "    private t(Int b) {\n" +
                    "        if (b instanceof Impl) {\n" +
                    "            b.tes",
                    "test",
                    "testCast3.pass");
    }

    public void testCast4() throws Exception {
        //calling an overridden method
        performTest("Empty", 0,
                    "package test;\n" +
                    "public class Test {\n" +
                    "    public interface Base {\n" +
                    "         public void test() { }\n" +
                    "    }\n" +
                    "    public interface Int extends Base {\n" +
                    "    }\n" +
                    "    public static class Impl implements Int {\n" +
                    "         public void test() { }\n" +
                    "    }\n" +
                    "    private t(Base b) {\n" +
                    "        if (b instanceof Impl) {\n" +
                    "            b.tes",
                    "test",
                    "testCast4.pass");
    }
}