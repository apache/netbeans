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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
public class CustomizeProfilingPointAction extends SystemAction implements ContextAwareAction {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CustomizeProfilingPointAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        setEnabled(false);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeProfilingPointAction.class);
    }

    @NbBundle.Messages("CustomizeProfilingPointAction_ActionName=Edit")
    @Override
    public String getName() {
        return Bundle.CustomizeProfilingPointAction_ActionName();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (!ProfilingPointsManager.getDefault().isProfilingSessionInProgress()) {
            Collection<? extends CodeProfilingPoint.Annotation> anns = actionContext.lookupAll(CodeProfilingPoint.Annotation.class);
            if (anns.size() == 1) {
                final CodeProfilingPoint pp = anns.iterator().next().profilingPoint();
                return new AbstractAction(getName()) {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        pp.customize(false, true);
                    }
                };
            }
        }
        return this;
    }
}
