/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.loaders.reveng.HibernateRevengDataObject;
import org.netbeans.modules.hibernate.reveng.model.HibernateReverseEngineering;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.netbeans.modules.hibernate.wizards.support.Table;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateRevengWizard implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    private Project project;
    private WizardDescriptor wizardDescriptor;    
    private WizardDescriptor.Panel[] panels;
    private HibernateRevengDbTablesWizardDescriptor dbTablesDescriptor;
    

    private final String DEFAULT_REVENG_FILENAME = "hibernate.reveng"; // NOI18N
    private final String CATALOG_NAME = "match-catalog"; // NOI18N
    private final String EXCLUDE_NAME = "exclude"; // NOI18N
    private final String ATTRIBUTE_NAME = "match-schema"; // NOI18N
    private final String MATCH_NAME = "match-name"; // NOI18N
    private final String resourceAttr = "resource"; // NOI18N
    private final String classAttr = "class"; // NOI18N
    private Logger logger = Logger.getLogger(HibernateRevengWizard.class.getName());

public static HibernateRevengWizard create() {
        return new HibernateRevengWizard();
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
                Project prj = Templates.getProject(wizardDescriptor);

                SourceGroup[] groups = ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
                WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(prj, groups);

                panels = new WizardDescriptor.Panel[]{
                            targetChooser,
                            dbTablesDescriptor                            
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

 
    public String name() {
        return NbBundle.getMessage(HibernateRevengWizard.class, "Templates/Hibernate/RevEng"); // NOI18N

    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
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

    public void removeChangeListener(ChangeListener l) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizardDescriptor.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N

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

    private boolean foundRevengFileInProject(List<FileObject> revengFiles, String revengFileName) {
        for (FileObject fo : revengFiles) {
            if (fo.getName().equals(revengFileName)) {
                return true;
            }
        }
        return false;
    }

    
    public final void initialize(WizardDescriptor wiz) {
        wizardDescriptor = wiz;
        project = Templates.getProject(wiz);
        
        String wizardTitle = NbBundle.getMessage(HibernateRevengWizard.class, "Templates/Hibernate/RevEng"); // NOI18N
        dbTablesDescriptor = new HibernateRevengDbTablesWizardDescriptor(project, wizardTitle);        

        if (Templates.getTargetFolder(wiz) == null) {
            HibernateFileLocationProvider provider = project != null ? project.getLookup().lookup(HibernateFileLocationProvider.class) : null;
            FileObject location = provider != null ? provider.getSourceLocation() : null;
            if (location != null) {
                Templates.setTargetFolder(wiz, location);
            }
        }

        // Set the targetName here. Default name for new files should be in the form : 'hibernate<i>.reveng.xml
        // and not like : hibernate.reveng<i>.xml.
        if (wiz instanceof TemplateWizard) {
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
            // Check for unsupported projects. #142296
            if (hibernateEnv == null) {
                // Returning without initialization. User will be informed about this using error panel.
                return;
            }
            List<FileObject> revengFiles = hibernateEnv.getAllHibernateReverseEnggFileObjects();
            String targetName = DEFAULT_REVENG_FILENAME;
            if (!revengFiles.isEmpty() && foundRevengFileInProject(revengFiles, DEFAULT_REVENG_FILENAME)) {
                int revengFilesCount = revengFiles.size();
                targetName = "hibernate" + (revengFilesCount++) + ".reveng";  //NOI18N
                while (foundRevengFileInProject(revengFiles, targetName)) {
                    targetName = "hibernate" + (revengFilesCount++) + ".reveng";  //NOI18N
                }
            }
            ((TemplateWizard) wiz).setTargetName(targetName);
        }
    }   
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }

    public Set instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        String targetName = Templates.getTargetName(wizardDescriptor);
        FileObject templateFileObject = Templates.getTemplate(wizardDescriptor);
        DataObject templateDataObject = DataObject.find(templateFileObject);


        DataObject newOne = templateDataObject.createFromTemplate(targetDataFolder, targetName);
        try {
            HibernateRevengDataObject hro = (HibernateRevengDataObject) newOne;
            HibernateReverseEngineering hre = hro.getHibernateReverseEngineering();

            // Add Schema Selection.
            int jx = hre.addSchemaSelection(true);
            if (dbTablesDescriptor.getCatalogName() != null && !"".equals(dbTablesDescriptor.getCatalogName())) {
                hre.setAttributeValue(HibernateReverseEngineering.SCHEMA_SELECTION, jx, CATALOG_NAME, dbTablesDescriptor.getCatalogName());
            } else {
                hre.setAttributeValue(HibernateReverseEngineering.SCHEMA_SELECTION, jx, CATALOG_NAME, null);
            }
            if (dbTablesDescriptor.getSchemaName() != null && !"".equals(dbTablesDescriptor.getSchemaName())) {
                hre.setAttributeValue(HibernateReverseEngineering.SCHEMA_SELECTION, jx, ATTRIBUTE_NAME, dbTablesDescriptor.getSchemaName());
            } else {
                hre.setAttributeValue(HibernateReverseEngineering.SCHEMA_SELECTION, jx, ATTRIBUTE_NAME, null);
            }

            // Add Table filters.
            List<Table> list = new ArrayList<Table> (dbTablesDescriptor.getSelectedTables());         
            for (int i = 0; i < list.size(); i++) {                
                int ix = hre.addTableFilter(true);
                hre.setAttributeValue(HibernateReverseEngineering.TABLE_FILTER, ix, CATALOG_NAME, null);
                hre.setAttributeValue(HibernateReverseEngineering.TABLE_FILTER, ix, ATTRIBUTE_NAME, null);
                hre.setAttributeValue(HibernateReverseEngineering.TABLE_FILTER, ix, MATCH_NAME, list.get(i).getName());
                hre.setAttributeValue(HibernateReverseEngineering.TABLE_FILTER, ix, EXCLUDE_NAME, null);
            }
            hro.addReveng();
            hro.save();            
            return Collections.singleton(hro.getPrimaryFile());
        } catch (Exception e) {
            return Collections.EMPTY_SET;
        }
    }
}
