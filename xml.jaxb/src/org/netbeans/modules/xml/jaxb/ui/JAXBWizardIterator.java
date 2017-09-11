/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, //NOI18N
                                                    new Integer(i)); 
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
                project.getProjectDirectory().getFileSystem().refresh(true);
                throw new IOException(msg);
            }
            schemaCompiler.compileSchema(wiz);
        } else {
            String msg = NbBundle.getMessage(JAXBWizardIterator.class,
                    "MSG_NoSchemaCompiler");//NOI18N
            wiz.putProperty(JAXBWizModuleConstants.WIZ_ERROR_MSG, msg);
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
        if (prop != null && prop instanceof String[]) {
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, //NOI18N
                        new Integer(i)); 
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
