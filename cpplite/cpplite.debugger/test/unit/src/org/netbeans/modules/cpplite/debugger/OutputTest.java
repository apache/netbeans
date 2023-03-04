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
package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbModuleSuite;
import static org.netbeans.modules.cpplite.debugger.AbstractDebugTest.compileC;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Test of application output.
 */
public class OutputTest extends AbstractDebugTest {

    public OutputTest (String s) {
        super (s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    private static String repeatText(String text, int count) {
        StringBuilder sb = new StringBuilder(text);
        while (--count > 0) {
            sb.append(text);
        }
        return sb.toString();
    }

    public void testOutputNoSuspend() throws Exception {
        String textToStdOut = repeatText("Text to STD OUT\\n", 100);
        File wd = getWorkDir();
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "output1.c");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append("#include<stdio.h>\n" +
                     "\n" +
                     "int main(int argc, char** args) {\n" +
                     "    printf(\""+textToStdOut+"\");\n" +
                     "}");
        }
        compileC("output1", wd);
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        startDebugging("output1", wd);

        assertEquals(0, waitAppProcessExit());

        assertEquals(textToStdOut.replace("\\n", "\n"), stdOut.toString());
    }

    public void testOutputSteps() throws Exception {
        final int N = 10;
        File wd = getWorkDir();
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "output2.c");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append("#include<stdio.h>\n" +
                     "\n" +
                     "int main(int argc, char** args) {\n" +
                     "    int N = "+N+";\n" +
                     "    for (int i = 0; i < N; i++) {\n" +
                     "        printf(\"Test output %d\\n\", i);\n" +
                     "    }\n" +
                     "}");
        }
        compileC("output2", wd);
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        CPPLiteBreakpoint bp6 = CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(5));
        DebuggerManager.getDebuggerManager().addBreakpoint(bp6);
        startDebugging("output2", wd);

        StringBuilder referenceOutput = new StringBuilder();
        for (int i = 0; i < N; i++) {
            waitSuspended(i+1);
            assertStoppedAt(source.toURI(), 6);
            assertEquals(referenceOutput.toString(), stdOut.toString());
            engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
            waitResumed(i+1);
            referenceOutput.append("Test output ");
            referenceOutput.append(i);
            referenceOutput.append('\n');
        }
        assertEquals(0, waitAppProcessExit());
        assertEquals(referenceOutput.toString(), stdOut.toString());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(OutputTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }
}
