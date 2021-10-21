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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.core.CreatorProvider;

import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.WizardDescriptor;

import org.netbeans.modules.websvc.api.support.SourceGroups;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebServiceFromWSDLWizardIterator implements TemplateWizard.Iterator {
    
    private Project project;

    /** Create a new wizard iterator. */
    public NewWebServiceFromWSDLWizardIterator() {
    }
        
    public static NewWebServiceFromWSDLWizardIterator create() {
        return new NewWebServiceFromWSDLWizardIterator();
    }

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        
        //new WebServiceCreator(project, wiz).createFromWSDL();
        ServiceCreator creator = CreatorProvider.getServiceCreator(project, wiz);
        if (creator!=null) {
            creator.createServiceFromWsdl();
            // logging usage of wizard
            Object[] params = new Object[5];
            String creatorClassName = creator.getClass().getName();
            params[0] = creatorClassName.contains("jaxrpc") ? LogUtils.WS_STACK_JAXRPC : LogUtils.WS_STACK_JAXWS; //NOI18N
            params[1] = project.getClass().getName();
            J2eeModule j2eeModule = JaxWsUtils.getJ2eeModule(project);
            params[2] = j2eeModule == null ? "J2SE" : j2eeModule.getModuleVersion()+"("+JaxWsUtils.getModuleType(project)+")"; //NOI18N
            params[3] = (Boolean) wiz.getProperty(WizardProperties.USE_PROVIDER) ? "PROVIDER": "WS FROM WSDL"; //NOI18N
            params[4] = (Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN) ? "STATELESS EJB" : "SERVLET"; //NOI18N
            LogUtils.logWsWizard(params);
        } else {
             Logger.getLogger(NewWebServiceFromWSDLWizardIterator.class.getName()).log(Level.WARNING, "Cannot fin=d service creator");
        }

        return Collections.singleton(dTemplate);
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient TemplateWizard wiz;
    private transient WizardDescriptor.Panel<WizardDescriptor> bottomPanel;

    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        bottomPanel = new WebServiceFromWSDL(wiz, project);
        WizardDescriptor.Panel<WizardDescriptor> firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0) {
            SourceGroup[] genericSourceGroups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, genericSourceGroups, bottomPanel), sourceGroups, true);

        } else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, bottomPanel, true));

        JComponent comp = (JComponent) firstPanel.getComponent();
        Utils.changeLabelInComponent(comp, NbBundle.getMessage(NewWebServiceFromWSDLWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_Webservice_Name") );
        Utils.hideLabelAndLabelFor(comp, NbBundle.getMessage(NewWebServiceFromWSDLWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        
        panels = new WizardDescriptor.Panel[] {
            firstPanel,
        };
        
        // Creating steps.
        Object prop = this.wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }
        panels = null;
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_WizardStepsCount"), Integer.valueOf(index + 1).toString(), Integer.valueOf(panels.length).toString()); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

}
