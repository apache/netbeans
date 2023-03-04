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
/*
 * CommonGeneralFinishPanel.java
 *
 * Created on October 10, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import javax.swing.JTextField;

import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  shirleyc
 */
public class CommonGeneralFinishPanel extends  ResourceWizardPanel {
        
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CommonGeneralFinishVisualPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    private boolean setupValid = true;
    
    /** Create the wizard panel descriptor. */
    public CommonGeneralFinishPanel(ResourceConfigHelper helper, Wizard wizardInfo, String[] groupNames) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = groupNames;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
                FieldGroup[] groups = new FieldGroup[groupNames.length];
                for (int i = 0; i < this.groupNames.length; i++) {
                    groups[i] = FieldGroupHelper.getFieldGroup(wizardInfo, this.groupNames[i]);  //NOI18N
                }
                String panelType = null;
                if (wizardInfo.getName().equals(__MailResource)) {
                    panelType = CommonGeneralFinishVisualPanel.TYPE_MAIL_RESOUCE;
                }
                component = new CommonGeneralFinishVisualPanel(this, groups, panelType);
        }
        return component;
    }
    
    public boolean createNew() {
        if (component == null)
            return false;
        else
            return component.createNew();
    }
    
    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
        if (wizardInfo.getName().equals(__MailResource)) {
            return new HelpCtx("AS_Wiz_Mail_general"); //NOI18N
        }else{
            return HelpCtx.DEFAULT_HELP;
        }
        
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if(! setupValid){
            setErrorMsg(bundle.getString("Err_InvalidSetup"));
            return false;
        }
        setErrorMsg(bundle.getString("Empty_String"));
        if (component != null && component.jLabels != null && component.jFields != null) {
            int i;
            for (i=0; i < component.jLabels.length; i++) {
                String jLabel = (String)component.jLabels[i].getText();
                if (jLabel.equals(Util.getCorrectedLabel(bundle, __JndiName))) { //NOI18N
                    String jndiName = (String)((JTextField)component.jFields[i]).getText();
                    if (jndiName == null || jndiName.length() == 0) {
                        setErrorMsg(bundle.getString("Err_InvalidJndiName"));
                        return false;
                    }else if(! ResourceUtils.isLegalResourceName(jndiName)){
                        setErrorMsg(bundle.getString("Err_InvalidJndiName"));
                        return false;
                    }else if(! ResourceUtils.isUniqueFileName(jndiName, this.helper.getData().getTargetFileObject(), __MAILResource)){
                        setErrorMsg(bundle.getString("Err_DuplFileJndiName"));
                        return false;
                    }
                }    
                if (wizardInfo.getName().equals(__MailResource)) {
                    if (jLabel.equals(Util.getCorrectedLabel(bundle, __Host))) { // NO18N
                        String host = (String)((JTextField)component.jFields[i]).getText();
                        if (host == null || host.length() == 0) {
                            setErrorMessage(bundle.getString("Err_EmptyValue"), jLabel);
                            return false;
                        }
                    }
                    if (jLabel.equals(Util.getCorrectedLabel(bundle, __MailUser))) { // NO18N
                        String user = (String)((JTextField)component.jFields[i]).getText();
                        if (user == null || user.length() == 0) {
                            setErrorMessage(bundle.getString("Err_EmptyValue"), jLabel);
                            return false;
                        }
                    }
                    if (jLabel.equals(Util.getCorrectedLabel(bundle, __From))) { //NOI18N
                        String from = (String)((JTextField)component.jFields[i]).getText();
                        if (from == null || from.length() == 0) {
                            setErrorMessage(bundle.getString("Err_EmptyValue"), jLabel);
                            return false;
                        }
                    }
                } //Validity checks applicable to only Mail Resource Wizard
            }//for
        }
        return true;
    }
  
    public boolean isFinishPanel() {
        return isValid();
    }
     
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        if (wizardInfo.getName().equals(__MailResource)) {
            TemplateWizard wizard = (TemplateWizard)settings;
            String targetName = wizard.getTargetName();
            FileObject resFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
            this.helper.getData().setTargetFileObject (resFolder);
            if(resFolder != null){
                String resourceName = helper.getData().getString("jndi-name"); //NOI18N
                if ((resourceName != null) && (!resourceName.equals(""))) {
                    targetName = resourceName;
                }
                targetName = ResourceUtils.createUniqueFileName (targetName, resFolder, __MAILResource);
                this.helper.getData ().setTargetFile (targetName);
                if(component == null)
                    getComponent ();
                component.setHelper (this.helper);
            }else
               setupValid = false; 
        }
    }
    
    public void initData() {
        this.component.initData();
    }
    
    private boolean setupValid(){
        return setupValid;
    }
}

