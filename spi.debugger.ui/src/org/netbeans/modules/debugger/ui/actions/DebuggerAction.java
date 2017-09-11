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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;


import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 *
 * @author   Jan Jancura
 */
public class DebuggerAction extends AbstractAction {
    
    private Object action;
    private boolean nameInBundle;

    private DebuggerAction (Object action) {
        this(action, true);
    }
    
    private DebuggerAction (Object action, boolean nameInBundle) {
        this.action = action;
        this.nameInBundle = nameInBundle;
        new Listener (this);
        setEnabled (isEnabled (getAction ()));
    }

    public Object getAction () {
        return action;
    }
    
    @Override
    public Object getValue(String key) {
        if (key == Action.NAME && nameInBundle) {
            return NbBundle.getMessage (DebuggerAction.class, (String) super.getValue(key));
        }
        return super.getValue(key);
    }
    
    @Override
    public void actionPerformed (ActionEvent evt) {
        // Post the action asynchronously, since we're on AWT
        getActionsManager(action).postAction(action);
    }
    
    /**
     * Get the actions manager of the current engine (if any).
     * @return The actions manager or <code>null</code>.
     */
    private static ActionsManager getCurrentEngineActionsManager() {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            return engine.getActionsManager();
        } else {
            return null;
        }
    }
    
    /**
     * Test whether the given action is enabled in either the current engine's
     * action manager, or the default action manager.
     * We need to take the default actions into account so that actions provided
     * by other debuggers are not ignored.
     */
    private static boolean isEnabled(Object action) {
        ActionsManager manager = getCurrentEngineActionsManager();
        if (manager != null) {
            if (manager.isEnabled(action)) {
                return true;
            }
        }
        return DebuggerManager.getDebuggerManager().getActionsManager().isEnabled(action);
    }
    
    /**
     * Get the actions manager for which the action is enabled.
     * It returns either the current engine's manager, or the default one.
     * @param the action
     * @return the actions manager
     */
    private static ActionsManager getActionsManager(Object action) {
        ActionsManager manager = getCurrentEngineActionsManager();
        if (manager != null) {
            if (manager.isEnabled(action)) {
                return manager;
            }
        }
        return DebuggerManager.getDebuggerManager().getActionsManager();
    }
    
    public static DebuggerAction createContinueAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_CONTINUE);
        action.putValue (Action.NAME, "CTL_Continue_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Continue.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createFixAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_FIX);
        action.putValue (Action.NAME, "CTL_Fix_action_name");
        action.putValue (
            "iconBase",
            "org/netbeans/modules/debugger/resources/actions/Fix.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createKillAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_KILL);
        action.putValue (Action.NAME, "CTL_KillAction_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Kill.gif" // NOI18N
        );
        action.setEnabled (false);
        return action;
    }
    
    public static DebuggerAction createMakeCalleeCurrentAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
        action.putValue (Action.NAME, "CTL_MakeCalleeCurrentAction_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCalledMethod.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createMakeCallerCurrentAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        action.putValue (Action.NAME, "CTL_MakeCallerCurrentAction_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCallingMethod.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createPauseAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_PAUSE);
        action.putValue (Action.NAME, "CTL_Pause_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Pause.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createPopTopmostCallAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_POP_TOPMOST_CALL);
        action.putValue (Action.NAME, "CTL_PopTopmostCallAction_name");
        return action;
    }
    
    public static DebuggerAction createRunIntoMethodAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_RUN_INTO_METHOD);
        action.putValue (Action.NAME, "CTL_Run_into_method_action_name");
        return action;
    }
    
    public static DebuggerAction createRunToCursorAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_RUN_TO_CURSOR);
        action.putValue (Action.NAME, "CTL_Run_to_cursor_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/RunToCursor.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createStepIntoAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_INTO);
        action.putValue (Action.NAME, "CTL_Step_into_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepInto.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepIntoNextMethodAction () {
        DebuggerAction action = new DebuggerAction("stepIntoNextMethod"); // NOI18N [TODO] add constant
        action.putValue (Action.NAME, "CTL_Step_into_next_method_action_name");
        return action;
    }
    
    public static DebuggerAction createStepOutAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OUT);
        action.putValue (Action.NAME, "CTL_Step_out_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOut.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOverAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OVER);
        action.putValue (Action.NAME, "CTL_Step_over_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOver.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOperationAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OPERATION);
        action.putValue (Action.NAME, "CTL_Step_operation_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOverOperation.gif" // NOI18N
        );
        return action;
    }
    
    private static final String[] BREAKPOINT_ANNOTATION_TYPES = new String[] {
        "Breakpoint_broken",
        "Breakpoint",
        "Breakpoint_stroke",
        "CondBreakpoint_broken",
        "CondBreakpoint",
        "CondBreakpoint_stroke",
        "DisabledBreakpoint",
        "DisabledCondBreakpoint",
        "DisabledBreakpoint_stroke",
        "CurrentExpressionLine",
        "CurrentExpression",
        "CurrentPC2",
        "CurrentPC2LinePart",
        "CurrentPC2_BP",
        "CurrentPC2_DBP",
    };
    
    public static DebuggerAction createToggleBreakpointAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        action.putValue (Action.NAME, "CTL_Toggle_breakpoint");
        action.putValue("default-action", true);
        action.putValue("supported-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
        //action.putValue("default-action-excluded-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
        return action;
    }

    public static DebuggerAction createEvaluateAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_EVALUATE);
        action.putValue (Action.NAME, "CTL_Evaluate"); // NOI18N
        return action;
    }

    /**
     * Use this method to register an additional debugger action.
     * Register in a module layer manually as follows:
     *   <pre style="background-color: rgb(255, 255, 153);">
     *   &lt;folder name="Actions"&gt;
     *       &lt;folder name="Debug"&gt;
     *           &lt;file name="ActionName.instance"&gt;
     *               &lt;attr name="instanceClass" stringvalue="org.netbeans.modules.debugger.ui.actions.DebuggerAction"/&gt;
     *               &lt;attr name="instanceOf" stringvalue="javax.swing.Action"/&gt;
     *               &lt;attr name="instanceCreate" methodvalue="org.netbeans.modules.debugger.ui.actions.DebuggerAction.createAction"/&gt;
     *               &lt;attr name="action" stringvalue="actionName"/&gt;
     *               &lt;attr name="name" bundlevalue="org.netbeans.modules.debugger.general.Bundle#CTL_MyAction_Title"/&gt;
     *               &lt;attr name="iconBase" stringvalue="org/netbeans/modules/debugger/general/MyAction.png"/&gt;
     *           &lt;/file&gt;
     *       &lt;/folder&gt;
     *   &lt;/folder&gt;</pre>
     * @param params "action", "name" and optional "iconBase".
     * @return The action object
     */
    public static DebuggerAction createAction(Map<String,?> params) {
        Object action = params.get("action");
        if (action == null) {
            throw new IllegalStateException("\"action\" parameter is missing.");
        }
        String name = (String) params.get("name");
        if (name == null) {
            throw new IllegalStateException("\"name\" parameter is missing.");
        }
        String iconBase = (String) params.get("iconBase");
        DebuggerAction a = new DebuggerAction(action, false);
        a.putValue(Action.NAME, name);
        if (iconBase != null) {
            a.putValue("iconBase", iconBase);
        }
        return a;
    }

    // Debug File Actions:
    
    public static Action createDebugFileAction() {
        Action a = FileSensitiveActions.fileCommandAction(
            ActionProvider.COMMAND_DEBUG_SINGLE,
            NbBundle.getMessage(DebuggerAction.class, "LBL_DebugSingleAction_Name"),
            ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugSingle.png", true));
        a.putValue("iconBase","org/netbeans/modules/debugger/resources/debugSingle.png"); //NOI18N
        a.putValue("noIconInMenu", true); //NOI18N
        return a;
    }
    
    public static Action createDebugTestFileAction()  {
        Action a = FileSensitiveActions.fileCommandAction(
            ActionProvider.COMMAND_DEBUG_TEST_SINGLE,
            NbBundle.getMessage(DebuggerAction.class, "LBL_DebugTestSingleAction_Name"),
            ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugTestSingle.png", true));
        a.putValue("iconBase","org/netbeans/modules/debugger/resources/debugTestSingle.png"); //NOI18N
        a.putValue("noIconInMenu", true); //NOI18N
        return a;
    }
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE and on current engine
     * on PROP_ACTION_STATE and updates state of this action instance.
     */
    static class Listener extends DebuggerManagerAdapter 
    implements ActionsManagerListener {
        
        private ActionsManager  currentActionsManager;
        private WeakReference<DebuggerAction> ref;

        
        Listener (DebuggerAction da) {
            ref = new WeakReference<DebuggerAction>(da);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            DebuggerManager.getDebuggerManager ().getActionsManager().addActionsManagerListener(
                ActionsManagerListener.PROP_ACTION_STATE_CHANGED,
                this
            );
            updateCurrentActionsManager ();
        }
        
        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            updateCurrentActionsManager ();
            final boolean en = DebuggerAction.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        @Override
        public void actionPerformed (Object action) {
        }
        @Override
        public void actionStateChanged (
            final Object action, 
            final boolean enabled
        ) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            if (!action.equals(da.getAction ())) return;
            // ignore the enabled argument, check it with respect to the proper
            // actions manager.
            final boolean en = DebuggerAction.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        private void updateCurrentActionsManager () {
            ActionsManager newActionsManager = getCurrentEngineActionsManager ();
            if (currentActionsManager == newActionsManager) return;
            
            if (currentActionsManager != null)
                currentActionsManager.removeActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            if (newActionsManager != null)
                newActionsManager.addActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            currentActionsManager = newActionsManager;
        }
        
        private DebuggerAction getDebuggerAction () {
            DebuggerAction da = ref.get ();
            if (da == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_CURRENT_ENGINE,
                    this
                );
                DebuggerManager.getDebuggerManager ().getActionsManager().removeActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED,
                    this
                );
                if (currentActionsManager != null)
                    currentActionsManager.removeActionsManagerListener 
                        (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
                currentActionsManager = null;
                return null;
            }
            return da;
        }
    }
}

