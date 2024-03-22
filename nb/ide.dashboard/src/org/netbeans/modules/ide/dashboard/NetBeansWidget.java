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
package org.netbeans.modules.ide.dashboard;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * IDE logo and links.
 */
@Messages({
    "LBL_ShowOnStartup=Show Dash on Startup",
    "STATUS_ShowOnStartup=Show dash on startup of the IDE.",
    "LBL_CheckForUpdates=Check for Updates",
    "STATUS_CheckForUpdates=Check for IDE and plugin updates."
})
public class NetBeansWidget implements DashboardWidget {

    private final Action showOnStartupAction;
    private final Action checkForUpdatesAction;
    private final List<WidgetElement> elements;

    public NetBeansWidget() {
        FileObject configFolder = FileUtil.getConfigFile("Dashboard/Main");
        showOnStartupAction = new ShowOnStartupAction(configFolder);
        checkForUpdatesAction = new CheckForUpdatesActions();
        elements = List.of(
                WidgetElement.image("org/netbeans/modules/ide/dashboard/resources/apache-netbeans.png"),
                WidgetElement.text(""),
                WidgetElement.action(checkForUpdatesAction),
                WidgetElement.component(() -> new JCheckBox(showOnStartupAction))
        );
    }

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return "";
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return elements;
    }

    private static class ShowOnStartupAction extends AbstractAction {

        private final FileObject configFile;

        private ShowOnStartupAction(FileObject configFile) {
            super(Bundle.LBL_ShowOnStartup());
            this.configFile = configFile;
            putValue(SHORT_DESCRIPTION, Bundle.STATUS_ShowOnStartup());
            if (configFile != null) {
                putValue(SELECTED_KEY, Boolean.TRUE.equals(configFile.getAttribute("showOnStartup")));
            } else {
                putValue(SELECTED_KEY, false);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (configFile != null) {
                try {
                    configFile.setAttribute("showOnStartup",
                            Boolean.TRUE.equals(getValue(SELECTED_KEY)));

                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
    }

    private static class CheckForUpdatesActions extends AbstractAction {

        private CheckForUpdatesActions() {
            super(Bundle.LBL_CheckForUpdates());
            putValue(SHORT_DESCRIPTION, Bundle.STATUS_CheckForUpdates());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.CheckForUpdatesAction")
                        .actionPerformed(e);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}
