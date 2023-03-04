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

package org.netbeans.modules.profiler.ppoints.ui;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.Action;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
public class EnableDisableProfilingPointAction extends SystemAction implements ContextAwareAction {
    private ContextAwareAction action;

    private class ContextAwareAction extends BooleanStateAction {

        private CodeProfilingPoint profilingPoint;

        ContextAwareAction() {
            setIcon(null);
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        }

        void setProfilingPoint(CodeProfilingPoint pp) {
            profilingPoint = pp;
        }

        @Override
        public String getName() {
            return EnableDisableProfilingPointAction.this.getName();
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(EnableDisableProfilingPointAction.class);
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            super.actionPerformed(ev);
            profilingPoint.setEnabled(getBooleanState());
        }
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public EnableDisableProfilingPointAction() {
        action = new ContextAwareAction();
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setEnabled(false);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EnableDisableProfilingPointAction.class);
    }

    @NbBundle.Messages("EnableDisableProfilingPointAction_ActionName=Enabled")
    @Override
    public String getName() {
        return Bundle.EnableDisableProfilingPointAction_ActionName();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (!ProfilingPointsManager.getDefault().isProfilingSessionInProgress()) {
            Collection<? extends CodeProfilingPoint.Annotation> anns = actionContext.lookupAll(CodeProfilingPoint.Annotation.class);
            if (anns.size() == 1) {
                CodeProfilingPoint pp = anns.iterator().next().profilingPoint();
                
                action.setProfilingPoint(pp);
                action.setBooleanState(pp.isEnabled());
                return action;
            }
        }
        return this;
    }
}
