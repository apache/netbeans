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
 * Tests C/C++ debugger breakpoints.
 */
public class BreakpointsTest extends AbstractDebugTest {

    public BreakpointsTest (String s) {
        super (s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testBreakpoints() throws Exception {
        File wd = getWorkDir();
        FileObject source = FileUtil.createData(FileUtil.toFileObject(wd), "breakpoints.c");
        try (OutputStream os = source.getOutputStream();
            Writer w = new OutputStreamWriter(os)) {
            w.append("#include<stdio.h>\n" +
                     "\n" +
                     "long double loop(unsigned int N) {\n" +
                     "    unsigned int i;\n" +
                     "    long double f = 1;\n" +
                     "    long double s = 0;\n" +
                     "    for (i = 1; i <= N; i++) {\n" +
                     "        f *= i;\n" +
                     "        s += i;\n" +
                     "        f /= s/i;\n" +
                     "    }\n" +
                     "    return f;\n" +
                     "}\n" +
                     "\n" +
                     "int main(int argc, char** args) {\n" +
                     "    int N = 100;\n" +
                     "    long double r = loop(N);\n" +
                     "    printf(\"Result(%d) = %.40Lg\\n\", N, r);\n" +
                     "}");
        }
        compileC("breakpoints", wd);
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        CPPLiteBreakpoint bp8 = CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(7));
        CPPLiteBreakpoint bp9 = CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(8));
        bp9.disable();
        DebuggerManager.getDebuggerManager().addBreakpoint(bp8);
        DebuggerManager.getDebuggerManager().addBreakpoint(bp9);
        startDebugging("breakpoints", wd);

        waitSuspended(1);
        assertStoppedAt(source.toURI(), 8);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(1);
        waitSuspended(2);
        assertStoppedAt(source.toURI(), 8);

        bp9.enable();

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(2);
        waitSuspended(3);
        assertStoppedAt(source.toURI(), 9);

        CPPLiteBreakpoint bp10 = CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(9));
        DebuggerManager.getDebuggerManager().addBreakpoint(bp10);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(3);
        waitSuspended(4);
        assertStoppedAt(source.toURI(), 10);

        DebuggerManager.getDebuggerManager().removeBreakpoint(bp8);
        DebuggerManager.getDebuggerManager().removeBreakpoint(bp9);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(4);
        waitSuspended(5);
        assertStoppedAt(source.toURI(), 10);

        bp10.disable();

        bp8 = CPPLiteBreakpoint.create(lc.getLineSet().getCurrent(7));
        DebuggerManager.getDebuggerManager().addBreakpoint(bp8);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(5);
        waitSuspended(6);
        assertStoppedAt(source.toURI(), 8);

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(5);
        waitSuspended(6);
        assertStoppedAt(source.toURI(), 8);

        bp8.disable();

        engine.getActionsManager().doAction(ActionsManager.ACTION_CONTINUE);
        waitResumed(6);

        engine.getActionsManager().doAction(ActionsManager.ACTION_KILL);
        assertEquals(0, waitAppProcessExit());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(BreakpointsTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }
}
