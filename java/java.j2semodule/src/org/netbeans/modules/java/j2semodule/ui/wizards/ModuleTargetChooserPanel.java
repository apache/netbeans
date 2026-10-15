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
package org.netbeans.modules.java.j2semodule.ui.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Dusan Balek
 */
public class ModuleTargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private ModuleTargetChooserPanelGUI gui;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    
    public ModuleTargetChooserPanel(Project project, SourceGroup folders[]) {
        this.project = project;
        this.folders = folders;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new ModuleTargetChooserPanelGUI(project, folders, null);
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
        this.wizard = wizard;
        if (gui != null) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(wizard);            
            // Init values
            gui.initValues(Templates.getTemplate(wizard), preselectedFolder);
        }
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (isValid()) {
            assert gui != null;
            FileObject rootFolder = gui.getRootFolder();
            Templates.setTargetFolder(wizard, rootFolder.isValid() ? rootFolder : null);
            Templates.setTargetName(wizard, gui.getTargetName());
        }        
        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty("NewFileWizard_Title", null); // NOI18N
        }
    }

    @Override
    public boolean isValid() {
        setErrorMessage( null );
        if (gui == null) {
            return false;
        }        
        if (gui.getTargetName() == null) {
            setErrorMessage("INFO_ModuleTargetChooser_ProvideModuleName"); // NOI18N
            return false;
        }
        if (!isValidModuleName(gui.getTargetName())) {
            setErrorMessage( "ERR_ModuleTargetChooser_InvalidModule" ); // NOI18N
            return false;
        }
        if (!isValidModule(gui.getRootFolder(), gui.getTargetName())) {
            setErrorMessage("ERR_ModuleTargetChooser_InvalidFolder"); // NOI18N
            return false;
        }
        if(isModuleNameAlreadyExists(gui.getRootFolder(), gui.getTargetName())){
            setErrorMessage("ERR_ModuleTargetChooser_AlreadyExistModule"); // NOI18N
            return false;
        }
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

    private void fireChange() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    private boolean isValidModuleName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer st = new StringTokenizer(str, ".");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    private boolean isValidModule (FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final StringTokenizer st = new StringTokenizer(path,".");   //NOI18N
        while (st.hasMoreTokens()) {
            root = root.getFileObject(st.nextToken());
            if (root == null) {
                return true;
            }
            else if (root.isData()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isModuleNameAlreadyExists(FileObject root, final String newModuleName){
         return Arrays.stream(root.getChildren()).anyMatch(module -> module.getName().equalsIgnoreCase(newModuleName));
    }

    private void setErrorMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(ModuleTargetChooserPanelGUI.class, key));
        }
    }
}
