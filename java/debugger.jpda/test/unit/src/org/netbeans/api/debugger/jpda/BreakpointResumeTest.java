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

package org.netbeans.api.debugger.jpda;

import org.netbeans.api.debugger.DebuggerManager;

import java.net.URL;
import junit.framework.Test;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;


/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class BreakpointResumeTest  extends NbTestCase {

    private String          sourceRoot = System.getProperty ("test.dir.src");

    public BreakpointResumeTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointResumeTest.class);
    }

    public void testBreakpointResume () throws Exception {
        JPDASupport support = null;
        JPDASupport.removeAllBreakpoints ();
        TestBreakpointListener tbl = new TestBreakpointListener ();
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            lb.addJPDABreakpointListener (tbl);
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);

            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
            assertTrue("The breakpoint "+lb+" was not reached.", tbl.isReached());
            DebuggerManager.getDebuggerManager ().removeBreakpoint (lb);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {
        
        private volatile boolean reached = false;

        public TestBreakpointListener() {
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            reached = true;
            event.resume();
        }
        
        public boolean isReached() {
            return reached;
        }
    }
}
