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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import javax.lang.model.SourceVersion;

/**
 *
 * @author mjayan
 */
public class ConvertToVTTest extends NbTestCase {

    public ConvertToVTTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        HintTest.create()
                .input("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ExecutorService executor;\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        executor = Executors.newFixedThreadPool(2); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToVT.class)
                .findWarning("9:8-9:51:verifier:" + Bundle.ERR_ConvertToVT())
                .applyFix()
                .assertCompilable()
                .assertOutput("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ExecutorService executor;\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        executor = Executors.newVirtualThreadPerTaskExecutor(); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}");
    }

    public void testVTHint2() throws Exception {
        HintTest.create()
                .input("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ExecutorService executor;\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        executor = Executors.newFixedThreadPool(2, factory); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToVT.class)
                .findWarning("9:8-9:60:verifier:" + Bundle.ERR_ConvertToThreadPerTask())
                .applyFix()
                .assertCompilable()
                .assertOutput("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ExecutorService executor;\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        executor = Executors.newFixedThreadPool(2, factory); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}");
    }
    
    public void testVTHint3() throws Exception {
        HintTest.create()
                .input("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        ExecutorService executor = Executors.newCachedThreadPool(); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertToVT.class)
                .findWarning("8:24-8:32:verifier:" + Bundle.ERR_ConvertToVT())
                .applyFix()
                .assertCompilable()
                .assertOutput("package testrecordpattern;\n"
                        + "\n"
                        + "import java.util.concurrent.ExecutorService;\n"
                        + "import java.util.concurrent.Executors;\n"
                        + "import java.util.concurrent.ThreadFactory;\n"
                        + "public class Test {\n"
                        + "    public void testVt() {\n"
                        + "        ThreadFactory factory = Thread.ofPlatform().daemon().name(\"worker-\", 0).factory();\n"
                        + "        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); \n"
                        + "        executor.submit(() -> System.out.println(\"Running Task! Thread Name: \" \n"
                        + "                + Thread.currentThread().getName()));\n"
                        + "        System.out.println(\"End\");\n"
                        + "        executor.shutdown();\n"
                        + "    }\n"
                        + "}");
    }
}
