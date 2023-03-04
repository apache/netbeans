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

package org.netbeans.lib.v8debug.client.cmdline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8ExceptionBreakType;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.V8ScriptLocation;
import org.netbeans.lib.v8debug.V8StepAction;
import org.netbeans.lib.v8debug.commands.Backtrace;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpointGroup;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.commands.Flags;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.GC;
import org.netbeans.lib.v8debug.commands.ListBreakpoints;
import org.netbeans.lib.v8debug.commands.Lookup;
import org.netbeans.lib.v8debug.commands.References;
import org.netbeans.lib.v8debug.commands.RestartFrame;
import org.netbeans.lib.v8debug.commands.Scope;
import org.netbeans.lib.v8debug.commands.Scopes;
import org.netbeans.lib.v8debug.commands.Scripts;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.commands.SetExceptionBreak;
import org.netbeans.lib.v8debug.commands.SetVariableValue;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.lib.v8debug.commands.Threads;
import org.netbeans.lib.v8debug.commands.V8Flags;
import org.netbeans.lib.v8debug.commands.Version;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.lib.v8debug.vars.NewValue;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * Test of node.js debugging. This test expects 'node' binary on PATH, or on
 * a path defined by 'nodeBinary' system or env variable.
 * 
 * @author Martin Entlicher
 */
public class V8DebugTest extends AbstractTestBase {
    
    private static final String TEST_FILE = "TestDebug.js"; // NOI18N
    private static final String NODE_ARG_DBG = "--debug-brk";   // NOI18N
    
    private static final int TEST_NUM_LINES = 244;
    private static final int TEST_NUM_CHARS = 6274;
    
    private static final int LINE_BEGIN = 45;
    private static final int LINE_FNC = LINE_BEGIN + 7;
    private static final int LINE_BRKP_VARS = LINE_BEGIN + 36;
    private static final int LINE_BRKP_LONG_STACK = LINE_BRKP_VARS + 10;
    private static final int LINE_BRKP_ARRAYS = LINE_BRKP_LONG_STACK + 18;
    private static final int LINE_BRKP_OBJECTS = LINE_BRKP_ARRAYS + 30;
    private static final int LINE_O4_AB_FNC = LINE_BRKP_OBJECTS - 14;
    private static final int LINE_BRKP_SCOPE = LINE_BRKP_OBJECTS + 26;
    private static final int LINE_CLOSURE_CALL = LINE_FNC + 4;
    private static final int LINE_CLOSURE_CALEE = LINE_BRKP_OBJECTS + 11;
    private static final int LINE_SCOPE_INNER_FUNC = LINE_CLOSURE_CALEE + 9;
    private static final int LINE_BRKP_COND = LINE_BRKP_SCOPE + 19;
    private static final int LINE_BRKP_REFS = LINE_BRKP_COND + 27;
    private static final int LINE_EBRKP1 = LINE_BRKP_REFS + 5;
    private static final int LINE_EBRKP2 = LINE_EBRKP1 + 5;
    private static final int LINE_EBRKP3 = LINE_EBRKP2 + 5;
    private static final int LINE_EBRKP4 = LINE_EBRKP3 + 6;
    private static final int LINE_EBRKP5 = LINE_EBRKP4 + 5;
    private static final int LINE_EBRKP6 = LINE_EBRKP5 + 4;
    
    private static final int POS_FNC = 2347;
    private static final int POS_VAR_F1 = POS_FNC + 494;
    private static final int POS_VAR_F2 = POS_VAR_F1 + 32;
    private static final int POS_BRKP_LONG_STACK = POS_VAR_F2 + 291;
    private static final int POS_O4_AB_FNC = POS_BRKP_LONG_STACK + 753;
    private static final int POS_SCOPE_INNER_FUNC = POS_O4_AB_FNC + 729;
    
    public V8DebugTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        // To block standard in:
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                    throw new IOException(ex.getLocalizedMessage());
                }
                return -1;
            }
        });
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException {
        startUp(V8DebugTest.class.getResourceAsStream(TEST_FILE), TEST_FILE, NODE_ARG_DBG);
    }
    
    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(2000); // To recover
    }
    
    /**
     * Test of main debug functionality, of class V8Debug. <br/>
     * Test all commands, variables: {@link #checkVariables()},
     * arrays: {@link #checkArrays()} and object properties: {@link #checkObjects()}.
     */
    @Test
    public void testMain() throws IOException, InterruptedException {
        // The set of commands that we need to test:
        Set<V8Command> commandsToTest = new HashSet<>(Arrays.asList(V8Command.values()));
        // Exception breakpoints are tested explicitly
        commandsToTest.remove(V8Command.Setexceptionbreak);
        // Suspend and ChangeLive are subject of a different test (V8SuspendAndChangeTest)
        commandsToTest.remove(V8Command.Suspend);
        commandsToTest.remove(V8Command.Changelive);
        
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        // Start testing:
        checkFrame(0, LINE_BEGIN-1, "var glob_n = 100;");
        commandsToTest.remove(V8Command.Frame);
        
        V8Debug.TestAccess.doCommand(v8dbg, "next");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(0, LINE_BEGIN+2-1, "var glob_m = glob_n % 27;");
        checkLocalVar("glob_n", 100l, false);
        checkLocalVar("glob_m", VALUE_UNDEFINED, false);
        
        V8Debug.TestAccess.send(v8dbg, Continue.createRequest(123, V8StepAction.next, 2));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkLocalVar("glob_m", Math.sin((100 % 27)) + Math.cos(100), false);
        
        V8Debug.TestAccess.doCommand(v8dbg, "step in");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(4, LINE_FNC-1, "    vars();");
        
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_VARS);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 2, testFilePath, LINE_BRKP_VARS-1, null, new long[] { 6, 4});
        // node.js v0.10.36: column 6
        // io.js v1.0.4: column 4
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(6, LINE_BRKP_VARS-1, "    s1.length;          // breakpoint");
        checkVariables();
        
        V8Debug.TestAccess.doCommand(v8dbg, "step out");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(4, LINE_FNC+1-1, "    longStack(20);");
        
        commandsToTest.remove(V8Command.Continue);
        
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_LONG_STACK);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 3, testFilePath, LINE_BRKP_LONG_STACK-1, null, 8);
        commandsToTest.remove(V8Command.Setbreakpoint);
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(8, LINE_BRKP_LONG_STACK-1, "        glob_n += n;");
        checkLongStackFrames();
        commandsToTest.remove(V8Command.Backtrace);
        
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_ARRAYS);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 4, testFilePath, LINE_BRKP_ARRAYS-1, null, new long[] {10, 4});
        // node.js v0.10.36: column 10
        // io.js v1.0.4: column 4
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(10, LINE_BRKP_ARRAYS-1, "    months.length;      // breakpoint");
        checkArrays();
        commandsToTest.remove(V8Command.Lookup);
        
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_OBJECTS);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 5, testFilePath, LINE_BRKP_OBJECTS-1, null, new long[] {6, 4});
        // node.js v0.10.36: column 6
        // io.js v1.0.4: column 4
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(6, LINE_BRKP_OBJECTS-1, "    o4.ab.vol;          // breakpoint");
        checkObjects();
        checkSetVars();
        commandsToTest.remove(V8Command.SetVariableValue);
        
        checkThreads();
        commandsToTest.remove(V8Command.Threads);
        
        checkV8Flags();
        commandsToTest.remove(V8Command.V8flags);
        checkFlags();
        commandsToTest.remove(V8Command.Flags);
        
        checkGC();
        commandsToTest.remove(V8Command.Gc);
        
        checkVersion();
        commandsToTest.remove(V8Command.Version);
        
        checkSource(0, 100, 150);
        checkSource(1, 170, 180);
        String testSrc = checkSource(2, 30, 100);
        String otherSrc = checkSource(3, 30, 100);
        assertFalse(testSrc.equals(otherSrc));
        commandsToTest.remove(V8Command.Source);
        
        V8Debug.TestAccess.doCommand(v8dbg, "step out");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(4, LINE_CLOSURE_CALL-1, "    var cl = closures();");
        
        V8Debug.TestAccess.doCommand(v8dbg, "step over");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(new long[] {7, 6}, LINE_CLOSURE_CALL+1-1, "    cl.getValue();");
        // node.js v0.10.36: column 7
        // io.js v1.0.4: column 6
        
        checkEval("1 + 2", 3l);
        checkEval("cl.append(\"a\")", VALUE_UNDEFINED);
        commandsToTest.remove(V8Command.Evaluate);
        
        V8Debug.TestAccess.doCommand(v8dbg, "step into");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(12, LINE_CLOSURE_CALEE-1, "            return private;");
        checkEval("private", "pa");
        
        V8Debug.TestAccess.doCommand(v8dbg, "step out");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(4, LINE_CLOSURE_CALL+2-1, "    scope();");
        
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_SCOPE);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 6, testFilePath, LINE_BRKP_SCOPE-1, null, 4);
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        assertEquals("scope()", ((BreakEventBody) lastEvent.getBody()).getInvocationText());
        assertEquals("    inner();            // breakpoint", ((BreakEventBody) lastEvent.getBody()).getSourceLineText());
        assertEquals(LINE_BRKP_SCOPE-1, ((BreakEventBody) lastEvent.getBody()).getSourceLine());
        checkScopes();
        commandsToTest.remove(V8Command.Scopes);
        commandsToTest.remove(V8Command.Scope);
        
        checkScripts();
        commandsToTest.remove(V8Command.Scripts);
        
        checkListAndChangeBreakpoints();
        commandsToTest.remove(V8Command.Listbreakpoints);
        commandsToTest.remove(V8Command.Clearbreakpoint);
        commandsToTest.remove(V8Command.Changebreakpoint);
        checkClearBreakpointGroup();
        commandsToTest.remove(V8Command.Clearbreakpointgroup);
        
        checkReferences();
        commandsToTest.remove(V8Command.References);
        
        checkRestartframe();
        commandsToTest.remove(V8Command.Restartframe);
        
        V8Debug.TestAccess.doCommand(v8dbg, "exit"); // disconnect
        lastResponse = responseHandler.getLastResponse();
        commandsToTest.remove(V8Command.Disconnect);
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        Thread.sleep(500);
        assertTrue("is connection closed", V8Debug.TestAccess.isClosed(v8dbg));
        assertTrue("is closed", responseHandler.isClosed());
        
        assertTrue("Commands remaining to test: "+commandsToTest.toString(), commandsToTest.isEmpty());
    }
    
    /**
     * Test of all exception breakpoints.
     */
    @Test
    public void testSetexceptionbreakAll() throws IOException, InterruptedException {
        // Exception breakpoints are tested explicitly
        
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        
        // Initially, we have one default breakpoint and no exception breakpoints
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertFalse(lbrb.isBreakOnUncaughtExceptions());
        V8Breakpoint[] breakpoints = lbrb.getBreakpoints();
        assertEquals(1, breakpoints.length);
        
        V8Debug.TestAccess.send(v8dbg, SetExceptionBreak.createRequest(11, V8ExceptionBreakType.all, true));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setexceptionbreak, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        SetExceptionBreak.ResponseBody serb = (SetExceptionBreak.ResponseBody) lastResponse.getBody();
        assertEquals(V8ExceptionBreakType.all, serb.getType());
        assertEquals(true, serb.isEnabled());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        ExceptionEventBody eeb = (ExceptionEventBody) lastEvent.getBody();
        assertFalse(eeb.isUncaught());
        assertEquals(LINE_EBRKP1-1, eeb.getSourceLine());
        assertEquals(8, eeb.getSourceColumn());
        assertEquals("        throw \"Test throw.\";", eeb.getSourceLineText());
        V8Script script = eeb.getScript();
        assertEquals(testFilePath, script.getName());
        assertEquals(0, script.getLineOffset());
        assertEquals(0, script.getColumnOffset());
        assertEquals(TEST_NUM_LINES, script.getLineCount());
        V8Value exception = eeb.getException();
        assertEquals("Test throw.", ((V8String) exception).getValue());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        eeb = (ExceptionEventBody) lastEvent.getBody();
        assertFalse(eeb.isUncaught());
        assertEquals(LINE_EBRKP2-1, eeb.getSourceLine());
        assertEquals(8, eeb.getSourceColumn());
        assertEquals("        throw 1.5;", eeb.getSourceLineText());
        exception = eeb.getException();
        assertEquals(1.5, ((V8Number) exception).getDoubleValue(), 1e-10);
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        eeb = (ExceptionEventBody) lastEvent.getBody();
        assertFalse(eeb.isUncaught());
        assertEquals(LINE_EBRKP3-1, eeb.getSourceLine());
        assertEquals(8, eeb.getSourceColumn());
        assertEquals("        throw false;", eeb.getSourceLineText());
        exception = eeb.getException();
        assertEquals(false, ((V8Boolean) exception).getValue());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        eeb = (ExceptionEventBody) lastEvent.getBody();
        assertFalse(eeb.isUncaught());
        assertEquals(LINE_EBRKP4-1, eeb.getSourceLine());
        assertEquals(10, eeb.getSourceColumn());
        assertEquals("        a.getOwnPropertyName(\"oops\");", eeb.getSourceLineText());
        exception = eeb.getException();
        V8Object err = (V8Object) exception;
        assertEquals(V8Value.Type.Error, err.getType());
        //assertEquals("TypeError: Object 10 has no method 'getOwnPropertyName'", err.getText());
        assertTrue(err.getText(), err.getText().startsWith("TypeError: "));
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        eeb = (ExceptionEventBody) lastEvent.getBody();
        assertFalse(eeb.isUncaught());
        assertEquals(LINE_EBRKP5-1, eeb.getSourceLine());
        assertEquals(14, eeb.getSourceColumn());
        assertEquals("        throw new Error(\"Test Error.\");", eeb.getSourceLineText());
        exception = eeb.getException();
        err = (V8Object) exception;
        assertEquals(V8Value.Type.Error, err.getType());
        assertEquals("Error: Test Error.", err.getText());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        eeb = (ExceptionEventBody) lastEvent.getBody();
        // assertTrue(eeb.isUncaught()); - trurns false, but should return true IMHO!
        assertEquals(LINE_EBRKP6-1, eeb.getSourceLine());
        assertEquals(4, eeb.getSourceColumn());
        assertEquals("    throw \"exiting...\";", eeb.getSourceLineText());
        exception = eeb.getException();
        assertEquals("exiting...", ((V8String) exception).getValue());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        
        
        /*V8Debug.TestAccess.doCommand(v8dbg, "exit"); // disconnect
        lastResponse = responseHandler.getLastResponse();
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());*/
        Thread.sleep(500);
        assertTrue("is connection closed", V8Debug.TestAccess.isClosed(v8dbg));
        assertTrue("is closed", responseHandler.isClosed());
    }
    
    /**
     * Test of uncaught exception breakpoints.
     */
    //@Test - TODO: Do not test now, debugger does not break on uncaught exceptions.
    public void testSetexceptionbreakUncaught() throws IOException, InterruptedException {
        // Exception breakpoints are tested explicitly
        
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        
        // First, activate break on uncaught exceptions:
        V8Debug.TestAccess.send(v8dbg, Flags.createRequest(123, Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION, true));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Flags, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        Flags.ResponseBody frb = (Flags.ResponseBody) lastResponse.getBody();
        Map<String, Boolean> flags = frb.getFlags();
        assertTrue(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION, flags.get(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION));
        
        // Initially, we have one default breakpoint and no exception breakpoints
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertTrue(lbrb.isBreakOnUncaughtExceptions());
        V8Breakpoint[] breakpoints = lbrb.getBreakpoints();
        assertEquals(1, breakpoints.length);
        
        V8Debug.TestAccess.send(v8dbg, SetExceptionBreak.createRequest(11, V8ExceptionBreakType.uncaught, true));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setexceptionbreak, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        SetExceptionBreak.ResponseBody serb = (SetExceptionBreak.ResponseBody) lastResponse.getBody();
        assertEquals(V8ExceptionBreakType.uncaught, serb.getType());
        assertEquals(true, serb.isEnabled());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (V8Event.Kind.AfterCompile == lastEvent.getKind());
        assertEquals(V8Event.Kind.Exception, lastEvent.getKind());
        ExceptionEventBody eeb = (ExceptionEventBody) lastEvent.getBody();
        assertTrue(eeb.isUncaught()); //- trurns false, but should return true IMHO!
        assertEquals(LINE_EBRKP6-1, eeb.getSourceLine());
        assertEquals(4, eeb.getSourceColumn());
        assertEquals("    throw \"exiting...\";", eeb.getSourceLineText());
        V8Value exception = eeb.getException();
        assertEquals("exiting...", ((V8String) exception).getValue());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        
        
        /*V8Debug.TestAccess.doCommand(v8dbg, "exit"); // disconnect
        lastResponse = responseHandler.getLastResponse();
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());*/
        Thread.sleep(500);
        assertTrue("is connection closed", V8Debug.TestAccess.isClosed(v8dbg));
        assertTrue("is closed", responseHandler.isClosed());
    }
    
    private void checkFrame(long column, long line, String sourceLineText) throws IOException, InterruptedException {
        checkFrame(new long[] {column}, line, sourceLineText);
    }
    
    private void checkFrame(long[] columns, long line, String sourceLineText) throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        V8Body body = lastResponse.getBody();
        Frame.ResponseBody fbody = (Frame.ResponseBody) body;
        V8Frame frame = fbody.getFrame();
        if (columns.length == 1) {
            assertEquals(columns[0], frame.getColumn());
        } else {
            long fc = frame.getColumn();
            boolean match = false;
            for (long c : columns) {
                if (c == fc) {
                    match = true;
                }
            }
            assertTrue("Frame column "+fc+" does not match expected colums: "+Arrays.toString(columns), match);
        }
        assertEquals(line, frame.getLine());
        assertEquals(sourceLineText, frame.getSourceLineText());
        
        long scriptRef = frame.getScriptRef();
        V8Debug.TestAccess.send(v8dbg, Lookup.createRequest(333, new long[]{ scriptRef }, false));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Lookup, lastResponse.getCommand());
        Lookup.ResponseBody lrb = (Lookup.ResponseBody) lastResponse.getBody();
        V8Value scriptValue = lrb.getValuesByHandle().get(scriptRef);
        assertEquals(testFilePath + " (lines: "+TEST_NUM_LINES+")", scriptValue.getText());
        assertEquals(V8Value.Type.Script, scriptValue.getType());
        assertEquals(scriptRef, scriptValue.getHandle());
        
        V8Script script = ((V8ScriptValue) scriptValue).getScript();
        assertNotNull(script);
        assertEquals(0, script.getColumnOffset());
        assertEquals(V8Script.CompilationType.API, script.getCompilationType());
        assertNull(script.getData());
        assertNull(script.getEvalFromLocation());
        assertNull(script.getEvalFromScript());
        long scriptId = script.getId();
        assertEquals(script.getName(), V8Debug.TestAccess.getScript(v8dbg, scriptId).getName());
        assertEquals(TEST_NUM_LINES, script.getLineCount());
        assertEquals(0, script.getLineOffset());
        assertEquals(testFilePath, script.getName());
        assertEquals(V8Script.Type.NORMAL, script.getScriptType());
        assertNull(script.getSource());
        assertEquals(TEST_NUM_CHARS, script.getSourceLength().getValue());
        assertEquals("(function (exports, require, module, __filename, __dirname) { /* \n * DO NOT ALTE", script.getSourceStart());
    }
    
    private void checkLongStackFrames() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "bt");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Backtrace, lastResponse.getCommand());
        Backtrace.ResponseBody brb = (Backtrace.ResponseBody) lastResponse.getBody();
        if (brb.getTotalFrames() > brb.getFrames().length) {
            // Have the first request, wait for the second one...
            lastResponse = responseHandler.getLastResponse();
            assertEquals(V8Command.Backtrace, lastResponse.getCommand());
            brb = (Backtrace.ResponseBody) lastResponse.getBody();
        }
        assertEquals(0, brb.getFromFrame());
        int numFrames = 20; // Should have more than 20 frames
        assertEquals(0, brb.getFromFrame());
        assertTrue("Total frames = "+brb.getTotalFrames(), brb.getTotalFrames() > numFrames);
        assertEquals(brb.getTotalFrames(), brb.getToFrame());
        V8Frame[] frames = brb.getFrames();
        assertEquals(brb.getTotalFrames(), frames.length);
        
        V8Frame f = frames[0];
        assertEquals(LINE_BRKP_LONG_STACK-1, f.getLine());
        assertEquals(8, f.getColumn());
        assertEquals(POS_BRKP_LONG_STACK, f.getPosition());
        
        Map<String, ReferencedValue> argumentRefs = f.getArgumentRefs();
        assertEquals(1, argumentRefs.size());
        ReferencedValue nVar = argumentRefs.get("n");
        assertNotNull(nVar);
        assertTrue(nVar.hasValue());
        assertNotNull(nVar.getValue());
        assertEquals(0l, ((V8Number) nVar.getValue()).getLongValue());
        
        Map<String, ReferencedValue> localRefs = f.getLocalRefs();
        assertEquals(1, localRefs.size());
        ReferencedValue local_iVar = localRefs.get("local_i");
        assertNotNull(local_iVar);
        assertTrue(local_iVar.hasValue());
        assertNotNull(local_iVar.getValue());
        assertEquals(10l, ((V8Number) local_iVar.getValue()).getLongValue());
        
        ReferencedValue functionRef = f.getFunction();
        assertNotNull(functionRef);
        assertTrue(functionRef.hasValue());
        V8Function function = (V8Function) functionRef.getValue();
        assertEquals("longStack", function.getName());
        assertEquals(testFilePath, V8Debug.TestAccess.getScript(v8dbg, function.getScriptId().getValue()).getName());
        
        ReferencedValue receiverRef = f.getReceiver();
        assertNotNull(receiverRef);
        assertTrue(receiverRef.hasValue());
        V8Object receiver = (V8Object) receiverRef.getValue();
        assertEquals("global", receiver.getClassName());
        
        assertEquals("#00 longStack(n=0) "+testFilePath+" line "+LINE_BRKP_LONG_STACK+" column 9 (position "+(POS_BRKP_LONG_STACK+1)+")", f.getText());
        
        long globalScopeIndex = -1;
        
        V8Scope[] scopes = f.getScopes();
        Set<V8Scope.Type> scopeTypes = new HashSet<>();
        if (scopes.length == 3) { // node.js v0.10.36
            assertEquals(3, scopes.length);
            scopeTypes.add(V8Scope.Type.Local);
            scopeTypes.add(V8Scope.Type.Closure);
            scopeTypes.add(V8Scope.Type.Global);
        } else {  // io.js v1.0.4
            assertEquals(4, scopes.length);
            scopeTypes.add(V8Scope.Type.Local);
            scopeTypes.add(V8Scope.Type.Closure);
            scopeTypes.add(V8Scope.Type.Global);
        }
        for (V8Scope s : scopes) {
            scopeTypes.remove(s.getType());
            if (V8Scope.Type.Global.equals(s.getType())) {
                globalScopeIndex = s.getIndex();
            }
        }
        assertTrue(scopeTypes.toString(), scopeTypes.isEmpty());
        
        long scriptRef = f.getScriptRef();
        
        for (int i = 1; i <= 20; i++) {
            f = frames[i];
            assertEquals(LINE_BRKP_LONG_STACK-3-1, f.getLine());
            assertEquals(8, f.getColumn());
            assertEquals(POS_BRKP_LONG_STACK-96, f.getPosition());
            
            argumentRefs = f.getArgumentRefs();
            assertEquals(1, argumentRefs.size());
            nVar = argumentRefs.get("n");
            assertNotNull(nVar);
            assertEquals((long) i, ((V8Number) nVar.getValue()).getLongValue());
            
            String nf = Integer.toString(i);
            if (nf.length() == 1) {
                nf = "0"+nf;
            }
            assertEquals("        longStack(n-1);", f.getSourceLineText());
            assertEquals("#"+nf+" longStack(n="+i+") "+testFilePath+" line "+(LINE_BRKP_LONG_STACK-3)+" column 9 (position "+(POS_BRKP_LONG_STACK-95)+")", f.getText());
        }
        
        f = frames[21];
        assertEquals(LINE_FNC+1-1, f.getLine());
        assertEquals(4, f.getColumn());
        assertEquals(POS_FNC, f.getPosition());
        argumentRefs = f.getArgumentRefs();
        assertEquals(0, argumentRefs.size());
        localRefs = f.getLocalRefs();
        assertEquals(1, localRefs.size());
        ReferencedValue clVar = localRefs.get("cl");
        assertEquals(V8Value.Type.Undefined, clVar.getValue().getType());
        assertEquals("    longStack(20);", f.getSourceLineText());
        
        ReferencedValue[] referencedValues = lastResponse.getReferencedValues();
        assertNotNull(referencedValues);
        assertEquals(3, referencedValues.length);
        assertEquals(scriptRef, referencedValues[0].getReference());
        assertTrue(referencedValues[0].hasValue());
        assertNotNull(referencedValues[0].getValue());
        V8ScriptValue script = (V8ScriptValue) referencedValues[0].getValue();
        assertEquals(testFilePath, script.getScript().getName());
        
        assertTrue(globalScopeIndex >= 0);
        responseHandler.clearLastResponse();
        V8Debug.TestAccess.doCommand(v8dbg, "scope "+globalScopeIndex);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scope, lastResponse.getCommand());
        Scope.ResponseBody srb = (Scope.ResponseBody) lastResponse.getBody();
        V8Scope scope = srb.getScope();
        assertEquals(V8Scope.Type.Global, scope.getType());
        assertEquals(globalScopeIndex, scope.getIndex());
        assertFalse(scope.getFrameIndex().hasValue());
        ReferencedValue<V8Object> sobjr = scope.getObject();
        assertEquals("#<ScopeMirror>", scope.getText());
        long sobjHandle = sobjr.getReference();
        assertFalse(sobjr.hasValue());
        referencedValues = lastResponse.getReferencedValues();
        V8Object sobj = null;
        for (ReferencedValue rv : referencedValues) {
            if (rv.getReference() == sobjHandle) {
                assertTrue(rv.hasValue());
                sobj = (V8Object) rv.getValue();
                break;
            }
        }
        assertNotNull("Did not find global scope object", sobj);
        assertEquals("Object", sobj.getClassName());
        assertTrue(sobj.getConstructorFunctionHandle().hasValue());
        assertTrue(sobj.getProtoObjectHandle().hasValue());
        assertTrue(sobj.getPrototypeObjectHandle().hasValue());
        Map<String, V8Object.Property> properties = sobj.getProperties();
        assertNotNull(properties);
        V8Object.Property prop = properties.get("v8debug");
        if (prop.getType() == V8Object.Property.Type.Normal) {
            // io.js v1.0.4
            assertEquals(V8Object.Property.Type.Normal, prop.getType());
        } else {
            // node.js v0.10.36
            assertNull(String.valueOf(prop.getType()), prop.getType());
        }
        assertEquals(V8Object.Property.ATTR_DONT_ENUM, prop.getAttributes());
        prop = properties.get("console");
        assertEquals(V8Object.Property.Type.Callbacks, prop.getType());
        assertEquals(V8Object.Property.ATTR_NONE, prop.getAttributes());
    }
    
    private void checkVariables() throws IOException, InterruptedException {
        checkLocalVar("int", -2l, false);
        checkLocalVar("double", -2.3, false);
        checkLocalVar("dnan", Double.NaN, false);
        checkLocalVar("dinf", Double.POSITIVE_INFINITY, false);
        checkLocalVar("dninf", Double.NEGATIVE_INFINITY, false);
        checkLocalVar("boolean", true, false);
        checkLocalVar("s1", "", false);
        checkLocalVar("s2", "abc", false);
        checkLocalVar("s3", "s", false);
        checkLocalVar("s4", "\u0011", false);
        checkLocalVar("f1",
                new FunctionCheck("", "f1", "function (){}", testFilePath, -1, POS_VAR_F1, LINE_BRKP_VARS-5-1, 21, null, null),
                false);
        checkLocalVar("f2",
                new FunctionCheck("myF2", "", "function myF2(){ return true; }", testFilePath, -1, POS_VAR_F2, LINE_BRKP_VARS-4-1, 26, null, null),
                false);
        checkLocalVar("undef", VALUE_UNDEFINED, false);
        checkLocalVar("nul", null, false);
        checkLocalVar("obj", new ObjectCheck("Object", null, null, "#<Object>"), false);
    }
    
    private void checkArrays() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        V8Body body = lastResponse.getBody();
        Frame.ResponseBody fbody = (Frame.ResponseBody) body;
        V8Frame frame = fbody.getFrame();
        Map<String, ReferencedValue> refs = frame.getLocalRefs();
        ReferencedValue[] referencedValues = lastResponse.getReferencedValues();
        Map<Long, V8Value> valuesByRefs = new HashMap<>();
        for (int i = 0; i < referencedValues.length; i++) {
            valuesByRefs.put(referencedValues[i].getReference(), referencedValues[i].getValue());
        }
        ReferencedValue rv = refs.get("empty");
        long ref = rv.getReference();
        V8Object obj = (V8Object) valuesByRefs.get(ref);
        assertEquals("Array", obj.getClassName());
        V8Object.Array array;
        array = obj.getArray();
        assertNotNull(array);
        assertTrue(array.isContiguous());
        assertEquals(0, array.getContiguousReferences().length);
        try {
            array.getReferenceAt(0);
            fail("No exception when retrieving an element from an empty array was thrown.");
        } catch (NoSuchElementException nsex) {
        }
        
        rv = refs.get("months");
        ref = rv.getReference();
        obj = (V8Object) valuesByRefs.get(ref);
        assertEquals("Array", obj.getClassName());
        array = obj.getArray();
        assertNotNull(array);
        assertTrue(array.isContiguous());
        long[] contigArray = array.getContiguousReferences();
        assertEquals(3, contigArray.length);
        array.getReferenceAt(0);
        array.getReferenceAt(2);
        try {
            array.getReferenceAt(3);
            fail("No exception when retrieving an element from an index larger than the array length, was thrown.");
        } catch (NoSuchElementException nsex) {
        }
        // check months values:
        String strRefs = Arrays.toString(contigArray);
        strRefs = strRefs.substring(1, strRefs.length() - 1).trim();
        V8Debug.TestAccess.doCommand(v8dbg, "lookup "+strRefs);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Lookup, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        body = lastResponse.getBody();
        Lookup.ResponseBody lrb = (Lookup.ResponseBody) body;
        V8Value val = lrb.getValuesByHandle().get(contigArray[0]);
        checkValue("months[0]", val, "Jan");
        val = lrb.getValuesByHandle().get(contigArray[1]);
        checkValue("months[1]", val, "Feb");
        val = lrb.getValuesByHandle().get(contigArray[2]);
        checkValue("months[2]", val, "Mar");
        
        rv = refs.get("d2");
        ref = rv.getReference();
        obj = (V8Object) valuesByRefs.get(ref);
        assertEquals("Array", obj.getClassName());
        array = obj.getArray();
        assertNotNull(array);
        contigArray = array.getContiguousReferences();
        assertEquals(3, contigArray.length);
        // check d2 values:
        assertEquals(3, array.getLength());
        strRefs = Arrays.toString(contigArray);
        strRefs = strRefs.substring(1, strRefs.length() - 1).trim();
        V8Debug.TestAccess.doCommand(v8dbg, "lookup "+strRefs);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Lookup, lastResponse.getCommand());
        lrb = (Lookup.ResponseBody) lastResponse.getBody();
        val = lrb.getValuesByHandle().get(contigArray[0]);
        obj = (V8Object) val;
        assertEquals("Array", obj.getClassName());
        assertNotNull(obj.getArray());
        assertEquals(0, obj.getArray().getLength());
        val = lrb.getValuesByHandle().get(contigArray[2]);
        obj = (V8Object) val;
        assertEquals("Array", obj.getClassName());
        assertNotNull(obj.getArray());
        assertEquals(2, obj.getArray().getLength());
        
        rv = refs.get("d3");
        ref = rv.getReference();
        obj = (V8Object) valuesByRefs.get(ref);
        assertEquals("Array", obj.getClassName());
        array = obj.getArray();
        assertNotNull(array);
        assertFalse(array.isContiguous());
        try {
            array.getContiguousReferences();
            fail("The array is not contiguous, shold throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException uoex) {}
        // check d3 values:
        assertEquals(56, array.getLength());
        V8Object.IndexIterator indexIterator = array.getIndexIterator();
        assertTrue(indexIterator.hasNextIndex());
        assertEquals(0, indexIterator.nextIndex());
        assertTrue(indexIterator.hasNextIndex());
        assertEquals(1, indexIterator.nextIndex());
        assertTrue(indexIterator.hasNextIndex());
        assertEquals(2, indexIterator.nextIndex());
        assertTrue(indexIterator.hasNextIndex());
        assertEquals(3, indexIterator.nextIndex());
        assertTrue(indexIterator.hasNextIndex());
        assertEquals(55, indexIterator.nextIndex());
        assertFalse(indexIterator.hasNextIndex());
        try {
            indexIterator.nextIndex();
            fail("Expecting NoSuchElementException.");
        } catch (NoSuchElementException nse) {}
        new ObjectCheck("Array",
                        new String[]{ "a", "10000000000", "10000000000000",
                                      "1.5", "-2" },
                        new Object[]{ "b", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                      5.1, -3l },
                        "#<Array>").check(obj);
        
        rv = refs.get("d4");
        ref = rv.getReference();
        obj = (V8Object) valuesByRefs.get(ref);
        assertEquals("Array", obj.getClassName());
        array = obj.getArray();
        assertNotNull(array); // There should be an empty array
        assertEquals(0, array.getLength());
        assertTrue(array.isContiguous());
        assertEquals(0, array.getContiguousReferences().length);
        new ObjectCheck("Array",
                        new String[]{ "-1" },
                        new Object[]{ Double.NEGATIVE_INFINITY },
                        "#<Array>").check(obj);
    }
    
    private void checkObjects() throws IOException, InterruptedException {
        checkLocalVar("o1", new ObjectCheck("Object", new String[]{ "0" }, new Object[]{ -1l }, "#<Object>"), false);
        ObjectCheck o2Check = new ObjectCheck("Object", new String[]{ "a" }, new Object[]{ "b" }, "#<Object>");
        checkLocalVar("o2", o2Check, false);
        checkLocalVar("o3", new ObjectCheck("Object", new String[]{ "o" }, new Object[]{ o2Check }, "#<Object>"), false);
        checkLocalVar("str", "This is a string", false);
        checkLocalVar("o4", new ObjectCheck("Object",
                new String[]{ "0", "1",
                              "ab",
                              "null", "true", "3.3333333", "500",
                              "undefined",
                              "NaN",
                              "Infinity",
                              "12000",
                              ""
                            },
                new Object[]{ new ObjectCheck("Object", null, null, "#<Object>"), 11l,
                              new ObjectCheck("Object",
                                              new String[]{ "vol", "year",
                                                            "fnc"
                                                          },
                                              new Object[]{ "bbb", 2014l,
                                                            new FunctionCheck("", "o4.ab.fnc", "function (i) { return i+1; }", testFilePath, -1, POS_O4_AB_FNC, LINE_O4_AB_FNC-1, 34, null, null)
                                                          },
                                              "#<Object>"),
                              0l, false, "three and third", "five hundred",
                              new ObjectCheck("Object", null, null, "#<Object>"),
                              new ObjectCheck("Array", null, new Object[]{ Double.NaN }, "#<Array>"),
                              new ObjectCheck("Array", null, new Object[]{ }, "#<Array>"),
                              12e-3,
                              "Empty"
                            },
                "#<Object>"), false);
        checkLocalVar("o5", null, false);
    }
    
    private void checkSetVars() throws IOException, InterruptedException {
        checkSetVars("o1", V8Value.Type.Number, "10", 0l, null);
        checkSetVars("o2", V8Value.Type.Number, "3.14", 0l, null);
        checkSetVars("o3", V8Value.Type.Boolean, "", 0l, null);
        checkSetVars("o3", V8Value.Type.Boolean, "something", 0l, null);
        checkSetVars("o1", V8Value.Type.String, "A test String", 0l, null);
    }
    private void checkSetVars(String varName, V8Value.Type type, String description, long scope, Long frame) throws IOException, InterruptedException {
        V8Debug.TestAccess.send(v8dbg, SetVariableValue.createRequest(345, varName, new NewValue(type, description), scope, frame));
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.SetVariableValue, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        SetVariableValue.ResponseBody svrb = (SetVariableValue.ResponseBody) lastResponse.getBody();
        V8Value newValue = svrb.getNewValue();
        assertEquals(type, newValue.getType());
        switch(type) {
            case Boolean:
                assertEquals(description != null && !description.isEmpty(), ((V8Boolean) newValue).getValue());
                break;
            case Null:
                break;
            case Number:
                V8Number num = (V8Number) newValue;
                if (V8Number.Kind.Long.equals(num.getKind())) {
                    assertEquals(description, Long.toString(num.getLongValue()));
                } else {
                    assertEquals(description, Double.toString(num.getDoubleValue()));
                }
                break;
            case String:
                assertEquals(description, ((V8String) newValue).getValue());
                break;
            case Undefined:
                break;
        }
    }

    private void checkThreads() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "threads");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Threads, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage());
        Threads.ResponseBody trb = (Threads.ResponseBody) lastResponse.getBody();
        assertEquals(1, trb.getNumThreads());
        Map<Long, Boolean> ids = trb.getIds();
        assertEquals(1, ids.size());
        assertTrue(ids.containsKey(1l));
        Boolean isCurrent = ids.get(1l);
        assertTrue(isCurrent);
    }

    private void checkV8Flags() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "flags use-strict");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.V8flags, lastResponse.getCommand());
        // v8flags command is not supported in io.js v1.0.4:
        if (lastResponse.getErrorMessage() != null) {
            System.err.println(lastResponse.getErrorMessage());
        } else {
            assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
            assertTrue(lastResponse.isSuccess());
        }
    }

    private void checkFlags() throws IOException, InterruptedException {
        // Get all flags first:
        V8Debug.TestAccess.send(v8dbg, Flags.createRequest(123));
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Flags, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        Flags.ResponseBody frb = (Flags.ResponseBody) lastResponse.getBody();
        Map<String, Boolean> flags = frb.getFlags();
        assertTrue(Flags.FLAG_BREAK_POINTS_ACTIVE, flags.get(Flags.FLAG_BREAK_POINTS_ACTIVE));
        assertFalse(Flags.FLAG_BREAK_ON_CAUGHT_EXCEPTION, flags.get(Flags.FLAG_BREAK_ON_CAUGHT_EXCEPTION));
        assertFalse(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION, flags.get(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION));
        
        // Set a flag
        V8Debug.TestAccess.send(v8dbg, Flags.createRequest(123, Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION, true));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Flags, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        frb = (Flags.ResponseBody) lastResponse.getBody();
        flags = frb.getFlags();
        assertTrue(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION, flags.get(Flags.FLAG_BREAK_ON_UNCAUGHT_EXCEPTION));
    }

    private void checkGC() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "gc");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Gc, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        GC.ResponseBody gcrb = (GC.ResponseBody) lastResponse.getBody();
        assertTrue(gcrb.getBefore() > 1000000);
        assertTrue(gcrb.getAfter() > 1000000);
    }

    private void checkVersion() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "version");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Version, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        Version.ResponseBody vrb = (Version.ResponseBody) lastResponse.getBody();
        String version = vrb.getVersion();
        assertNotNull(version);
    }

    private String checkSource(int frame, int fromLine, int toLine) throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "source "+frame+" "+fromLine+" "+toLine);
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Source, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        Source.ResponseBody srb = (Source.ResponseBody) lastResponse.getBody();
        if (frame < 3) {
            assertEquals(TEST_NUM_LINES, srb.getTotalLines());
        }
        assertEquals(fromLine, srb.getFromLine());
        assertEquals(toLine, srb.getToLine());
        if (frame < 3) {
            assertTrue("From position = "+srb.getFromPosition(), srb.getFromPosition() > 1000 && srb.getFromPosition() < TEST_NUM_CHARS);
            assertTrue("To position = "+srb.getToPosition(), srb.getToPosition() > 1000 && srb.getToPosition() <= TEST_NUM_CHARS);
        }
        assertTrue(srb.getFromPosition() < srb.getToPosition());
        assertNotNull(srb.getSource());
        assertTrue(!srb.getSource().isEmpty());
        return srb.getSource();
    }

    private void checkScopes() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "scopes");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scopes, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        Scopes.ResponseBody ssrb = (Scopes.ResponseBody) lastResponse.getBody();
        assertEquals(0, ssrb.getFromScope());
        V8Scope[] scopes = ssrb.getScopes();
        if (ssrb.getTotalScopes() == 3) { // node.js v0.10.36
            assertEquals(3, ssrb.getTotalScopes());
            assertEquals(3, ssrb.getToScope());
            assertEquals(3, scopes.length);
            assertEquals(V8Scope.Type.Local, scopes[0].getType());
            assertEquals(V8Scope.Type.Closure, scopes[1].getType());
            assertEquals(V8Scope.Type.Global, scopes[2].getType());
        } else {  // io.js v1.0.4
            assertEquals(4, ssrb.getTotalScopes());
            assertEquals(4, ssrb.getToScope());
            assertEquals(4, scopes.length);
            assertEquals(V8Scope.Type.Local, scopes[0].getType());
            assertEquals(V8Scope.Type.Closure, scopes[1].getType());
            assertEquals(V8Scope.Type.Module, scopes[2].getType());
            assertEquals(V8Scope.Type.Global, scopes[3].getType());
        }
        long[] handles = new long[scopes.length];
        for (int i = 0; i < scopes.length; i++) {
            assertEquals(i, scopes[i].getIndex());
            assertEquals(0l, scopes[i].getFrameIndex().getValue());
            assertEquals("#<ScopeMirror>", scopes[i].getText());
            assertFalse(scopes[i].getObject().hasValue());
            handles[i] = scopes[i].getObject().getReference();
        }
        V8Value scopeVal = lastResponse.getReferencedValue(handles[0]);
        checkValue("scope[0]", scopeVal, new ObjectCheck("Object", new String[] { "a", "aa", "b", "c" }, new Object[]{ 1l, 0l, 3l, 3l }, "#<Object>"));
        scopeVal = lastResponse.getReferencedValue(handles[1]);
        checkValue("scope[1]", scopeVal, new ObjectCheck("Object", new String[] { "glob_n" }, new Object[]{ 100l }, "#<Object>"));
        scopeVal = lastResponse.getReferencedValue(handles[scopes.length - 1]);
        checkValue("scope["+(scopes.length - 1)+"]", scopeVal, new ObjectCheck("Object", new String[] { "global" }, new Object[]{ new ObjectCheck("global", null, null, "#<Object>") }, "#<Object>"));
        
        //V8Debug.TestAccess.doCommand(v8dbg, "step in 2");
        V8Debug.TestAccess.send(v8dbg, Continue.createRequest(123, V8StepAction.in, 2));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        V8Event lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(8, LINE_SCOPE_INNER_FUNC+2-1, "        a = a + b;");
        
        V8Debug.TestAccess.doCommand(v8dbg, "scope 0 0");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scope, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        Scope.ResponseBody srb = (Scope.ResponseBody) lastResponse.getBody();
        V8Scope scope = srb.getScope();
        scopeVal = lastResponse.getReferencedValue(scope.getObject().getReference());
        checkValue("scope", scopeVal, new ObjectCheck("Object", new String[] { "b", "c", "d" }, new Object[]{ 20l, 30l, 40l }, "#<Object>"));
        assertEquals(3, ((V8Object) scopeVal).getProperties().size());
        
        V8Debug.TestAccess.doCommand(v8dbg, "scope 1 0");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scope, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scope.ResponseBody) lastResponse.getBody();
        scope = srb.getScope();
        scopeVal = lastResponse.getReferencedValue(scope.getObject().getReference());
        checkValue("scope 1 0", scopeVal, new ObjectCheck("Object", new String[] { "a" }, new Object[]{ 1l }, "#<Object>"));
        assertEquals(1, ((V8Object) scopeVal).getProperties().size());
        
        V8Debug.TestAccess.doCommand(v8dbg, "scope 0 1");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scope, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scope.ResponseBody) lastResponse.getBody();
        scope = srb.getScope();
        scopeVal = lastResponse.getReferencedValue(scope.getObject().getReference());
        checkValue("scope 0 1", scopeVal, new ObjectCheck("Object", new String[] { "a", "aa", "b", "c", "inner" }, new Object[]{ 1l, 0l, 3l, 3l,
                new FunctionCheck("inner", "", "function inner() {\n" +
                                                "        var b = 20, c = 30, d = 40;\n" +
                                                "        a = a + b;\n" +
                                                "    }",
                                  testFilePath, -1, POS_SCOPE_INNER_FUNC, LINE_SCOPE_INNER_FUNC-1, 18, null, null) }, "#<Object>"));
        assertEquals(5, ((V8Object) scopeVal).getProperties().size());
    }

    private void checkScripts() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "scripts");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        Scripts.ResponseBody srb = (Scripts.ResponseBody) lastResponse.getBody();
        V8Script[] scripts = srb.getScripts();
        assertTrue(scripts.length > 5);
        Map<String, V8Script> scriptsByName = new HashMap<>();
        for (int i = 0; i < scripts.length; i++) {
            scriptsByName.put(scripts[i].getName(), scripts[i]);
            assertNull(scripts[i].getData());
            assertNull(scripts[i].getEvalFromLocation());
            assertNull(scripts[i].getEvalFromScript());
        }
        V8Script testScript = scriptsByName.get(testFilePath);
        assertEquals(TEST_NUM_LINES, testScript.getLineCount());
        assertEquals(TEST_NUM_CHARS, testScript.getSourceLength().getValue());
        assertEquals(0, testScript.getLineOffset());
        assertEquals(0, testScript.getColumnOffset());
        assertEquals(testFilePath, V8Debug.TestAccess.getScript(v8dbg, testScript.getId()).getName());
        assertEquals("(function (exports, require, module, __filename, __dirname) { /* \n * DO NOT ALTE", testScript.getSourceStart());
        assertEquals(V8Script.Type.NORMAL, testScript.getScriptType());
        assertEquals(V8Script.CompilationType.API, testScript.getCompilationType());
        long contextRef = testScript.getContext().getReference();
        assertEquals(testFilePath+" (lines: "+TEST_NUM_LINES+")", testScript.getText());
        V8Value contextValue = lastResponse.getReferencedValue(contextRef);
        assertEquals(V8Value.Type.Context, contextValue.getType());
        
        assertNotNull(scriptsByName.get("node.js"));
        assertNotNull(scriptsByName.get("events.js"));
        assertNotNull(scriptsByName.get("module.js"));
        assertNotNull(scriptsByName.get("fs.js"));
        
        V8Debug.TestAccess.send(v8dbg, Scripts.createRequest(123, new V8Script.Types(true, false, false), false));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scripts.ResponseBody) lastResponse.getBody();
        scripts = srb.getScripts();
        for (int i = 0; i < scripts.length; i++) {
            assertEquals(V8Script.Type.NATIVE, scripts[i].getScriptType());
        }
        
        V8Debug.TestAccess.send(v8dbg, Scripts.createRequest(123, new V8Script.Types(false, true, false), false));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scripts.ResponseBody) lastResponse.getBody();
        scripts = srb.getScripts();
        for (int i = 0; i < scripts.length; i++) {
            assertEquals(V8Script.Type.EXTENSION, scripts[i].getScriptType());
        }
        
        V8Debug.TestAccess.send(v8dbg, Scripts.createRequest(123, new V8Script.Types(false, false, true), false));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scripts.ResponseBody) lastResponse.getBody();
        scripts = srb.getScripts();
        for (int i = 0; i < scripts.length; i++) {
            assertEquals(V8Script.Type.NORMAL, scripts[i].getScriptType());
        }
        
        V8Debug.TestAccess.send(v8dbg, Scripts.createRequest(123, null, null, true, "TestDebug"));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scripts.ResponseBody) lastResponse.getBody();
        scripts = srb.getScripts();
        assertEquals(1, scripts.length);
        assertEquals(TEST_NUM_CHARS, scripts[0].getSource().length());
        
        int numIDs = 5;
        long[] ids = new long[numIDs];
        int i = 0;
        for (V8Script script : scriptsByName.values()) {
            ids[i++] = script.getId();
            if (i >= numIDs) {
                break;
            }
        }
        V8Debug.TestAccess.send(v8dbg, Scripts.createRequest(123, null, ids, false, null));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Scripts, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        srb = (Scripts.ResponseBody) lastResponse.getBody();
        scripts = srb.getScripts();
        assertEquals(numIDs, scripts.length);
        Set<Long> allIDs = new HashSet<>();
        for (i = 0; i < numIDs; i++) {
            allIDs.add(ids[i]);
        }
        for (i = 0; i < numIDs; i++) {
            allIDs.remove(scripts[i].getId());
        }
        assertTrue("scripts with IDs "+allIDs.toString()+" were not returned, loaded ids = "+Arrays.toString(ids), allIDs.isEmpty());
    }

    private void checkListAndChangeBreakpoints() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertTrue(lbrb.isBreakOnUncaughtExceptions()); // Changed by flags
        V8Breakpoint[] breakpoints = lbrb.getBreakpoints();
        assertEquals(6, breakpoints.length);
        for (int i = 0; i < breakpoints.length; i++) {
            V8Breakpoint b = breakpoints[i];
            assertTrue(b.isActive());
            if (i == 0) {
                assertEquals(V8Breakpoint.Type.scriptId, b.getType());
            } else {
                assertEquals(V8Breakpoint.Type.scriptName, b.getType());
                assertEquals(testFilePath, b.getScriptName());
            }
            assertFalse(b.getGroupId().hasValue());
            assertTrue(b.getLine().hasValue());
            if (i == 0) {
                assertTrue(b.getColumn().hasValue());
            } else {
                assertFalse(b.getColumn().hasValue());
            }
            assertEquals(1, b.getHitCount());
            assertEquals(0, b.getIgnoreCount());
            assertNull(b.getCondition());
            assertEquals(i+1, b.getNumber());
            V8Breakpoint.ActualLocation[] actualLocations = b.getActualLocations();
            assertEquals(1, actualLocations.length);
            assertEquals(testFilePath, V8Debug.TestAccess.getScript(v8dbg, actualLocations[0].getScriptId().getValue()).getName());
            assertTrue(actualLocations[0].getColumn() >= 0);
            assertTrue(actualLocations[0].getLine() >= 0);
        }
        
        // Test failure
        V8Debug.TestAccess.doCommand(v8dbg, "clear -1");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Clearbreakpoint, lastResponse.getCommand());
        assertTrue(lastResponse.getErrorMessage(), lastResponse.getErrorMessage().length() > 0);
        assertFalse(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        V8Debug.TestAccess.doCommand(v8dbg, "clear 1");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Clearbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ClearBreakpoint.ResponseBody cbrb = (ClearBreakpoint.ResponseBody) lastResponse.getBody();
        assertEquals(1, cbrb.getBreakpoint());
        
        V8Debug.TestAccess.doCommand(v8dbg, "clear 2");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Clearbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        cbrb = (ClearBreakpoint.ResponseBody) lastResponse.getBody();
        assertEquals(2, cbrb.getBreakpoint());
        
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertTrue(lbrb.isBreakOnUncaughtExceptions()); // Changed by flags
        breakpoints = lbrb.getBreakpoints();
        assertEquals(4, breakpoints.length);
        
        // Conditional breakpoint:
        String condition = "sum == 55";
        Long ignoreCount = 2l;
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(123, V8Breakpoint.Type.scriptName, testFilePath, (long) LINE_BRKP_COND-1, null, false, condition, ignoreCount, null));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) lastResponse.getBody();
        long cbNum = sbrb.getBreakpoint();
        
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertTrue(lbrb.isBreakOnUncaughtExceptions());
        breakpoints = lbrb.getBreakpoints();
        assertEquals(5, breakpoints.length);
        assertFalse(breakpoints[4].isActive());
        assertEquals(LINE_BRKP_COND-1, breakpoints[4].getActualLocations()[0].getLine());
        assertEquals(8, breakpoints[4].getActualLocations()[0].getColumn());
        assertEquals(condition, breakpoints[4].getCondition());
        assertEquals(ignoreCount.longValue(), breakpoints[4].getIgnoreCount());
        assertEquals(0, breakpoints[4].getHitCount());
        
        condition = null;//"sum == 1275";
        ignoreCount = 12l;
        V8Debug.TestAccess.send(v8dbg, ChangeBreakpoint.createRequest(123, cbNum, true, condition, ignoreCount));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Changebreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        assertFalse(lbrb.isBreakOnExceptions());
        assertTrue(lbrb.isBreakOnUncaughtExceptions());
        breakpoints = lbrb.getBreakpoints();
        assertEquals(5, breakpoints.length);
        assertTrue(breakpoints[4].isActive());
        assertEquals(LINE_BRKP_COND-1, breakpoints[4].getActualLocations()[0].getLine());
        assertEquals(8, breakpoints[4].getActualLocations()[0].getColumn());
        assertEquals(condition, breakpoints[4].getCondition());
        assertEquals(ignoreCount.longValue(), breakpoints[4].getIgnoreCount());
        assertEquals(0, breakpoints[4].getHitCount());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        V8Event lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(8, LINE_BRKP_COND-1, "        sum += i;       // count and conditional breakpoint");
        checkLocalVar("i", 12l, false);
        checkLocalVar("sum", 66l, false);
        
        condition = "sum == 1275";
        ignoreCount = 0l;
        V8Debug.TestAccess.send(v8dbg, ChangeBreakpoint.createRequest(123, cbNum, true, condition, ignoreCount));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Changebreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkLocalVar("i", 51l, false);
        checkLocalVar("sum", 1275l, false);
    }
    
    private void checkClearBreakpointGroup() throws IOException, InterruptedException {
        long testGroupId = 654321;
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(123,
                                                                   V8Breakpoint.Type.scriptName,
                                                                   testFilePath,
                                                                   (long) LINE_BRKP_COND+2-1,
                                                                   null, null, null, null,
                                                                   testGroupId));
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        SetBreakpoint.ResponseBody sbrb = (SetBreakpoint.ResponseBody) lastResponse.getBody();
        long cbNum1 = sbrb.getBreakpoint();
        
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(123,
                                                                   V8Breakpoint.Type.scriptName,
                                                                   testFilePath,
                                                                   (long) LINE_BRKP_COND+3-1,
                                                                   null, null, null, null,
                                                                   testGroupId));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        sbrb = (SetBreakpoint.ResponseBody) lastResponse.getBody();
        assertEquals(cbNum1+1, sbrb.getBreakpoint());
        
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(123,
                                                                   V8Breakpoint.Type.scriptName,
                                                                   testFilePath,
                                                                   (long) LINE_BRKP_COND+4-1,
                                                                   null, null, null, null,
                                                                   testGroupId));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        sbrb = (SetBreakpoint.ResponseBody) lastResponse.getBody();
        long cbNum2 = sbrb.getBreakpoint();
        assertEquals(cbNum1+2, cbNum2);
        
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        V8Breakpoint[] breakpoints = lbrb.getBreakpoints();
        long cbNumGroup = cbNum2 - cbNum1 + 1;
        assertEquals(5 + cbNumGroup, breakpoints.length);
        
        for (long i = cbNum1; i <= cbNum2; i++) {
            V8Breakpoint gbp = breakpoints[(int) i - 2 - 1]; // 2 breakpoints deleted in tests before.
            assertTrue(gbp.getGroupId().hasValue());
            assertEquals(testGroupId, gbp.getGroupId().getValue());
        }
        
        V8Debug.TestAccess.send(v8dbg, ClearBreakpointGroup.createRequest(123, testGroupId));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Clearbreakpointgroup, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ClearBreakpointGroup.ResponseBody cbgrb = (ClearBreakpointGroup.ResponseBody) lastResponse.getBody();
        long[] bpCleared = cbgrb.getBreakpointsCleared();
        assertEquals(cbNumGroup, bpCleared.length);
        int j = 0;
        for (long i = cbNum1; i <= cbNum2; i++) {
            assertEquals(i, bpCleared[j++]);
        }
    }

    private void checkReferences() throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "stop at "+testFilePath+":"+LINE_BRKP_REFS);
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        checkBRResponse((SetBreakpoint.ResponseBody) lastResponse.getBody(), 11, testFilePath, LINE_BRKP_REFS-1, null, 4);
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertTrue(lastResponse.isRunning());
        V8Event lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(4, LINE_BRKP_REFS-1, "    r4();               // breakpoint");
        
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        Frame.ResponseBody fbody = (Frame.ResponseBody) lastResponse.getBody();
        V8Frame frame = fbody.getFrame();
        Map<String, ReferencedValue> refs = frame.getLocalRefs();
        ReferencedValue r1 = refs.get("r1");
        V8Value r1Val = lastResponse.getReferencedValue(r1.getReference());
        long ref = ((V8Object) r1Val).getProperties().get("ref").getReference();
        
        V8Debug.TestAccess.doCommand(v8dbg, "references "+ref);
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.References, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        References.ResponseBody rrb = (References.ResponseBody) lastResponse.getBody();
        V8Value[] references = rrb.getReferences();
        if (references.length == 3) {
            // node.js v0.10.36
            assertEquals(3, references.length);
        } else {
            // io.js v1.0.4
            assertEquals(4, references.length);
        }
        V8Object objRef = null;
        V8Function refFunc = null;
        V8Function person = null;
        for (V8Value oref : references) {
            if (V8Value.Type.Function.equals(oref.getType())) {
                V8Function fn = (V8Function) oref;
                if ("refFunc".equals(fn.getName())) {
                    refFunc = fn;
                } else if ("Person".equals(fn.getName())) {
                    person = fn;
                }
            } else {
                V8Object or1 = (V8Object) oref;
                assertEquals(V8Value.Type.Object, or1.getType());
                if ("Array".equals(or1.getClassName())) {
                    // io.js v1.0.4
                } else {
                    assertEquals("Object", or1.getClassName());
                    V8Object.Property or1RefProp = or1.getProperties().get("ref");
                    if (or1RefProp.getType() == V8Object.Property.Type.Field) {
                        // node.js v0.10.36
                        assertEquals(V8Object.Property.Type.Field, or1RefProp.getType());
                    } else {
                        // io.js v1.0.4
                        assertEquals(V8Object.Property.Type.Normal, or1RefProp.getType());
                    }
                    objRef = or1;
                }
            }
        }
        assertNotNull("ref field", objRef);
        assertNotNull("refFunc", refFunc);
        assertNotNull("Person", person);
        
        V8Debug.TestAccess.doCommand(v8dbg, "instances "+person.getHandle());
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.References, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        rrb = (References.ResponseBody) lastResponse.getBody();
        references = rrb.getReferences();
        assertEquals(2, references.length);
        ObjectCheck johnCheck = new ObjectCheck("Object", new String[] { "name", "age", "sex" }, new Object[] { "John", 30l, "m"}, "#<Person>");
        ObjectCheck sandraCheck = new ObjectCheck("Object", new String[] { "name", "age", "sex" }, new Object[] { "Sandra", 29l, "f"}, "#<Person>");
        try {
            johnCheck.check((V8Object) references[0]);
            sandraCheck.check((V8Object) references[1]);
        } catch (AssertionError afe) {
            sandraCheck.check((V8Object) references[0]);
            johnCheck.check((V8Object) references[1]);
        }
    }
    
    private void checkRestartframe() throws IOException, InterruptedException {
        checkFrame(4, LINE_BRKP_REFS-1, "    r4();               // breakpoint");
        
        V8Debug.TestAccess.doCommand(v8dbg, "step in");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        V8Event lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(8, LINE_BRKP_REFS-9-1, "        console.log(r2);");
        
        V8Debug.TestAccess.send(v8dbg, RestartFrame.createRequest(123));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Restartframe, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        RestartFrame.ResponseBody rrb = (RestartFrame.ResponseBody) lastResponse.getBody();
        Map<String, Object> result = rrb.getResult();
        Boolean needsStepIn = (Boolean) result.get(RestartFrame.RESULT_STACK_UPDATE_NEEDS_STEP_IN);
        assertTrue(RestartFrame.RESULT_STACK_UPDATE_NEEDS_STEP_IN, needsStepIn.booleanValue());
        
        checkFrame(4, LINE_BRKP_REFS-1, "    r4();               // breakpoint");
        
        V8Debug.TestAccess.doCommand(v8dbg, "step in");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkFrame(8, LINE_BRKP_REFS-9-1, "        console.log(r2);");
        
        V8Debug.TestAccess.send(v8dbg, RestartFrame.createRequest(123, 1));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Restartframe, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        rrb = (RestartFrame.ResponseBody) lastResponse.getBody();
        result = rrb.getResult();
        needsStepIn = (Boolean) result.get(RestartFrame.RESULT_STACK_UPDATE_NEEDS_STEP_IN);
        assertTrue(RestartFrame.RESULT_STACK_UPDATE_NEEDS_STEP_IN, needsStepIn.booleanValue());
        
        checkFrame(4, LINE_FNC+9-1, "    testReferences();");
    }
    
}
