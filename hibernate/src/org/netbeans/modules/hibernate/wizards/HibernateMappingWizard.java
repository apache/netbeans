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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.wizards;

import java.io.IOException;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Collections;

import javax.swing.JComponent;
import java.awt.Component;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Sources;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author gowri
 */
public class HibernateMappingWizard implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private Project project;
    private WizardDescriptor wizard;
    private HibernateMappingWizardDescriptor descriptor;
    private transient WizardDescriptor.Panel[] panels;
    private final String resourceAttr = "resource"; // NOI18N
    private static String DEFAULT_MAPPING_FILENAME = "hibernate.hbm"; // NOI18N
    private Logger logger = Logger.getLogger(HibernateMappingWizard.class.getName());

    public static HibernateMappingWizard create() {
        return new HibernateMappingWizard();
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
            // Check for unsupported projects. #142296
            if (hibernateEnv == null) {
                logger.info("Unsupported project " + project + ". Existing config wizard.");
                panels = new WizardDescriptor.Panel[]{
                            WizardErrorPanel.getWizardErrorWizardPanel()
                        };

            } else {
                Project p = Templates.getProject(wizard);
                SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
                WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups);

                panels = new WizardDescriptor.Panel[]{
                            targetChooser,
                            descriptor
                        };
            }
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components

                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                    // Sets steps names for a panel

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step

                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps

                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N

                }
            }
        }
        return panels;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        project = Templates.getProject(wizard);
        String wizardTitle = NbBundle.getMessage(HibernateMappingWizard.class, "LBL_MappingWizardTitle"); // NOI18N   
        descriptor = new HibernateMappingWizardDescriptor(project, wizardTitle);
        if (Templates.getTargetFolder(wizard) == null) {
            HibernateFileLocationProvider provider = project != null ? project.getLookup().lookup(HibernateFileLocationProvider.class) : null;
            FileObject location = provider != null ? provider.getSourceLocation() : null;
            if (location != null) {
                Templates.setTargetFolder(wizard, location);
            }
        }

        // Set the targetName here. Default name for the new files should be in the form : 'hibernate<i>.hbm.xml
        // and not like : hibernate.hbm<i>.xml
        if (wizard instanceof TemplateWizard) {
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
            // Check for unsupported projects. #142296
            if (hibernateEnv == null) {
                // Returning without initialization. User will be informed about this using error panel.
                return;
            }
            List<FileObject> mappingFiles = hibernateEnv.getAllHibernateMappingFileObjects();
            String targetName = DEFAULT_MAPPING_FILENAME;
            if (!mappingFiles.isEmpty() && foundMappingFileInProject(mappingFiles, DEFAULT_MAPPING_FILENAME)) {
                int mappingFilesCount = mappingFiles.size();
                targetName = "hibernate" + (mappingFilesCount++) + ".hbm";  //NOI18N
                while (foundMappingFileInProject(mappingFiles, targetName)) {
                    targetName = "hibernate" + (mappingFilesCount++) + ".hbm";  //NOI18N
                }
            }
            ((TemplateWizard) wizard).setTargetName(targetName);
        }
    }

    private boolean foundMappingFileInProject(List<FileObject> mappingFiles, String mappingFileName) {
        for (FileObject fo : mappingFiles) {
            if (fo.getName().equals(mappingFileName)) {
                return true;
            }
        }
        return false;
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public Set instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        String targetName = Templates.getTargetName(wizard);
        FileObject templateFileObject = Templates.getTemplate(wizard);
        DataObject templateDataObject = DataObject.find(templateFileObject);
        HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);

        DataObject newOne = templateDataObject.createFromTemplate(targetDataFolder, targetName);
        FileObject confFile = null;
        MyClass myClass = new MyClass();

        // Adding mapping entry in the selected config file.
        if (descriptor.getConfigurationFile() != null && !"".equals(descriptor.getConfigurationFile())) {
            confFile = (FileObject) descriptor.getConfigurationFile();
            DataObject confDataObject = DataObject.find(confFile);
            HibernateCfgDataObject hco = (HibernateCfgDataObject) confDataObject;
            SessionFactory sf = hco.getHibernateConfiguration().getSessionFactory();
            int mappingIndex = sf.addMapping(true);
            //check for duplicates
            boolean exist = false;
            String relPath = HibernateUtil.getRelativeSourcePath(newOne.getPrimaryFile(), hibernateEnv.getSourceLocation());
            for(int i=0;i<mappingIndex;i++){
                String tmpPath = sf.getAttributeValue(SessionFactory.MAPPING, i, resourceAttr);
                if(tmpPath == null ? relPath == null : tmpPath.equals(relPath)){
                    exist = true;
                    break;
                }
            }
            if(!exist){
                sf.setAttributeValue(SessionFactory.MAPPING, mappingIndex, resourceAttr,
                        relPath);
            }
            hco.modelUpdatedFromUI();
            hco.save();
        }

        try {
            HibernateMappingDataObject hmo = (HibernateMappingDataObject) newOne;
            if (descriptor.getClassName() != null && !"".equals(descriptor.getClassName())) {
                myClass.setAttributeValue("name", descriptor.getClassName());  //NOI18N
                if (descriptor.getDatabaseTable() != null && !"".equals(descriptor.getDatabaseTable())) {
                    myClass.setAttributeValue("table", descriptor.getDatabaseTable());  //NOI18N
                    myClass.setAttributeValue("dynamic-insert", null);  //NOI18N
                    myClass.setAttributeValue("dynamic-update", null);  //NOI18N
                    myClass.setAttributeValue("mutable", null);  //NOI18N
                    myClass.setAttributeValue("optimistic-lock", null);  //NOI18N
                    myClass.setAttributeValue("polymorphism", null);  //NOI18N
                    myClass.setAttributeValue("select-before-update", null);  //NOI18N
                }
                hmo.addMyClass(myClass);
            }
            hmo.save();
            return Collections.singleton(hmo.getPrimaryFile());
        } catch (Exception e) {
            return Collections.EMPTY_SET;

        }
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public String name() {
        return NbBundle.getMessage(HibernateMappingWizard.class, "LBL_MappingWizardTitle"); // NOI18N

    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N

        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
}
