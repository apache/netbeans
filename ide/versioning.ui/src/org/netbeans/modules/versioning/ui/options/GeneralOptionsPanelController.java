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

package org.netbeans.modules.versioning.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

final class GeneralOptionsPanelController extends OptionsPanelController implements VCSOptionsKeywordsProvider {
    
    private GeneralOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    
    public GeneralOptionsPanelController() { }

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (!validateFields()) return;
        getPanel().store();        
        changed = false;
    }
    
    @Override
    public void cancel() {
        getPanel().cancel();
        changed = false;
    }
    
    @Override
    public boolean isValid() {
        return getPanel().valid();
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(OptionsPanelController.class);
    }
    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }            

    @Override
    public boolean acceptKeywords (List<String> keywords) {
        Set<String> allKeywords = new HashSet<String>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    private Boolean validateFields() {
        
        return true;
    }

    private GeneralOptionsPanel getPanel() {
        if (panel == null) {
            panel = new GeneralOptionsPanel(this);
        }
        return panel;
    }
    
    void changed (boolean changed) {
        if (this.changed != changed) {
            this.changed = changed;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, !changed, changed);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
 
}
