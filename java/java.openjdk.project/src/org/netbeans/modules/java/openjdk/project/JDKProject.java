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
package org.netbeans.modules.java.openjdk.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.event.ChangeListener;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.modules.java.openjdk.project.ConfigurationImpl.ProviderImpl;
import org.netbeans.modules.java.openjdk.project.ModuleDescription.ModuleRepository;
import org.netbeans.modules.java.openjdk.project.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class JDKProject implements Project {

    public static final String PROJECT_KEY = "org-netbeans-modules-java-openjdk-project-JDKProject";
    private final FileObject projectDir;
    private final Lookup lookup;
    private final List<Root> roots;
    private final URI fakeOutput;
            final ModuleRepository moduleRepository;
            final ModuleDescription currentModule;
    private final PropertyEvaluator evaluator;
            final MapPropertyProvider properties;
            final ProviderImpl configurations;

    public JDKProject(FileObject projectDir, @NullAllowed ModuleRepository moduleRepository, @NullAllowed ModuleDescription currentModule) {
        try {
        this.projectDir = projectDir;
        this.moduleRepository = moduleRepository;
        this.currentModule = currentModule;
        
        URI jdkDirURI = projectDir.toURI();

        properties = new MapPropertyProvider();
        
        properties.setProperty("basedir", stripTrailingSlash(jdkDirURI.toString()));
        properties.setProperty("module", projectDir.getNameExt());

        String osKey;
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            osKey = "macosx";
        } else if (osName.contains("windows")) {
            osKey = "windows";
        } else if (osName.contains("solaris")) {
            osKey = "solaris";
        } else {
            osKey = "linux";
        }

        String legacyOsKey;
        String generalizedOsKey;

        switch (osKey) {
            case "macosx": generalizedOsKey = "unix"; legacyOsKey = "macosx"; break;
            case "solaris": generalizedOsKey = "unix"; legacyOsKey = "solaris"; break;
            case "linux": generalizedOsKey = "unix"; legacyOsKey = "solaris"; break;
            case "windows": generalizedOsKey = "no-such-key"; legacyOsKey = "windows"; break;
            default:
                throw new IllegalStateException(osKey);
        }

        properties.setProperty("os", osKey);
        properties.setProperty("generalized-os", generalizedOsKey);
        properties.setProperty("legacy-os", legacyOsKey);
        FileObject jdkRoot = moduleRepository != null ? moduleRepository.getJDKRoot() : BuildUtils.getFileObject(projectDir, "..");
        properties.setProperty("jdkRoot", stripTrailingSlash(jdkRoot.toURI().toString()));
        configurations = ConfigurationImpl.getProvider(jdkRoot);

        configurations.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || evt.getPropertyName().equals(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE)) {
                    updateConfiguration();
                }
            }
        });

        updateConfiguration();
        
        evaluator = PropertyUtils.sequentialPropertyEvaluator(properties);
        
        boolean closed = BuildUtils.getFileObject(projectDir, "src/closed/share/classes/javax/swing/plaf/basic/icons/JavaCup16.png") != null;
        boolean modular = currentModule != null;
        Configuration configuration =  modular ? MODULAR_CONFIGURATION
                                               : closed ? LEGACY_CLOSED_CONFIGURATION : LEGACY_OPEN_CONFIGURATION;
        
        this.roots = new ArrayList<>(configuration.mainSourceRoots.size());
        
        addRoots(RootKind.MAIN_SOURCES, configuration.mainSourceRoots);
        addRoots(RootKind.NATIVE_SOURCES, configuration.nativeSourceRoots);
        addRoots(RootKind.TEST_SOURCES, configuration.testSourceRoots);

        URL fakeOutputURL;
        try {
            URI fakeOutputJar = new URI(evaluator.evaluate("${basedir}/fake-target.jar"));
            fakeOutput = FileUtil.getArchiveRoot(fakeOutputJar.toURL()).toURI();
            fakeOutputURL = fakeOutput.toURL();
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }

        if (currentModule != null) {
            //XXX: hacks for modules that exist in more than one repository - would be better to handle them automatically.
            switch (currentModule.name) {
                case "java.base":
                    addRoots(RootKind.MAIN_SOURCES, Arrays.asList(Pair.<String, String>of("${jdkRoot}/langtools/src/java.base/share/classes/", null)));
                    break;
                case "jdk.compiler":
                    addRoots(RootKind.MAIN_SOURCES, Arrays.asList(Pair.<String, String>of("${jdkRoot}/jdk/src/jdk.compiler/share/classes/", null)));
                    break;
                case "jdk.dev":
                    addRoots(RootKind.MAIN_SOURCES, Arrays.asList(Pair.<String, String>of("${jdkRoot}/jdk/src/jdk.dev/share/classes/", null)));
                    break;
            }

            FileObject shareClasses = BuildUtils.getFileObject(projectDir, "share/classes");

            if (shareClasses != null && Arrays.stream(shareClasses.getChildren()).anyMatch(c -> c.isFolder() && c.getNameExt().contains("."))) {
                List<String> submodules = Arrays.stream(shareClasses.getChildren()).filter(c -> c.isFolder()).map(c -> c.getNameExt()).collect(Collectors.toList());
                List<Root> newRoots = new ArrayList<>();

                for (Root r : roots) {
                    if (r.kind != RootKind.MAIN_SOURCES) {
                        newRoots.add(r);
                        continue;
                    }

                    newRoots.add(new Root(r.relPath, r.displayName, RootKind.MAIN_SOURCES, evaluator, Pattern.compile("module-info.java"), null));

                    for (String submodule : submodules) {
                        newRoots.add(new Root(r.relPath + submodule + "/src/", r.displayName + submodule + "/src/", RootKind.MAIN_SOURCES, evaluator, null));
                    }
                }

                roots.clear();
                roots.addAll(newRoots);
            }
            String testRoots = moduleRepository.moduleTests(currentModule.name);

            if (testRoots != null) {
                addRoots(RootKind.TEST_SOURCES, Arrays.asList(Pair.<String, String>of(testRoots, null)));
            }

        }

        ClassPathProviderImpl cpp = new ClassPathProviderImpl(this, moduleRepository);

        Lookup base = Lookups.fixed(cpp,
                                    new OpenProjectHookImpl(this, cpp, moduleRepository),
                                    new SourcesImpl(this),
                                    new LogicalViewProviderImpl(this),
                                    new SourceLevelQueryImpl(jdkRoot),
                                    new SourceForBinaryQueryImpl(fakeOutputURL, cpp.getSourceCP()),
                                    new ProjectInformationImpl(),
                                    configurations,
                                    new SubProjectProviderImpl(this),
                                    new ActionProviderImpl(this),
                                    new AccessibilityQueryImpl(currentModule),
                                    new CustomizerProviderImpl(this),
                                    new Settings(this),
                                    new BinaryForSourceQueryImpl(this, cpp.getSourceCP()),
                                    this);
        this.lookup = LookupProviderSupport.createCompositeLookup(base, "Projects/" + PROJECT_KEY + "/Lookup");
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private void addRoots(RootKind kind, Iterable<Pair<String, String>> rootSpecifications) {
        for (Pair<String, String> sr : rootSpecifications) {
            roots.add(new Root(sr.first(), sr.first(), kind, evaluator, sr.second() != null ? Pattern.compile(sr.second()) : null));
        }
    }
    
    private static String stripTrailingSlash(String from) {
        if (from.endsWith("/")) return from.substring(0, from.length() - 1);
        else return from;
    }

    private void updateConfiguration() {
        ProjectManager.mutex().readAccess(new Runnable() {
            @Override public void run() {
                ConfigurationImpl activeConfig = configurations.getActiveConfiguration();
                File configurationDir = activeConfig != null ? activeConfig.getLocation() : null;
                properties.setProperty("outputRoot", configurationDir != null ? stripTrailingSlash(configurationDir.toURI().toString())
                                                                              : FileUtil.normalizeFile(new File(URI.create("file:///non-existing"))).toURI().toString());
            }
        });
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public List<Root> getRoots() {
        return roots;
    }

    public URI getFakeOutput() {
        return fakeOutput;
    }

    public PropertyEvaluator evaluator() {
        return evaluator;
    }

    public static final class Root implements PropertyChangeListener {
        public final String relPath;
        public final String displayName;
        public final RootKind kind;
        public final Pattern includes;
        public final Pattern excludes;
        private final PropertyEvaluator evaluator;
        private URL location;
        private final ChangeSupport cs = new ChangeSupport(this);
        private Root(String relPath, String displayName, RootKind kind, PropertyEvaluator evaluator, Pattern excludes) {
            this(relPath, displayName, kind, evaluator, null, excludes);
        }
        private Root(String relPath, String displayName, RootKind kind, PropertyEvaluator evaluator, Pattern includes, Pattern excludes) {
            this.relPath = relPath;
            this.displayName = displayName;
            this.kind = kind;
            this.evaluator = evaluator;
            this.includes = includes;
            this.excludes = excludes;
            this.evaluator.addPropertyChangeListener(this);

        }
        public URL getLocation() {
            if (location == null) {
                try {
                    location = new URL(evaluator.evaluate(relPath)).toURI().normalize().toURL();
                } catch (MalformedURLException | URISyntaxException ex) {
                    Exceptions.printStackTrace(ex); //XXX
                }
            }
            return location;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            location = null;
            cs.fireChange();
        }

        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
    }
    
    public enum RootKind {
        MAIN_SOURCES,
        NATIVE_SOURCES,
        TEST_SOURCES;
    }

    private static final class Configuration {
        public final List<Pair<String, String>> mainSourceRoots;
        private final List<Pair<String, String>> nativeSourceRoots;
        public final List<Pair<String, String>> testSourceRoots;

        public Configuration(List<Pair<String, String>> mainSourceRoots, List<Pair<String, String>> nativeSourceRoots, List<Pair<String, String>> testSourceRoots) {
            this.mainSourceRoots = mainSourceRoots;
            this.nativeSourceRoots = nativeSourceRoots;
            this.testSourceRoots = testSourceRoots;
        }
    }

    private static final Configuration LEGACY_OPEN_CONFIGURATION = new Configuration(
            Arrays.asList(Pair.<String, String>of("${basedir}/src/share/classes/",
                                                  "com/sun/jmx/snmp/.*|com/sun/jmx/snmp|sun/management/snmp/.*|sun/management/snmp|sun/dc/.*|sun/dc"),
                          Pair.<String, String>of("${basedir}/src/${legacy-os}/classes/", null),
                          Pair.<String, String>of("${outputRoot}/jdk/gensrc/", null),
                          Pair.<String, String>of("${outputRoot}/jdk/impsrc/", null)),
            Arrays.<Pair<String, String>>asList(),
            Arrays.asList(Pair.<String, String>of("${basedir}/test", null))
    );

    private static final Configuration LEGACY_CLOSED_CONFIGURATION = new Configuration(
            Arrays.asList(Pair.<String, String>of("${basedir}/src/share/classes/", null),
                          Pair.<String, String>of("${basedir}/src/${legacy-os}/classes/", null),
                          Pair.<String, String>of("${basedir}/src/closed/share/classes/", null),
                          Pair.<String, String>of("${basedir}/src/closed/${legacy-os}/classes/", null),
                          Pair.<String, String>of("${outputRoot}/jdk/gensrc/", null),
                          Pair.<String, String>of("${outputRoot}/jdk/impsrc/", null)),
            Arrays.<Pair<String, String>>asList(),
            Arrays.asList(Pair.<String, String>of("${basedir}/test", null))
    );

    private static final Configuration MODULAR_CONFIGURATION = new Configuration(
            Arrays.asList(Pair.<String, String>of("${basedir}/share/classes/", null),
                          Pair.<String, String>of("${basedir}/${os}/classes/", null),
                          Pair.<String, String>of("${basedir}/${generalized-os}/classes/", null),
                          Pair.<String, String>of("${basedir}/../closed/${module}/share/classes/", null),
                          Pair.<String, String>of("${basedir}/../closed/${module}/${os}/classes/", null),
                          Pair.<String, String>of("${basedir}/../closed/${module}/${generalized-os}/classes/", null),
                          Pair.<String, String>of("${basedir}/../../../closed/src/${module}/share/classes/", null),
                          Pair.<String, String>of("${basedir}/../../../closed/src/${module}/${os}/classes/", null),
                          Pair.<String, String>of("${basedir}/../../../closed/src/${module}/${generalized-os}/classes/", null),
                          Pair.<String, String>of("${outputRoot}/jdk/gensrc/${module}/", null),
                          Pair.<String, String>of("${outputRoot}/support/gensrc/${module}/", null)),
            Arrays.asList(Pair.<String, String>of("${basedir}/share/native/", null),
                          Pair.<String, String>of("${basedir}/${os}/native/", null),
                          Pair.<String, String>of("${basedir}/${generalized-os}/native/", null),
                          Pair.<String, String>of("${outputRoot}/support/headers/${module}/", null)),
            Arrays.<Pair<String, String>>asList()
    );

    static boolean isJDKProject(FileObject projectDirectory) {
        try {
            ModuleRepository repository = ModuleDescription.getModules(projectDirectory);

            if (repository != null) {
                return repository.findModule(projectDirectory.getNameExt()) != null;
            } else {
                return BuildUtils.getFileObject(projectDirectory, "src/share/classes/java/lang/Object.java") != null;
            }
        } catch (Exception ex) {
            Logger.getLogger(JDKProject.class.getName()).log(Level.FINE, null, ex);
            return false;
        }
    }

    @ServiceProvider(service = ProjectFactory.class)
    public static final class JDKProjectFactory implements ProjectFactory {

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return isJDKProject(projectDirectory);
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                Project prj = loadModularProject(projectDirectory);

                if (prj != null)
                    return prj;

                if (BuildUtils.getFileObject(projectDirectory, "src/share/classes/java/lang/Object.java") != null) {
                    //legacy project:
                    return new JDKProject(projectDirectory, null, null);
                }
            }

            return null;
        }

        private Project loadModularProject(FileObject projectDirectory) {
            try {
                ModuleRepository repository = ModuleDescription.getModules(projectDirectory);
                
                if (repository == null) {
                    return null;
                }
                
                ModuleDescription thisModule = repository.findModule(projectDirectory.getNameExt());

                if (thisModule == null) {
                    return null;
                }

                if (!projectDirectory.equals(repository.findModuleRoot(thisModule.name))) {
                    return null;
                }


                return new JDKProject(projectDirectory, repository, thisModule);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(JDKProject.class.getName()).log(Level.FINE, null, ex);
                return null;
            }
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
            //no configuration yet.
        }
        
    }

    @Messages({
        "DN_Project=J2SE - {0}",
        "DN_Module=Module - {0} - {1}"
    })
    private final class ProjectInformationImpl implements ProjectInformation {

        @Override
        public String getName() {
            return currentModule != null ? getProjectDirectory().getNameExt()
                                         : "j2se";
        }

        @Override
        public String getDisplayName() {
            return currentModule != null ? Bundle.DN_Module(getProjectDirectory().getNameExt(), moduleRepository.getJDKRoot().getNameExt())
                                         : Bundle.DN_Project(getProjectDirectory().getParent().getNameExt());
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/java/openjdk/project/resources/jdk-project.png", false);
        }

        @Override
        public Project getProject() {
            return JDKProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

    }

    static final class MapPropertyProvider implements PropertyProvider {

        private final Map<String, String> properties = new HashMap<>();
        private final ChangeSupport cs = new ChangeSupport(this);
        
        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void setProperty(String key, String value) {
            properties.put(key, value);
            cs.fireChange();
        }

    }
}
