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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RunToCursorInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ContinueAtAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxObjectAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.TerminateProcessAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.PopToCurrentFrameAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepOutInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.StepOverInstAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.NewBreakpointAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.DetachAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.EvaluateAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.PopLastDebuggerCallAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RunToFunctionAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxFrameAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RerunAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.DisassemblerWindowAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.RegistersWindowAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.EvaluationWindowAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MemoryWindowAction;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.PioWindowAction;
import java.util.LinkedList;

import org.openide.util.actions.SystemAction;

import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;

class ActionEnabler {
    private final List<StateListener> actions = new LinkedList<StateListener>();

    ActionEnabler() {

	// Register actions that are singleton SystemActions.
	// 
	// actions that are implemented using debuggercores ActionsProvider
	// interface register themselves with the debugger but are noted
	// here (in comments) so we can keep track of things



	// ToggleBreakpointActionProvider

	// ContinueActionProvider
	// PauseActionProvider

	// StepIntoActionProvider
	// StepOutActionProvider
	// StepOverActionProvider
	// RunToCursorActionProvider
	// RunIntoMethodActionProvider

	actions.add(SystemAction.get(RunToFunctionAction.class));
	actions.add(SystemAction.get(ContinueAtAction.class));

	actions.add(SystemAction.get(RerunAction.class));
	actions.add(SystemAction.get(TerminateProcessAction.class));
	actions.add(SystemAction.get(DetachAction.class));

	// FixActionProvider

	// MakeCalleeCurrentActionProvider
	// MakeCallerCurrentActionProvider
	// PopTopmostCallActionProvider
	actions.add(SystemAction.get(PopLastDebuggerCallAction.class));
	actions.add(SystemAction.get(PopToCurrentFrameAction.class));
	actions.add(SystemAction.get(MaxFrameAction.class));
	actions.add(SystemAction.get(MaxObjectAction.class));
	actions.add(SystemAction.get(DisassemblerWindowAction.class));
        actions.add(SystemAction.get(RegistersWindowAction.class));
        actions.add(SystemAction.get(MemoryWindowAction.class));
        actions.add(SystemAction.get(PioWindowAction.class));
        actions.add(SystemAction.get(EvaluationWindowAction.class));
        
	// 6640192
	actions.add(SystemAction.get(EvaluateAction.class));

	actions.add(SystemAction.get(NewBreakpointAction.class));

	actions.add(SystemAction.get(StepInstAction.class));
	actions.add(SystemAction.get(StepOverInstAction.class));
	actions.add(SystemAction.get(StepOutInstAction.class));
	actions.add(SystemAction.get(RunToCursorInstAction.class));
	// use Lookup actions.add(SystemAction.get(ConfigCurrentDebugTarget.class));

    }

    void update(State state) {
	// pass update() on to each explicitly registered action up above
	for (StateListener action : actions) {
	    action.update(state);
	}

	// pass update() on to each registered service
	Collection<? extends StateListener> stateListeners =
		Lookup.getDefault().lookupAll(StateListener.class);
	for (StateListener sl : stateListeners) {
	    sl.update(state);
        }
    }
}
