package org.netbeans.modules.python.project2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import static org.netbeans.api.project.ProjectInformation.PROP_DISPLAY_NAME;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.python.api.PythonException;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.editor.codecoverage.PythonCoverageProvider;
import org.netbeans.modules.python.project2.classpath.ClassPathProviderImpl;
import org.netbeans.modules.python.project2.ui.customizer.PythonCustomizerProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PythonProject2 implements Project {

    /**
     * the only property change fired by the class, means that the setup.py file
     * has changed.
     */
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N

    public static final String TEMPORARY_SETUPPY = "tmp_ENCODING_APPENDED_AT_BEGINNING_FOR_PYTHON_2_setup.py"; //NOI18N

    private static final String NS_PYTHON_1 = "http://nbpython.dev.java.net/ns/php-project/1"; // NOI18N
    private static final String EL_PYTHON = "python-data"; // NOI18N
    private static final ImageIcon PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/python/project/resources/py_25_16.png", false);

    private static final String MAIN_MODULE = "main.file"; //NOI18N
    private static final String APPLICATION_ARGS = "application.args";   //NOI18N
    private static final String ACTIVE_PLATFORM = "platform.active"; //NOI18N
    private static final String PROP_VERSION = "version";
    public static final String SOURCES_TYPE_PYTHON = "python"; //NOI18N
    static final String SETUPPY = "setup.py"; //NOI18N

    private final FileObject projectDirectory;
    protected Lookup lkp;
    protected AuxiliaryConfiguration aux;
    protected LogicalViewProvider logicalView;
    private final Info info;
    private final PropertyChangeSupport support;
    private final PythonSources sources;
    private static final Set<String> REGISTRED_SETUPPY = new HashSet<>();

    public PythonProject2(FileObject projectDirectory, ProjectState state) throws PythonException {
        support = new PropertyChangeSupport(this);
        this.logicalView = new Python2LogicalView(this);
        this.projectDirectory = projectDirectory;
        info = new PythonProject2.Info();
        aux = new PythonAuxilaryConfig(projectDirectory, true);
        sources = new PythonSources(this);
        this.lkp = createLookup(state);
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    private Lookup createLookup(ProjectState state) {
        return Lookups.fixed(new Object[]{
            this, //project spec requires a project be in it's own lookup
            aux, //Auxiliary configuartion to store bookmarks and so on
            new PythonActionProvider(this), //Provides Standard like build and cleen
            info, // Project information Implementation
            logicalView, // Logical view if project implementation
            new PythonProject2.PythonOpenedHook(), //Called by project framework when project is opened (closed)
            sources, //Python source grops - used by package view, factories, refactoring, ...
            new ClassPathProviderImpl(this, sources),
//            new PythonProjectOperations(this), //move, rename, copy of project
//            new PythonProject.RecommendedTemplatesImpl(this.updateHelper), // Recommended Templates
            new PythonCustomizerProvider(this), //Project custmoizer
//            new PythonProjectFileEncodingQuery(getEvaluator()), //Provides encoding of the project - used by editor, runtime
//            new PythonSharabilityQuery(helper, getEvaluator(), getSourceRoots(), getTestRoots()), //Sharabilit info - used by VCS
//            helper.createCacheDirectoryProvider(), //Cache provider
//            helper.createAuxiliaryProperties(), // AuxiliaryConfiguraion provider - used by bookmarks, project Preferences, etc
//            new PythonPlatformProvider(getEvaluator()),
            new PythonCoverageProvider(this),
            new PythonProjectSourceLevelQuery(this),
            new RecommendedTemplatesImpl(),
            state
        });
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    public PythonPlatform getActivePlatform() {
        String pid = getProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.ACTIVE_PLATFORM);
        final PythonPlatformManager manager = PythonPlatformManager.getInstance();

        PythonPlatform activePlatform = manager.getPlatform(pid);
        if (activePlatform == null) {
            activePlatform = manager.getPlatform(manager.getDefaultPlatform());
        }

        return activePlatform;
    }

    public void setActivePlatform(final PythonPlatform platform) {
        storeProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.ACTIVE_PLATFORM, platform.getId());
    }

    public String getApplicationArgs() {
        return getProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.APPLICATION_ARGS);
    }

    public void setApplicationArgs(final String args) {
        storeProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.APPLICATION_ARGS, args);
    }

    public String getMainModule() {
        return getProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.MAIN_MODULE);
    }

    public void setMainModule(final String main) {
        storeProp(ProjectUtils.getAuxiliaryConfiguration(this), PythonProject2.MAIN_MODULE, main);
    }

    private String getProp(final AuxiliaryConfiguration auxiliaryConfiguration, final String key) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            @Override
            public String run() {
                Element data = auxiliaryConfiguration.getConfigurationFragment(PythonProject2.EL_PYTHON, PythonProject2.NS_PYTHON_1, true);
                if (data == null) {
                    return null;
                }
                NodeList nl = data.getElementsByTagNameNS(PythonProject2.NS_PYTHON_1, key);
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return null; // NOI18N
            }
        });
    }

    private void storeProp(final AuxiliaryConfiguration auxiliaryConfiguration, final String key, final String main) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                Element data = auxiliaryConfiguration.getConfigurationFragment(PythonProject2.EL_PYTHON, PythonProject2.NS_PYTHON_1, true);
                if (data == null) {
                    data = XMLUtil.createDocument(PythonProject2.EL_PYTHON, PythonProject2.NS_PYTHON_1, null, null).getDocumentElement();
                    auxiliaryConfiguration.putConfigurationFragment(data, false);
                }
                NodeList nl = data.getElementsByTagNameNS(PythonProject2.NS_PYTHON_1, key);
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(PythonProject2.NS_PYTHON_1, key);
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(main));
                auxiliaryConfiguration.putConfigurationFragment(data, true);
                return null;
            }
        });
    }

    private static Properties findProjectProperties(FileObject projectDirectory, FileChangeListener listener) throws PythonException {
        Properties props = new Properties();
        PythonExecution pye;
        try {
            FileObject setuppy = projectDirectory.getFileObject(SETUPPY);
            if (listener != null && !REGISTRED_SETUPPY.contains(setuppy.getPath())) {
                REGISTRED_SETUPPY.add(setuppy.getPath());
                setuppy.addFileChangeListener(listener);
            }
            pye = createProjectPropertiesReader(projectDirectory, setuppy);
            Future<Integer> result = pye.run();
            Integer value = result.get();
            if (value == 0) {
                fillPropertiesFromSetupOutput(props, pye.getOutput());
            } else {
                findProjectPropertiesForceUtf8InSetuppy(props, projectDirectory, setuppy);
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return props;
    }

    private static PythonExecution createProjectPropertiesReader(FileObject projectDirectory, FileObject setuppy) {
        final PythonPlatformManager manager = PythonPlatformManager.getInstance();
        PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
        PythonExecution pye = new PythonExecution();
        pye.setCommand(platform.getInterpreterCommand());
        pye.setDisplayName("Python Project Info");

        pye.setScript(FileUtil.toFile(setuppy).getAbsolutePath());
        pye.setScriptArgs("--name --version"); //NOI18N
        pye.setShowControls(false);
        pye.setShowInput(false);
        pye.setShowWindow(false);
        pye.setShowProgress(false);
        pye.setShowSuspended(false);
        pye.setWorkingDirectory(FileUtil.toFile(projectDirectory).getAbsolutePath());
        pye.attachOutputProcessor();

        return pye;
    }

    private static void fillPropertiesFromSetupOutput(Properties props, Reader output) throws IOException {
        try (Scanner sc = new Scanner(new ReaderInputStream(output))) {
            String newName = sc.nextLine();
            props.setProperty(PROP_DISPLAY_NAME, newName);
            String newVersion = sc.nextLine();
            props.setProperty(PROP_VERSION, newVersion);
        }
    }

    private static void findProjectPropertiesForceUtf8InSetuppy(Properties props, FileObject projectDirectory, FileObject setuppy)
            throws IOException, PythonException, InterruptedException, ExecutionException {
        // We trying to force UTF-8 encoding in setup.py to help Jython read the properties.
        List<String> lines = Files.readAllLines(Paths.get(setuppy.getPath()));
        lines.add(0, "# -*- encoding: utf-8 -*-");

        Path tmpSetupPath = Paths.get(projectDirectory.getFileObject(SETUPPY).getParent().getPath(), TEMPORARY_SETUPPY);
        Files.write(tmpSetupPath, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileObject tmpSetup = projectDirectory.getFileObject(TEMPORARY_SETUPPY);

        PythonExecution pye = createProjectPropertiesReader(projectDirectory, tmpSetup);
        Future<Integer> result = pye.run();
        Integer value = result.get();
        tmpSetup.delete();
        if (value == 0) {
            fillPropertiesFromSetupOutput(props, pye.getOutput());
        } else {
            throw new PythonException("Could not discover Python Project Info in " + pye.getWorkingDirectory());
        }
    }

    public static boolean isProject(FileObject projectDirectory) {
        try {
            return projectDirectory.getFileObject(PythonProject2.SETUPPY) != null &&
                    findProjectProperties(projectDirectory, null) != null;
        } catch (PythonException ex) {
            return false;
        }
    }

    private final class Info implements ProjectInformation, FileChangeListener {

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
        private final Properties properties;

        public Info() throws PythonException {
            properties = findProjectProperties(projectDirectory, this);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        public Properties getProperties() {
            return properties;
        }

        @Override
        public String getDisplayName() {
            return properties.getProperty(PROP_DISPLAY_NAME);
        }

        @Override
        public Icon getIcon() {
            return PROJECT_ICON;
        }

        @Override
        public String getName() {
            return projectDirectory.getName();
//            return name;
        }

        public String getVersion() {
            return properties.getProperty(PROP_VERSION);
//            return version;
        }

        @Override
        public Project getProject() {
            return PythonProject2.this;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

                @Override
                public Void run() {
                    try {
                        Properties newProps = findProjectProperties(projectDirectory, Info.this);
                        for (String propName : newProps.stringPropertyNames()) {
                            String value = newProps.getProperty(propName);
                            Object oldValue = properties.setProperty(propName, value);
                            if (!value.equals(oldValue)) {
                                propertyChangeSupport.firePropertyChange(propName, oldValue, value);
                            }
                        }
                    } catch (PythonException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return null;
                }
            });
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            //TODO: What when it is deleted?
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            //TODO: What when it is renamed?
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

    public final class PythonOpenedHook extends ProjectOpenedHook {

        @Override
        protected void projectOpened() {
            // register project's classpaths to GlobalPathRegistry
//            final ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
//            assert cpProvider != null;
//            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
//            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));

            // Ensure that code coverage is initialized in case it's enabled...
//            PythonCoverageProvider codeCoverage = getLookup().lookup(PythonCoverageProvider.class);
//            if (codeCoverage.isEnabled()) {
//                codeCoverage.notifyProjectOpened();
//            }
        }

        @Override
        protected void projectClosed() {
            // unregister project's classpaths to GlobalPathRegistry
//            final ClassPathProviderImpl cpProvider = getLookup().lookup(ClassPathProviderImpl.class);
//            assert cpProvider != null;
//            //GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
//            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
//            try {
//                ProjectManager.getDefault().saveProject(PythonProject2.this);
//            } catch (IOException e) {
//                Exceptions.printStackTrace(e);
//            }
        }
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        // List of primarily supported templates
        private static final String[] APPLICATION_TYPES = new String[]{
            "python", // NOI18N
            "XML", // NOI18N
            "simple-files" // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/Python/_package", // NOI18N
            "Templates/Python/_module.py", //NOI18N
            "Templates/Python/_main.py", // NOI18N
            "Templates/Python/_empty_module.py", // NOI18N
            "Templates/Python/_test.py", // NOI18N
        };

        @Override
        public String[] getRecommendedTypes() {
            return APPLICATION_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
}
