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
package org.netbeans.modules.csl.hints.infrastructure;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

final class HintsOptionsPanelController extends OptionsPanelController {
    private GsfHintsManager manager;
    private HintsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
                    
    HintsOptionsPanelController(GsfHintsManager manager) {
        this.manager = manager;
    }

    public void update() {
        if (panel != null) {
            panel.update();
        }
    }
    
    public void applyChanges() {
        if ( isChanged() ) {
            panel.applyChanges();
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
	return null; // new HelpCtx("...ID") if you have a help set
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if ( panel == null ) {
            OptionsFilter filter = masterLookup.lookup(OptionsFilter.class);
            panel = new HintsPanel(filter, manager.getHintsTreeModel(), manager);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
        
    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
}
