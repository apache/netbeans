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
package org.netbeans.modules.javafx2.scenebuilder.options;

import org.netbeans.modules.javafx2.scenebuilder.Settings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.javafx2.scenebuilder.Home;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

@OptionsPanelController.SubRegistration(location = JavaOptions.JAVA,
displayName = "#AdvancedOption_DisplayName_SB",
keywords = "#AdvancedOption_Keywords_SB",
keywordsCategory = JavaOptions.JAVA + "/JavaFX",
id=SBOptionsPanelController.SUBREG_ID)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_SB=JavaFX", "AdvancedOption_Keywords_SB=javafx"})
public final class SBOptionsPanelController extends OptionsPanelController {
    public static final String SUBREG_CAT = JavaOptions.JAVA;
    public static final String SUBREG_ID = "SceneBuilder"; // NOI18N
    private SBOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    private Settings settings;
    
    @Override
    public void update() {
        settings = Settings.getInstance();
        Parameters.notNull("settings", settings); //NOI18N

        panel.load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        panel.store();
        settings.store();
        changed = false;
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return getPanel().isChanged();
    }

    Home getDefaultSBHome() {
        return settings.getPredefinedHome();
    }
    
    List<Home> getUserDefinedHomes() {
        return settings.getUserDefinedHomes();
    }
    
    void setUserDefinedHomes(List<Home> userDefs) {
        settings.setUserDefinedHomes(userDefs);
    }
    
    public Home getSbHome() {
        return settings.getSelectedHome();
    }
    
    public void setSbHome(Home sbHome) {
        settings.setSelectedHome(sbHome);
    }
    
    public boolean isSaveBeforeLaunch() {
        return settings.isSaveBeforeLaunch();
    }
    
    public void setSaveBeforeLaunch(boolean val) {
        settings.setSaveBeforeLaunch(val);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private SBOptionsPanel getPanel() {
        if (panel == null) {
            panel = new SBOptionsPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
}
