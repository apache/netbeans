/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
