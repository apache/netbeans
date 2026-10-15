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

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.util.Context;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;

import static org.netbeans.lib.nbjavac.services.Utilities.DEV_NULL;

/**
 *
 * @author lahvac
 */
public class NBClassReaderTest extends NbTestCase {

    public NBClassReaderTest(String testName) {
        super(testName);
    }

    public void testTooNewClassFile() throws Exception {
        compile("package test; public class Test { }");
        File testClass = new File(new File(workingDir, "test"), "Test.class");
        try (RandomAccessFile raf = new RandomAccessFile(testClass, "rw")) {
            raf.seek(6);

            raf.write(0xFF);
            raf.write(0xFF);
        }
        testCompile("package test; class Test2 { Test t; }",
                    "/Test.java:1:compiler.warn.big.major.version");
    }

    public void testReadBytes() throws Exception {
        byte[] data = new byte[1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        for (int i = 0; i <= data.length; i++) {
            int available = i;
            ByteArrayInputStream dataIS = new ByteArrayInputStream(data);
            InputStream testIS = new InputStream() {
                @Override
                public int read() throws IOException {
                    return dataIS.read();
                }
                @Override
                public int available() throws IOException {
                    return available;
                }
            };
            byte[] actual = NBClassReader.readFile(testIS);
            Assert.assertArrayEquals(data, actual);
        }
    }
    //<editor-fold defaultstate="collapsed" desc=" Test Infrastructure ">
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

    private void compile(String code) throws Exception {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(workingDir));

        Context context = new Context();
        NBLog.preRegister(context, DEV_NULL);
        final JavacTaskImpl ct = (JavacTaskImpl) ((JavacTool)tool).getTask(null, std, null, Arrays.asList("-source", "8", "-target", "8"), null, Arrays.asList(new MyFileObject(code)), context);

        ct.call();
    }

    private void testCompile(String code, String... expectedErrors) throws IOException {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        assert tool != null;

        StandardJavaFileManager std = tool.getStandardFileManager(null, null, null);

        std.setLocation(StandardLocation.CLASS_PATH, Collections.singleton(workingDir));

        Context context = new Context();
        NBLog.preRegister(context, DEV_NULL);
        DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
        JavacTaskImpl ct = (JavacTaskImpl)((JavacTool)tool).getTask(null, std, diags, Arrays.asList("-source", "1.8", "-target", "1.8", "-Xlint:-options", "-XDrawDiagnostics"), null, Arrays.asList(new MyFileObject(code)), context);

        NBClassReader.preRegister(ct.getContext());
        NBNames.preRegister(ct.getContext());

        ct.analyze();

        List<String> actualErrors = diags.getDiagnostics().stream().map(d -> (d.getSource() != null ? d.getSource().getName() + ":" : "") + d.getLineNumber() + ":" + d.getCode()).collect(Collectors.toList());

        assertEquals(Arrays.asList(expectedErrors), actualErrors);
    }
    //</editor-fold>
}
