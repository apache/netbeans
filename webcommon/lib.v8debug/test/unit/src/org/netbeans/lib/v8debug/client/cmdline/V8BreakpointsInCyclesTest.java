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

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.Frame;
import org.netbeans.lib.v8debug.commands.ListBreakpoints;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.events.BreakEventBody;

/**
 *
 * @author Martin Entlicher
 */
public class V8BreakpointsInCyclesTest extends AbstractTestBase {
    
    private static final String TEST_FILE = "TestBreakpointsInCycles.js"; // NOI18N
    private static final String NODE_ARG_DBG = "--debug-brk";   // NOI18N
    
    private static final int LINE_BEGIN = 43;
    private static final int LINE_BRKP_1 = LINE_BEGIN + 7;
    private static final int LINE_BRKP_2 = LINE_BRKP_1 + 7;
    
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
    
    @Test
    public void testBreakpoints() throws IOException, InterruptedException {
        // Wait to stop first:
        V8Event lastEvent;
        do {
            lastEvent = responseHandler.getLastEvent();
        } while (lastEvent.getKind() != V8Event.Kind.Break);
        V8Response lastResponse = responseHandler.getLastResponse();
        checkFrame(LINE_BEGIN-1);
        
        // Conditional breakpoints:
        String condition1 = "d > 700";
        Long ignoreCount1 = null;
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(1, V8Breakpoint.Type.scriptName, testFilePath, (long) LINE_BRKP_1-1, null, true, condition1, ignoreCount1, null));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        String condition2 = "d < 1";
        Long ignoreCount2 = 2l;
        V8Debug.TestAccess.send(v8dbg, SetBreakpoint.createRequest(2, V8Breakpoint.Type.scriptName, testFilePath, (long) LINE_BRKP_2-1, null, true, condition2, ignoreCount2, null));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Setbreakpoint, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        
        // breakpoint 1:
        
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        BreakEventBody beb = (BreakEventBody) lastEvent.getBody();
        assertEquals(2, beb.getBreakpoints()[0]);
        assertEquals(LINE_BRKP_1-1l, beb.getSourceLine());
        checkLocalVar("d", 720l, false);
        checkLocalVar("i", 7l, false);
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        assertNull(lastResponse.getErrorMessage(), lastResponse.getErrorMessage());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        ListBreakpoints.ResponseBody lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        V8Breakpoint[] breakpoints = lbrb.getBreakpoints();
        assertEquals(3, breakpoints.length);
        assertEquals(condition1, breakpoints[1].getCondition());
        assertEquals(0, breakpoints[1].getIgnoreCount());
        assertEquals(1, breakpoints[1].getHitCount());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        checkLocalVar("d", 5040l, false);
        checkLocalVar("i", 8l, false);
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        breakpoints = lbrb.getBreakpoints();
        assertEquals(0, breakpoints[1].getIgnoreCount());
        assertEquals(2, breakpoints[1].getHitCount());
        // deactivate breakpoint 1:
        V8Debug.TestAccess.send(v8dbg, ChangeBreakpoint.createRequest(123, 2, false, null, null));
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Changebreakpoint, lastResponse.getCommand());
        
        V8Debug.TestAccess.doCommand(v8dbg, "cont");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Continue, lastResponse.getCommand());
        
        // breakpoint 2:
        
        lastEvent = responseHandler.getLastEvent();
        assertEquals(V8Event.Kind.Break, lastEvent.getKind());
        beb = (BreakEventBody) lastEvent.getBody();
        assertEquals(3, beb.getBreakpoints()[0]);
        assertEquals(LINE_BRKP_2-1l, beb.getSourceLine());
        checkLocalVar("d", 1d/11/12/13, false);
        //checkLocalVar("d", Double.toString(1d/11/12), false);
        checkLocalVar("i", 11l+ignoreCount2+1, false);
        V8Debug.TestAccess.doCommand(v8dbg, "breakpoints");
        lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Listbreakpoints, lastResponse.getCommand());
        lbrb = (ListBreakpoints.ResponseBody) lastResponse.getBody();
        breakpoints = lbrb.getBreakpoints();
        assertEquals(ignoreCount2.longValue(), breakpoints[2].getIgnoreCount());
        assertEquals(ignoreCount2 + 1, breakpoints[2].getHitCount());
    }
    
    private void checkFrame(long line) throws IOException, InterruptedException {
        V8Debug.TestAccess.doCommand(v8dbg, "frame");
        V8Response lastResponse = responseHandler.getLastResponse();
        assertEquals(V8Command.Frame, lastResponse.getCommand());
        assertTrue(lastResponse.isSuccess());
        assertFalse(lastResponse.isRunning());
        V8Body body = lastResponse.getBody();
        Frame.ResponseBody fbody = (Frame.ResponseBody) body;
        V8Frame frame = fbody.getFrame();
        assertEquals(line, frame.getLine());
    }
}
