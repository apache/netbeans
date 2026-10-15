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
package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.jaxb.spi.SchemaCompiler;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * @author gmpatil
 * @author lgao
 */
public class JAXBWizardIterator implements TemplateWizard.Iterator  {
    private WizardDescriptor wizardDescriptor;    
    private WizardDescriptor.Panel[] panels = null;
    private int cursor;
    private Project project;

    public JAXBWizardIterator() {
    }
    
    public JAXBWizardIterator(Project project) {
        this.project = project;
        initWizardPanels();
    }
    
    public static JAXBWizardIterator create() {
        return new JAXBWizardIterator();
    }
  
    private void initWizardPanels() {
        cursor = 0;
        panels = new WizardDescriptor.Panel[] {
            new JAXBWizBindingCfgPanel(),
        };
    }

    public void addChangeListener(ChangeListener changeListener) {
    }

    public void removeChangeListener(ChangeListener changeListener) {
    }

    public WizardDescriptor.Panel current() {
        return panels[ cursor ];
    }

    public boolean hasNext() {
        return cursor < panels.length - 1;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String name() {
        return NbBundle.getMessage(this.getClass(),"LBL_JAXBWizTitle");// NOI18N                
    }
    
    public void nextPanel() {
        cursor ++;
    }

    public void previousPanel() {
        cursor --;
    }

    public boolean hasPrevious() {
        return cursor > 0;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); //NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); //NOI18N
            }
        }        
    }

    public Set instantiate() throws IOException {        
        return new HashSet();
    }

    public void uninitialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = null;
    }

    // TemplateWizard specific - Start
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        wiz.setMessage(null);
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );  
        
        SchemaCompiler schemaCompiler = CompilerFinder.findCompiler(project);
        if (schemaCompiler != null) {
            try {
                schemaCompiler.importResources(wiz);
            } catch (Throwable ex ) {
                String msg = NbBundle.getMessage(JAXBWizardIterator.class,
                        "MSG_ErrorReadingSchema");//NOI18N
                wiz.putProperty(JAXBWizModuleConstants.WIZ_ERROR_MSG, msg);
                wiz.setMessage(msg);
                wiz.setMessageType(NotifyDescriptor.ERROR_MESSAGE);
                wiz.setValid(false);
                project.getProjectDirectory().getFileSystem().refresh(true);
                throw new IOException(msg, ex);
            }
            schemaCompiler.compileSchema(wiz);
        } else {
            String msg = NbBundle.getMessage(JAXBWizardIterator.class,
                    "MSG_NoSchemaCompiler");//NOI18N
            wiz.putProperty(JAXBWizModuleConstants.WIZ_ERROR_MSG, msg);
            wiz.setMessage(msg);
            wiz.setMessageType(NotifyDescriptor.ERROR_MESSAGE);
            wiz.setValid(false);
            project.getProjectDirectory().getFileSystem().refresh(true);
            throw new IOException(msg);
        }

        ProjectHelper.addJaxbApiEndorsed(project);
        ProjectHelper.disableCoS(project, true);
        return Collections.singleton(dTemplate);
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
    
    public void initialize(TemplateWizard wiz) {
        project = Templates.getProject(wiz);
        List<String> schemas = ProjectHelper.getSchemaNames(project);
        wiz.putProperty(JAXBWizModuleConstants.EXISTING_SCHEMA_NAMES, schemas);
        initWizardPanels();     

        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        String name = ProjectUtils.getInformation(project).getName();
        wiz.putProperty(JAXBWizModuleConstants.PROJECT_NAME, name);
        wiz.putProperty(JAXBWizModuleConstants.PROJECT_DIR, 
                FileUtil.toFile(project.getProjectDirectory()));
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); //NOI18N
            }
        }
        
    }

    public void uninitialize(TemplateWizard wiz) {
        if ( wiz.getValue() == TemplateWizard.FINISH_OPTION ) {
            this.project = null;
        }
    }
    // TemplateWizard specific - End
}
