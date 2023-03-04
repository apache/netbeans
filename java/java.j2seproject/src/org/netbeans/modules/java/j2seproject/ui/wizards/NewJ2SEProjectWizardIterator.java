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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard to create a new J2SE project.
 */
public class NewJ2SEProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    enum WizardType {APP, LIB, EXT}

    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N
    static final String PROP_BUILD_SCRIPT_NAME = "buildScriptName"; //NOI18N
    static final String PROP_DIST_FOLDER = "distFolder";    //NOI18N

    private static final Logger LOG = Logger.getLogger(NewJ2SEProjectWizardIterator.class.getName());
    private static final String MANIFEST_FILE = "manifest.mf"; // NOI18N
    private static final long serialVersionUID = 1L;

    private final WizardType type;

    private NewJ2SEProjectWizardIterator(WizardType type) {
        this.type = type;
    }

    @TemplateRegistration(folder="Project/AntJava", position=100, displayName="#template_app", iconBase="org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", description="../resources/emptyProject.html")
    @Messages("template_app=Java Application")
    public static NewJ2SEProjectWizardIterator app() {
        return new NewJ2SEProjectWizardIterator(WizardType.APP);
    }

    @TemplateRegistration(folder="Project/AntJava", position=200, displayName="#template_library", iconBase="org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", description="../resources/emptyLibrary.html")
    @Messages("template_library=Java Class Library")
    public static NewJ2SEProjectWizardIterator library() {
        return new NewJ2SEProjectWizardIterator(WizardType.LIB);
    }

    @TemplateRegistration(folder="Project/AntJava", position=300, displayName="#template_existing", iconBase="org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", description="../resources/existingProject.html")
    @Messages("template_existing=Java Project with Existing Sources")
    public static NewJ2SEProjectWizardIterator existing() {
        return new NewJ2SEProjectWizardIterator(WizardType.EXT);
    }

    private WizardDescriptor.Panel[] createPanels() {
        switch (type) {
            case EXT:
                return new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(type),
                    new PanelSourceFolders.Panel(),
                    new PanelIncludesExcludes(),
                };
            default:
                return new WizardDescriptor.Panel[] {
                    new PanelConfigureProject(type)
                };
        }
    }

    private String[] createSteps() {
        switch (type) {
            case EXT:
                return new String[] {
                    NbBundle.getMessage(NewJ2SEProjectWizardIterator.class,"LAB_ConfigureProject"),
                    NbBundle.getMessage(NewJ2SEProjectWizardIterator.class,"LAB_ConfigureSourceRoots"),
                    NbBundle.getMessage(NewJ2SEProjectWizardIterator.class,"LAB_PanelIncludesExcludes"),
                };
            default:
                return new String[] {
                    NbBundle.getMessage(NewJ2SEProjectWizardIterator.class,"LAB_ConfigureProject"),
                };
        }
    }


    @Override
    public Set<?> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set<FileObject> instantiate (ProgressHandle handle) throws IOException {
        final WizardDescriptor myWiz = this.wiz;
        if (myWiz == null) {
            LOG.warning("The uninitialize called before instantiate."); //NOI18N
            return Collections.emptySet();
        }
        handle.start (4);
        //handle.progress (NbBundle.getMessage (NewJ2SEProjectWizardIterator.class, "LBL_NewJ2SEProjectWizardIterator_WizardProgress_ReadingProperties"));
        Set<FileObject> resultSet = new HashSet<>();
        File dirF = (File)myWiz.getProperty("projdir");        //NOI18N
        if (dirF == null) {
            throw new NullPointerException ("projdir == null, props:" + myWiz.getProperties());
        }
        dirF = FileUtil.normalizeFile(dirF);
        String name = (String)myWiz.getProperty("name");        //NOI18N
        String mainClass = (String)myWiz.getProperty("mainClass");        //NOI18N
        String librariesDefinition = (String)myWiz.getProperty(PanelOptionsVisual.SHARED_LIBRARIES);
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME;
        }
        handle.progress (NbBundle.getMessage (NewJ2SEProjectWizardIterator.class, "LBL_NewJ2SEProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        switch (type) {
        case EXT:
            File[] sourceFolders = (File[])myWiz.getProperty("sourceRoot");        //NOI18N
            File[] testFolders = (File[])myWiz.getProperty("testRoot");            //NOI18N
            String buildScriptName = (String) myWiz.getProperty(PROP_BUILD_SCRIPT_NAME);
            String distFolder = (String) myWiz.getProperty(PROP_DIST_FOLDER);
            AntProjectHelper h = new J2SEProjectBuilder(dirF, name).
                addSourceRoots(sourceFolders).
                addTestRoots(testFolders).
                skipTests(testFolders.length == 0).
                setManifest(MANIFEST_FILE).
                setLibrariesDefinitionFile(librariesDefinition).
                setBuildXmlName(buildScriptName).
                setDistFolder(distFolder).
                build();
            EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String includes = (String) myWiz.getProperty(ProjectProperties.INCLUDES);
            if (includes == null) {
                includes = "**"; // NOI18N
            }
            ep.setProperty(ProjectProperties.INCLUDES, includes);
            String excludes = (String) myWiz.getProperty(ProjectProperties.EXCLUDES);
            if (excludes == null) {
                excludes = ""; // NOI18N
            }
            ep.setProperty(ProjectProperties.EXCLUDES, excludes);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            handle.progress (2);
            for (File f : sourceFolders) {
                FileObject srcFo = FileUtil.toFileObject(f);
                if (srcFo != null) {
                    resultSet.add (srcFo);
                }
            }
            break;
        default:
            h = J2SEProjectGenerator.createProject(dirF, name, mainClass, type == WizardType.APP ? MANIFEST_FILE : null, librariesDefinition, true);
            handle.progress (2);
            if (mainClass != null && mainClass.length () > 0) {
                final FileObject sourcesRoot = h.getProjectDirectory ().getFileObject ("src");        //NOI18N
                if (sourcesRoot != null) {
                    final FileObject mainClassFo = getMainClassFO (sourcesRoot, mainClass);
                    if (mainClassFo != null) {
                        resultSet.add (mainClassFo);
                    }
                }
            }
            // if ( type == TYPE_LIB ) {
                // resultSet.add( h.getProjectDirectory ().getFileObject ("src") );        //NOI18N
                // resultSet.add( h.getProjectDirectory() ); // Only expand the project directory
            // }
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        switch (type) {
            case APP:
                createManifest(dir, false);
                break;
            case EXT:
                createManifest(dir, true);
                break;
        }
        handle.progress (3);

        // Returning FileObject of project diretory.
        // Project will be open and set as main
        final Integer ind = (Integer) myWiz.getProperty(PROP_NAME_INDEX);
        if (ind != null) {
            switch (type) {
                case APP:
                    WizardSettings.setNewApplicationCount(ind);
                    break;
                case LIB:
                    WizardSettings.setNewLibraryCount(ind);
                    break;
                case EXT:
                    WizardSettings.setNewProjectCount(ind);
                    break;
            }
        }
        resultSet.add (dir);
        handle.progress (NbBundle.getMessage (NewJ2SEProjectWizardIterator.class, "LBL_NewJ2SEProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        SharableLibrariesUtils.setLastProjectSharable(librariesDefinition != null);
        return resultSet;
    }


    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient volatile WizardDescriptor wiz;

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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]);    //NOI18N
        this.wiz.putProperty("testRoot", new File[0]);      //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        this.panels = null;
    }

    @Override
    public String name() {
        return NbBundle.getMessage(NewJ2SEProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length);
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
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}

    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO (FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
        mainClass = mainClass.replace ('.', '/'); // NOI18N

        // ignore unvalid mainClass ???

        return sourcesRoot.getFileObject (mainClass+ ".java"); // NOI18N
    }

    static String getPackageName (String displayName) {
        StringBuilder builder = new StringBuilder ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart (c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
            }
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewJ2SEProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @throws IOException in case of problems
     */
    private static void createManifest(final FileObject dir, final boolean skeepIfExists) throws IOException {
        if (!skeepIfExists || dir.getFileObject(MANIFEST_FILE) == null) {
            FileObject manifest = dir.createData(MANIFEST_FILE);
            FileLock lock = manifest.lock();
            try {
                try (OutputStream os = manifest.getOutputStream(lock)) {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println("Manifest-Version: 1.0"); // NOI18N
                    pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                    pw.println(); // safest to end in \n\n due to JRE parsing bug
                    pw.flush();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }

}
