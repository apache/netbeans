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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test cases for the Different Case Kinds errors fix.
 * 
 */
public class DifferentCaseKindsFixTest extends ErrorHintsTestBase {

    private static final List<String> EXTRA_OPTIONS = new ArrayList<>();

    public DifferentCaseKindsFixTest(String name) {
        super(name, DifferentCaseKindsFix.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceLevel = "12";
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        EXTRA_OPTIONS.add("--enable-preview");
    }

    @ServiceProvider(service = CompilerOptionsQueryImplementation.class, position = 100)
    public static class TestCompilerOptionsQueryImplementation implements CompilerOptionsQueryImplementation {

        @Override
        public CompilerOptionsQueryImplementation.Result getOptions(FileObject file) {
            return new CompilerOptionsQueryImplementation.Result() {
                @Override
                public List<? extends String> getArguments() {
                    return EXTRA_OPTIONS;
                }

                @Override
                public void addChangeListener(ChangeListener listener) {
                }

                @Override
                public void removeChangeListener(ChangeListener listener) {
                }
            };
        }

    }

    public void testCase1() throws Exception {
        performFixTest("test/Test.java",
                "package test; \n"
                + "public class Test {\n"
                + "     private void test(int p) {\n"
                + "         String result;\n"
                + "         switch (p) {\n"
                + "             case 1: result = \"1\"; break;\n"
                + "             case 2: result = \"2\"; break;\n"
                + "             default -> result = \"3\";\n"
                + "         }\n"
                + "     }\n"
                + "}\n",
                -1,
                NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"),
                ("package test; \n"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> result = \"1\";\n"
                        + "             case 2 -> result = \"2\";\n"
                        + "             default -> result = \"3\";\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n").replaceAll("[\\s]+", " "));
    }

    public void testCase2() throws Exception {
        performFixTest("test/Test.java",
                "package test; \n"
                + "public class Test {\n"
                + "     private void test(int p) {\n"
                + "         String result;\n"
                + "         switch (p) {\n"
                + "             case 1: result = \"1\"; break;\n"
                + "             case 2 -> result = \"2\";\n"
                + "             default -> result = \"3\";\n"
                + "         }\n"
                + "     }\n"
                + "}\n",
                -1,
                NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"),
                ("package test; \n"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> result = \"1\";\n"
                        + "             case 2 -> result = \"2\";\n"
                        + "             default -> result = \"3\";\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n").replaceAll("[\\s]+", " "));
    }

    public void testCase3() throws Exception {
        performFixTest("test/Test.java",
                "package test; \n"
                + "public class Test {\n"
                + "     private void test(int p) {\n"
                + "         String result;\n"
                + "         switch (p) {\n"
                + "             case 1 -> result = \"1\";\n"
                + "             case 2 -> result = \"2\";\n"
                + "             default : result = \"3\"; break;\n"
                + "         }\n"
                + "     }\n"
                + "}\n",
                -1,
                NbBundle.getMessage(DifferentCaseKindsFix.class, "FIX_DifferentCaseKinds"),
                ("package test; \n"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> result = \"1\";\n"
                        + "             case 2 -> result = \"2\";\n"
                        + "             default -> result = \"3\";\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n").replaceAll("[\\s]+", " "));
    }    

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new DifferentCaseKindsFix().run(info, null, pos, path, null);
    }

    @Override
    protected Set<String> getSupportedErrorKeys() {
        return new DifferentCaseKindsFix().getCodes();
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
}
