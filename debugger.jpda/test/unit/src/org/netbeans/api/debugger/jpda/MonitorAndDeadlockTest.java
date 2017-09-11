/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
