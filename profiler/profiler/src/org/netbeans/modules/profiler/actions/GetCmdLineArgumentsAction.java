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

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;


/**
 * An action to print command-line arguments from target app.
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "GetCmdLineArgumentsAction_TargetJvmInactiveMsg=Target JVM is inactive",
    "GetCmdLineArgumentsAction_JvmArgumentsString=JVM arguments:",
    "GetCmdLineArgumentsAction_MainClassAndArgsString=Main class (JAR) and its arguments:",
    "LBL_GetCmdLineArgumentsAction=&View Command-line Arguments",
    "HINT_GetCmdLineArgumentsAction=View Command-line Arguments",
    "CAPTION_JVMandMainClassCommandLineArguments=JVM and Main Class Command-line Arguments",
    "MSG_NotAvailableNow=Not available at this time: {0}"
})
@ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.GetCmdLineArgumentsAction")
//@ActionRegistration(displayName="#LBL_GetCmdLineArgumentsAction")
//@ActionReference(path="Menu/Profile/Advanced", position=200)
public final class GetCmdLineArgumentsAction extends ProfilingAwareAction {
    private static final int[] enabledStates = new int[]{Profiler.PROFILING_RUNNING};
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public GetCmdLineArgumentsAction() {
        putValue(Action.NAME, Bundle.LBL_GetCmdLineArgumentsAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_GetCmdLineArgumentsAction());
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    public void performAction() {
        try {
            final TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();
            final ProfilerClient profilerClient = runner.getProfilerClient();
            final ProfilingSessionStatus status = runner.getProfilingSessionStatus();
            final ProfilerEngineSettings settings = runner.getProfilerEngineSettings();

            if (!profilerClient.targetJVMIsAlive()) {
                throw new ClientUtils.TargetAppOrVMTerminated(1, Bundle.GetCmdLineArgumentsAction_TargetJvmInactiveMsg());
            }

            final String jvmArgs;
            final String javaCommand;

            if (status.runningInAttachedMode) {
                jvmArgs = status.jvmArguments;
                javaCommand = status.javaCommand;
            } else {
                jvmArgs = settings.getJVMArgsAsSingleString();
                javaCommand = settings.getMainClassName() + " " + settings.getMainArgsAsSingleString(); // NOI18N
            }

            final StringBuffer s = new StringBuffer();
            s.append("<b>"); // NOI18N
            s.append(Bundle.GetCmdLineArgumentsAction_JvmArgumentsString());
            s.append("</b><br>"); // NOI18N
            s.append(jvmArgs);
            s.append("<br><br>"); // NOI18N
            s.append("<b>"); // NOI18N
            s.append(Bundle.GetCmdLineArgumentsAction_MainClassAndArgsString());
            s.append("</b><br>"); // NOI18N
            s.append(javaCommand);

            final HTMLTextArea textArea = new HTMLTextArea(s.toString());
            textArea.getAccessibleContext()
                    .setAccessibleName(Bundle.CAPTION_JVMandMainClassCommandLineArguments());

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            p.add(new JScrollPane(textArea), BorderLayout.CENTER);
            p.setPreferredSize(new Dimension(600, 200));

            DialogDisplayer.getDefault().createDialog(new DialogDescriptor(p,
                                                              Bundle.CAPTION_JVMandMainClassCommandLineArguments(),
                                                              true, new Object[] { DialogDescriptor.CLOSED_OPTION },
                                                              DialogDescriptor.CLOSED_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                                                              null, null)).setVisible(true);
        } catch (ClientUtils.TargetAppOrVMTerminated e) {
            ProfilerDialogs.displayWarning(Bundle.MSG_NotAvailableNow(e.getMessage()));
            ProfilerLogger.log(e.getMessage());
        }
    }

    @Override
    protected int[] enabledStates() {
        return enabledStates;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String getName() {
        return Bundle.LBL_GetCmdLineArgumentsAction();
    }
}
