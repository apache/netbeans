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
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * Plugin / features information and link.
 */
@Messages({
    "TITLE_Plugins=Features & Plugins",
    "TXT_Features=Apache NetBeans activates features as you use them - "
    + "just start creating and opening projects.",
    "TXT_Plugins=Add support for additional languages and technologies by "
    + "installing community plugins.",
    "LBL_InstallPlugins=Install Plugins",
    "STATUS_InstallPlugins=Open the plugin manager"
})
public class PluginsWidget implements DashboardWidget {

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_Plugins();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.of(
                WidgetElement.text(Bundle.TXT_Features()),
                WidgetElement.separator(),
                WidgetElement.text(Bundle.TXT_Plugins()),
                WidgetElement.action(new ShowPluginManagerAction())
        );
    }

    // derived from Welcome page action
    private static class ShowPluginManagerAction extends AbstractAction {

        private final String initialTab;

        private ShowPluginManagerAction() {
            super(Bundle.LBL_InstallPlugins());
            putValue(SHORT_DESCRIPTION, Bundle.STATUS_InstallPlugins());
            this.initialTab = "available";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction")
                        .actionPerformed(new ActionEvent(e.getSource(), 100, initialTab));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
