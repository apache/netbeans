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
 * JMSWizardPanel.java
 *
 * Created on November 17, 2003, 12:57 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.Vector;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  nityad
 */
public class JMSWizardPanel extends ResourceWizardPanel {
        
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private JMSWizardVisualPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private String[] groupNames;
    private boolean setupValid = true;
    
    /** Creates a new instance of JMSWizardPanel */
    public JMSWizardPanel(ResourceConfigHelper helper, Wizard wizardInfo) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
        this.groupNames = new String[]{"general"}; //NOI18N
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
                component = new JMSWizardVisualPanel(this, groups); 
        }
        return component;
    }
    
    public boolean createNew() {
        if (component == null)
            return false;
        else{
            return true;
            //return component.createNew();
        }    
    }
    
    public HelpCtx getHelp() {
       return new HelpCtx("AS_Wiz_JMS_general"); //NOI18N
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public boolean isValid() {
        //Fix for bug# 5025502 & 5025573 - User should not be allowed to register a 
        //JMS Resource without JNDI name or invalid name 
        if(! setupValid){
            setErrorMsg(bundle.getString("Err_InvalidSetup"));
            return false;
        }
        setErrorMsg(bundle.getString("Empty_String"));
        String jndiName = helper.getData().getString("jndi-name"); //NOI18N
        if(jndiName.trim().length() == 0 || jndiName.trim().equals("")) {//NOI18N
            setErrorMsg(bundle.getString("Err_InvalidJndiName"));
            return false;
        }else if(! ResourceUtils.isLegalResourceName(jndiName)){
            setErrorMsg(bundle.getString("Err_InvalidJndiName"));
            return false;
        }else if(! ResourceUtils.isUniqueFileName(jndiName, this.helper.getData().getTargetFileObject(), __JMSResource)){
            setErrorMsg(bundle.getString("Err_DuplFileJndiName"));
            return false;
        }else
            return true;
    }
  
    public FieldGroup getFieldGroup(String groupName) {
        return FieldGroupHelper.getFieldGroup(wizardInfo, groupName); 
    }
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        FileObject resFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject (resFolder);
        if(resFolder != null){
            String resourceName = helper.getData().getString("jndi-name"); //NOI18N
            if((resourceName != null) && (! resourceName.equals(""))) {
                targetName = resourceName;
            }
            targetName = ResourceUtils.createUniqueFileName (targetName, resFolder, __JMSResource);
            this.helper.getData ().setTargetFile (targetName);
            if(component == null)
                getComponent ();
            component.setHelper (this.helper);
        }else
            setupValid = false;
    }
    
    public boolean isFinishPanel() {
        isValid();
        ResourceConfigData data = helper.getData();
        Vector vec = data.getProperties();
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            if (pair.getParamName() == null || pair.getParamValue() == null ||
                    pair.getParamName().length() == 0 || pair.getParamValue().length() == 0){
                return false;
            }
        }
        return true;
    }
    
    private boolean setupValid(){
        return setupValid;
    }
}
