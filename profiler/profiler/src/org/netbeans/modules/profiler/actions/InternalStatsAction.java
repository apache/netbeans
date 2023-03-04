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

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import java.awt.*;
import javax.swing.*;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;


/**
 * Provisionary action to display internal profiler stats.
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LBL_InternalStatsAction=&Display Internal Statistics",
    "HINT_InternalStatsAction=Display Internal Statistics",
    "CAPTION_InternalStatisticsInstrHotswap=Internal Statistics of Instrumentation and Hotswapping Operations"
})
@ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.InternalStatsAction")
//@ActionRegistration(displayName="#LBL_InternalStatsAction")
//@ActionReference(path="Menu/Profile/Advanced", position=300, separatorAfter=400)
public final class InternalStatsAction extends ProfilingAwareAction {
    private static final int[] enabledStates = new int[]{Profiler.PROFILING_RUNNING};
            
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InternalStatsAction() {
        putValue(Action.NAME, Bundle.LBL_InternalStatsAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_InternalStatsAction());
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    @Override
    public void performAction() {
        String stats;

        try {
            stats = Profiler.getDefault().getTargetAppRunner().getInternalStats();

            final HTMLTextArea textArea = new HTMLTextArea(stats);
            textArea.getAccessibleContext()
                    .setAccessibleName(Bundle.CAPTION_InternalStatisticsInstrHotswap());

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            p.add(new JScrollPane(textArea), BorderLayout.CENTER);

            DialogDisplayer.getDefault().createDialog(new DialogDescriptor(p,
                                                              Bundle.CAPTION_InternalStatisticsInstrHotswap(),
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
        return Bundle.LBL_InternalStatsAction();
    }
    
}
