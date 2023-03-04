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

package org.netbeans.modules.java.hints.suggestions;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class CreateSubclassTest {
    
    @Test
    public void testTypeParams226791() throws Exception {
        CreateSubclass.overrideNameAndPackage = new String[] {
            "NewTest",
            "test"
        };
        HintTest test = HintTest.create();
        try (OutputStream out = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java").getOutputStream()) {
            out.write("public class New {}\n".getBytes(StandardCharsets.UTF_8));
        }
        test.setCaretMarker('^')
            .input("package test;\n" +
                   "public class Te^st<F extends CharSequence, S extends Number&Runnable> {}\n")
            .run(CreateSubclass.class)
            .findWarning("1:15-1:15:hint:ERR_CreateSubclass")
            .applyFix()
            .assertCompilable("test/NewTest.java")
            .assertOutput("test/NewTest.java",
                          "package test;\n" +
                          "public class NewTest<F extends CharSequence, S extends Number & Runnable> extends Test<F, S> {}\n");
    }
    
    @Test
    public void testTypeParams226791b() throws Exception {
        CreateSubclass.overrideNameAndPackage = new String[] {
            "NewTest",
            "test"
        };
        HintTest test = HintTest.create();
        try (OutputStream out = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java").getOutputStream()) {
            out.write("public class New {}\n".getBytes(StandardCharsets.UTF_8));
        }
        test.setCaretMarker('^')
            .input("package test;\n" +
                   "public class Te^st<F, S extends Object & CharSequence> {}\n")
            .run(CreateSubclass.class)
            .findWarning("1:15-1:15:hint:ERR_CreateSubclass")
            .applyFix()
            .assertCompilable("test/NewTest.java")
            .assertOutput("test/NewTest.java",
                          "package test;\n" +
                          "public class NewTest<F, S extends CharSequence> extends Test<F, S> {}\n");
    }

    @Test
    public void testNotConfusedByCommentedLBRACE() throws Exception {
        CreateSubclass.overrideNameAndPackage = new String[] {
            "NewTest",
            "test"
        };
        HintTest test = HintTest.create();
        try (OutputStream out = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java").getOutputStream()) {
            out.write("public class New {}\n".getBytes(StandardCharsets.UTF_8));
        }
        test.setCaretMarker('^')
            .input("package test;\n" +
                   "public/*{*/ class Te^st {}\n")
            .run(CreateSubclass.class)
            .findWarning("1:20-1:20:hint:ERR_CreateSubclass")
            .applyFix()
            .assertCompilable("test/NewTest.java")
            .assertOutput("test/NewTest.java",
                          "package test;\n" +
                          "public class NewTest extends Test {}\n");
    }

    @Test
    public void testDontSuggestIfNonCompilable222487() throws Exception {
        HintTest.create()
            .setCaretMarker('^')
            .input("package test;\n" +
                   "import base.Base;\n" +
                   "public class Te^st extends Base {}\n",
                   false)
            .input("base/Base.java",
                   "package base;\n" +
                   "public class Base {\n" +
                   "    private Base() {\n" +
                   "    }\n" +
                   "}\n")
            .run(CreateSubclass.class)
            .assertWarnings();
    }

    @Test
    public void testDontSuggestIfNonCompilable222487b() throws Exception {
        HintTest.create()
            .setCaretMarker('^')
            .input("package test;\n" +
                   "import base.Base;\n" +
                   "public class Te^st extends Base {}\n",
                   false)
            .input("base/Base.java",
                   "package base;\n" +
                   "public class Base {\n" +
                   "    protected Base(String value) {\n" +
                   "    }\n" +
                   "}\n")
            .run(CreateSubclass.class)
            .assertWarnings();
    }

    @Test
    public void testDontSuggestIfNonCompilable222487c() throws Exception {
        HintTest.create()
            .setCaretMarker('^')
            .input("package test;\n" +
                   "public class Te^st implements Runnable {}\n",
                   false)
            .run(CreateSubclass.class)
            .assertWarnings();
    }
}
