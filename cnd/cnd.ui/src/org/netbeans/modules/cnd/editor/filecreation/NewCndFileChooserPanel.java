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
package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * SimpleTargetChooserPanel extended with extension selector and logic
 *
 */
public class NewCndFileChooserPanel extends CndPanel {

    private final MIMEExtensions es;
    private final String defaultExt;
    private final boolean fileWithoutExtension;

    NewCndFileChooserPanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel, MIMEExtensions es, String defaultExt) {
        super(project, folders, bottomPanel);
        this.es = es;
        this.defaultExt = defaultExt;
        this.fileWithoutExtension = "".equals(defaultExt);
    }

    @Override
    public Component getComponent() {
        synchronized (guiLock) {
            if (gui == null) {
                gui = new NewCndFileChooserPanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), es, defaultExt);
                gui.addChangeListener(this);
            }
        }
        return gui;
    }

    @Override
    protected void doStoreSettings(WizardDescriptor settings) {
        if (getTargetExtension().length() > 0) {
            if (((NewCndFileChooserPanelGUI)gui).useTargetExtensionAsDefault()) {
                es.setDefaultExtension(getTargetExtension());
            } else {
                es.addExtension(getTargetExtension());
            }
        }
    }

    @Override
    public boolean isValid() {
        boolean ok = super.isValid();

        setErrorMessage (""); // NOI18N
        
        if (!ok) {

            return false;
        }
        
        String documentName = gui.getTargetName();

        if ((!fileWithoutExtension && getTargetExtension().length() == 0) || documentName.charAt(0) == '.') {
            // ignore invalid filenames
            setErrorMessage(NbBundle.getMessage(NewCndFileChooserPanel.class, "MSG_Invalid_File_Name"));
            return false;
        }

        if (!fileWithoutExtension && !es.getValues().contains(getTargetExtension())) {
            //MSG_new_extension_introduced
            String msg = NbBundle.getMessage(NewCndFileChooserPanel.class, "MSG_new_extension_introduced", getTargetExtension()); // NOI18N

            setErrorMessage(msg); // NOI18N
        }

        // check if the file name can be created
        String errorMessage = canUseFileName(gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), documentName, false);
        if (errorMessage != null) {
            setErrorMessage(errorMessage); // NOI18N
            return false;
        }

        return true;
    }
    
    private String getTargetExtension() {
        return ((NewCndFileChooserPanelGUI)gui).getTargetExtension();
    }

}
