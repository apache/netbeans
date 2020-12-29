package org.netbeans.modules.python.project2.templates;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

public class NewPythonProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    static final String PROP_MAIN_FILE = "mainFile";      //NOI18N
    static final String PROP_PROJECT_NAME = "projectName";  //NOI18N
    static final String PROP_PROJECT_LOCATION = "pojectLocation";   //NOI18N
    static final String PROP_PLATFORM_ID = "platform";              //NOI18N

    public static enum WizardType {
        APP
    }

    private final WizardType wizardType;
    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel[] panels;
    private int index;

    public NewPythonProjectWizardIterator() {
        this(WizardType.APP);
    }

    private NewPythonProjectWizardIterator(WizardType wizardType) {
        this.wizardType = wizardType;
    }

    @TemplateRegistration(folder = "Project/Python", position = 130, displayName = "org.netbeans.modules.python.project2.Bundle#Templates/Project/Python/PythonProject2.xml", iconBase = "org/netbeans/modules/python/project2/resources/py_25_16.png", description = "/org/netbeans/modules/python/project2/templates/EmptyPythonProjectDescription.html")
    public static NewPythonProjectWizardIterator createApplication() {
        return new NewPythonProjectWizardIterator();
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // normally we would do it in uninitialize but we have listener on ide options (=> NPE)
        initDescriptor(wizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
        descriptor = null;
    }

    @Override
    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        final Set<FileObject> resultSet = new HashSet<>();

        handle.start(5);

        String msg = NbBundle.getMessage(
                NewPythonProjectWizardIterator.class, "LBL_NewPythonProjectWizardIterator_WizardProgress_CreatingProject");
        handle.progress(msg, 3);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {

                @Override
                public Void run() throws MutexException {
                    try {
                        // project
                        File projectDirectory = (File) descriptor.getProperty(PROP_PROJECT_LOCATION);
                        ProjectChooser.setProjectsFolder(projectDirectory.getParentFile());

                        final FileObject projectFO = FileUtil.createFolder(projectDirectory);
                        String projectName = (String) descriptor.getProperty(PROP_PROJECT_NAME);

                        Map<String, Object> params = new HashMap<>(2);
                        /* org/netbeans/modules/python/editor/templates/setup.py
                         * __author__="${user}"
                         * __date__ ="$${date} ${time}$"
                         *
                         * from setuptools import setup,find_packages
                         *
                         * setup (
                         *   name = '${project_name}',
                         *   version = '0.1',
                         *   packages = find_packages(),
                         *
                         *   # Declare your packages' dependencies here, for eg:
                         *   install_requires=[],
                         *
                         *   # Fill in these to make your Egg ready for upload to
                         *   # PyPI
                         *   author = '${user}',
                         *   author_email = '',
                         *
                         *   summary = 'Just another Python package for the cheese shop',
                         *   url = '',
                         *   license = '',
                         *   long_description= 'Long description of the package',
                         *
                         *   # could also include long_description, download_url, classifiers, etc.
                         * )
                         */
                        params.put("project_name", projectName); // NOI18N
                        params.put("python3style", Boolean.TRUE);
                        DataObject setuppy = createFromTemplate(FileUtil.getConfigFile("Templates/Python/_setup.py"), projectFO, "setup.py", params); // NOI18N
                        // Do we want to open the setup.py?
//        resultSet.add(setuppy.getPrimaryFile());
                        resultSet.add(projectFO);

                        if (wizardType == WizardType.APP) {
                            // package
                            final String packageName = projectFO.getName().toLowerCase();
                            FileObject packageFO = projectFO.createFolder(packageName);
                            createFromTemplate(FileUtil.getConfigFile("Templates/Python/_init.py"), packageFO, "__init__.py", null); //NOI18N
                            ProjectManager.getDefault().clearNonProjectCache();
                            Project project = ProjectManager.getDefault().findProject(projectFO);
                            // TODO: Change with provider in lookup cast is known to break
                            PythonProject2 pyProj = (PythonProject2) project;
                            final String platformId = (String) descriptor.getProperty(NewPythonProjectWizardIterator.PROP_PLATFORM_ID);
                            PythonPlatform platform = PythonPlatformManager.getInstance().getPlatform(platformId);
                            if(platform != null) {
                                pyProj.setActivePlatform(platform);
                            }
                            // main file
                            final String mainName = (String) descriptor.getProperty(NewPythonProjectWizardIterator.PROP_MAIN_FILE);
                            if (mainName != null) {
                                final FileObject mainFile = createFromTemplate(FileUtil.getConfigFile("Templates/Python/_module.py"), //NOI18N
                                        packageFO, mainName, params).getPrimaryFile();
                                resultSet.add(mainFile);
                                pyProj.setMainModule(FileUtil.getRelativePath(projectFO, mainFile));
                            }
                            ProjectManager.getDefault().saveProject(project);
                        }
                    } catch (IOException ex) {
                        throw new MutexException(ex);
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }

        msg = NbBundle.getMessage(NewPythonProjectWizardIterator.class, "LBL_NewPythonProjectWizardIterator_WizardProgress_PreparingToOpen");
        handle.progress(msg, 5);
        return resultSet;
    }

    @Override
    public String name() {
        return NbBundle.getMessage(NewPythonProjectWizardIterator.class, "LBL_IteratorName", index + 1, panels.length);
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
        // wizard title
        String title = NbBundle.getMessage(NewPythonProjectWizardIterator.class, wizardType == WizardType.APP ? "TXT_PythonProject" : "TXT_ExistingPythonProject");
        descriptor.putProperty(
                "NewProjectWizard_Title", title); // NOI18N
        return panels[index];
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    static String getFreeFolderName(final File owner, final String proposal) {
        assert owner != null;
        assert proposal != null;
        String freeName = proposal;
        File f = new File(owner, freeName);
        int counter = 1;
        while (f.exists()) {
            counter++;
            freeName = proposal + counter;
            f = new File(owner, freeName);
        }
        return freeName;

    }

    private WizardDescriptor.Panel[] createPanels() {
        switch (wizardType) {
            case APP: {
                String[] steps = new String[]{
                    NbBundle.getMessage(NewPythonProjectWizardIterator.class, "LBL_ProjectNameLocation"),};

                PanelConfigureProject configureProjectPanel = new PanelConfigureProject(wizardType, steps);
                return new WizardDescriptor.Panel[]{
                    configureProjectPanel,};
            }
            default:
                throw new IllegalStateException(wizardType.toString());
        }
    }

    // prevent incorrect default values (empty project => back => existing project)
    private void initDescriptor(WizardDescriptor settings) {
        settings.putProperty(PROP_PROJECT_NAME, null);
        settings.putProperty(PROP_PROJECT_LOCATION, null);
    }

    private DataObject createFromTemplate(FileObject template, FileObject sourceDir, String name, Map<String, ? extends Object> parameters) throws IOException {
        DataFolder dataFolder = DataFolder.findFolder(sourceDir);
        DataObject dataTemplate = DataObject.find(template);
        //Strip extension when needed
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1 && "py".equalsIgnoreCase(name.substring(dot + 1))) { //NOI18N
            name = name.substring(0, dot);
        }
        if (parameters != null) {
            return dataTemplate.createFromTemplate(dataFolder, name, parameters);
        } else {
            return dataTemplate.createFromTemplate(dataFolder, name);
        }
    }
}
