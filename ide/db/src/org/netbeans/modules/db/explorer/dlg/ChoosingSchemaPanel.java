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
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ChoosingSchemaPanel implements AddConnectionWizard.Panel, WizardDescriptor.FinishablePanel<AddConnectionWizard> {

    private final int stepIndex;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SchemaPanel component;
    private AddConnectionWizard pw;
    private static HelpCtx CHOOSING_SCHEMA_PANEL_HELPCTX = new HelpCtx(ChoosingSchemaPanel.class);

    public ChoosingSchemaPanel(int stepIndex) {
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
            assert pw != null : "ChoosingSchemaPanel must be initialized.";
            component = new SchemaPanel(pw, pw.getDatabaseConnection());
            component.setSchemas(pw.getSchemas(), pw.getCurrentSchema());
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, stepIndex);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, pw.getSteps());
            component.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.FALSE);
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.FALSE);
            component.setName(pw.getSteps()[stepIndex]);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return CHOOSING_SCHEMA_PANEL_HELPCTX;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(AddConnectionWizard settings) {
        this.pw = settings;
    }

    @Override
    public void storeSettings(AddConnectionWizard settings) {
        pw.setCurrentSchema(component.getSchema());
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
