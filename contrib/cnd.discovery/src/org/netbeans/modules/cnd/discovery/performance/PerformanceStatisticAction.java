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
package org.netbeans.modules.cnd.discovery.performance;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;

/**
 *
 */
@Messages({
    "statistic.action.name.text=Project Performance Statistic",
    "statistic.title.text=Project Performance Statistic"
})
public class PerformanceStatisticAction extends NodeAction {

    private final boolean visibleAction;

    public PerformanceStatisticAction() {
        visibleAction = Boolean.getBoolean("test.xref.action"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        JPanel panel = new StatisticPanel();
        DialogDescriptor descr = new DialogDescriptor(panel, Bundle.statistic_title_text(), true,
                new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        DialogDisplayer.getDefault().notify(descr);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenuItem out = super.getMenuPresenter();
        out.setVisible(visibleAction);
        return out;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem out = super.getPopupPresenter();
        out.setVisible(visibleAction);
        return out;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!visibleAction) {
            return false;
        }
        return PerformanceLogger.isProfilingEnabled() && PerformanceIssueDetector.getActiveInstance() != null;
    }

    @Override
    public String getName() {
        return Bundle.statistic_action_name_text();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
