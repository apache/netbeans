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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebuggerManagerListener;
import org.netbeans.api.debugger.test.TestLazyDebuggerManagerListener;

import java.util.*;
import java.beans.PropertyChangeEvent;

/**
 * Launches and finishes a debugger session. Tests services registration and lookup and event firing.
 *
 * @author Maros Sandor
 */
public class LaunchDebuggerTest extends DebuggerApiTestBase {

    public LaunchDebuggerTest(String s) {
        super(s);
    }

    public void testLookup () throws Exception {

        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        TestDebuggerManagerListener dml = new TestDebuggerManagerListener();
        dm.addDebuggerListener(dml);

        TestLazyDebuggerManagerListener ldml = null;
        for (LazyDebuggerManagerListener _ldml : dm.lookup(null, LazyDebuggerManagerListener.class)) {
            if (_ldml instanceof TestLazyDebuggerManagerListener) {
                ldml = (TestLazyDebuggerManagerListener) _ldml;
                break;
            }
        }
        assertNotNull("Lazy debugger manager listener not loaded", ldml);

        Map args = new HashMap();
        TestDICookie tdi = TestDICookie.create(args);

        Object [] services = new Object[] { tdi, this };
        DebuggerInfo di = DebuggerInfo.create(TestDICookie.ID, services);

        DebuggerEngine engines [] = dm.startDebugging(di);
        assertEquals("Wrong number of debugger engines started", engines.length, 1);

        testStartEvents(dml, engines);
        testStartEvents(ldml, engines);

        DebuggerEngine debugger = engines[0];
        DebuggerInfo dic = debugger.lookupFirst(null, DebuggerInfo.class);
        assertSame("Wrong debugger info in engine lookup", dic, di);
        assertTrue("Engine did not start", tdi.hasInfo(ActionsManager.ACTION_START));

        DebuggerActionsTest.kill(dm.getCurrentSession());
        assertTrue("Engine did not finish", tdi.hasInfo(ActionsManager.ACTION_KILL));

        testKillEvents(dml);
        testKillEvents(ldml);

        dm.removeDebuggerListener(dml);
    }

    private void testKillEvents(TestDebuggerManagerListener dml) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionRemoved")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertNull("Bad current engine", pce.getNewValue());
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    assertNull("Bad current session", pce.getNewValue());
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }

    private void testStartEvents(TestDebuggerManagerListener dml, DebuggerEngine[] engines) {
        List events;
        TestDebuggerManagerListener.Event event;
        events = dml.getEvents();
        assertEquals("Wrong number of events generated", events.size(), 5);
        for (Iterator i = events.iterator(); i.hasNext();) {
            event = (TestDebuggerManagerListener.Event) i.next();
            if (event.getName().equals("sessionAdded")) {
                i.remove();
            } else if (event.getName().equals("propertyChange")) {
                PropertyChangeEvent pce = (PropertyChangeEvent) event.getParam();
                if (pce.getPropertyName().equals("sessions")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("debuggerEngines")) {
                    i.remove();
                } else if (pce.getPropertyName().equals("currentEngine")) {
                    assertSame("Bad PCE new current engine", pce.getNewValue(), engines[0]);
                    i.remove();
                } else if (pce.getPropertyName().equals("currentSession")) {
                    i.remove();
                }
            }
        }
        assertEquals("Wrong events generated", events.size(), 0);
    }
}
