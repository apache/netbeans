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
package org.netbeans.lib.nbjavac.services;

import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lahvac
 */
public class NBClassFinderTest extends NbTestCase {

    public NBClassFinderTest(String testName) {
        super(testName);
    }

    public void testEmptyClassPath() throws Exception {
        String code = "package test; public class Test { void t(String s) { Integer i = s; } }";
        List<String> expectedErrors;
        expectedErrors =
                Arrays.asList(
                    "Test.java:1:22: compiler.err.cant.access: java.lang, (compiler.err.cant.resolve: package, java.lang)",
                    "Test.java:1:42: compiler.err.cant.resolve.location: kindname.class, String, , , (compiler.misc.location: kindname.class, test.Test, null)"
                );
        List<String> actualErrors;
        actualErrors = compile(code, "-bootclasspath", "", "--system", "none", "-XDrawDiagnostics", "-source", "8", "-XDide", "-Xlint:-options");
        assertEquals(expectedErrors, actualErrors);
        actualErrors = compile(code, "-bootclasspath", "", "--system", "none", "-XDrawDiagnostics", "-XDide", "-Xlint:-options");
        assertEquals(expectedErrors, actualErrors);
        expectedErrors =
                Arrays.asList(
                    "Test.java:1:22: compiler.err.cant.access: java.lang, (compiler.err.cant.resolve: package, java.lang)",
                    "Test.java:1:42: compiler.err.cant.resolve.location: kindname.class, String, , , (compiler.misc.location: kindname.class, test.Test, null)"
                );
        actualErrors = compile(code, "-bootclasspath", "", "--system", "none", "-XDrawDiagnostics", "-source", "8", "-XDide", "-XDbackgroundCompilation", "-Xlint:-options");
        assertEquals(expectedErrors, actualErrors);
        actualErrors = compile(code, "-bootclasspath", "", "--system", "none", "-XDrawDiagnostics", "-XDide", "-XDbackgroundCompilation", "-Xlint:-options");
        assertEquals(expectedErrors, actualErrors);
    }

    public void testEmptyClassPath2() throws Exception {
        String code = "package java.lang.nb.test; public class Test { String t(String s) { return s.toString(); } }";
        List<String> expectedErrors;
        expectedErrors = Arrays.asList("");
        List<String> actualErrors;
        actualErrors = compile(code, "-XDrawDiagnostics", "-XDide", "-Xlint:-options");
        assertEquals(expectedErrors, actualErrors);
    }

    private static class MyFileObject extends SimpleJavaFileObject {
        private String text;

        public MyFileObject(String text) {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
            this.text = text;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return text;
        }
    }

    private File workingDir;

    @Override
    protected void setUp() throws Exception {
        workingDir = getWorkDir();
    }

    private List<String> compile(String code, String... options) throws Exception {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        Context context = new Context();
        NBClassFinder.preRegister(context);
        StringWriter sw = new StringWriter();
        List<String> optionsList = Arrays.asList(options);
        final JavacTask ct = ((JavacTool)tool).getTask(sw, std, null, optionsList, null, Arrays.asList(new MyFileObject(code)), context);

        ct.analyze();

        return Arrays.asList(sw.toString().split("\\R"));
    }

}
