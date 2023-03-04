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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.spi.ClientProjectExtender;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation.ProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class ClientSideProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectWizardIterator.class.getName());

    @StaticResource
    private static final String NEW_HTML5_PROJECT_ICON = "org/netbeans/modules/web/clientproject/ui/resources/new-html5-project.png"; // NOI18N
    @StaticResource
    private static final String NEW_JS_LIBRARY_ICON = "org/netbeans/modules/web/clientproject/ui/resources/new-js-library.png"; // NOI18N

    private final Wizard wizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor.Panel<WizardDescriptor>[] extenderPanels;
    private WizardDescriptor.Panel<WizardDescriptor>[] initPanels;
    private Collection<? extends ClientProjectExtender> extenders;
    private WizardDescriptor wizardDescriptor;
    private boolean withExtenders;


    private ClientSideProjectWizardIterator(Wizard wizard) {
        this(wizard, false);
    }

    private ClientSideProjectWizardIterator(Wizard wizard, boolean withExtenders) {
        assert wizard != null;
        this.wizard = wizard;
        this.withExtenders = withExtenders;
    }

    @TemplateRegistration(folder="Project/ClientSide",
            displayName="#ClientSideProjectWizardIterator.newProject.displayName",
            description="../resources/NewClientSideProjectDescription.html",
            iconBase=NEW_HTML5_PROJECT_ICON,
            position=100)
    @NbBundle.Messages("ClientSideProjectWizardIterator.newProject.displayName=HTML5/JS Application")
    public static ClientSideProjectWizardIterator newHtml5Project() {
        return new ClientSideProjectWizardIterator(new NewHtml5ProjectWizard());
    }

    @TemplateRegistration(folder="Project/ClientSide",
            displayName="#ClientSideProjectWizardIterator.newLibrary.displayName",
            description="../resources/NewClientSideLibraryDescription.html",
            iconBase=NEW_JS_LIBRARY_ICON,
            position=200)
    @NbBundle.Messages("ClientSideProjectWizardIterator.newLibrary.displayName=JavaScript Library")
    public static ClientSideProjectWizardIterator newLibraryProject() {
        return new ClientSideProjectWizardIterator(new NewJsLibraryProjectWizard());
    }

    @TemplateRegistration(folder="Project/ClientSide",
            displayName="#ClientSideProjectWizardIterator.existingProject.displayName",
            description="../resources/ExistingClientSideProjectDescription.html",
            iconBase=NEW_HTML5_PROJECT_ICON,
            position=300)
    @NbBundle.Messages("ClientSideProjectWizardIterator.existingProject.displayName=HTML5/JS Application with Existing Sources")
    public static ClientSideProjectWizardIterator existingHtml5Project() {
        return new ClientSideProjectWizardIterator(new ExistingHtml5ProjectWizard());
    }

    public static ClientSideProjectWizardIterator newProjectWithExtender() {
        return new ClientSideProjectWizardIterator(new NewHtml5ProjectWizard(true), true);
    }

    @NbBundle.Messages({
        "ClientSideProjectWizardIterator.progress.creatingProject=Creating project",
        "ClientSideProjectWizardIterator.error.noSources=<html>Source folder cannot be created.<br><br>Use <i>Resolve Project Problems...</i> action to repair the project.",
        "ClientSideProjectWizardIterator.error.noSiteRoot=<html>Site Root folder cannot be created.<br><br>Use <i>Resolve Project Problems...</i> action to repair the project.",
    })
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.ClientSideProjectWizardIterator_progress_creatingProject());
        Set<FileObject> files = new LinkedHashSet<>();
        File projectDirectory = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(Wizard.PROJECT_DIRECTORY));
        String name = (String) wizardDescriptor.getProperty(Wizard.NAME);
        if (!projectDirectory.isDirectory() && !projectDirectory.mkdirs()) {
            throw new IOException("Cannot create project directory"); //NOI18N
        }
        FileObject dir = FileUtil.toFileObject(projectDirectory);
        CommonProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(dir, name);
        // Always open top dir as a project:
        files.add(dir);

        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        Pair<FileObject, FileObject> folders = wizard.instantiate(files, handle, wizardDescriptor, project);
        FileObject sources = folders.first();
        FileObject siteRoot = folders.second();

        if (sources != null) {
            // main file
            FileObject mainFile = sources.getFileObject("main.js"); // NOI18N
            if (mainFile != null) {
                files.add(mainFile);
            }
        } else if (wizard.hasSources()) {
            errorOccured(Bundle.ClientSideProjectWizardIterator_error_noSources());
        }
        if (siteRoot != null) {
            // index file
            FileObject indexFile = siteRoot.getFileObject("index", "html"); // NOI18N
            if (indexFile != null) {
                files.add(indexFile);
            }
        } else if (wizard.hasSiteRoot()) {
            errorOccured(Bundle.ClientSideProjectWizardIterator_error_noSiteRoot());
        }

        File parent = projectDirectory.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();

        wizard.logUsage(wizardDescriptor, dir, sources, siteRoot);

        return files;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        throw new UnsupportedOperationException("never implemented - use progress one"); //NOI18N
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initializeInternal();
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void initializeInternal() {
        assert EventQueue.isDispatchThread();
        index = 0;
        if (withExtenders) {
            extenders = Lookup.getDefault().lookupAll(ClientProjectExtender.class);
        } else {
            extenders = Collections.EMPTY_LIST;
        }
        panels = wizard.createPanels();

        // Make sure list of steps is accurate.
        LinkedList<String> steps = new LinkedList<String>();
        steps.addAll(Arrays.asList(wizard.createSteps()));

        //Compute steps from extenders
        ArrayList<Panel<? extends WizardDescriptor>> extenderPanelsCol = new ArrayList<Panel<? extends WizardDescriptor>>();
        ArrayList<Panel<? extends WizardDescriptor>> initPanelsCol = new ArrayList<Panel<? extends WizardDescriptor>>();
        for (ClientProjectExtender extender: extenders) {
            extender.initialize(wizardDescriptor);
            for (Panel<WizardDescriptor> panel: extender.createWizardPanels()) {
                extenderPanelsCol.add(panel);
                steps.add(panel.getComponent().getName());
            }
            int i =0;
            for (Panel<WizardDescriptor> panel: extender.createInitPanels()) {
                initPanelsCol.add(panel);
                steps.add(i++,panel.getComponent().getName());
            }

        }

        extenderPanels = extenderPanelsCol.toArray(new Panel[0]);
        initPanels = initPanelsCol.toArray(new Panel[0]);

        int i = 0;
        // XXX should be lazy
        //Extenders
        for (; i < initPanels.length; i++) {
            Component c = initPanels[i].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray());
                // name
                jc.setName(steps.get(i));
            }
        }

        // XXX should be lazy
        //Regular panels
        for (; i < panels.length + initPanels.length; i++) {
            Component c = panels[i-initPanels.length].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
                // name
                jc.setName(steps.get(i));
            }
        }

        // XXX should be lazy
        //Extenders
        for (; i < extenderPanels.length + panels.length + initPanels.length; i++) {
            Component c = extenderPanels[i-panels.length-initPanels.length].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i; //NOI18N
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray());
                // name
                jc.setName(steps.get(i));
            }
        }

    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        wizardDescriptor.putProperty(Wizard.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(Wizard.NAME, null);
        wizard.uninitialize(wizardDescriptor);
        panels = null;
        extenders = null;
        extenderPanels = null;
        initPanels = null;
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "ClientSideProjectWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.ClientSideProjectWizardIterator_name(Integer.valueOf(index + 1), Integer.valueOf(panels.length));
    }

    @Override
    public boolean hasNext() {
        return index < panels.length + extenderPanels.length + initPanels.length -1;
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        setTitle();
        if (index < initPanels.length) {
            return initPanels[index];
        }
       if (index < initPanels.length + panels.length) {
            return panels[index - initPanels.length];
       }
       return extenderPanels[index - initPanels.length - panels.length];
    }

    private void setTitle() {
        if (wizardDescriptor != null) {
            // wizard title
            String title = wizard.getTitle();
            assert title != null : "Title expected for wizard type: " + wizard;
            wizardDescriptor.putProperty("NewProjectWizard_Title", title); // NOI18N
        }
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    static void errorOccured(final String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    //~ Inner classes

    public interface Wizard {
        String PROJECT_DIRECTORY = "PROJECT_DIRECTORY"; // NOI18N
        String NAME = "NAME"; // NOI18N

        String getTitle();
        boolean hasSources();
        boolean hasSiteRoot();
        WizardDescriptor.Panel<WizardDescriptor>[] createPanels();
        String[] createSteps();
        /** @return &lt;sources, site root> */
        Pair<FileObject, FileObject> instantiate(Set<FileObject> files, ProgressHandle handle,
                WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException;
        void uninitialize(WizardDescriptor wizardDescriptor);
        void logUsage(WizardDescriptor wizardDescriptor, FileObject projectDir, @NullAllowed FileObject sources, @NullAllowed FileObject siteRoot);
    }

    public static final class NewHtml5ProjectWizard implements Wizard {

        public static final String SITE_TEMPLATE = "SITE_TEMPLATE"; // NOI18N
        public static final String SITE_ROOT = "SITE_ROOT"; // NOI18N

        private static final String HTML_PROJECT_NAME = "HTML5Application"; // NOI18N

        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;

        private boolean withExtenders;


        public NewHtml5ProjectWizard(boolean withExtenders) {
            this.withExtenders = withExtenders;
            baseWizard = CreateProjectUtils.createBaseWizardPanel(HTML_PROJECT_NAME);
            toolsWizard = CreateProjectUtils.createToolsWizardPanel(CreateProjectUtils.Tools.all());
        }

        public NewHtml5ProjectWizard() {
            this(false);
        }

        @Override
        public String getTitle() {
            return Bundle.ClientSideProjectWizardIterator_newProject_displayName();
        }

        @Override
        public boolean hasSources() {
            return false;
        }

        @Override
        public boolean hasSiteRoot() {
            return true;
        }

        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings({"rawtypes", "unchecked"})
            WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                baseWizard.first(),
                new SiteTemplateWizardPanel(),
                toolsWizard.first(),
            };
            return panels;
        }

        @NbBundle.Messages({
            "NewProjectWizard.step.chooseSite=Site Template",
        })
        @Override
        public String[] createSteps() {
            return new String[] {
                baseWizard.second(),
                Bundle.NewProjectWizard_step_chooseSite(),
                toolsWizard.second(),
            };
        }

        @Override
        public Pair<FileObject, FileObject> instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException {
            CommonProjectHelper projectHelper = project.getProjectHelper();
            String customSiteRoot = (String) wizardDescriptor.getProperty(SITE_ROOT);
            // site template
            SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(SITE_TEMPLATE);
            ProjectProperties projectProperties = new ProjectProperties()
                    .setSiteRootFolder(customSiteRoot!=null?customSiteRoot:ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER)
                    .setTestFolder(ClientSideProjectConstants.DEFAULT_TEST_FOLDER);
            if (siteTemplate != null) {
                // configure
                siteTemplate.configure(projectProperties);
                // init project
                initProject(project, projectProperties, wizardDescriptor);
                // any site template selected
                applySiteTemplate(projectHelper.getProjectDirectory(), projectProperties, siteTemplate, handle);
            } else {
                // init standard project
                initProject(project, projectProperties, wizardDescriptor);
            }

            FileObject siteRootDir = project.getSiteRootFolder();
            if (siteRootDir == null) {
                // #221550
                return Pair.of(null, null);
            }

            // index file (#216293)
            File[] htmlFiles = FileUtil.toFile(siteRootDir).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // accept html or xhtml files
                    return pathname.isFile()
                            && pathname.getName().toLowerCase().endsWith("html"); // NOI18N
                }
            });
            if (htmlFiles != null && htmlFiles.length == 0) {
                createIndexFile(siteRootDir);
            }

            // apply extenders
            if (withExtenders) {
                for (ClientProjectExtender extender : Lookup.getDefault().lookupAll(ClientProjectExtender.class)) {
                    // XXX - jsLibsPath??
                    extender.apply(project.getProjectDirectory(), siteRootDir, "js/lib"); // NOI18N
                }
            }

            // tools
            files.addAll(CreateProjectUtils.instantiateTools(project, toolsWizard.first()));

            return Pair.of(null, siteRootDir);
        }

        @Override
        public void logUsage(WizardDescriptor wizardDescriptor, FileObject projectDir, FileObject sources, FileObject siteRoot) {
            SiteTemplateImplementation siteTemplate = (SiteTemplateImplementation) wizardDescriptor.getProperty(SITE_TEMPLATE);
            ClientSideProjectUtilities.logUsageProjectCreate(false, siteTemplate, null, false, null, false);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(SITE_TEMPLATE, null);
            wizardDescriptor.putProperty(SITE_ROOT, null);
        }

        private void initProject(ClientSideProject project, ProjectProperties properties, WizardDescriptor wizardDescriptor) throws IOException {
            ClientSideProjectUtilities.initializeProject(project,
                    properties.getSourceFolder(),
                    properties.getSiteRootFolder(),
                    properties.getTestFolder(),
                    properties.getTestSeleniumFolder());
            // js testing provider
            String jsTestingProvider = properties.getJsTestingProvider();
            if (jsTestingProvider != null) {
                ClientSideProjectUtilities.setJsTestingProvider(project, jsTestingProvider);
            }
            // selenium testing provider
            String seleniumTestingProvider = properties.getSeleniumTestingProvider();
            if (seleniumTestingProvider != null) {
                ClientSideProjectUtilities.setSeleniumTestingProvider(project, seleniumTestingProvider);
            }
        }

        @NbBundle.Messages({
            "# {0} - template name",
            "ClientSideProjectWizardIterator.error.applyingSiteTemplate=Cannot apply template \"{0}\"."
        })
        private void applySiteTemplate(FileObject projectDir, ProjectProperties projectProperties, SiteTemplateImplementation siteTemplate, final ProgressHandle handle) {
            assert !EventQueue.isDispatchThread();
            final String templateName = siteTemplate.getName();
            try {
                siteTemplate.apply(projectDir, projectProperties, handle);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                errorOccured(Bundle.ClientSideProjectWizardIterator_error_applyingSiteTemplate(templateName));
            }
        }

        private void createIndexFile(FileObject siteRoot) throws IOException {
            FileObject indexTemplate = FileUtil.getConfigFile("Templates/Other/html.html"); // NOI18N
            DataFolder dataFolder = DataFolder.findFolder(siteRoot);
            DataObject dataIndex = DataObject.find(indexTemplate);
            dataIndex.createFromTemplate(dataFolder, "index"); // NOI18N
        }

    }

    public static final class ExistingHtml5ProjectWizard implements Wizard {

        public static final String SITE_ROOT = "SITE_ROOT"; // NOI18N
        public static final String SOURCE_ROOT = "SOURCES_ROOT"; // NOI18N
        public static final String TEST_ROOT = "TEST_ROOT"; // NOI18N


        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                new ExistingClientSideProjectPanel(),
            };
            return panels;
        }

        @Override
        public String getTitle() {
            return Bundle.ClientSideProjectWizardIterator_existingProject_displayName();
        }

        @Override
        public boolean hasSources() {
            return false;
        }

        @Override
        public boolean hasSiteRoot() {
            return false;
        }

        @NbBundle.Messages("ExistingProjectWizard.step.createProject=Name and Location")
        @Override
        public String[] createSteps() {
            return new String[] {
                Bundle.ExistingProjectWizard_step_createProject(),
            };
        }

        @org.netbeans.api.annotations.common.SuppressWarnings(value = "NP_LOAD_OF_KNOWN_NULL_VALUE", justification = "No idea what is wrong here")
        @Override
        public Pair<FileObject, FileObject> instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException {
            File projectDir = FileUtil.toFile(project.getProjectDirectory());
            File siteRoot = (File) wizardDescriptor.getProperty(SITE_ROOT);
            File sources = (File) wizardDescriptor.getProperty(SOURCE_ROOT);
            assert siteRoot != null || sources != null : String.valueOf(siteRoot) + " :: " + String.valueOf(sources);
            // #218736
            String testFolder;
            if (projectDir.equals(siteRoot)) {
                testFolder = null;
            } else {
                testFolder = getExistingDir(wizardDescriptor, TEST_ROOT);
                if (testFolder == null) {
                    testFolder = findTestFolder(project.getProjectDirectory());
                }
            }
            ClientSideProjectUtilities.initializeProject(
                    project,
                    sources != null ? sources.getAbsolutePath() : null,
                    siteRoot != null ? siteRoot.getAbsolutePath() : null,
                    testFolder,
                    null);
            return Pair.of(
                    sources != null ? FileUtil.toFileObject(sources) : null,
                    siteRoot != null ? FileUtil.toFileObject(siteRoot) : null);
        }

        @Override
        public void logUsage(WizardDescriptor wizardDescriptor, FileObject projectDir, FileObject sources, FileObject siteRoot) {
            boolean hasSiteRoot = siteRoot != null;
            ClientSideProjectUtilities.logUsageProjectCreate(true, null,
                    hasSiteRoot ? ClientSideProjectUtilities.isParentOrItself(projectDir, siteRoot) : null, !hasSiteRoot, null, false);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            wizardDescriptor.putProperty(SITE_ROOT, null);
            wizardDescriptor.putProperty(SOURCE_ROOT, null);
            wizardDescriptor.putProperty(TEST_ROOT, null);
        }

        @CheckForNull
        private String getExistingDir(WizardDescriptor wizardDescriptor, String property) {
            File dir = (File) wizardDescriptor.getProperty(property);
            if (dir != null) {
                // dir set
                return dir.getAbsolutePath();
            }
            return null;
        }

        @CheckForNull
        private String findTestFolder(FileObject projectDir) {
            for (String name : new String[]{"test", "spec"}) { // NOI18N
                FileObject folder = projectDir.getFileObject(name);
                if (folder != null
                        && folder.isFolder()) {
                    return FileUtil.toFile(folder).getAbsolutePath();
                }
            }
            return null;
        }

    }

    public static final class NewJsLibraryProjectWizard implements Wizard {

        private static final String LIBRARY_PROJECT_NAME = "JsLibrary"; // NOI18N

        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
        private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;


        public NewJsLibraryProjectWizard() {
            baseWizard = CreateProjectUtils.createBaseWizardPanel(LIBRARY_PROJECT_NAME);
            toolsWizard = CreateProjectUtils.createToolsWizardPanel(CreateProjectUtils.Tools.all());
        }

        @Override
        public String getTitle() {
            return Bundle.ClientSideProjectWizardIterator_newLibrary_displayName();
        }

        @Override
        public boolean hasSources() {
            return true;
        }

        @Override
        public boolean hasSiteRoot() {
            return false;
        }

        @Override
        public Panel<WizardDescriptor>[] createPanels() {
            @SuppressWarnings({"rawtypes", "unchecked"})
            WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                baseWizard.first(),
                toolsWizard.first(),
            };
            return panels;
        }

        @NbBundle.Messages({
            "NewProjectWizard.step.createLibrary=Name and Location",
        })
        @Override
        public String[] createSteps() {
            return new String[] {
                baseWizard.second(),
                toolsWizard.second(),
            };
        }

        @Override
        public Pair<FileObject, FileObject> instantiate(Set<FileObject> files, ProgressHandle handle, WizardDescriptor wizardDescriptor, ClientSideProject project) throws IOException {
            ClientSideProjectUtilities.initializeProject(project, ClientSideProjectConstants.DEFAULT_SOURCE_FOLDER, null, null, null);
            FileObject sources = project.getProjectDirectory().getFileObject(ClientSideProjectConstants.DEFAULT_SOURCE_FOLDER);
            FileObject mainFile = createMainFile(sources);
            files.add(mainFile);

            // tools
            files.addAll(CreateProjectUtils.instantiateTools(project, toolsWizard.first()));

            return Pair.of(sources, null);
        }

        @Override
        public void uninitialize(WizardDescriptor wizardDescriptor) {
            // noop
        }

        @Override
        public void logUsage(WizardDescriptor wizardDescriptor, FileObject projectDir, FileObject sources, FileObject siteRoot) {
            ClientSideProjectUtilities.logUsageProjectCreate(false, null, null, true, null, false);
        }

        private FileObject createMainFile(FileObject sources) throws IOException {
            assert sources != null;
            FileObject indexTemplate = FileUtil.getConfigFile("Templates/Other/javascript.js"); // NOI18N
            DataFolder dataFolder = DataFolder.findFolder(sources);
            DataObject dataIndex = DataObject.find(indexTemplate);
            return dataIndex.createFromTemplate(dataFolder, "main").getPrimaryFile(); // NOI18N
        }

    }

}
