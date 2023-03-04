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
package org.netbeans.modules.java.hints.spiimpl.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities.ClassPathBasedHintWrapper;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

//XXX: not finished!
public final class HintsOptionsPanelController extends OptionsPanelController {
    
    private HintsSettings settings;
    private HintsPanel panel;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
                    
    public void update() {
        if (panel != null) {
            panel.update(false);
            panel.setOverlayPreferences(settings, false);
        }
    }
    
    public void applyChanges() {
        if ( isChanged() ) {
            panel.applyChanges();
            FileHintPreferences.fireChange();
        }
    }
    
    public void cancel() {
        if (panel != null) {
            panel.cancel();
        }
    }
    
    public boolean isValid() {
        return true; 
    }
    
    public boolean isChanged() {
        return panel == null ? false : panel.isChanged();
    }
    
    public HelpCtx getHelpCtx() {
	return new HelpCtx("netbeans.optionsDialog.java.hints");
    }
    
    public synchronized HintsPanel getComponent(Lookup masterLookup) {
        Preferences prefs = null;
        if(masterLookup != null) {
            prefs = masterLookup.lookup(Preferences.class);
        }
        if (prefs != null) {
            settings = HintsSettings.createPreferencesBasedHintsSettings(prefs, true, null);
        } else {
            settings = null;
        }
        if (panel == null || settings != null) {
            panel = new HintsPanel(masterLookup.lookup(OptionsFilter.class), settings);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    @Override
    protected void setCurrentSubcategory(String subpath) {
        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {
            if (hm.id.equals(subpath)) {
                HintsPanel c = getComponent(null);
                c.select(hm, true);
                return;
            }
        }

        Logger.getLogger(HintsOptionsPanelController.class.getName()).log(Level.WARNING, "setCurrentSubcategory: cannot find: {0}", subpath);
    }

    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
}
