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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.hibernate.HibernateException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.tool.hbm2x.HibernateMappingExporter;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author gowri
 */
public class HibernateCodeGenWizard implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final String PROP_HELPER = "wizard-helper"; //NOI18N
    private int index;
    private Project project;
    private WizardDescriptor wizardDescriptor;
    private HibernateCodeGenWizardHelper helper;
    private HibernateCodGenNameLocationWizardDescriptor nameLocationDescriptor;
    private HibernateRevengDbTablesWizardDescriptor dbTablesDescriptor;
    private HibernateCodeGenWizardDescriptor codeGenDescriptor;
    private WizardDescriptor.Panel[] panels;
    private final String DEFAULT_REVENG_FILENAME = "hibernate.reveng"; // NOI18N
    private final String CATALOG_NAME = "match-catalog"; // NOI18N
    private final String EXCLUDE_NAME = "exclude"; // NOI18N
    private final String ATTRIBUTE_NAME = "match-schema"; // NOI18N
    private final String MATCH_NAME = "match-name"; // NOI18N
    private final String resourceAttr = "resource"; // NOI18N
    private final String classAttr = "class"; // NOI18N
    private Logger logger = Logger.getLogger(HibernateCodeGenWizard.class.getName());

    public static HibernateCodeGenWizard create() {
        return new HibernateCodeGenWizard();
    }

    /**
     * Initialize panels representing individual wizard's steps and sets various
     * properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);
            // Check for unsupported projects. #142296
            if (hibernateEnv == null) {
                logger.log(Level.INFO, "Unsupported project {0}. Existing config wizard.", project);
                panels = new WizardDescriptor.Panel[]{
                    WizardErrorPanel.getWizardErrorWizardPanel()
                };

            } else {
                panels = new WizardDescriptor.Panel[]{
                    codeGenDescriptor
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

    static HibernateCodeGenWizardHelper getHelper(WizardDescriptor wizardDescriptor) {
        return (HibernateCodeGenWizardHelper) wizardDescriptor.getProperty(PROP_HELPER);
    }

    @Override
    public String name() {
        return NbBundle.getMessage(HibernateCodeGenWizard.class, "Templates/Hibernate/CodeGen"); // NOI18N

    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
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

    @Override
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public final void initialize(WizardDescriptor wiz) {
        wizardDescriptor = wiz;
        project = Templates.getProject(wiz);
        helper = new HibernateCodeGenWizardHelper(project);

        wiz.putProperty(PROP_HELPER, helper);
        String wizardTitle = NbBundle.getMessage(HibernateCodeGenWizard.class, "Templates/Hibernate/CodeGen"); // NOI18N
        nameLocationDescriptor = new HibernateCodGenNameLocationWizardDescriptor(project, wizardTitle);
        dbTablesDescriptor = new HibernateRevengDbTablesWizardDescriptor(project, wizardTitle);
        codeGenDescriptor = new HibernateCodeGenWizardDescriptor(project, wizardTitle);

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

    // Generates POJOs and hibernate mapping files based on a .reveng.xml file
    public void generateClasses(FileObject revengFile, ProgressHandle handle) throws IOException {
        JDBCMetaDataConfiguration cfg = null;
        ReverseEngineeringSettings settings;
        ClassLoader oldClassLoader = null;

        File confFile = FileUtil.toFile(helper.getConfigurationFile());
        File outputDirJava = FileUtil.toFile(helper.getLocation().getRootFolder());
        File outputDirHbm = FileUtil.toFile(HibernateUtil.getFirstSourceGroup(project).getRootFolder());
        try {

            // Setup classloader.
            logger.info("Setting up classloader");
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            List<URL> urls = env.getProjectClassPath(revengFile);
            HibernateCfgDataObject hibernateCfgDataObject = null;
            try {
                hibernateCfgDataObject = (HibernateCfgDataObject) DataObject.find(helper.getConfigurationFile());
            } catch (DataObjectNotFoundException ex) {
            }
            if (hibernateCfgDataObject != null) {
                HibernateConfiguration hCfg = hibernateCfgDataObject.getHibernateConfiguration();
                DatabaseConnection dbConnection = null;
                try {
                    dbConnection = HibernateUtil.getDBConnection(hCfg);
                } catch (DatabaseException ex) {
                }
                if (dbConnection != null) {
                    JDBCDriver jdbcDriver = dbConnection.getJDBCDriver();
                    if (jdbcDriver != null) {
                        urls.addAll(Arrays.asList(jdbcDriver.getURLs()));
                    }
                }
            }
            ClassLoader ccl = env.getProjectClassLoader(
                    urls.toArray(new URL[]{}));
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ccl);

            // Configuring the reverse engineering strategy
            try {

                cfg = new JDBCMetaDataConfiguration();

                DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();
                ReverseEngineeringStrategy revStrategy = defaultStrategy;
                OverrideRepository or = new OverrideRepository();
                Configuration c = cfg.configure(confFile);
                or.addFile(FileUtil.toFile(revengFile));
                revStrategy = or.getReverseEngineeringStrategy(revStrategy);

                settings = new ReverseEngineeringSettings(revStrategy);
                settings.setDefaultPackageName(helper.getPackageName());

                defaultStrategy.setSettings(settings);
                revStrategy.setSettings(settings);

                cfg.setReverseEngineeringStrategy(or.getReverseEngineeringStrategy(revStrategy));

                DataObject confDataObject = DataObject.find(helper.getConfigurationFile());
                HibernateCfgDataObject hco = (HibernateCfgDataObject) confDataObject;
                HibernateConfiguration hibConf = hco.getHibernateConfiguration();
                DatabaseConnection dbconn = HibernateUtil.getDBConnection(hibConf);
                if (dbconn != null) {
                    dbconn.getJDBCConnection();
                }

                cfg.readFromJDBC();
                cfg.buildMappings();
            } catch (HibernateException e) {
                throw e;
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            // Generating POJOs            
            try {
                if (helper.getDomainGen()) {
                    handle.progress(NbBundle.getMessage(HibernateCodeGenWizard.class, "HibernateCodeGenerationPanel_WizardProgress_GenPOJO"), 1); // NOI18N
                    POJOExporter exporter = new POJOExporter(cfg, outputDirJava);
                    exporter.getProperties().setProperty("jdk5", new Boolean(helper.getJavaSyntax()).toString());
                    exporter.getProperties().setProperty("ejb3", new Boolean(helper.getEjbAnnotation()).toString());
                    exporter.start();
                    FileUtil.refreshFor(outputDirJava);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            // Generating Mappings
            try {
                if (helper.getHbmGen()) {
                    handle.progress(NbBundle.getMessage(HibernateCodeGenWizard.class, "HibernateCodeGenerationPanel_WizardProgress_GenMapping"), 2); // NOI18N
                    HibernateMappingExporter exporter = new HibernateMappingExporter(cfg, outputDirHbm);
                    exporter.start();
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    // Update mapping entries in the selected configuration file 
    public void updateConfiguration() {
        try {
            DataObject confDataObject = DataObject.find(helper.getConfigurationFile());
            HibernateCfgDataObject hco = (HibernateCfgDataObject) confDataObject;
            SessionFactory sf = hco.getHibernateConfiguration().getSessionFactory();
            HibernateEnvironment hibernateEnv = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);

            FileObject pkg = SourceGroups.getFolderForPackage(HibernateUtil.getFirstSourceGroup(project), helper.getPackageName(), false);
            boolean useJavaSourceLocation = false;//hack(need a lot of changes in hibernate support), currently hibernate support have mixed support for different source/resources location, and also getSOurce... is used to get source roots/folders etc for resources and not for java sources.
            if (pkg == null && helper.getDomainGen() && helper.getEjbAnnotation() && !helper.getHbmGen()) {//in some cases resource root and java is different
                String relativePkgName = helper.getPackageName().replace('.', '/');
                pkg = helper.getLocation().getRootFolder().getFileObject(relativePkgName);
                useJavaSourceLocation = true;
            }
            if (pkg != null && pkg.isFolder()) {
                // bugfix: 137052
                pkg.getFileSystem().refresh(true);

                Enumeration<? extends FileObject> enumeration = pkg.getChildren(true);

                // Generate cfg.xml with annotated pojos
                if (helper.getDomainGen() && helper.getEjbAnnotation() && !helper.getHbmGen()) {
                    while (enumeration.hasMoreElements()) {
                        FileObject fo = enumeration.nextElement();
                        if (fo.getNameExt() != null && fo.getMIMEType().equals("text/x-java")) { // NOI18N

                            int mappingIndex = sf.addMapping(true);
                            String javaFileName = useJavaSourceLocation ? HibernateUtil.getRelativeSourcePath(fo, helper.getLocation().getRootFolder()) : HibernateUtil.getRelativeSourcePath(fo, hibernateEnv.getSourceLocation());
                            String fileName = javaFileName.replaceAll("/", ".").substring(0, javaFileName.indexOf(".java", 0)); // NOI18N

                            sf.setAttributeValue(SessionFactory.MAPPING, mappingIndex, classAttr, fileName);
                            hco.modelUpdatedFromUI();
                            hco.save();
                        }
                    }
                } else {

                    // Generate cfg.xml with hbm files
                    enumarate:
                    while (enumeration.hasMoreElements()) {
                        FileObject fo = enumeration.nextElement();
                        if (fo.getNameExt() != null && fo.getMIMEType().equals(HibernateMappingDataLoader.REQUIRED_MIME)) {
                            int mappingIndex = sf.addMapping(true);
                            String path = HibernateUtil.getRelativeSourcePath(fo, hibernateEnv.getSourceLocation());
                            //check for duplicates
                            for (int i = 0; i < mappingIndex; i++) {
                                String tmpPath = sf.getAttributeValue(SessionFactory.MAPPING, i, resourceAttr);
                                if (tmpPath == null ? path == null : tmpPath.equals(path)) {
                                    continue enumarate;
                                }
                            }
                            sf.setAttributeValue(SessionFactory.MAPPING, mappingIndex, resourceAttr, path);
                            hco.modelUpdatedFromUI();
                            hco.save();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        try {
            generateClasses(helper.getRevengFile(), handle);
            handle.progress(NbBundle.getMessage(HibernateCodeGenWizard.class, "HibernateCodeGenerationPanel_WizardProgress_UpdateConf"), 3); // NOI18N
            updateConfiguration();
            return Collections.singleton(helper.getRevengFile());
        } catch (Exception e) {
            return Collections.EMPTY_SET;
        }
    }
}
