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

import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.actions.ProfilingAwareAction;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LBL_InsertProfilingPointMenuAction=&Insert Profiling Point...",
    "HINT_InsertProfilingPointMenuAction=Insert Profiling Point..."
})
public class InsertProfilingPointMenuAction extends ProfilingAwareAction {
    
    private static final int[] ENABLED_STATES = new int[]{ Profiler.PROFILING_INACTIVE };

    private static final class Singleton {

        private static final InsertProfilingPointMenuAction INSTANCE = new InsertProfilingPointMenuAction();
    }

    @ActionID(id = "org.netbeans.modules.profiler.actions.InsertProfilingPointMenuAction", category = "Profile")
    @ActionRegistration(displayName = "#LBL_InsertProfilingPointMenuAction")
    @ActionReferences(value = {
        @ActionReference(path = "Menu/Profile", position = 1200)})
    public static InsertProfilingPointMenuAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public InsertProfilingPointMenuAction() {
        setIcon(Icons.getIcon(ProfilingPointsIcons.ADD));
        putValue("iconBase", Icons.getResource(ProfilingPointsIcons.ADD)); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(MyAction.class);
    }

    public String getName() {
        return Bundle.LBL_InsertProfilingPointMenuAction();
    }

    @Override
    protected int[] enabledStates() {
        return ENABLED_STATES;
    }

    @Override
    public void performAction() {
        SystemAction.get(InsertProfilingPointAction.class).performAction((Lookup.Provider)null);
    }
    
}
