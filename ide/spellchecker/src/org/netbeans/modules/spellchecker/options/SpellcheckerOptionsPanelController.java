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
package org.netbeans.modules.spellchecker.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.spi.options.OptionsPanelController.SubRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
@SubRegistration(
    location=OptionsDisplayer.EDITOR,
    position=750,
    displayName="#TITLE_OptionsPanel",
    keywords="#KW_SpellcheckerOptions",
    keywordsCategory="Editor/Spellchecker")
public class SpellcheckerOptionsPanelController extends OptionsPanelController {

    private SpellcheckerOptionsPanel comp;
    private boolean valid = true;
    private boolean changed = false;

    public SpellcheckerOptionsPanelController() {}

    public void update() {
        getComponentImpl().update();
        changed = false;
    }

    public void applyChanges() {
        getComponentImpl().commit();
        changed = false;
    }

    public void cancel() {
        getComponentImpl().update();
        changed = false;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isChanged() {
        return changed;
    }
    
    void notifyChanged(boolean changed) {
        this.changed = changed;
    }
    
    void setValid(boolean valid) {
        this.valid = valid;
        pcs.firePropertyChange(PROP_VALID, null, valid);
    }

    private synchronized SpellcheckerOptionsPanel getComponentImpl() {
        if (comp == null) {
            comp = new SpellcheckerOptionsPanel(this);
            setValid(true);
        }
        
        return comp;
    }
    
    public JComponent getComponent(Lookup masterLookup) {
        return getComponentImpl();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.editor.spellchecker"); //NOI18N
    }

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
}
