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
