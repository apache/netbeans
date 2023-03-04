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

package org.netbeans.api.debugger;

import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestActionsManagerListener;
import org.netbeans.api.debugger.test.TestLazyActionsManagerListener;

import java.util.*;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.openide.util.Task;

/**
 * Tests invocations of debugger actions.
 *
 * @author Maros Sandor
 */
public class DebuggerActionsTest extends DebuggerApiTestBase {

    public DebuggerActionsTest(String s) {
        super(s);
    }

    /* TODO:  Add this to simulate the IDE runtime behavior
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(ProvidersAnnotationTest.class));
    }
     */

    public void testLookup() throws Exception {

        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Map args = new HashMap();
        TestDICookie tdi = TestDICookie.create(args);

        Object [] services = new Object[] { tdi, this };
        DebuggerInfo di = DebuggerInfo.create(TestDICookie.ID, services);

        DebuggerEngine engines [] = dm.startDebugging(di);
        assertEquals("Wrong number of engines started", 1, engines.length);
        DebuggerEngine debugger = engines[0];

        ActionsManager am = debugger.getActionsManager();
        TestActionsManagerListener tam = new TestActionsManagerListener();
        am.addActionsManagerListener(tam);

        TestLazyActionsManagerListener laml = (TestLazyActionsManagerListener) debugger.lookupFirst(null, LazyActionsManagerListener.class);
        assertNotNull("Lazy actions manager listener not loaded", laml);

        am.doAction(ActionsManager.ACTION_CONTINUE);
        am.doAction(ActionsManager.ACTION_FIX);
        am.doAction(ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
        am.doAction(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        am.doAction(ActionsManager.ACTION_PAUSE);
        am.doAction(ActionsManager.ACTION_POP_TOPMOST_CALL);
        am.doAction(ActionsManager.ACTION_RESTART);
        am.doAction(ActionsManager.ACTION_RUN_INTO_METHOD);
        am.doAction(ActionsManager.ACTION_RUN_TO_CURSOR);
        am.doAction(ActionsManager.ACTION_STEP_INTO);
        am.doAction(ActionsManager.ACTION_STEP_OUT);
        am.doAction(ActionsManager.ACTION_STEP_OVER);
        am.doAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        kill(dm.getCurrentSession());

        am.removeActionsManagerListener(tam);

        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_START));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_CONTINUE));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_FIX));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_MAKE_CALLEE_CURRENT));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_MAKE_CALLER_CURRENT));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_PAUSE));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_POP_TOPMOST_CALL));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_RESTART));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_RUN_INTO_METHOD));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_RUN_TO_CURSOR));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_STEP_INTO));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_STEP_OUT));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_STEP_OVER));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_TOGGLE_BREAKPOINT));
        assertTrue("Action was not performed", tdi.hasInfo(ActionsManager.ACTION_KILL));

        testReceivedEvents(tam.getPerformedActions(), false);
        testReceivedEvents(laml.getPerformedActions(), true);
    }

    private void testReceivedEvents(List eventActions, boolean expectStartAction) {
        if (expectStartAction) assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_START));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_CONTINUE));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_FIX));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_MAKE_CALLEE_CURRENT));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_MAKE_CALLER_CURRENT));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_PAUSE));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_POP_TOPMOST_CALL));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_RESTART));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_RUN_INTO_METHOD));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_RUN_TO_CURSOR));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_STEP_INTO));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_STEP_OUT));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_STEP_OVER));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_TOGGLE_BREAKPOINT));
        assertTrue("ActionListener was not notified", eventActions.remove(ActionsManager.ACTION_KILL));
        assertEquals("ActionListener notification failed", eventActions.size(), 0);
    }

    public static void kill(Session s) {
        String[] languagesToKill = s.getSupportedLanguages();
        for (String language : languagesToKill) {
            Task kill = s.getEngineForLanguage(language).getActionsManager ().
                postAction (ActionsManager.ACTION_KILL);
            kill.waitFinished();
        }
    }
}
