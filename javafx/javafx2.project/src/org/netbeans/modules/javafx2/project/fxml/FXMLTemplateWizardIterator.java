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
package org.netbeans.modules.javafx2.project.fxml;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.modules.javafx2.project.fxml.SourceGroupSupport.SourceGroupProxy;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Wizard to create a new FXML file and optionally Java Controller and CSS file.
 *
 * @author Anton Chechel
 * @author Petr Somol
 */
// TODO register via annotations instead of layer.xml
public class FXMLTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    static final String PROP_SRC_ROOTS = "srcRootFolder"; // NOI18N
    static final String PROP_ROOT_FOLDER = "rootFolder"; // NOI18N
    static final String PROP_JAVA_CONTROLLER_ENABLED = "javaControllerEnabled"; // NOI18N
    static final String PROP_JAVA_CONTROLLER_NAME_PROPERTY = "javaController"; // NOI18N
    static final String PROP_JAVA_CONTROLLER_EXISTING_PROPERTY = "javaControllerExisting"; // NOI18N
    static final String PROP_CSS_ENABLED = "cssEnabled"; // NOI18N
    static final String PROP_CSS_NAME_PROPERTY = "CSS"; // NOI18N
    static final String PROP_CSS_EXISTING_PROPERTY = "CSSExisting"; // NOI18N

    static final String FXML_FILE_EXTENSION = ".fxml"; // NOI18N
    static final String JAVA_FILE_EXTENSION = ".java"; // NOI18N
    static final String CSS_FILE_EXTENSION = ".css"; // NOI18N

    static final String defaultMavenFXMLPackage = "fxml"; //NOI18N
    static final String defaultMavenImagesPackage = "images"; //NOI18N
    static final String defaultMavenCSSPackage = "styles"; //NOI18N
    
    static final char[] NO_FILENAME_CHARS = { '/', '<', '>', '\\', '|', '\"', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', ':' }; //NOI18N
    
    private WizardDescriptor wizard;
    private SourceGroupSupport supportFXML;
    private SourceGroupSupport supportController;
    private SourceGroupSupport supportCSS;
    private boolean isMavenOrGradle = false;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;

    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> create() {
        return new FXMLTemplateWizardIterator();
    }

    private FXMLTemplateWizardIterator() {
    }

    @Override
    public String name() {
        switch (index) {
            default:
            case 0:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Name"); // NOI18N
            case 1:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Controller_Name"); // NOI18N
            case 2:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_CSS_Name"); // NOI18N
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;

        Project project = Templates.getProject(wizard);
        if (project == null) {
            throw new IllegalStateException(
                    NbBundle.getMessage(FXMLTemplateWizardIterator.class,
                    "MSG_ConfigureFXMLPanel_Project_Null_Error")); // NOI18N
        }
        isMavenOrGradle = JFXProjectUtils.isMavenProject(project) || JFXProjectUtils.isGradleProject(project);

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroupsJava = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] sourceGroupsResources = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (sourceGroupsJava == null || sourceGroupsJava.length == 0) {
            throw new IllegalStateException(
                    NbBundle.getMessage(FXMLTemplateWizardIterator.class,
                    "MSG_ConfigureFXMLPanel_SGs_Error")); // NOI18N
        }
        if(isMavenOrGradle) {
            supportFXML = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            supportCSS = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            if(sourceGroupsResources != null && sourceGroupsResources.length > 0) {
                supportFXML.addSourceGroups(sourceGroupsResources);
                supportCSS.addSourceGroups(sourceGroupsResources);
            } else {
                supportFXML.addSourceGroupProxy(project, NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_ProjectResources"), // NOI18N
                        new String[]{defaultMavenFXMLPackage, defaultMavenImagesPackage, defaultMavenCSSPackage});
                supportCSS.addSourceGroupProxy(project, NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_ProjectResources"), // NOI18N
                        new String[]{defaultMavenFXMLPackage, defaultMavenImagesPackage, defaultMavenCSSPackage});
                FileObject dirFXML = supportFXML.getCurrentPackageFolder(true);
                if (dirFXML == null) {
                    // default Maven resources are overriden in the project's pom.xml (#250097)
                    supportFXML = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    supportFXML.addSourceGroups(sourceGroupsJava); //must exist
                    supportCSS = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    supportCSS.addSourceGroups(sourceGroupsJava); //must exist
                }
            }
        } else {
            supportFXML = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
            supportFXML.addSourceGroups(sourceGroupsJava); //must exist
            supportCSS = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
            supportCSS.addSourceGroups(sourceGroupsJava); //must exist
        }
        supportController = new SourceGroupSupport(JavaProjectConstants.SOURCES_TYPE_JAVA);
        supportController.addSourceGroups(sourceGroupsJava); //must exist
        supportController.setParent(supportFXML);
        supportCSS.setParent(supportFXML);
        
        index = 0;
        panels = createPanels(project, supportFXML, supportController, supportCSS);
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    private WizardDescriptor.Panel[] createPanels(Project project, SourceGroupSupport supportFXML, SourceGroupSupport supportController, SourceGroupSupport supportCSS) {
        return new WizardDescriptor.Panel[]{
                    new ConfigureFXMLPanelVisual.Panel(project, supportFXML, isMavenOrGradle),
                    new ConfigureFXMLControllerPanelVisual.Panel(supportController, isMavenOrGradle),
                    new ConfigureFXMLCSSPanelVisual.Panel(supportCSS, isMavenOrGradle)
                };
    }

    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_FXMLStep0"),
            NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_FXMLStep1"),
            NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_FXMLStep2"),
            NbBundle.getMessage(FXMLTemplateWizardIterator.class,"LAB_FXMLStep3"),
        };
    }
    
    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Set instantiate() throws IOException, IllegalArgumentException {
        Set<FileObject> set = new HashSet<FileObject>(3);
        FileObject dirFXML = supportFXML.getCurrentPackageFolder(true);
        DataFolder dfFXML = DataFolder.findFolder(dirFXML);        
        String targetNameFXML = supportFXML.getCurrentFileName();
        
        DataFolder dfController = null;
        String targetNameController = null;
        Object enabledControllerProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_ENABLED);
        Object controllerNameProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_NAME_PROPERTY);
        Object controllerExistingProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_EXISTING_PROPERTY);
        boolean enabledController = enabledControllerProperty != null ? (Boolean) enabledControllerProperty : false;
        String controllerName = controllerNameProperty != null ? (String) controllerNameProperty : null;
        String controllerExisting = controllerExistingProperty != null ? (String) controllerExistingProperty : null;
        String controllerFullName = null;
        if(controllerExisting != null && !controllerExisting.isEmpty()) {
            File f = new File(controllerExisting);
            if(f.exists()) {
                FileObject fo = FileUtil.toFileObject(f);
                controllerFullName = JFXProjectUtils.getRelativePath(dirFXML, fo);
                if(controllerFullName == null) {
                    controllerFullName = controllerExisting;
                }
            } else {
                controllerFullName = controllerExisting;
            }
        } else {
            if(enabledController) {
                FileObject dirController = supportController.getCurrentPackageFolder(true);
                dfController = DataFolder.findFolder(dirController);
                targetNameController = supportController.getCurrentFileName();
                controllerFullName = getPreselectedPackage(supportController.getCurrentSourceGroup(), dirController) + "." + targetNameController; // NOI18N
            }
        }
        
        DataFolder dfCSS = null;
        Object enabledCSSProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_CSS_ENABLED);
        Object cssNameProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_CSS_NAME_PROPERTY);
        Object cssExistingProperty = wizard.getProperty(FXMLTemplateWizardIterator.PROP_CSS_EXISTING_PROPERTY);
        boolean enabledCSS = enabledCSSProperty != null ? (Boolean) enabledCSSProperty : false;
        String cssName = cssNameProperty != null ? (String) cssNameProperty : null;
        String cssExisting = cssExistingProperty != null ? (String) cssExistingProperty : null;
        String cssFullName = null;
        if(enabledCSS) {
            if(cssExisting != null && !cssExisting.isEmpty()) {
                final String srcPath = FileUtil.normalizeFile(FileUtil.toFile(supportCSS.getCurrentSourceGroupFolder())).getPath();
                File f = new File(srcPath + File.separator + cssExisting);
                if(f.exists()) {
                    FileObject fo = FileUtil.toFileObject(f);
                    cssFullName = fo.getParent().equals(dirFXML) ? fo.getNameExt() :
                            JFXProjectUtils.getRelativePath(supportCSS.getCurrentSourceGroupFolder(), fo);
                    if(cssFullName == null) {
                        cssFullName = cssExisting;
                    }
                } else {
                    cssFullName = cssExisting;
                }
                if((cssFullName.contains("/") || cssFullName.contains("\\")) && 
                        !cssFullName.startsWith("/") && !cssFullName.startsWith("\\")) {
                    cssFullName = "/" + cssFullName;
                }
            } else {
                FileObject dirCSS = supportCSS.getCurrentPackageFolder(true);
                dfCSS = DataFolder.findFolder(dirCSS);
                String targetNameCSS = supportCSS.getCurrentFileName();
                assert targetNameCSS.equals(cssName);
                Object path = getPreselectedPackage(supportCSS.getCurrentSourceGroup(), dirCSS);
                if(path != null && (dfFXML != null && !dfFXML.equals(dfCSS))) {
                    cssFullName =  "/" + ((String) path).replace('.', '/') + "/" + targetNameCSS; // NOI18N
                } else {
                    cssFullName = targetNameCSS;
                }
            }
        }
        
        Map<String, String> params = new HashMap<String, String>();
        if (controllerFullName != null) {
            params.put("controller", controllerFullName); // NOI18N
        }
        if (cssFullName != null) {
            //remove file extension from name
            cssFullName = cssFullName.substring(0, cssFullName.length() - CSS_FILE_EXTENSION.length());
            // normalize path
            cssFullName = cssFullName.replace("\\", "/"); // NOI18N
        
            params.put("css", cssFullName); // NOI18N
        }
        if (cssName != null) {
            //remove file extension from name
            cssName = cssName.substring(0, cssName.length() - CSS_FILE_EXTENSION.length());
        }

        FileObject xmlTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.fxml"); // NOI18N
        DataObject dXMLTemplate = DataObject.find(xmlTemplate);
        DataObject dobj = dXMLTemplate.createFromTemplate(dfFXML, targetNameFXML, params);
        set.add(dobj.getPrimaryFile());

        if (enabledController && dfController != null) {
            assert controllerName.equals(targetNameController);
            FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXMLController.java"); // NOI18N
            DataObject dJavaTemplate = DataObject.find(javaTemplate);
            DataObject dobj2 = dJavaTemplate.createFromTemplate(dfController, controllerName);
            set.add(dobj2.getPrimaryFile());
        }

        if (enabledCSS && dfCSS != null) {
            FileObject cssTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.css"); // NOI18N
            DataObject dCSSTemplate = DataObject.find(cssTemplate);
            DataObject dobj3 = dCSSTemplate.createFromTemplate(dfCSS, cssName);
            set.add(dobj3.getPrimaryFile());
        }

        return set;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
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
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    // Utility methods ---------------------------------------------------------
    /**
     * Get a package combo model item for the package the user selected before
     * opening the wizard. May return null if it cannot find it; or a String
     * instance if there is a well-defined package but it is not listed among
     * the packages shown in the list model.
     */
    static Object getPreselectedPackage(SourceGroupProxy group, FileObject folder) {
        if (folder == null) {
            return null;
        }

        FileObject root = group.getRootFolder();
        String relPath = FileUtil.getRelativePath(root, folder);
        if (relPath == null) {
            // Group Root folder is not a parent of the preselected folder
            // No package should be selected
            return null;
        } else {
            // Find the right item.            
            String name = relPath.replace('/', '.'); // NOI18N
            return name;
        }
    }

    static void setErrorMessage(String key, WizardDescriptor settings) {
        if (key == null) {
            settings.getNotificationLineSupport().clearMessages();
        } else {
            settings.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(FXMLTemplateWizardIterator.class, key));
        }
    }

    static void setInfoMessage(String key, WizardDescriptor settings) {
        if (key == null) {
            settings.getNotificationLineSupport().clearMessages();
        } else {
            settings.getNotificationLineSupport().setInformationMessage(NbBundle.getMessage(FXMLTemplateWizardIterator.class, key));
        }
    }

    public static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') { // NOI18N
            return false;
        }
        StringTokenizer st = new StringTokenizer(str, "."); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.isEmpty()) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidPackage(FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null || path == null) {
            return false;
        }

        final StringTokenizer st = new StringTokenizer(path, "."); // NOI18N
        while (st.hasMoreTokens()) {
            root = root.getFileObject(st.nextToken());
            if (root == null) {
                return true;
            } else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    static String fileExist(String fileName) {
        if (!new File(fileName).exists()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_doesnt_exist", fileName); // NOI18N
        }
        return null;
    }
    
    static boolean validFileName(String name) {
        if(name == null) {
            return false;
        }
        for(int i = 0; i < name.length(); i++) {
            for(int j = 0; j < NO_FILENAME_CHARS.length; j++) {
                if(name.charAt(i) == NO_FILENAME_CHARS[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String canUseFileName(File rootFolder, String fileName) {
        assert rootFolder != null;
        String relFileName = rootFolder.getPath() + File.separatorChar + fileName;

        // test for illegal characters in file name
        if (!validFileName(fileName)) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_invalid_file_name"); // NOI18N
        }
        
        // test whether the file already exists
        if (new File(relFileName).exists()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_already_exist", relFileName); // NOI18N
        }

        // target folder should be writable
        if (!rootFolder.canWrite()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
        }

        // all ok
        return null;
    }

    // helper methods copied and refactored from JavaTargetChooserPanel
    /**
     * Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    static String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
        String newObjectNameToDisplay = newObjectName;
        if (newObjectName != null) {
            newObjectName = newObjectName.replace('.', '/'); // NOI18N
        }
        if (extension != null && extension.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }

        if (extension != null && extension.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectNameToDisplay);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectNameToDisplay = sb.toString();
        }

        String relFileName = folderName + '/' + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target package should be writable
        File targetPackage = folderName != null ? new File(FileUtil.toFile(targetFolder), folderName) : FileUtil.toFile(targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists() && !targetPackage.canWrite()) {
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        File fold = FileUtil.toFile(targetFolder);
        return fold.exists()
                ? new File(fold, relFileName).exists()
                : targetFolder.getFileObject(relFileName) != null;
    }

    // Utility classes ---------------------------------------------------------
    static class SrcFileSystemView extends FileSystemView {

        private static final String newFolderStringWin = UIManager.getString("FileChooser.win32.newFolder"); // NOI18N
        private static final String newFolderNextStringWin = UIManager.getString("FileChooser.win32.newFolder.subsequent"); // NOI18N
        private static final String newFolderString = UIManager.getString("FileChooser.other.newFolder"); // NOI18N
        private static final String newFolderNextString = UIManager.getString("FileChooser.other.newFolder.subsequent"); // NOI18N
        
        // Roots, bloody roooooots! :D
        private File[] roots;

        public SrcFileSystemView(File[] roots) {
            assert roots != null && roots.length > 0;
            this.roots = roots;
        }

        @Override
        public File[] getRoots() {
            return roots;
        }

        @Override
        public boolean isRoot(File file) {
            for (File root : roots) {
                if (root.equals(file)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public File getHomeDirectory() {
            return roots[0];
        }

        @Override
        public File createNewFolder(File containingDir) throws IOException {
            assert containingDir != null;
            boolean win = Utilities.isWindows();
            File newFolder = createFileObject(containingDir, win ? newFolderStringWin : newFolderString);
            int i = 2;
            while (newFolder.exists() && (i < 100)) {
                newFolder = createFileObject(containingDir, MessageFormat.format(
                        win ? newFolderNextStringWin : newFolderNextString, new Object[]{i}));
                i++;
            }

            if (newFolder.exists()) {
                throw new IOException(NbBundle.getMessage(
                        FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Name", // NOI18N
                        newFolder.getAbsolutePath()));
            } else {
                newFolder.mkdirs();
            }

            return newFolder;
        }
    }
    
    static class FXMLTemplateFileFilter extends FileFilter {
        
        private enum Type {FXML, JAVA, CSS}
        
        private Type type;

        private FXMLTemplateFileFilter(Type type) {
            this.type = type;
        }
        
        public static FXMLTemplateFileFilter createFXMLFilter() {
            return new FXMLTemplateFileFilter(Type.FXML);
        }

        public static FXMLTemplateFileFilter createJavaFilter() {
            return new FXMLTemplateFileFilter(Type.JAVA);
        }

        public static FXMLTemplateFileFilter createCSSFilter() {
            return new FXMLTemplateFileFilter(Type.CSS);
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension;
            switch (type) {
                default:
                case FXML: extension = FXML_FILE_EXTENSION;
                    break;
                case JAVA: extension = JAVA_FILE_EXTENSION;
                    break;
                case CSS: extension = CSS_FILE_EXTENSION;
                    break;
            }
            return ("." + FileUtil.getExtension(f.getName())).equals(extension); // NOI18N
        }

        @Override
        public String getDescription() {
            String key;
            switch (type) {
                default:
                case FXML: key = "LBL_ConfigureFXMLPanel_FileChooser_FXML_Description"; // NOI18N
                    break;
                case JAVA: key = "LBL_ConfigureFXMLPanel_FileChooser_Java_Description"; // NOI18N
                    break;
                case CSS: key = "LBL_ConfigureFXMLPanel_FileChooser_CSS_Description"; // NOI18N
                    break;
            }
            return NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, key);
        }
    }

}
