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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Tests C/C++ debugger stepping actions: step in, step out and step over.
 *
 * @author Jan Jancura
 */
public class StepTest extends AbstractDebugTest {

    public StepTest (String s) {
        super (s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testStepOver () throws Exception {
        File wd = getWorkDir();
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "main.cpp");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append("#include <iostream>\n" +
                     "\n" +
                     "void test(void) {\n" +
                     "    std::cerr << \"Hello, from err!\" << std::endl;\n" +
                     "    std::cout << \"Hello, second time!\" << std::endl;\n" +
                     "}\n" +
                     "\n" +
                     "int main(void) {\n" +
                     "    int i = 42;\n" +
                     "    std::cout << \"Hello, world!\" << std::endl;\n" +
                     "    test();\n" +
                     "    std::cout << \"Hello, second time!\" << std::endl;\n" +
                     "}");
        }
        compileCPP("main", wd);
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        DebuggerManager.getDebuggerManager().addBreakpoint(CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(4)));
        startDebugging("main", wd);

        waitSuspended(1);

        assertStoppedAt(source.toURI(), 5);

        engine.getActionsManager().doAction(ActionsManager.ACTION_STEP_OVER);

        waitResumed(1);

        waitSuspended(2);

        assertStoppedAt(source.toURI(), 6);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        assertEquals(0, waitAppProcessExit());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(StepTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }
}
