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
package org.netbeans.modules.java.hints.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result.Item;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result.Kind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class PatternGeneratorTest extends TestBase {

    public PatternGeneratorTest(String testName) {
        super(testName);
    }

    public void testTest() throws Exception {
        //TODO: what if the outcome would not compile (see test3 and change Object to String).
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "    private Object test3(char[] val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "    private Object test4(char[] val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "    private Object test5(String val) {\n" +
                                  "        return new String(val.substring(1, 2));\n" +
                                  "    }\n" +
                                  "    private Object test6(String val) {\n" +
                                  "        return new String(val.substring(1, 2));\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                 "    private String test1(String str) {\n" +
                                 "        return str;\n" +
                                 "    }\n" +
                                 "    private String test2(String val) {\n" +
                                 "        return new String(val);\n" +
                                 "    }\n" +
                                 "    private Object test3(char[] val) {\n" +
                                 "        return new String(val);\n" +
                                 "    }\n" +
                                 "    private Object test4(char[] val) {\n" +
                                 "        return new String(val);\n" +
                                 "    }\n" +
                                 "    private Object test5(String val) {\n" +
                                 "        return new String(val.substring(1, 2));\n" +
                                 "    }\n" +
                                 "    private Object test6(String val) {\n" +
                                 "        return new String(val.substring(1, 2));\n" +
                                 "    }\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2)
                        .assertAllChecked()
                        .markNegative("test/Test.java",
                                      "package test;\n" +
                                      "public class Test {\n" +
                                      "    private String test1(String str) {\n" +
                                      "        return str;\n" +
                                      "    }\n" +
                                      "    private String test2(String val) {\n" +
                                      "        return new String(val);\n" +
                                      "    }\n" +
                                      "    private Object test3(char[] val) {\n" +
                                      "        return val;\n" +
                                      "    }\n" +
                                      "    private Object test4(char[] val) {\n" +
                                      "        return new String(val);\n" +
                                      "    }\n" +
                                      "    private Object test5(String val) {\n" +
                                      "        return new String(val.substring(1, 2));\n" +
                                      "    }\n" +
                                      "    private Object test6(String val) {\n" +
                                      "        return new String(val.substring(1, 2));\n" +
                                      "    }\n" +
                                      "}\n")
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 3)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 3)
                        .assertAllChecked()
                        .markNegative("test/Test.java",
                                      "package test;\n" +
                                      "public class Test {\n" +
                                      "    private String test1(String str) {\n" +
                                      "        return str;\n" +
                                      "    }\n" +
                                      "    private String test2(String val) {\n" +
                                      "        return new String(val);\n" +
                                      "    }\n" +
                                      "    private Object test3(char[] val) {\n" +
                                      "        return new String(val);\n" +
                                      "    }\n" +
                                      "    private Object test4(char[] val) {\n" +
                                      "        return new String(val);\n" +
                                      "    }\n" +
                                      "    private Object test5(String val) {\n" +
                                      "        return val.substring(1, 2);\n" +
                                      "    }\n" +
                                      "    private Object test6(String val) {\n" +
                                      "        return new String(val.substring(1, 2));\n" +
                                      "    }\n" +
                                      "}\n")
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return val;\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "    private String test2(String val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test3(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test4(char[] val) {\n" +
                                "        return new String(val);\n" +
                                "    }\n" +
                                "    private Object test5(String val) {\n" +
                                "        return new String(val.substring(1, 2));\n" +
                                "    }\n" +
                                "    private Object test6(String val) {\n" +
                                "        return val.substring(1, 2);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                .assertAllChecked();
    }

    public void testMethodChange() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private void test1() {\n" +
                                  "        a();\n" +
                                  "    }\n" +
                                  "    private java.net.URL test2(java.io.File f) {\n" +
                                  "        a();\n" +
                                  "    }\n" +
                                  "    private void a() {}\n" +
                                  "    private void b() {}\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                 "    private void test1() {\n" +
                                 "        b();\n" +
                                 "    }\n" +
                                 "    private java.net.URL test2(java.io.File f) {\n" +
                                 "        a();\n" +
                                 "    }\n" +
                                 "    private void a() {}\n" +
                                 "    private void b() {}\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                "    private void test1() {\n" +
                                "        b();\n" +
                                "    }\n" +
                                "    private java.net.URL test2(java.io.File f) {\n" +
                                "        b();\n" +
                                "    }\n" +
                                "    private void a() {}\n" +
                                "    private void b() {}\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testMultipleFiles() throws Exception {
        new TestBuilder().addFile("test/Test1.java",
                                  "package test;\n" +
                                  "public class Test1 {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "}\n")
                         .addFile("test/Test2.java",
                                  "package test;\n" +
                                  "public class Test2 {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test1.java",
                                  "package test;\n" +
                                  "public class Test1 {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return str;\n" +
                                  "    }\n" +
                                  "}\n")
                        .update()
                        .expect("test/Test2.java",
                                "package test;\n" +
                                "public class Test2 {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str;\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testStaticMethods1() throws Exception {
        new TestBuilder().addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import static java.lang.Integer.toHexString;\n" +
                                  "public class Test1 {\n" +
                                  "    private String test1(Integer i) {\n" +
                                  "        return Integer.toHexString(i);\n" +
                                  "    }\n" +
                                  "    private String test2(Integer i) {\n" +
                                  "        return toHexString(i);\n" +
                                  "    }\n" +
                                  "    private String test3(Long l) {\n" +
                                  "        return Long.toHexString(l);\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import static java.lang.Integer.toHexString;\n" +
                                  "public class Test1 {\n" +
                                  "    private String test1(Integer i) {\n" +
                                  "        return i.toString();\n" +
                                  "    }\n" +
                                  "    private String test2(Integer i) {\n" +
                                  "        return toHexString(i);\n" +
                                  "    }\n" +
                                  "    private String test3(Long l) {\n" +
                                  "        return Long.toHexString(l);\n" +
                                  "    }\n" +
                                  "}\n")
                        .update()
                        .expect("test/Test1.java",
                                "package test;\n" +
                                "import static java.lang.Integer.toHexString;\n" +
                                "public class Test1 {\n" +
                                "    private String test1(Integer i) {\n" +
                                "        return i.toString();\n" +
                                "    }\n" +
                                "    private String test2(Integer i) {\n" +
                                "        return i.toString();\n" +
                                "    }\n" +
                                "    private String test3(Long l) {\n" +
                                "        return Long.toHexString(l);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test1.java",
                                "package test;\n" +
                                "import static java.lang.Integer.toHexString;\n" +
                                "public class Test1 {\n" +
                                "    private String test1(Integer i) {\n" +
                                "        return i.toString();\n" +
                                "    }\n" +
                                "    private String test2(Integer i) {\n" +
                                "        return toHexString(i);\n" +
                                "    }\n" +
                                "    private String test3(Long l) {\n" +
                                "        return l.toString();\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testStaticMethods2() throws Exception {
        new TestBuilder().addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import java.util.*;\n" +
                                  "public class Test1 {\n" +
                                  "    private List<?> test(List<?> l) {\n" +
                                  "        return l.subList(0, 0);\n" +
                                  "    }\n" +
                                  "}\n")
                        .addFile("test/Test2.java",
                                 "package test;\n" +
                                 "public class Test2 {\n" +
                                 "    private java.util.List<?> test(java.util.List<?> l) {\n" +
                                 "        return l.subList(0, 0);\n" +
                                 "    }\n" +
                                 "}\n")
                        .record()
                        .addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import java.util.*;\n" +
                                  "public class Test1 {\n" +
                                  "    private List<?> test(List<?> l) {\n" +
                                  "        return Collections.emptyList();\n" +
                                  "    }\n" +
                                  "}\n")
                        .update()
                        .expect("test/Test2.java",
                                "package test;\n\n" +
                                "import java.util.Collections;\n\n" +
                                "public class Test2 {\n" +
                                "    private java.util.List<?> test(java.util.List<?> l) {\n" +
                                "        return Collections.emptyList();\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testStaticMethods3() throws Exception {
        new TestBuilder().addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import java.util.*;\n" +
                                  "import static java.util.Collections.*;\n" +
                                  "public class Test1 {\n" +
                                  "    private List<?> test(List<?> l) {\n" +
                                  "        return l.subList(0, 0);\n" +
                                  "    }\n" +
                                  "}\n")
                        .addFile("test/Test2.java",
                                 "package test;\n" +
                                 "public class Test2 {\n" +
                                 "    private java.util.List<?> test(java.util.List<?> l) {\n" +
                                 "        return l.subList(0, 0);\n" +
                                 "    }\n" +
                                 "}\n")
                        .record()
                        .addFile("test/Test1.java",
                                  "package test;\n" +
                                  "import java.util.*;\n" +
                                  "import static java.util.Collections.*;\n" +
                                  "public class Test1 {\n" +
                                  "    private List<?> test(List<?> l) {\n" +
                                  "        return emptyList();\n" +
                                  "    }\n" +
                                  "}\n")
                        .update()
                        .expect("test/Test2.java",
                                "package test;\n\n" +
                                "import java.util.Collections;\n\n" +
                                "public class Test2 {\n" +
                                "    private java.util.List<?> test(java.util.List<?> l) {\n" +
                                "        return Collections.emptyList();\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testMethodParametersCount() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String test2(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                 "    private String test1(String str) {\n" +
                                 "        return str.toLowerCase(java.util.Locale.US);\n" +
                                 "    }\n" +
                                 "    private String test2(String str) {\n" +
                                 "        return str.toLowerCase();\n" +
                                 "    }\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(java.util.Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testAccept() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String test2(CharSequence str) {\n" +
                                  "        return str.toString().toLowerCase();\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                 "    private String test1(String str) {\n" +
                                 "        return str.toLowerCase(java.util.Locale.US);\n" +
                                 "    }\n" +
                                 "    private String test2(CharSequence str) {\n" +
                                 "        return str.toString().toLowerCase();\n" +
                                 "    }\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(java.util.Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(CharSequence str) {\n" +
                                "        return str.toString().toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2)
                        .assertAllChecked()
                        .markPositive("test/Test.java",
                                      "package test;\n\n" +
                                      "import java.util.Locale;\n\n" +
                                      "public class Test {\n" +
                                      "    private String test1(String str) {\n" +
                                      "        return str.toLowerCase(java.util.Locale.US);\n" +
                                      "    }\n" +
                                      "    private String test2(CharSequence str) {\n" +
                                      "        return str.toString().toLowerCase(Locale.US);\n" +
                                      "    }\n" +
                                      "}\n")
                        .expect("test/Test.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(java.util.Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(CharSequence str) {\n" +
                                "        return str.toString().toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN);
    }

    public void testApply() throws Exception {
        new TestBuilder().addFile("test/Test1.java",
                                  "package test;\n" +
                                  "public class Test1 {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String test2(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String test3(Test1 str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String toLowerCase() {\n" +
                                  "        return null;\n" +
                                  "    }\n" +
                                  "}\n")
                         .addFile("test/Test2.java",
                                  "package test;\n" +
                                  "public class Test2 {\n" +
                                  "    private String test1(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "    private String test2(String str) {\n" +
                                  "        return str.toLowerCase();\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test1.java",
                                 "package test;\n\n" +
                                 "import java.util.Locale;\n\n" +
                                 "public class Test1 {\n" +
                                 "    private String test1(String str) {\n" +
                                 "        return str.toLowerCase(Locale.US);\n" +
                                 "    }\n" +
                                 "    private String test2(String str) {\n" +
                                 "        return str.toLowerCase();\n" +
                                 "    }\n" +
                                 "    private String test3(Test1 str) {\n" +
                                 "        return str.toLowerCase();\n" +
                                 "    }\n" +
                                 "    private String toLowerCase() {\n" +
                                 "        return null;\n" +
                                 "    }\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test1.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test1 {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "    private String test3(Test1 str) {\n" +
                                "        return str.toLowerCase();\n" +
                                "    }\n" +
                                "    private String toLowerCase() {\n" +
                                "        return null;\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test1.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test1 {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(String str) {\n" +
                                "        return str.toLowerCase();\n" +
                                "    }\n" +
                                "    private String test3(Test1 str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "    private String toLowerCase() {\n" +
                                "        return null;\n" +
                                "    }\n" +
                                "}\n",
                                Kind.NEGATIVE, //XXX: this should presumably be POSITIVE with a small certainty....
                                PatternGenerator.CERTAIN)
                        .expect("test/Test2.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test2 {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "    private String test2(String str) {\n" +
                                "        return str.toLowerCase();\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test2.java",
                                "package test;\n\n" +
                                "import java.util.Locale;\n\n" +
                                "public class Test2 {\n" +
                                "    private String test1(String str) {\n" +
                                "        return str.toLowerCase();\n" +
                                "    }\n" +
                                "    private String test2(String str) {\n" +
                                "        return str.toLowerCase(Locale.US);\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked()
                        .apply()
                        .assertFile("test/Test1.java",
                                    "package test;\n\n" +
                                    "import java.util.Locale;\n\n" +
                                    "public class Test1 {\n" +
                                    "    private String test1(String str) {\n" +
                                    "        return str.toLowerCase(Locale.US);\n" +
                                    "    }\n" +
                                    "    private String test2(String str) {\n" +
                                    "        return str.toLowerCase(Locale.US);\n" +
                                    "    }\n" +
                                    "    private String test3(Test1 str) {\n" +
                                    "        return str.toLowerCase();\n" +
                                    "    }\n" +
                                    "    private String toLowerCase() {\n" +
                                    "        return null;\n" +
                                    "    }\n" +
                                    "}\n")
                        .assertFile("test/Test2.java",
                                    "package test;\n\n" +
                                    "import java.util.Locale;\n\n" +
                                    "public class Test2 {\n" +
                                    "    private String test1(String str) {\n" +
                                    "        return str.toLowerCase(Locale.US);\n" +
                                    "    }\n" +
                                    "    private String test2(String str) {\n" +
                                    "        return str.toLowerCase(Locale.US);\n" +
                                    "    }\n" +
                                    "}\n");
    }

    public void testDetectLocalVariable() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        return str;\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                 "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n" +
                                "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        return str;\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return val;\n" +
                                  "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .assertAllChecked();
    }

    public void testAddNewStatements() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        str = \"a\";\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                 "}\n")
                        .update();
    }

    public void testDiffTreeToNoTree() throws Exception {
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "public class Test {\n" +
                                  "    private String test1() {\n" +
                                  "        String str = \"\";\n" +
                                  "        return new String(str);\n" +
                                  "    }\n" +
                                  "    private String test2(String val) {\n" +
                                  "        return new String(val);\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "")
                        .update();
    }

    public void testElideCall() throws Exception {
        //TODO: naked contains
        new TestBuilder().addFile("test/Test.java",
                                  "package test;\n" +
                                  "import java.util.Set;\n" +
                                  "public class Test {\n" +
                                  "    private void test1(Set<String> set, String str) {\n" +
                                  "        if (!set.contains(str)) {\n" +
                                  "            set.add(str);\n" +
                                  "            System.err.println(0);\n" +
                                  "        }\n" +
                                  "    }\n" +
                                  "    private void test2(Set<String> set2, String str2) {\n" +
                                  "        if (!set2.contains(str2)) {\n" +
                                  "            set2.add(str2);\n" +
                                  "            System.err.println(0);\n" +
                                  "        }\n" +
                                  "    }\n" +
                                  "    private int test3(Set<String> set3, String str3) {\n" +
                                  "        if (!set3.contains(str3)) {\n" +
                                  "            set3.add(str3);\n" +
                                  "            return 0;\n" +
                                  "        }\n" +
                                  "        return 1;\n" +
                                  "    }\n" +
                                  "    private void test4(Set<String> set, Set<String> set2, String str) {\n" +
                                  "        if (!set.contains(str)) {\n" +
                                  "            set2.add(str);\n" +
                                  "            System.err.println(0);\n" +
                                  "        }\n" +
                                  "    }\n" +
                                  "}\n")
                        .record()
                        .addFile("test/Test.java",
                                 "package test;\n" +
                                 "import java.util.Set;\n" +
                                 "public class Test {\n" +
                                 "    private void test1(Set<String> set, String str) {\n" +
                                 "        if (set.add(str)) {\n" +
                                 "            System.err.println(0);\n" +
                                 "        }\n" +
                                 "    }\n" +
                                 "    private void test2(Set<String> set2, String str2) {\n" +
                                 "        if (!set2.contains(str2)) {\n" +
                                 "            set2.add(str2);\n" +
                                 "            System.err.println(0);\n" +
                                 "        }\n" +
                                 "    }\n" +
                                  "    private int test3(Set<String> set3, String str3) {\n" +
                                  "        if (!set3.contains(str3)) {\n" +
                                  "            set3.add(str3);\n" +
                                  "            return 0;\n" +
                                  "        }\n" +
                                  "        return 1;\n" +
                                  "    }\n" +
                                  "    private void test4(Set<String> set, Set<String> set2, String str) {\n" +
                                  "        if (!set.contains(str)) {\n" +
                                  "            set2.add(str);\n" +
                                  "            System.err.println(0);\n" +
                                  "        }\n" +
                                  "    }\n" +
                                  "}\n")
                        .update()
                        .expect("test/Test.java",
                                "package test;\n" +
                                "import java.util.Set;\n" +
                                "public class Test {\n" +
                                "    private void test1(Set<String> set, String str) {\n" +
                                "        if (set.add(str)) {\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private void test2(Set<String> set2, String str2) {\n" +
                                "        if (set2.add(str2)) {\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private int test3(Set<String> set3, String str3) {\n" +
                                "        if (!set3.contains(str3)) {\n" +
                                "            set3.add(str3);\n" +
                                "            return 0;\n" +
                                "        }\n" +
                                "        return 1;\n" +
                                "    }\n" +
                                "    private void test4(Set<String> set, Set<String> set2, String str) {\n" +
                                "        if (!set.contains(str)) {\n" +
                                "            set2.add(str);\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN)
                        .expect("test/Test.java",
                                "package test;\n" +
                                "import java.util.Set;\n" +
                                "public class Test {\n" +
                                "    private void test1(Set<String> set, String str) {\n" +
                                "        if (set.add(str)) {\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private void test2(Set<String> set2, String str2) {\n" +
                                "        if (!set2.contains(str2)) {\n" +
                                "            set2.add(str2);\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private int test3(Set<String> set3, String str3) {\n" +
                                "        if (set3.add(str3)) {\n" +
                                "            return 0;\n" +
                                "        }\n" +
                                "        return 1;\n" +
                                "    }\n" +
                                "    private void test4(Set<String> set, Set<String> set2, String str) {\n" +
                                "        if (!set.contains(str)) {\n" +
                                "            set2.add(str);\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN) //TODO: probability?
                        .expect("test/Test.java",
                                "package test;\n" +
                                "import java.util.Set;\n" +
                                "public class Test {\n" +
                                "    private void test1(Set<String> set, String str) {\n" +
                                "        if (set.add(str)) {\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private void test2(Set<String> set2, String str2) {\n" +
                                "        if (!set2.contains(str2)) {\n" +
                                "            set2.add(str2);\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "    private int test3(Set<String> set3, String str3) {\n" +
                                "        if (!set3.contains(str3)) {\n" +
                                "            set3.add(str3);\n" +
                                "            return 0;\n" +
                                "        }\n" +
                                "        return 1;\n" +
                                "    }\n" +
                                "    private void test4(Set<String> set, Set<String> set2, String str) {\n" +
                                "        if (set2.add(str)) {\n" +
                                "            System.err.println(0);\n" +
                                "        }\n" +
                                "    }\n" +
                                "}\n",
                                Kind.POSITIVE,
                                PatternGenerator.CERTAIN / 2) //TODO: probability?
                        .assertAllChecked();
    }

    final class TestBuilder {
        private final List<FileDesc> files = new ArrayList<>();
        private final FileObject sourceRoot;
        private PatternGenerator generator;

        public TestBuilder() throws Exception {
            sourceRoot = setUpTest();
        }

        public TestBuilder addFile(String fileName, String content) {
            files.add(new FileDesc(fileName, content));
            return this;
        }

        public TestBuilder record() throws Exception {
            for (FileDesc fd : files) {
                copyStringToFile(FileUtil.createData(sourceRoot, fd.fileName), fd.content);
            }

            generator = PatternGenerator.record(Arrays.asList(sourceRoot), ProgressHandleFactory.createHandle("record"), new AtomicBoolean());

            return this;
        }

        public TestResult update() throws Exception {
            for (FileDesc fd : files) {
                copyStringToFile(FileUtil.createData(sourceRoot, fd.fileName), fd.content);
            }

            Result result = generator.updated(Arrays.asList(sourceRoot), ProgressHandleFactory.createHandle("update"), new AtomicBoolean());

            return new TestResult(result);
        }

        public TestBuilder assertScript(String expected) {
            assertEquals(expected.replaceAll("\\s+", " "),
                         generator.getScript().replaceAll("\\s+", " "));
            return this;
        }

        final class TestResult {
            private final Result result;
            private final List<Item> uncheckedSoFar;

            public TestResult(Result result) {
                this.result = result;
                this.uncheckedSoFar = new ArrayList<>(result.changes);
            }

            public TestResult expect(String fileName,
                                     String expectedContent,
                                     Result.Kind expectedKind,
                                     long expectedCertainty) throws IOException {
                FileObject file = sourceRoot.getFileObject(fileName);

                for (Iterator<Item> it = uncheckedSoFar.iterator(); it.hasNext();) {
                    Item item = it.next();
                    if (item.diffs.getModifiedFileObjects().contains(file)) {
                        String actualContent = item.diffs.getResultingSource(file);

                        if (expectedContent.equals(actualContent)) {
                            assertEquals(expectedKind, item.kind);
                            assertEquals(expectedCertainty, item.certainty);

                            it.remove();
                            return this;
                        }
                    }
                }

                throw new AssertionError("Cannot find expected change in: " + uncheckedSoFar);
            }

            public TestResult markPositive(String fileName,
                                           String expectedContent) throws IOException {
                return new TestResult(generator.updatePositive(result, findItem(fileName, expectedContent)));
            }

            public TestResult markNegative(String fileName,
                                           String expectedContent) throws IOException {
                return new TestResult(generator.updateNegative(result, findItem(fileName, expectedContent)));
            }

            private Item findItem(String fileName, String expectedContent) throws IllegalArgumentException, IOException {
                FileObject file = sourceRoot.getFileObject(fileName);
                
                for (Item item : result.changes) {
                    if (item.diffs.getModifiedFileObjects().contains(file)) {
                        String actualContent = item.diffs.getResultingSource(file);

                        if (expectedContent.equals(actualContent)) {
                            return item;
                        }
                    }
                }

                throw new AssertionError("Cannot find expected change in: " + uncheckedSoFar);
            }

            public TestResult assertAllChecked() {
                assertTrue(uncheckedSoFar.toString(), uncheckedSoFar.isEmpty());
                return this;
            }

            private FileResult apply() throws IOException {
                generator.doRefactoring(result);
                return new FileResult();
            }
        }

        final class FileResult {

            public FileResult assertFile(String file, String content) throws IOException {
                FileObject resolveFile = sourceRoot.getFileObject(file);
                
                assertNotNull(resolveFile);
                
                assertEquals(content, resolveFile.asText("UTF-8"));

                return this;
            }
        }

    }

    static final class FileDesc {
        final String fileName;
        final String content;

        public FileDesc(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
        }
    }

}
