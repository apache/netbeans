package org.netbeans.modules.bugtracking.settings;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


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
