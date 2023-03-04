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
package org.netbeans.modules.javafx2.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.javafx2.project.api.JavaFXProjectUtils;
import org.netbeans.modules.javafx2.project.fxml.ConfigureFXMLControllerPanelVisual;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new JavaFX project
 * 
 * @author Petr Hrebejk
 * @author Anton Chechel
 * @author Petr Somol
 */
public class JavaFXProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(JavaFXProjectWizardIterator.class.getName());

    public static enum WizardType {APPLICATION, PRELOADER, FXML, SWING, LIBRARY, EXTISTING}
    
    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    static final String MAIN_CLASS = "mainClass";
    static final String PROP_PRELOADER_NAME = "preloaderName"; // NOI18N
    static final String SHARED_LIBRARIES = "sharedLibraries"; // NOI18N
    static final String FXML_NAME = "fxmlName"; // NOI18N
    
    static final String MANIFEST_FILE = "manifest.mf"; // NOI18N
    //static final String GENERATED_PRELOADER_CLASS_NAME = "SimplePreloader"; // NOI18N
//    static final String GENERATED_FXML_CLASS_NAME = "Sample"; // NOI18N

    private static final long serialVersionUID = 1L;
    
    private WizardType type;

    public JavaFXProjectWizardIterator() {
        this(WizardType.APPLICATION);
    }

    public static JavaFXProjectWizardIterator fxml() {
        return new JavaFXProjectWizardIterator(WizardType.FXML);
    }

    public static JavaFXProjectWizardIterator preloader() {
        return new JavaFXProjectWizardIterator(WizardType.PRELOADER);
    }

    public static JavaFXProjectWizardIterator swing() {
        return new JavaFXProjectWizardIterator(WizardType.SWING);
    }

    public static JavaFXProjectWizardIterator library() {
        return new JavaFXProjectWizardIterator(WizardType.LIBRARY);
    }

    public static JavaFXProjectWizardIterator existing() {
        return new JavaFXProjectWizardIterator(WizardType.EXTISTING);
    }

    private JavaFXProjectWizardIterator(WizardType type) {
        this.type = type;
    }

    private WizardDescriptor.Panel[] createPanels() {
        switch (type) {
            case EXTISTING:
                return new WizardDescriptor.Panel[]{
                            new PanelConfigureProject(type),
                            new PanelSourceFolders.Panel(),
                            new PanelIncludesExcludes(),};
            default:
                return new WizardDescriptor.Panel[]{
                            new PanelConfigureProject(type)
                        };
        }
    }

    private String[] createSteps() {
        switch (type) {
            case EXTISTING:
                return new String[]{
                            NbBundle.getMessage(JavaFXProjectWizardIterator.class, "LAB_ConfigureProject"), // NOI18N
                            NbBundle.getMessage(JavaFXProjectWizardIterator.class, "LAB_ConfigureSourceRoots"), // NOI18N
                            NbBundle.getMessage(JavaFXProjectWizardIterator.class, "LAB_PanelIncludesExcludes"),}; // NOI18N
            default:
                return new String[]{
                            NbBundle.getMessage(JavaFXProjectWizardIterator.class, "LAB_ConfigureProject"),}; // NOI18N
        }
    }

    @Override
    public Set<?> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(5);
        //handle.progress (NbBundle.getMessage (NewJ2SEProjectWizardIterator.class, "LBL_NewJ2SEProjectWizardIterator_WizardProgress_ReadingProperties"));
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        FileObject mainClassFo = null;
        File dirF = (File) wiz.getProperty("projdir"); // NOI18N
        if (dirF == null) {
            warnIssue204880("Wizard property projdir is null."); // NOI18N
            throw new IOException(); // return to wizard
        }
        dirF = FileUtil.normalizeFile(dirF);
        
        String name = (String) wiz.getProperty("name"); // NOI18N
        String mainClass = (String) wiz.getProperty(MAIN_CLASS); // NOI18N
        String fxmlName = (String) wiz.getProperty(FXML_NAME);
        
        String librariesDefinition = (String) wiz.getProperty(SHARED_LIBRARIES);
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME;
        }
        
        String platformName = (String) wiz.getProperty(JavaFXProjectUtils.PROP_JAVA_PLATFORM_NAME);
        String preloader = (String) wiz.getProperty(JavaFXProjectWizardIterator.PROP_PRELOADER_NAME);
        
        handle.progress(NbBundle.getMessage(JavaFXProjectWizardIterator.class,
                "LBL_NewJ2SEProjectWizardIterator_WizardProgress_CreatingProject"), 1); // NOI18N
        AntProjectHelper projectHelper;
        switch (type) {
            case EXTISTING:
                File[] sourceFolders = (File[]) wiz.getProperty("sourceRoot"); // NOI18N
                File[] testFolders = (File[]) wiz.getProperty("testRoot"); // NOI18N
                String buildScriptName = (String) wiz.getProperty("buildScriptName"); // NOI18N
                projectHelper = JFXProjectGenerator.createProject(dirF, name, sourceFolders, testFolders,
                        MANIFEST_FILE, librariesDefinition, buildScriptName, platformName,
                        preloader, WizardType.EXTISTING);
                
                EditableProperties ep = projectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String includes = (String) wiz.getProperty(ProjectProperties.INCLUDES);
                if (includes == null) {
                    includes = "**"; // NOI18N
                }
                ep.setProperty(ProjectProperties.INCLUDES, includes);
                String excludes = (String) wiz.getProperty(ProjectProperties.EXCLUDES);
                if (excludes == null) {
                    excludes = ""; // NOI18N
                }
                ep.setProperty(ProjectProperties.EXCLUDES, excludes);
                boolean fxInSwing = (Boolean) wiz.getProperty(JFXProjectProperties.JAVAFX_SWING);
                if (fxInSwing) {
                    ep.put(JFXProjectProperties.JAVAFX_SWING, String.valueOf(true));
                }
                projectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                
                handle.progress(2);
                for (File f : sourceFolders) {
                    FileObject srcFo = FileUtil.toFileObject(f);
                    if (srcFo != null) {
                        resultSet.add(srcFo);
                    }
                }
                break;
            default:
                String manifest = null;
                if (type == WizardType.APPLICATION || type == WizardType.FXML || type == WizardType.SWING) {
                    manifest = MANIFEST_FILE;
                }
                if (type == WizardType.PRELOADER) {
                    projectHelper = JFXProjectGenerator.createPreloaderProject(dirF, name, librariesDefinition, platformName, mainClass);
                } else {
                    projectHelper = JFXProjectGenerator.createProject(dirF, name, mainClass, fxmlName, manifest, librariesDefinition,
                            platformName, preloader, type);
                }
                handle.progress(2);
                FileObject sourcesRoot = projectHelper.getProjectDirectory().getFileObject("src"); // NOI18N
                if (mainClass != null && mainClass.length() > 0) {
                    try {
                        //String sourceRoot = "src"; //(String)j2seProperties.get (J2SEProjectProperties.SRC_DIR); // NOI18N
                        mainClassFo = getClassFO(sourcesRoot, mainClass);
                        assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass; // NOI18N
                        // Returning FileObject of main class, will be called its preferred action
                        //resultSet.add(mainClassFo); postponed because of creation order
                    } catch (Exception x) {
                        ErrorManager.getDefault().notify(x);
                    }
                }
        
                // create additional files
                if (type == WizardType.FXML) {
                    String pName = ""; // NOI18N
                    if (mainClass != null && mainClass.length() > 0) {
                        int lastDotIdx = mainClass.lastIndexOf('.'); // NOI18N
                        if (lastDotIdx != -1) {
                            pName = mainClass.substring(0, lastDotIdx).trim();
                            pName = pName.replace('.', '/'); // NOI18N
                            pName += '/'; // NOI18N
                        }
                    }
                    
                    FileObject controller = sourcesRoot.getFileObject(pName + fxmlName + NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "TXT_FileNameControllerPostfix") + ".java"); // NOI18N
                    if (controller != null) {
                        resultSet.add(controller);
                    }
                    FileObject fxml = sourcesRoot.getFileObject(pName + fxmlName + ".fxml"); // NOI18N
                    if (fxml != null) {
                        resultSet.add(fxml);
                    }
                }
        }

        FileObject dir = FileUtil.toFileObject(dirF);
        switch (type) {
            case APPLICATION:
            case FXML:
            case SWING:
                createManifest(dir, false);
                break;
            case EXTISTING:
                createManifest(dir, true);
                break;
        }
        handle.progress(3);

        resultSet.add(dir);
        
        // create preloader project
        handle.progress(NbBundle.getMessage(JavaFXProjectWizardIterator.class,
                "LBL_NewJ2SEProjectWizardIterator_WizardProgress_Preloader"), 4); // NOI18N
        if (preloader != null && preloader.length() > 0) {
            preloader = preloader.trim();
            File preloaderDir = new File(dirF.getParentFile().getAbsolutePath() + File.separatorChar + preloader);
            FileUtil.normalizeFile(preloaderDir);
            
            String preloaderClassName = JavaFXProjectWizardIterator.generatePreloaderClassName(preloader);
            AntProjectHelper preloaderProjectHelper = JFXProjectGenerator.createPreloaderProject(preloaderDir,
                    preloader, librariesDefinition, platformName, preloaderClassName);
            FileObject sourcesRoot = preloaderProjectHelper.getProjectDirectory().getFileObject("src"); // NOI18N
            FileObject preloaderClassFo = getClassFO(sourcesRoot, generatePreloaderClassName(preloader));
            resultSet.add(preloaderClassFo);
            
            FileObject preloaderDirFO = FileUtil.toFileObject(preloaderDir);
            resultSet.add(preloaderDirFO);
            
            // dependency to preloader project
            final Project[] p = new Project[] {ProjectManager.getDefault().findProject(preloaderDirFO)};
            FileObject ownerSourcesRoot = projectHelper.getProjectDirectory().getFileObject("src"); // NOI18N
            ProjectClassPathModifier.addProjects(p, ownerSourcesRoot, ClassPath.COMPILE);
        }
        
        // Returning FileObject of project directory. 
        // Project will be opened
        int ind = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        switch (type) {
            case APPLICATION:
            case FXML:
                WizardSettings.setNewApplicationCount(ind);
                break;
            case PRELOADER:
                WizardSettings.setNewPreloaderCount(ind);
                break;
            case SWING:
                WizardSettings.setNewFxSwingCount(ind);
                break;
            case LIBRARY:
                WizardSettings.setNewLibraryCount(ind);
                break;
            case EXTISTING:
                WizardSettings.setNewProjectCount(ind);
                break;
        }
        
        handle.progress(NbBundle.getMessage(JavaFXProjectWizardIterator.class,
                "LBL_NewJ2SEProjectWizardIterator_WizardProgress_PreparingToOpen"), 5); // NOI18N
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder(dirF);
        }

        SharableLibrariesUtils.setLastProjectSharable(librariesDefinition != null);
        if(mainClassFo != null) {
            resultSet.add(mainClassFo);
        }
        return resultSet;
    }
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]); // NOI18N
        this.wiz.putProperty("testRoot", new File[0]); // NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir", null); // NOI18N
            this.wiz.putProperty("name", null); // NOI18N
            this.wiz.putProperty(MAIN_CLASS, null); // NOI18N
            switch (type) {
                case EXTISTING:
                    this.wiz.putProperty("sourceRoot", null); // NOI18N
                    this.wiz.putProperty("testRoot", null); // NOI18N
            }
            this.wiz = null;
            panels = null;
        }
    }

    @Override
    public String name() {
        return NbBundle.getMessage(JavaFXProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length); // NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    // helper methods, finds mainclass's FileObject
    private static FileObject getClassFO(FileObject sourcesRoot, String className) {
        className = className.replace('.', '/'); // NOI18N
        return sourcesRoot.getFileObject(className + ".java"); // NOI18N
    }

    static String getPackageName(String displayName) {
        StringBuilder builder = new StringBuilder();
        boolean firstLetter = true;
        for (int i = 0; i < displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart(c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
            }
        }
        return builder.length() == 0 ? NbBundle.getMessage(JavaFXProjectWizardIterator.class, "TXT_DefaultPackageName") : builder.toString(); // NOI18N
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @throws IOException in case of problems
     */
    static void createManifest(final FileObject dir, final boolean skeepIfExists) throws IOException {
        if (!skeepIfExists || dir.getFileObject(MANIFEST_FILE) == null) {
            FileObject manifest = dir.createData(MANIFEST_FILE);
            FileLock lock = manifest.lock();
            try {
                OutputStream os = manifest.getOutputStream(lock);
                try {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println("Manifest-Version: 1.0"); // NOI18N
                    pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                    pw.println(); // safest to end in \n\n due to JRE parsing bug
                    pw.flush();
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }

    static boolean isIllegalProjectName(final String name) {
        return name.length() == 0   || 
            name.indexOf('/')  >= 0 ||        //NOI18N
            name.indexOf('\\') >= 0 ||        //NOI18N
            name.indexOf(':')  >= 0 ||        //NOI18N
            name.indexOf("\"") >= 0 ||        //NOI18N
            name.indexOf('<')  >= 0 ||        //NOI18N
            name.indexOf('>')  >= 0;          //NOI18N
    }

    static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }

    // TODO there should be additional wizard page for preloader,
    // class name will be taken from UI
    static String generatePreloaderClassName(String preloaderProjectName) {
        StringBuilder sb = new StringBuilder();
        if(preloaderProjectName.matches("\\b\\d.*\\b")) { //NOI18N
            sb.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_PackageNamePrefix")); //NOI18N
            sb.append(preloaderProjectName.toLowerCase().replace('-', '.')); // NOI18N
            sb.append('.'); // NOI18N
            //sb.append(JavaFXProjectWizardIterator.GENERATED_PRELOADER_CLASS_NAME);
            sb.append(NbBundle.getMessage(PanelOptionsVisual.class, "TXT_ClassNamePrefix")); //NOI18N
            sb.append(preloaderProjectName.replace('-','_').replace('.','_')); // NOI18N
        } else {
            sb.append(preloaderProjectName.toLowerCase().replace('-', '.')); // NOI18N
            sb.append('.'); // NOI18N
            //sb.append(JavaFXProjectWizardIterator.GENERATED_PRELOADER_CLASS_NAME);
            sb.append(preloaderProjectName.replace('-','_').replace('.','_')); // NOI18N
        }
        return  sb.toString();
    }

    private void warnIssue204880(final String msg) {
        LOG.log(Level.SEVERE, msg + " (issue 204880)."); // NOI18N
        Exception npe = new NullPointerException(msg + " (issue 204880)."); // NOI18N
        npe.printStackTrace();
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(JavaFXProjectWizardIterator.class,"WARN_Issue204880"), NotifyDescriptor.ERROR_MESSAGE); // NOI18N
        DialogDisplayer.getDefault().notify(d);
    }
}
