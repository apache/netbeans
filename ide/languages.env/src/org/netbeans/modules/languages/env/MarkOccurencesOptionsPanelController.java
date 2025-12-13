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
package org.netbeans.modules.languages.env;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public final class MarkOccurencesOptionsPanelController extends OptionsPanelController {

    private MarkOccurencesPanel panel;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    @Override
    public void update() {
        getPanel().load(this);
    }

    @Override
    public void applyChanges() {
        getPanel().store();
    }

    @Override
    public void cancel() {
    // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return true; // Always valid
    }

    @Override
    public boolean isChanged() {
        return getPanel().changed();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.env.markoccurrences"); // NOI18N
    }

    @Override
    public synchronized JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    public synchronized MarkOccurencesPanel getPanel() {
        if (panel == null) {
            panel = new MarkOccurencesPanel(this);
        }
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
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
