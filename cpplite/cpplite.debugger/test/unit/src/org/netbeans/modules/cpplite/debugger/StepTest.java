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

package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cpplite.debugger.breakpoints.CPPLiteBreakpoint;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Tests Ant debugger stepping actions: step in, step out and step over.
 *
 * @author Jan Jancura
 */
public class StepTest extends NbTestCase {

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
        Process compile = new ProcessBuilder("g++", "-o", "main", "-g", "main.cpp").directory(wd).start();
        assertEquals(0, compile.waitFor());
        LineCookie lc = DataObject.find(source).getLookup().lookup(LineCookie.class);
        assertNotNull(lc);
        DebuggerManager.getDebuggerManager().addBreakpoint(new CPPLiteBreakpoint(lc.getLineSet().getCurrent(4)));
        CPPLiteDebugger d = CPPLiteDebugger.startDebugging(new CPPLiteDebuggerConfig(Arrays.asList(new File(wd, "main").getAbsolutePath()))).first();
        int[] suspendCount = new int[1];
        int[] resumeCount = new int[1];
        d.addStateListener(new CPPLiteDebugger.StateListener() {
            @Override
            public void suspended(boolean suspended) {
                int[] count;
                if (suspended) {
                    count = suspendCount;
                } else {
                    count = resumeCount;
                }
                synchronized (count) {
                    count[0]++;
                    count.notifyAll();
                }
            }

            @Override
            public void finished() {
            }
        });

        synchronized (suspendCount) {
            while (suspendCount[0] < 1) {
                suspendCount.wait();
            }
        }

        //on line 4, there is a breakpoint and the PC
        assertEquals(2, lc.getLineSet().getCurrent(4).getAnnotationCount());

        d.doAction(ActionsManager.ACTION_STEP_OVER);

        synchronized (resumeCount) {
            while (resumeCount[0] < 1) {
                resumeCount.wait();
            }
        }

        synchronized (suspendCount) {
            while (suspendCount[0] < 2) {
                suspendCount.wait();
            }
        }

        //on line 4, there is a breakpoint
        assertEquals(1, lc.getLineSet().getCurrent(4).getAnnotationCount());
        //PC:
        assertEquals(1, lc.getLineSet().getCurrent(5).getAnnotationCount());
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(StepTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }
}
