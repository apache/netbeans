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
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbModuleSuite;
import static org.netbeans.modules.cpplite.debugger.AbstractDebugTest.compileCPP;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Entlicher
 */
public class ExitTest extends AbstractDebugTest {

    public ExitTest(String s) {
        super(s);
    }

    public void testAppExit () throws Exception {
        File wd = getWorkDir();
        for (int exitCode = 0; exitCode <= 200; exitCode += 100) {
            FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "mainExit" + exitCode + ".cpp");
            try (OutputStream os = source.getOutputStream();
                Writer w = new OutputStreamWriter(os)) {
                w.append("int main(int argc, char** args) {\n" +
                         "    int N = 100;\n" +
                         "    return " + exitCode + ";\n" +
                         "}");
            }
            compileCPP("mainExit" + exitCode, wd);
            LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
            assertNotNull(lc);
            DebuggerManager.getDebuggerManager().addBreakpoint(CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(4)));
            startDebugging("mainExit" + exitCode, wd);
            assertEquals(exitCode, waitAppProcessExit());
        }
    }

    public void testAppKill () throws Exception {
        File wd = getWorkDir();
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "mainKill.cpp");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append("int main(int argc, char** args) {\n" +
                     "    for(;;);\n" +
                     "}");
        }
        compileCPP("mainKill", wd);
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        DebuggerManager.getDebuggerManager().addBreakpoint(CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(4)));
        startDebugging("mainKill", wd);
        assertTrue(isAppProcessAlive());
        Thread.sleep(10);
        assertTrue(isAppProcessAlive());
        engine.getActionsManager().doAction(ActionsManager.ACTION_KILL);
        assertEquals(0, waitAppProcessExit());
        assertFalse(isAppProcessAlive());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(ExitTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }
}
