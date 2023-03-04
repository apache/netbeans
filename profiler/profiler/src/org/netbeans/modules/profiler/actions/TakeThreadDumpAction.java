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
package org.netbeans.modules.profiler.actions;

import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import javax.swing.SwingWorker;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.results.threads.ThreadDump;
import org.netbeans.modules.profiler.ThreadDumpWindow;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "LBL_TakeThreadDumpAction=&Take Thread Dump",
    "HINT_TakeThreadDumpAction=Take thread dump from the profiled process"
})
public class TakeThreadDumpAction extends ProfilingAwareAction {

    private static final int[] ENABLED_STATES = new int[]{Profiler.PROFILING_RUNNING};

    private static final class Singleton {

        private static final TakeThreadDumpAction INSTANCE = new TakeThreadDumpAction();
    }

    @ActionID(id = "org.netbeans.modules.profiler.actions.TakeThreadDumpAction", category = "Profile")
    @ActionRegistration(displayName = "#LBL_TakeThreadDumpAction")
    @ActionReferences(value = {
        //        @ActionReference(path = "Shortcuts", name = "C-F3"),
        @ActionReference(path = "Menu/Profile", position = 500)})
    public static TakeThreadDumpAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public TakeThreadDumpAction() {
        setIcon(Icons.getIcon(ProfilerIcons.SNAPSHOT_THREADS));
        putValue("iconBase", Icons.getResource(ProfilerIcons.SNAPSHOT_THREADS)); // NOI18N
        putProperty(Action.SHORT_DESCRIPTION, Bundle.HINT_TakeThreadDumpAction());
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(MyAction.class);
    }

    public String getName() {
        return Bundle.LBL_TakeThreadDumpAction();
    }

    @Override
    protected int[] enabledStates() {
        return ENABLED_STATES;
    }

    @Override
    public void performAction() {
        new SwingWorker<ThreadDump, Object>() {

            @Override
            protected ThreadDump doInBackground()throws Exception {
                try {
                    ProfilerClient client = Profiler.getDefault().getTargetAppRunner().getProfilerClient();
                    return client.takeThreadDump();

                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    ProfilerLogger.log(ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ThreadDump threadDump = get();
                    if (threadDump != null) {
                        ThreadDumpWindow win = new ThreadDumpWindow(threadDump);
                        win.open();
                        win.requestActive();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        }.execute();
    }
}
