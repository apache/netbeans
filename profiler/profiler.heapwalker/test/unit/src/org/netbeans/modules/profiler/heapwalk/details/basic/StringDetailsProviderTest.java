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
package org.netbeans.modules.profiler.heapwalk.details.basic;

import com.sun.jdi.AbsentInformationException;
import java.io.File;
import java.net.URI;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.debugger.jpda.heapwalk.HeapImpl;
import org.openide.util.Utilities;

public class StringDetailsProviderTest extends NbTestCase {
    private JPDASupport support;
    private ExceptionBreakpoint breakPoint;


    public StringDetailsProviderTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(StringDetailsProviderTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        breakPoint = ExceptionBreakpoint.create(
            "java.lang.IllegalStateException",
            ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT_UNCAUGHT
        );
        DebuggerManager.getDebuggerManager().addBreakpoint(breakPoint);

        final URI cpUrl = test.SampleMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        System.setProperty ("test.dir.src", test.SampleMain.findSourcePath());
        support = JPDASupport.attach(new String[0], "test.SampleMain", new String[0], new File[] {
            Utilities.toFile(cpUrl)
        });
    }



    /**
     * Check fields of {@link test.SampleMain}.
     */
    public void testHeapRepresentationWhenDebuggerIsStopped() throws Throwable {
        HeapImpl heap = new HeapImpl(support.getDebugger());
        
        Throwable[] arr = { null };
        assertOnBreakpoint((event) -> {
            try {
                CallStackFrame frame = event.getThread().getCallStack(0, 1)[0];

                This self = frame.getThisVariable();
                String value = self.getField("when").getValue();
                assertEquals("\"Now\"", value);
                
                Instance thiz = null;
                for (JavaClass clazz : heap.getAllClasses()) {
                    if (test.SampleMain.class.getName().equals(clazz.getName())) {
                        thiz = (Instance) clazz.getInstances().get(0);
                        break;
                    }
                }
                assertNotNull("This instance found in the heap", thiz);
                
                Instance str = (Instance) thiz.getValueOfField("when");
                assertNotNull("When field is found", str);
                assertEquals("java.lang.String", str.getJavaClass().getName());
                
                StringDetailsProvider p = new StringDetailsProvider();
                String detail = p.getDetailsString("java.lang.String", str, heap);
                assertEquals("Now", detail);

                Instance fiveEuro = (Instance) thiz.getValueOfField("utf16");
                assertNotNull("utf16 field is found", fiveEuro);
                assertEquals("java.lang.String", fiveEuro.getJavaClass().getName());

                String five = p.getDetailsString("java.lang.String", fiveEuro, heap);
                assertEquals("5 €", five);
            } catch (Throwable ex) {
                arr[0] = ex;
            }
        });
        if (arr[0] != null) {
            throw arr[0];
        }
    }

    private void assertOnBreakpoint(JPDABreakpointListener listener) {
        int[] cnt = { 0 };
        JPDABreakpointListener countingListener = (event) -> {
            cnt[0]++;
            listener.breakpointReached(event);
        };
        breakPoint.addJPDABreakpointListener(countingListener);

        //
        // launch the debugger and run the test
        //

        for (;;) {
            support.waitState(JPDADebugger.STATE_STOPPED);
            if (support.getDebugger().getState() == JPDADebugger.STATE_DISCONNECTED) {
                break;
            }
            support.doContinue();
        }

        breakPoint.removeJPDABreakpointListener(countingListener);

        assertEquals("Breakpoint has been reached and listener is cleared", 1, cnt[0]);
    }
}
