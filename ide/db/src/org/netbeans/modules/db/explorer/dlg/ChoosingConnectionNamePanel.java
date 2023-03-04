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
package org.netbeans.modules.db.explorer.dlg;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;

public class ChoosingConnectionNamePanel implements AddConnectionWizard.Panel {

    private final int stepIndex;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ConnectionNamePanel component;
    private AddConnectionWizard pw;
    private static HelpCtx CHOOSING_SCHEMA_PANEL_HELPCTX = new HelpCtx(ChoosingConnectionNamePanel.class);
    private boolean blockEventListener = false;

    public ChoosingConnectionNamePanel(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            if (pw == null) {
                return null;
            }
            assert pw != null : "ChoosingConnectionNamePanel must be initialized.";
            component = new ConnectionNamePanel(pw, pw.getDatabaseConnection().getDisplayName());
            component.setName(pw.getSteps()[stepIndex]);
            component.addPropertyChangeListener(ConnectionNamePanel.PROP_CONNECTION_NAME,
                    new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    fireChangeEvent();
                }
            });
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return CHOOSING_SCHEMA_PANEL_HELPCTX;
    }

    @Override
    public boolean isValid() {
        return component != null && (! component.getConntionName().isEmpty());
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        if(blockEventListener) return;
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(AddConnectionWizard settings) {
        this.pw = settings;
        blockEventListener = true;
        ((ConnectionNamePanel) getComponent()).setConnectionName(pw.getDatabaseConnection().getDisplayName());
        blockEventListener = false;
    }

    @Override
    public void storeSettings(AddConnectionWizard settings) {
        pw.getDatabaseConnection().setDisplayName(((ConnectionNamePanel) getComponent()).getConntionName());
    }
}
