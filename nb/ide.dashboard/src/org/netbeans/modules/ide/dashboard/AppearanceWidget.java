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
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import javax.swing.AbstractAction;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.LifecycleManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * Look & Feel info and links.
 */
@Messages({
    "TITLE_Appearance=Look & Feel",
    "TXT_Appearance=The default NetBeans theme is FlatLaf Light. You could also try FlatLaf Dark.",
    "TXT_RequiresRestart=Theme changes require restart.",
    "TXT_AdvancedLAF=Change theme accent color and more in the options panel.",
    "LBL_AdvancedLAFPanel=FlatLaf Options",
    "STATUS_AdvancedLAFPanel=Open FlatLaf options panel.",
    "LBL_DarkLaf=Switch to FlatLaf Dark",
    "LBL_DefaultLaf=Reset to FlatLaf Light",
    "TITLE_RestartIDE=Restart IDE",
    "TXT_RestartIDE=Click here to restart IDE and apply your theme change."
})
public class AppearanceWidget implements DashboardWidget {

    private final static String FLAT_LIGHT_LAF = "com.formdev.flatlaf.FlatLightLaf";
    private final static String FLAT_LIGHT_PROFILE = "FlatLaf Light";
    private final static String FLAT_DARK_LAF = "com.formdev.flatlaf.FlatDarkLaf";
    private final static String FLAT_DARK_PROFILE = "FlatLaf Dark";

    private final boolean isDefaultLaf;

    public AppearanceWidget() {
        String laf = NbPreferences.root().node("laf").get("laf", "");
        isDefaultLaf = laf.isEmpty() || FLAT_LIGHT_LAF.equals(laf);
    }

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_Appearance();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.of(
                WidgetElement.text(Bundle.TXT_Appearance()),
                WidgetElement.action(new SwitchLafAction(isDefaultLaf)),
                WidgetElement.aside(Bundle.TXT_RequiresRestart()),
                WidgetElement.separator(),
                WidgetElement.text(Bundle.TXT_AdvancedLAF()),
                WidgetElement.actionLink(new ShowFlatLafOptions())
        );
    }

    private static class SwitchLafAction extends AbstractAction {

        private static Notification restartNotification;

        private final boolean dark;

        private SwitchLafAction(boolean dark) {
            super(dark ? Bundle.LBL_DarkLaf() : Bundle.LBL_DefaultLaf());
            this.dark = dark;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String laf = dark ? FLAT_DARK_LAF : FLAT_LIGHT_LAF;
            String profile = dark ? FLAT_DARK_PROFILE : FLAT_LIGHT_PROFILE;
            NbPreferences.root().node("laf").put("laf", laf);
            FileObject editorConfig = FileUtil.getConfigFile("Editors");
            if (editorConfig != null) {
                try {
                    editorConfig.setAttribute("currentFontColorProfile", profile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            askForRestart();
        }

        private void askForRestart() {
            if (restartNotification != null) {
                restartNotification.clear();
            }
            restartNotification = NotificationDisplayer.getDefault().notify(
                    Bundle.TITLE_RestartIDE(),
                    ImageUtilities.loadImageIcon("org/netbeans/core/windows/resources/restart.png", true), //NOI18N
                    Bundle.TXT_RestartIDE(),
                    e -> {
                        if (restartNotification != null) {
                            restartNotification.clear();
                            restartNotification = null;
                        }
                        LifecycleManager.getDefault().markForRestart();
                        LifecycleManager.getDefault().exit();
                    });
        }

    }

    private static class ShowFlatLafOptions extends AbstractAction {

        private ShowFlatLafOptions() {
            super(Bundle.LBL_AdvancedLAFPanel());
            putValue(SHORT_DESCRIPTION, Bundle.STATUS_AdvancedLAFPanel());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open("Appearance/org-netbeans-swing-laf-flatlaf-FlatLafOptionsPanelController");
        }

    }

}
