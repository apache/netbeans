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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import java.util.Set;
import junit.framework.Test;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.junit.NbTestCase;

/**
 * The test of monitors and deadlocking functionality.
 * 
 * @author Martin Entlicher
 */
public class MonitorAndDeadlockTest extends NbTestCase {
    
    private JPDASupport     support;
    
    /** Creates a new instance of MonitorAndDeadlockTest */
    public MonitorAndDeadlockTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(MonitorAndDeadlockTest.class);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
    }
    
    public void testMonitors() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/MonitorAndDeadlockApp.java");
        List<Breakpoint> bpts = bp.getBreakpoints();
        for (Breakpoint lb : bpts) {
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        }
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.MonitorAndDeadlockApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
        
        JPDADebugger debugger = support.getDebugger();
        JPDAThread t = debugger.getCurrentThread();
        assertNull(t.getContendedMonitorAndOwner());
        assertEquals("Non-empty monitors and frames "+t.getOwnedMonitorsAndFrames(), 0, t.getOwnedMonitorsAndFrames().size());
        support.doContinue();
        support.waitState (JPDADebugger.STATE_STOPPED);
        
            List<MonitorInfo> mis = t.getOwnedMonitorsAndFrames();
            assertEquals(1, mis.size());
            MonitorInfo mi = mis.get(0);
            assertEquals(t, mi.getThread());
            assertEquals(2, mi.getFrame().getFrameDepth());
            assertEquals("Lock1", mi.getMonitor().getValue());
            support.doContinue();
            support.waitState (JPDADebugger.STATE_STOPPED);

            mis = t.getOwnedMonitorsAndFrames();
            assertEquals(2, mis.size());
    }
    
    public void testDeadlock () throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/MonitorAndDeadlockApp.java");
        List<Breakpoint> bpts = bp.getBreakpoints();
        Breakpoint lb = bpts.get(bpts.size() - 1);
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.MonitorAndDeadlockApp",
            new String[] { "deadlock" }
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
        
        ThreadsCollector tc = support.getDebugger().getThreadsCollector();
        DeadlockDetector dd = tc.getDeadlockDetector();
        final boolean[] detected = new boolean [] { false };
        dd.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DeadlockDetector.PROP_DEADLOCK.equals(evt.getPropertyName())) {
                    // Deadlock detected
                    synchronized (detected) {
                        detected[0] = true;
                        detected.notifyAll();
                    }
                }
            }
        });
        support.doContinue();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException iex) {}
        for (JPDAThread t : tc.getAllThreads()) {
            if (t.getName().startsWith("Deadlock")) {
                t.suspend();
                //System.out.println("Thread '"+t.getName()+"' suspended at "+t.getSourceName(null)+":"+t.getLineNumber(null));
                //System.out.println("    "+t.getCallStack()[1].getSourceName(null)+":"+t.getCallStack()[1].getLineNumber(null));
                //System.out.println("    "+t.getCallStack()[2].getSourceName(null)+":"+t.getCallStack()[2].getLineNumber(null));
            }
        }
        synchronized (detected) {
            detected.wait(5000);
            //System.out.println("Deadlocks = "+dd.getDeadlocks());
            assertTrue("Deadlock not detected!", detected[0]);
        }
        Set<Deadlock> deadlocks = dd.getDeadlocks();
        assertNotNull(deadlocks);
        assertEquals(1, deadlocks.size());
        Deadlock d = deadlocks.iterator().next();
        Collection<JPDAThread> dts = d.getThreads();
        assertEquals(2, dts.size());
        for (JPDAThread dt : dts) {
            String name = dt.getName();
            assertTrue(name.startsWith("Deadlock"));
            if ("Deadlock1".equals(name)) {
                assertEquals("\"Lock2\"", dt.getContendedMonitor().getValue());
            }
            if ("Deadlock2".equals(name)) {
                assertEquals("\"Lock1\"", dt.getContendedMonitor().getValue());
            }
        }
    }

}
