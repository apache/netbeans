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
package org.netbeans.modules.debugger.jpda.heapwalk;

import com.sun.jdi.AbsentInformationException;
import java.io.File;
import java.net.URI;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
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
import org.openide.util.Utilities;

public class HeapImplTest extends NbTestCase {
    private JPDASupport support;
    private ExceptionBreakpoint breakPoint;


    public HeapImplTest(String name) {
        super(name);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(HeapImplTest.class);
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
        final Session session = support.getDebugger().getSession();

        HeapImpl heap = new HeapImpl(support.getDebugger());

        Throwable[] arr = { null };
        assertOnBreakpoint((event) -> {
            try {
                CallStackFrame frame = event.getThread().getCallStack(0, 1)[0];

                This self = frame.getThisVariable();
                assertEquals("42", self.getField("i").getValue());
                
                JavaClass thizClass = null;
                Instance thiz = null;
                for (JavaClass clazz : heap.getAllClasses()) {
                    if (test.SampleMain.class.getName().equals(clazz.getName())) {
                        thizClass = clazz;
                        thiz = (Instance) clazz.getInstances().get(0);
                        break;
                    }
                }
                assertNotNull("This instance found in the heap", thiz);
                {
                    Object i = thiz.getValueOfField("i");
                    assertEquals(Integer.class, i.getClass());
                    assertEquals(42, i);

                    Object b = thiz.getValueOfField("b");
                    assertEquals(Byte.class, b.getClass());
                    assertEquals(0x7f, ((Byte)b).intValue());

                    Object s = thiz.getValueOfField("s");
                    assertEquals(Short.class, s.getClass());
                    assertEquals(0x7f7f, ((Short) s).intValue());

                    Object l = thiz.getValueOfField("l");
                    assertEquals(Long.class, l.getClass());
                    assertEquals(42 * 42, ((Long) l).intValue());

                    Object f = thiz.getValueOfField("f");
                    assertEquals(Float.class, f.getClass());
                    assertEquals(0.3f, ((Float) f).floatValue());

                    Object d = thiz.getValueOfField("d");
                    assertEquals(Double.class, d.getClass());
                    assertEquals(0.4, ((Double) d).doubleValue());

                    Object ch = thiz.getValueOfField("ch");
                    assertEquals(Character.class, ch.getClass());
                    assertEquals('C', ((Character) ch).charValue());
                }
                {
                    Object i = thizClass.getValueOfStaticField("I");
                    assertEquals(Integer.class, i.getClass());
                    assertEquals(42, i);

                    Object b = thizClass.getValueOfStaticField("B");
                    assertEquals(Byte.class, b.getClass());
                    assertEquals(0x7f, ((Byte)b).intValue());

                    Object s = thizClass.getValueOfStaticField("S");
                    assertEquals(Short.class, s.getClass());
                    assertEquals(0x7f7f, ((Short) s).intValue());

                    Object l = thizClass.getValueOfStaticField("L");
                    assertEquals(Long.class, l.getClass());
                    assertEquals(42 * 42, ((Long) l).intValue());

                    Object f = thizClass.getValueOfStaticField("F");
                    assertEquals(Float.class, f.getClass());
                    assertEquals(0.3f, ((Float) f).floatValue());

                    Object d = thizClass.getValueOfStaticField("D");
                    assertEquals(Double.class, d.getClass());
                    assertEquals(0.4, ((Double) d).doubleValue());

                    Object ch = thizClass.getValueOfStaticField("CH");
                    assertEquals(Character.class, ch.getClass());
                    assertEquals('C', ((Character) ch).charValue());
                }
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
