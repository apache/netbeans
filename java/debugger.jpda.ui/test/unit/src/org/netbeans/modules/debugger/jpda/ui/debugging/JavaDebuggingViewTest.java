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
package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class JavaDebuggingViewTest extends NbTestCase {

    private JPDASupport     support;

    public JavaDebuggingViewTest(String s) {
        super (s);
    }

    public static junit.framework.Test suite() {
        return JPDASupport.createTestSuite(JavaDebuggingViewTest.class);
    }

    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
    }

    public void testEvaluate() throws Exception {
        String code = "public class Test {\n" +
                      "    public static void main(String... args) throws Exception {\n" +
                      "        System.out.println(); //LBREAKPOINT\n" +
                      "        Thread t = new Thread(() -> {\n" +
                      "            System.out.println(); //LBREAKPOINT\n" +
                      "        }, \"test\");\n" +
                      "        t.start();\n" +
                      "        t.join();\n" +
                      "    }\n" +
                      "}\n";
        clearWorkDir();
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        assertNotNull(wd);
        FileObject source = wd.createData("Test.java");
        try (OutputStream out = source.getOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write(code);
        }
        Utils.BreakPositions bp = Utils.getBreakPositions(source.toURL());
        for (LineBreakpoint lb : bp.getLineBreakpoints()) {
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        }
        support = JPDASupport.attach (
            new String[0],
            FileUtil.toFile(source).getAbsolutePath(),
            new String[0],
            new File[0]
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
        CountDownLatch threadFound = new CountDownLatch(1);
        DVSupport dvSupport = support.getDebugger().getSession().lookupFirst(null, DVSupport.class);
        dvSupport.addPropertyChangeListener(evt -> {
            if (DVSupport.PROP_THREAD_SUSPENDED.equals(evt.getPropertyName())) {
                DVThread thread = (DVThread) evt.getNewValue();
                assertEquals("test", thread.getName());
                threadFound.countDown();
            }
        });

        support.doContinue();
        support.waitState (JPDADebugger.STATE_STOPPED);
        //ensure the threads have been updated:
        assertTrue(threadFound.await(10, TimeUnit.SECONDS));
        support.doContinue();
        support.doFinish();
    }

}
