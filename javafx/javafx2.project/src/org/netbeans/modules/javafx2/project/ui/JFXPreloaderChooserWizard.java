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
package org.netbeans.modules.javafx2.project.ui;

import java.io.File;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * A wizard allowing the user to select a project or JAR file,
 * from which the JavaFX 2.0 preloader class can be selected
 * in Project Properties Run panel.
 * 
 * @author Petr Somol
 */
public class JFXPreloaderChooserWizard extends WizardDescriptor {
    
    JFXPreloaderChooserWizardIterator wizardIterator;
    
    private JFXProjectProperties.PreloaderSourceType sourceType = JFXProjectProperties.PreloaderSourceType.PROJECT;
    private File selectedSource;

    private java.awt.Dialog dialog;

    // ---------

    public JFXPreloaderChooserWizard() {
        this(new JFXPreloaderChooserWizardIterator());
    }

    private JFXPreloaderChooserWizard(JFXPreloaderChooserWizardIterator iterator) {
        wizardIterator = iterator;

        putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                    new String[] { NbBundle.getMessage(JFXPreloaderChooserWizard.class, "CTL_SelectType_Step"), // NOI18N
                                   NbBundle.getMessage(JFXPreloaderChooserWizard.class, "CTL_SelectSource_Step") }); // NOI18N

        setTitle(NbBundle.getMessage(JFXPreloaderChooserWizard.class, "CTL_JFXPreloaderChooserWizard_Title")); // NOI18N
        setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
    }
    
    @SuppressWarnings("unchecked")
    public boolean show() {
        setPanelsAndSettings(wizardIterator, this);
        updateState();
        if (dialog == null)
            dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        dialog.toFront();
        dialog.dispose();
        return getValue() == FINISH_OPTION;
    }

    // -------

//    public void stepToNext() {
//        if (wizardIterator.hasNext()) {
//            wizardIterator.nextPanel();
//            updateState();
//        }
//    }
    public void update() {
        this.updateState();
    }

    public void setSourceType(JFXProjectProperties.PreloaderSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public JFXProjectProperties.PreloaderSourceType getSourceType() {
        return sourceType;
    }

    public void setSelectedSource(File selectedSource) {
        this.selectedSource = selectedSource;
    }

    public File getSelectedSource() {
        return selectedSource;
    }

}
