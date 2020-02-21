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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.util.ArrayList;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class SelectBinaryPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, NamedPanel, ChangeListener {
    private WizardDescriptor wizardDescriptor;
    private SelectBinaryPanelVisual component;
    private final String name;
    private boolean isValid = false;
    private final BinaryWizardStorage wizardStorage;
    private final Set<ChangeListener> listeners = new HashSet<>(1);

    public SelectBinaryPanel(){
        name = NbBundle.getMessage(SelectBinaryPanel.class, "SelectBinaryPanelVisual.Title"); // NOI18N
        wizardStorage = new BinaryWizardStorage(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFinishPanel() {
        return  Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String[] res;
        Object o = component.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] names = (String[]) o;
        if (Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wizardDescriptor))){
            res = new String[]{names[0]};
        } else {
            res = new String[]{names[0], "..."}; // NOI18N
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, res);
      	fireChangeEvent();
    }

    @Override
    public SelectBinaryPanelVisual getComponent() {
        if (component == null) {
            component = new SelectBinaryPanelVisual(this);
      	    component.setName(name);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewBinaryWizardP1"); // NOI18N
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void validate(){
        isValid = component.valid();
        if (SwingUtilities.isEventDispatchThread()) {
            fireChangeEvent();
        } else {
            SwingUtilities.invokeLater(() -> {
                fireChangeEvent();
            });
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    WizardDescriptor getWizardDescriptor(){
        return wizardDescriptor;
    }

    public BinaryWizardStorage getWizardStorage(){
        return wizardStorage;
    }

    public static class BinaryWizardStorage {
        private List<FSPath> binaryPath;
        private FSPath sourceFolderPath;
        private final SelectBinaryPanel controller;

        public BinaryWizardStorage(SelectBinaryPanel controller) {
            this.controller = controller;
        }

        public List<FSPath> getBinaryPath() {
            return binaryPath;
        }

        public void setBinaryPath(FileSystem fs, String path) {
            binaryPath = new ArrayList<>();
            for(String s : path.split(";")) { //NOI18N
                String p = s.trim();
                if (!p.isEmpty()) {
                    binaryPath.add(new FSPath(fs, p));
                }
            }
            controller.validate();
        }

        public FSPath getSourceFolderPath() {
            return sourceFolderPath;
        }

        public void setSourceFolderPath(FSPath path) {
            this.sourceFolderPath = path;
            controller.validate();
        }

        public void validate() {
            controller.validate();
        }
    }
}
