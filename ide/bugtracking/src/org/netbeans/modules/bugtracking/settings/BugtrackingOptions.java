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

package org.netbeans.modules.bugtracking.settings;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.team.ide.spi.SettingsServices;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// XXX could use @ContainerRegistration were it not for special folder path & special GUI

/**
 * Bugtracking options panel combined from panels for various settings.
 *
 * @author Pavel Buzek
 * @author Tomas Stupka
 */
@OptionsPanelController.SubRegistration(
    id = BugtrackingOptions.OPTIONS_PATH,
    displayName="#LBL_IssueTracking",
    location=SettingsServices.TEAM_SETTINGS_LOCATION,
    keywords="#KW_IssueTracking",
    keywordsCategory="Team/IssueTracking")
public class BugtrackingOptions extends OptionsPanelController {
        private BugtrackingOptionsPanel panel;
        private boolean initialized = false;
        private Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();
        private DashboardOptions tasksPanel;

        public static final String OPTIONS_PATH = SettingsServices.TASKS_SETTINGS_ID;

        public BugtrackingOptions() {
            if (initialized) return;
            initialized = true;
            tasksPanel = new DashboardOptions();
            panel = new BugtrackingOptionsPanel(tasksPanel);
            
            Lookup lookup = Lookups.forPath("BugtrackingOptionsDialog"); // NOI18N
            Iterator<? extends AdvancedOption> it = lookup.lookupAll(AdvancedOption.class).iterator();
            while (it.hasNext()) {
                AdvancedOption option = it.next();
                String category = option.getDisplayName();
                OptionsPanelController controller;
                try {
                    controller = option.create();
                } catch (Throwable t) {
                    BugtrackingManager.LOG.log(Level.WARNING, "Problems while creating option category : " + category, t);  // NOI18N
                    continue;
                }
                categoryToController.put(category, controller);
            }
        }
        
        @Override
        public JComponent getComponent(Lookup masterLookup) {
            final Set<Entry<String, OptionsPanelController>> controllerEntries = categoryToController.entrySet();
            if(controllerEntries.isEmpty()) {
                panel.setPluginListVisible(false);                
            } else {
                panel.setPluginListVisible(true);   
                for(Entry<String, OptionsPanelController> controllerEntry : controllerEntries) {                                
                    panel.addPanel(controllerEntry.getKey(), controllerEntry.getValue().getComponent(masterLookup));
                }
            }
            return panel;
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            for (OptionsPanelController c: categoryToController.values()) {
                c.removePropertyChangeListener(l);
            }
            tasksPanel.getPropertySupport().removePropertyChangeListener(l);
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            for (OptionsPanelController c: categoryToController.values()) {
                c.addPropertyChangeListener(l);
            }
            tasksPanel.getPropertySupport().addPropertyChangeListener(l);
        }
        
        @Override
        public void update() {
            Iterator<OptionsPanelController> it = categoryToController.values().iterator();
            while (it.hasNext()) {
                it.next().update();
            }
            tasksPanel.update();
        }
        
        @Override
        public void applyChanges() {
            Iterator<OptionsPanelController> it = categoryToController.values().iterator();
            while (it.hasNext()) {
                it.next().applyChanges();
            }
            tasksPanel.applyChanges();
        }
        
        @Override
        public void cancel() {
            Iterator<OptionsPanelController> it = categoryToController.values().iterator();
            while (it.hasNext()) {
                it.next().cancel();
            }
            tasksPanel.cancel();
        }
        
        @Override
        public boolean isValid() {
            Iterator<OptionsPanelController> it = categoryToController.values().iterator();
            while (it.hasNext()) {
                if (!it.next().isValid()) {
                    return false;
                }
            }
            return tasksPanel.isDataValid();
        }
        
        @Override
        public boolean isChanged() {
            Iterator<OptionsPanelController> it = categoryToController.values().iterator();
            while (it.hasNext()) {
                if (it.next().isChanged()) {
                    return true;
                }
            }
            return tasksPanel.isChanged();
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.bugtracking.settings.BugtrackingOptions"); // NOI18N
        }
}
