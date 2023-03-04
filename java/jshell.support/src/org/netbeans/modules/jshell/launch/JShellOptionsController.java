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
package org.netbeans.modules.jshell.launch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@OptionsPanelController.SubRegistration(
    id = "JShell",
    location=JavaOptions.JAVA,
    displayName="#LBL_JavaShellOptions",
    keywords="#KW_JavaShell",
    keywordsCategory = JavaOptions.JAVA + "/JavaShell"
)
@NbBundle.Messages({
    "LBL_JavaShellOptions=Java Shell",
    "KW_JavaShell=JavaShell,Shell,JShell"
})
public class JShellOptionsController extends OptionsPanelController {
    private JShellOptionsPanel ui;
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private boolean changed;
    
    @Override
    public void update() {
        ui.load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        ui.store();
        changed = false;
    }

    @Override
    public void cancel() {
        // no op.
    }

    @Override
    public boolean isValid() {
        return ui.valid();
    }

    @Override
    public boolean isChanged() {
        return ui.isChanged();
    }
    
    private JShellOptionsPanel panel() {
        if (ui == null) {
            ui = new JShellOptionsPanel(this);
        }
        return ui;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return panel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("JShell.Options");
    }
    
    void changed() {
        if (!changed) {
            changed = true;
            supp.firePropertyChange(PROP_CHANGED, false, true);
        }
        supp.firePropertyChange(PROP_VALID, null, null);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        supp.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        supp.removePropertyChangeListener(l);
    }
    
}
