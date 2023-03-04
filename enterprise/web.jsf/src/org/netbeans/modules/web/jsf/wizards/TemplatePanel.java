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

package org.netbeans.modules.web.jsf.wizards;

import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.JsfPreferences;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class TemplatePanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private TemplatePanelVisual component;
    private WizardDescriptor wizard;
    
    /** Creates a new instance of TemplatePanel */
    public TemplatePanel(WizardDescriptor wizard) {
        this.wizard = wizard;
        component = null;
    }

    @Override
    public TemplatePanelVisual getComponent() {
        if (component == null)
            component = new TemplatePanelVisual();
        
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.TemplatePanel");
    }

    @Override
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public boolean isValid() {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            JsfPreferences preferences = JsfPreferences.forProject(project);
            if (preferences.getPreferredLanguage() == null) { //NOI18N
                ClassPath cp  = wm.getDocumentBase() != null
                        ? ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE)
                        : ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.COMPILE);
                if (!JSFUtils.isFaceletsPresent(cp)) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(TemplatePanel.class, "ERR_NoJSFLibraryFound"));
                    return false;
                }
            }
            if (wm.getDocumentBase() == null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(TemplatePanel.class, "ERR_NoDocumentRootFound"));
                return false;
            }
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

//    InputStream getTemplate(){
//        getComponent();
//        return component.getTemplate();
//    }
//
//    InputStream getDefaultCSS(){
//        getComponent();
//        return component.getDefaultCSS();
//    }
//
//    InputStream getLayoutCSS(){
//        getComponent();
//        return component.getLayoutCSS();
//    }
//
//    String getLayoutFileName(){
//        getComponent();
//        return component.getLayoutFileName();
//    }
}
