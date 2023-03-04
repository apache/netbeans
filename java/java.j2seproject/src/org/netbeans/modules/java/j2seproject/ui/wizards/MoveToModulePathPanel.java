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
package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Dusan Balek
 */
public final class MoveToModulePathPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    public static final String CP_ITEMS_TO_MOVE = "cp_items_to_move"; //NOI18N

    private final J2SEProject project;
    private final EditableProperties ep;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private MoveToModulePathPanelGUI gui;

    public MoveToModulePathPanel(J2SEProject project, EditableProperties ep) {
        this.project = project;
        this.ep = ep;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new MoveToModulePathPanelGUI(project);
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor wizard) {
        if (gui != null) {
            ClassPathSupport cs = new ClassPathSupport(project.evaluator(), project.getReferenceHelper(), project.getAntProjectHelper(), project.getUpdateHelper(), null);
            gui.setCPItems(cs.itemsList(ep.get(ProjectProperties.JAVAC_CLASSPATH)));
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizard) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value)
                || WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (gui != null) {
            wizard.putProperty(CP_ITEMS_TO_MOVE, gui.getCPItemsToMove());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }
}
