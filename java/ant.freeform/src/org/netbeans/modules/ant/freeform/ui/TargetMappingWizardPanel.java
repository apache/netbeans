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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.AntScriptUtils;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class TargetMappingWizardPanel implements WizardDescriptor.Panel {

    public static final String PROP_TARGET_MAPPINGS = "targetMappings"; // <List> NOI18N
    
    private TargetMappingPanel component;
    private WizardDescriptor wizardDescriptor;
    private List<TargetDescriptor> targets;
    private List<String> targetNames;
    
    public TargetMappingWizardPanel(List<TargetDescriptor> targets) {
        this.targets = targets;
        getComponent().setName(NbBundle.getMessage(TargetMappingWizardPanel.class, "WizardPanel_BuildAndRunActions"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new TargetMappingPanel(targets, false);
            component.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TargetMappingWizardPanel.class, "ACSD_TargetMappingWizardPanel"));
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( TargetMappingWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return true;
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        File f = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        FileObject fo = FileUtil.toFileObject(f);
        // Util.getAntScriptTargetNames can return null when script is 
        // invalid but first panel checks script validity so it is OK here.
        List<String> l = null;
        try {
            l = AntScriptUtils.getCallableTargetNames(fo);
        } catch (IOException x) {/* ignore */}
        // #47784 - update panel only once or when Ant script has changed
        if (targetNames == null || !targetNames.equals(l)) {
            targetNames = new ArrayList<String>(l);
            component.setTargetNames(l, true);
        }
        File projDir = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        File antScript = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT);
        if (!(antScript.getParentFile().equals(projDir) && antScript.getName().equals("build.xml"))) { // NOI18N
            // NON-DEFAULT location of build file
            component.setScript("${"+ProjectConstants.PROP_ANT_SCRIPT+"}"); // NOI18N
        } else {
            component.setScript(null);
        }
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        wizardDescriptor.putProperty(PROP_TARGET_MAPPINGS, component.getMapping());
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
}
