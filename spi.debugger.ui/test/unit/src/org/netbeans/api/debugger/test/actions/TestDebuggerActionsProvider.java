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

package org.netbeans.api.debugger.test.actions;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.api.debugger.test.TestDICookie;

import java.util.*;

/**
 * Provides all debugging actions and records when they are performed.
 *
 * @author Maros Sandor
 */
public class TestDebuggerActionsProvider extends ActionsProvider {

    private TestDebugger    debuggerImpl;
    private ContextProvider  lookupProvider;
    private Set             supportedActions;

    public TestDebuggerActionsProvider(ContextProvider lookupProvider) {
        debuggerImpl = lookupProvider.lookupFirst(null, TestDebugger.class);
        this.lookupProvider = lookupProvider;
        supportedActions = new HashSet();
        supportedActions.add(ActionsManager.ACTION_CONTINUE);
        supportedActions.add(ActionsManager.ACTION_FIX);
        supportedActions.add(ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
        supportedActions.add(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        supportedActions.add(ActionsManager.ACTION_PAUSE);
        supportedActions.add(ActionsManager.ACTION_POP_TOPMOST_CALL);
        supportedActions.add(ActionsManager.ACTION_RESTART);
        supportedActions.add(ActionsManager.ACTION_RUN_INTO_METHOD);
        supportedActions.add(ActionsManager.ACTION_RUN_TO_CURSOR);
        supportedActions.add(ActionsManager.ACTION_STEP_INTO);
        supportedActions.add(ActionsManager.ACTION_STEP_OUT);
        supportedActions.add(ActionsManager.ACTION_STEP_OVER);
        supportedActions.add(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    public Set getActions () {
        return supportedActions;
    }

    public void doAction (Object action) {
        if (debuggerImpl == null) return;
        final TestDICookie cookie = lookupProvider.lookupFirst(null, TestDICookie.class);
        cookie.addInfo(action);
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
}
