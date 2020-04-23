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

package org.netbeans.modules.debugger.jpda.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.sl.SLLanguage;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import org.graalvm.polyglot.Engine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class DebugSLTest extends NbTestCase {
    private DebuggerManager dm = DebuggerManager.getDebuggerManager();
    private final String sourceRoot = System.getProperty("test.dir.src");
    private JPDASupport support;

    public DebugSLTest(String name) {
        super(name);
    }

    public static Test suite() throws URISyntaxException {
        final File sdkAPI = new File(Engine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File truffleAPI = new File(TruffleLanguage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File sl = new File(SLLanguage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File antlr4 = new File(org.antlr.v4.runtime.Parser.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File junit = new File(TestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue("SDK API exists: " + sdkAPI, sdkAPI.exists());
        assertTrue("truffle-api JAR exists: " + truffleAPI, truffleAPI.exists());
        assertTrue("sl JAR exists: " + sl, sl.exists());
        assertTrue("antlr4 JAR exists: " + antlr4, antlr4.exists());
        assertTrue("junit JAR exists: " + junit, junit.exists());

        System.setProperty("graal-sdk.jar", sdkAPI.getAbsolutePath());
        System.setProperty("truffle.jar", truffleAPI.getAbsolutePath());
        System.setProperty("sl.jar", sl.getAbsolutePath());
        System.setProperty("antlr4.jar", antlr4.getAbsolutePath());
        System.setProperty("junit.jar", junit.getAbsolutePath());

        return JPDASupport.createTestSuite(DebugSLTest.class);
    }

    public void testStepIntoMainSL() throws Exception {
        doStepIntoSL(0, "main", 2);
    }
    
    public void testStepIntoInvokeAs() throws Exception {
        doStepIntoSL(1, "init", 7);
    }
    
    public void testStepIntoDynamicInterface() throws Exception {
        doStepIntoSL(2, "main", 2);
    }
    
    private void doStepIntoSL(int bpNum, String methodName, int lineNo) throws Exception {
        try {
            JPDASupport.removeAllBreakpoints();
            org.netbeans.api.debugger.jpda.Utils.BreakPositions bp = org.netbeans.api.debugger.jpda.Utils.getBreakPositions(
                sourceRoot
                + "org/netbeans/modules/debugger/jpda/truffle/testapps/SLApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(bpNum);
            dm.addBreakpoint(lb);
            support = JPDASupport.attach("org.netbeans.modules.debugger.jpda.truffle.testapps.SLApp",
                new String[0],
                new File[] {
                    new File(System.getProperty("graal-sdk.jar")),
                    new File(System.getProperty("truffle.jar")),
                    new File(System.getProperty("antlr4.jar")),
                    new File(System.getProperty("sl.jar")),
                    new File(System.getProperty("junit.jar")),
                }
            );
            support.waitState(JPDADebugger.STATE_STOPPED);
            support.stepInto();
            final JPDADebugger debugger = support.getDebugger();
            CallStackFrame frame = debugger.getCurrentCallStackFrame();
            assertNotNull(frame);
            // Check that frame is in the Truffle access method
            String haltedClass = TruffleAccess.BASIC_CLASS_NAME;
            Field haltedMethodField = TruffleAccess.class.getDeclaredField("METHOD_EXEC_HALTED");
            haltedMethodField.setAccessible(true);
            String haltedMethod = (String) haltedMethodField.get(null);
            /* Debug where it's stopped at:
            System.err.println("Stopped in "+frame.getClassName()+"."+frame.getMethodName()+"()");
            CallStackFrame[] callStack = frame.getThread().getCallStack();
            for (CallStackFrame sf : callStack) {
                System.err.println("  at "+sf.getClassName()+"."+sf.getMethodName()+"():"+sf.getLineNumber(null)+"  stratum: "+sf.getDefaultStratum());
            }*/
            assertEquals("Stopped in Truffle halted class", haltedClass, frame.getClassName());
            assertEquals("Stopped in Truffle halted method", haltedMethod, frame.getMethodName());
            assertEquals("Unexpected stratum", TruffleStrataProvider.TRUFFLE_STRATUM, frame.getDefaultStratum());
            
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(frame.getThread());
            assertNotNull("Missing CurrentPCInfo", currentPCInfo);
            TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
            assertNotNull("No top frame", topFrame);
            SourcePosition sourcePosition = topFrame.getSourcePosition();
            assertEquals("Bad source", "Meaning of world.sl", sourcePosition.getSource().getName());
            assertEquals("Bad line", lineNo, sourcePosition.getStartLine());
            assertEquals("Bad method name", methodName, topFrame.getMethodName());
            
            support.doContinue();
            support.waitState(JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish();
            }
        }
    }

    public void testBreakpointsInSL() throws Exception {
        try {
            JPDASupport.removeAllBreakpoints();
            org.netbeans.api.debugger.jpda.Utils.BreakPositions bp = org.netbeans.api.debugger.jpda.Utils.getBreakPositions(
                sourceRoot
                + "org/netbeans/modules/debugger/jpda/truffle/testapps/TestApp.sl");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            // An ugly way to transform the Java breakpoint into Truffle breakpoint, used in SL.
            FileObject fo = URLMapper.findFileObject(new URL(lb.getURL()));
            dm.addBreakpoint(new TruffleLineBreakpoint(EditorLineHandlerFactory.getHandler(fo, lb.getLineNumber())));
            support = JPDASupport.attach("org.netbeans.modules.debugger.jpda.truffle.testapps.SLAppFromFile",
                new String[] {
                    sourceRoot + "org/netbeans/modules/debugger/jpda/truffle/testapps/TestApp.sl"
                },
                new File[] {
                    new File(System.getProperty("graal-sdk.jar")),
                    new File(System.getProperty("truffle.jar")),
                    new File(System.getProperty("antlr4.jar")),
                    new File(System.getProperty("sl.jar")),
                    new File(System.getProperty("junit.jar")),
                }
            );
            support.waitState(JPDADebugger.STATE_STOPPED);
            
            final JPDADebugger debugger = support.getDebugger();
            CallStackFrame frame = debugger.getCurrentCallStackFrame();
            assertNotNull(frame);
            // Check that frame is in the Truffle guest language
            assertEquals("Unexpected stratum", TruffleStrataProvider.TRUFFLE_STRATUM, frame.getDefaultStratum());
            
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(frame.getThread());
            assertNotNull("Missing CurrentPCInfo", currentPCInfo);
            TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
            assertNotNull("No top frame", topFrame);
            SourcePosition sourcePosition = topFrame.getSourcePosition();
            assertEquals("Bad source", "TestApp.sl", sourcePosition.getSource().getName());
            assertEquals("Bad line", lb.getLineNumber(), sourcePosition.getStartLine());
            assertEquals("Bad method name", "main", topFrame.getMethodName());
            
            support.doContinue();
            support.waitState(JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish();
            }
        }
    }

}
