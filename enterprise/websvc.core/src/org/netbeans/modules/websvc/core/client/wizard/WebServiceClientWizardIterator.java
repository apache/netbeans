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

package org.netbeans.modules.websvc.core.client.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements TemplateWizard.Iterator {

    private int index = 0;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;

    private TemplateWizard wiz;
    // !PW FIXME How to handle freeform???
    private Project project;

    /** Entry point specified in layer
     */
    public static WebServiceClientWizardIterator create() {
        return new WebServiceClientWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }

    public void initialize(TemplateWizard wizard) {
        wiz = wizard;
        project = Templates.getProject(wiz);

        index = 0;
        panels = createPanels();

        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = JaxWsUtils.createSteps (beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }

            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
        }
    }

    public void uninitialize(TemplateWizard wizard) {
        wiz = null;
        panels = null;
    }
    
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        ClientCreator creator = CreatorProvider.getClientCreator(project, wiz);
        if (creator!=null) creator.createClient();

        // logging usage of wizard
        Object[] params = new Object[5];
        boolean isJaxWs = ClientWizardProperties.JAX_WS.equals(wiz.getProperty(ClientWizardProperties.JAX_VERSION));
        params[0] = isJaxWs ? LogUtils.WS_STACK_JAXWS : LogUtils.WS_STACK_JAXRPC ;
        params[1] = project.getClass().getName();
        J2eeModule j2eeModule = JaxWsUtils.getJ2eeModule(project);
        params[2] = j2eeModule == null ? "J2SE" : j2eeModule.getModuleVersion()+"("+JaxWsUtils.getModuleType(project)+")"; //NOI18N
        params[3] = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH) ? "DISPATCH": "WS CLIENTL"; //NOI18N
        int wsdlSource = (Integer)wiz.getProperty(ClientWizardProperties.WSDL_SOURCE);
        switch (wsdlSource) {
            case 0: params[4] = "FROM PROJECT";break; //NOI18N
            case 1: params[4] = "FROM FILE";break; //NOI18N
            case 3: params[4] = "FROM SAAS WSDL";break; //NOI18N
            default: params[4] = "FROM URL"; //NOI18N
        }
        LogUtils.logWsWizard(params);

        return Collections.singleton(dTemplate);
    }

    public String name() {
        return NbBundle.getMessage(WebServiceClientWizardIterator.class, "LBL_WebServiceClient"); // NOI18N
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
       return panels[index];
    }

    public boolean hasNext() {
        return index < panels.length - 1;  
    }

    public void nextPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index++;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void previousPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index--;
    }

    public void addChangeListener(ChangeListener l) {
        // nothing to do yet
    }

    public void removeChangeListener(ChangeListener l) {
        // nothing to do yet
    }
}
