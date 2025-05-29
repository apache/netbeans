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
package org.netbeans.modules.debugger.jpda.ui.values;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.ui.values.ComputeInlineValues.InlineVariable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ComputeInlineValuesTest extends NbTestCase {

    private FileObject srcDir;

    public ComputeInlineValuesTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);

        clearWorkDir();

        FileObject wd = FileUtil.toFileObject(getWorkDir());

        srcDir = FileUtil.createFolder(wd, "src");

        FileObject buildDir = FileUtil.createFolder(wd, "build");
        FileObject cacheDir = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(srcDir, buildDir, cacheDir);
    }

    public void testSimpleComputeInlineVariables() throws Exception {
        runCompileInlineVariablesTest("""
                                      public class Test {
                                          private void test(String /param/) {
                                              int /i1/ = 0;
                                              |int i2 = /i1/;
                                              int i3 = 1;
                                          }
                                      }
                                      """);
    }

    public void testNestedMethodsComputeInlineVariables() throws Exception {
        runCompileInlineVariablesTest("""
                                      public class Test {
                                          private void test(String /param/) {
                                              int /i1/ = 0;
                                              Runnable /r1/ = new Runnable() {
                                                  public void run() {
                                                      int n = 0;
                                                  }
                                              };
                                              Runnable /r2/ = () -> {
                                                  int n = 0;
                                              };
                                              |int i2 = /i1/;
                                              int i3 = 1;
                                          }
                                      }
                                      """);
    }

    public void testNoEnumConstants() throws Exception {
        runCompileInlineVariablesTest("""
                                      package test;
                                      import static test.Test.E.*;
                                      public class Test {
                                          private void test(String /param/) {
                                              E /e/ = A;
                                              |E e2 = /e/;
                                          }
                                          public enum E {
                                              A, B, C;
                                          }
                                      }
                                      """);
    }

    public void testDeclarationAndUseOnTheSameLine() throws Exception {
        runCompileInlineVariablesTest("""
                                      public class Test {
                                          private void test(String /param/) {
                                              int /i1/ = 0;
                                              |for (int j = 0; /j/ < 10; /j/++) {
                                              }
                                          }
                                      }
                                      """);
    }

    public void testFields() throws Exception {
        runCompileInlineVariablesTest("""
                                      public class Test {
                                          int /i1/ = 0;
                                          |int j = /i1/;
                                      }
                                      """);
    }

    public void testClassNoCrash() throws Exception {
        runCompileInlineVariablesTest("""
                                      |public class Test {
                                      }
                                      """);
    }

    private void runCompileInlineVariablesTest(String codeResultAndPos) throws Exception {
        FileObject source = FileUtil.createData(srcDir, "Test.java");
        int pos = codeResultAndPos.replace("/", "").indexOf("|");
        int idx = 0;
        List<String> parts = new ArrayList<>(List.of(codeResultAndPos.replace("|", "").split("/")));
        Set<String> expectedSpans = new HashSet<>();

        while (parts.size() > 1) {
            int start = idx += parts.remove(0).length();
            int end = idx += parts.remove(0).length();

            expectedSpans.add("" + start + "-" + end);
        }

        String code = codeResultAndPos.replace("/", "").replace("|", "");

        TestUtilities.copyStringToFile(source, code);
        CompilationInfo info =
                SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(source),
                                                       JavaSource.Phase.RESOLVED);
        int stackLine = (int) info.getCompilationUnit().getLineMap().getLineNumber(pos);
        Collection<InlineVariable> computedVariables = ComputeInlineValues.computeVariables(info, stackLine, 0, new AtomicBoolean());

        for (InlineVariable var : computedVariables) {
            String snippet = code.substring(var.start(), var.end());

            assertEquals(snippet, var.expression());
            assertFalse(code.substring(var.end(), var.lineEnd()).contains("\n"));
            assertEquals('\n', code.charAt(var.lineEnd()));

            if (!expectedSpans.remove("" + var.start() + "-" + var.end())) {
                throw new AssertionError("Returned span: " + var.start() + "-" + var.end() + " (" + snippet + "), but it is not among the expected spans.");
            }
        }

        if (!expectedSpans.isEmpty()) {
            throw new AssertionError("Spans not found: " + expectedSpans);
        }
    }

}
