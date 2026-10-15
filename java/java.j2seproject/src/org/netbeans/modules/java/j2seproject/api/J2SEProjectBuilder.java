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

package org.netbeans.modules.java.j2seproject.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.netbeans.modules.java.j2seproject.J2SEProjectUtil.ref;

/**
 * Builder for creating a new J2SE project.
 * Typical usage is:
 * <pre>
 *      new J2SEProjectBuilder(projectFolder, projectName).
 *      addSourceRoots(sourceFolders).
 *      addTestRoots(testFolders).
        setMainClass(mainClass).
 *      build();
 * </pre>
 * XXX: Uses Bundle from org.netbeans.modules.java.j2seproject package not to affect
 * existing localizations.
 * @author Tomas Zezula
 * @since 1.42
 */
public class J2SEProjectBuilder {

    private static final Logger LOG = Logger.getLogger(J2SEProjectBuilder.class.getName());
    private static final String DEFAULT_PLATFORM_ID = "default_platform";   //NOI18N

    private final File projectDirectory;
    private final String name;
    private final Collection<File> sourceRoots;
    private final Collection<File> testRoots;
    private final Collection<Library> compileLibraries;
    private final Collection<Library> runtimeLibraries;
    private final StringBuilder jvmArgs;

    private boolean hasDefaultRoots;
    private boolean skipTests;
    private SpecificationVersion defaultSourceLevel;
    private String mainClass;
    private String manifest;
    private String librariesDefinition;
    private String buildXmlName;
    private String distFolder;
    private String mainClassTemplate;
    private JavaPlatform platform;

    /**
     * Creates a new instance of {@link J2SEProjectBuilder}
     * @param projectDirectory the directory in which the project should be created
     * @param name the name of the project
     */
    public J2SEProjectBuilder(
            final @NonNull File projectDirectory,
            final @NonNull String name) {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        Parameters.notNull("name", name);                           //NOI18N
        this.projectDirectory = projectDirectory;
        this.name = name;
        this.sourceRoots = new ArrayList<File>();
        this.testRoots = new ArrayList<File>();
        this.jvmArgs = new StringBuilder();
        this.compileLibraries = new ArrayList<Library>();
        this.runtimeLibraries = new ArrayList<Library>();
        this.platform = JavaPlatformManager.getDefault().getDefaultPlatform();
    }

    /**
     * Adds the default source roots, "src" and "test".
     * @return the builder
     */
    public J2SEProjectBuilder addDefaultSourceRoots() {
        this.hasDefaultRoots = true;
        return this;
    }

    /**
     * Avoids creating the test root folder and adding JUnit dependencies.
     * The test folder is still registered so {@link SourceGroupModifier} with {@link JavaProjectConstants#SOURCES_HINT_TEST} will work later.
     * @return the builder
     */
    public J2SEProjectBuilder skipTests(boolean skipTests) {
        this.skipTests = skipTests;
        return this;
    }

    /**
     * Adds source roots into the project
     * @param sourceRoots the roots to be added
     * @return the builder
     */
    public J2SEProjectBuilder addSourceRoots(final @NonNull File... sourceRoots) {
        Parameters.notNull("sourceRoots", sourceRoots); //NOI18N
        this.sourceRoots.addAll(Arrays.asList(sourceRoots));
        return this;
    }

    /**
     * Adds test roots into the project
     * @param testRoots the roots to be added
     * @return the builder
     */
    public J2SEProjectBuilder addTestRoots(final @NonNull File... testRoots) {
        Parameters.notNull("testRoots", testRoots);     //NOI18N
        this.testRoots.addAll(Arrays.asList(testRoots));
        return this;
    }

    /**
     * Adds compile time libraries
     * @param libraries the libraries to be added to compile classpath.
     * @return the builder
     */
    public J2SEProjectBuilder addCompileLibraries(@NonNull final Library... libraries) {
        Parameters.notNull("libraries", libraries); //NOI18N
        this.compileLibraries.addAll(Arrays.asList(libraries));
        return this;
    }

    /**
     * Adds runtime libraries
     * @param libraries the libraries to be added to runtime classpath.
     * @return the builder
     */
    public J2SEProjectBuilder addRuntimeLibraries(@NonNull final Library... libraries) {
        Parameters.notNull("libraries", libraries); //NOI18N
        this.runtimeLibraries.addAll(Arrays.asList(libraries));
        return this;
    }

    /**
     * Sets a main class
     * @param mainClass the fully qualified name of the main class,
     * if null main class is not created
     * @return the builder
     */
    public J2SEProjectBuilder setMainClass (final @NullAllowed String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    /**
     * Sets a path to manifest file
     * @param manifest the name (path) to manifest file,
     * if not manifest is not set
     * @return the builder
     */
    public J2SEProjectBuilder setManifest (final @NullAllowed String manifest) {
        this.manifest = manifest;
        return this;
    }

    /**
     * Sets a library definition file for per project libraries,
     * @param librariesDefinition the name (path) to libraries definition file,
     * if null project libraries are not used
     * @return the builder
     */
    public J2SEProjectBuilder setLibrariesDefinitionFile (final @NullAllowed String librariesDefinition) {
        this.librariesDefinition = librariesDefinition;
        return this;
    }

    /**
     * Sets a source level of the project
     * @param sourceLevel the source level,
     * if null the default source level is used.
     * @return the builder
     */
    public J2SEProjectBuilder setSourceLevel (final @NullAllowed SpecificationVersion sourceLevel) {
        if (sourceLevel != null && SourceLevelQuery.MINIMAL_SOURCE_LEVEL.compareTo(sourceLevel) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Source level less than %s is unsupported.",       //NOI18N
                    SourceLevelQuery.MINIMAL_SOURCE_LEVEL));
        }
        this.defaultSourceLevel = sourceLevel;
        return this;
    }

    /**
     * Sets a name of build.xml file
     * @param name the name of build.xml file,
     * if null the default 'build.xml' is used
     * @return the builder
     */
    public J2SEProjectBuilder setBuildXmlName(final @NullAllowed String name) {
        this.buildXmlName = name;
        return this;
    }

    /**
     * Sets a name of dist (build artifact) folder
     * @param distFolderName the name of the dist folder
     * if null the default 'dist' is used
     * @return the builder
     * @since 1.49
     */
    @NonNull
    public J2SEProjectBuilder setDistFolder(@NullAllowed final String distFolderName) {
        this.distFolder = distFolderName;
        return this;
    }

    /**
     * Sets a main class template
     * @param mainClassTemplatePath the path to main class template on the system filesystem,
     * if null the default template is used
     * @return the builder
     */
    public J2SEProjectBuilder setMainClassTemplate(final @NullAllowed String mainClassTemplatePath) {
        this.mainClassTemplate = mainClassTemplatePath;
        return this;
    }

    /**
     * Adds a JVM arguments
     * @param jvmArgs the arguments to be added
     * @return the builder
     */
    public J2SEProjectBuilder addJVMArguments(final @NonNull String jvmArgs) {
        Parameters.notNull("jvmArgs", jvmArgs); //NOI18N
        if (this.jvmArgs.length() != 0) {
            this.jvmArgs.append(' ');   //NOI18N
        }
        this.jvmArgs.append(jvmArgs);
        return this;
    }

    /**
     * Sets a platform to be used for a new project
     * @param platform to be used
     * @return the builder
     * @since 1.53
     */
    public J2SEProjectBuilder setJavaPlatform (@NonNull final JavaPlatform platform) {
        Parameters.notNull("platform", platform);
        if (platform.getProperties().get(J2SEProjectProperties.PROP_PLATFORM_ANT_NAME) == null) {
            throw new IllegalArgumentException("Invalid platform, the platform has no platform.ant.name");  //NOI18N
        }
        this.platform = platform;
        return this;
    }

    /**
     * Creates the J2SEProject
     * @return the {@link AntProjectHelper} of the created project
     * @throws IOException when creation fails
     */
    public AntProjectHelper build() throws IOException {
        final FileObject dirFO = FileUtil.createFolder(this.projectDirectory);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final FileObject[] srcFolder = new FileObject[1];
        dirFO.getFileSystem().runAtomicAction(() -> {
            final SpecificationVersion sourceLevel = getSourceLevel();
            h[0] = createProject(
                    dirFO,
                    name,
                    sourceLevel,
                    hasDefaultRoots ? "src" : null,     //NOI18N
                    hasDefaultRoots ? "test" : null,    //NOI18N
                    skipTests,
                    buildXmlName,
                    distFolder,
                    mainClass,
                    manifest,
                    manifest == null,
                    librariesDefinition,
                    jvmArgs.toString(),
                    toClassPathElements(compileLibraries),
                    toClassPathElements(runtimeLibraries, ref(ProjectProperties.JAVAC_CLASSPATH,false), ref(ProjectProperties.BUILD_CLASSES_DIR,true)),
                    platform.getProperties().get(J2SEProjectProperties.PROP_PLATFORM_ANT_NAME));   //NOI18N
            final J2SEProject p = (J2SEProject) ProjectManager.getDefault().findProject(dirFO);
            ProjectManager.getDefault().saveProject(p);
            final ReferenceHelper refHelper = p.getReferenceHelper();
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        registerRoots(h[0], refHelper, sourceRoots, false);
                        registerRoots(h[0], refHelper, testRoots, true);
                        ProjectManager.getDefault().saveProject (p);
                        final List<Library> libsToCopy = new ArrayList<Library>();
                        libsToCopy.addAll(getMandatoryLibraries(skipTests));
                        libsToCopy.addAll(compileLibraries);
                        libsToCopy.addAll(runtimeLibraries);
                        copyRequiredLibraries(h[0], refHelper, libsToCopy);
                        ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                        return null;
                    }
                });
            } catch (MutexException ex) {
                Exceptions.printStackTrace(ex.getException());
            }
            if (hasDefaultRoots) {
                srcFolder[0] = dirFO.createFolder("src") ; // NOI18N
                if (!skipTests) {
                    dirFO.createFolder("test"); // NOI18N
                }
            } else if (!sourceRoots.isEmpty()) {
                srcFolder[0] = FileUtil.toFileObject(sourceRoots.iterator().next());
            }
        });                    
        if ( mainClass != null && srcFolder[0] != null) {
            createMainClass(mainClass, srcFolder[0], mainClassTemplate);
        }
        return h[0];
    }

    /**
     * Sets default module system properties if they are not set.
     * @param ep the {@link EditableProperties} to write the properties into.
     * @since 1.85
     */
    public static void createDefaultModuleProperties(
            @NonNull final EditableProperties ep,
            final boolean hasUnitTests) {
        if (ep.getProperty(ProjectProperties.JAVAC_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.JAVAC_MODULEPATH, new String[0]);
        }
        if (ep.getProperty(ProjectProperties.JAVAC_PROCESSORMODULEPATH) == null) {
            ep.setProperty(ProjectProperties.JAVAC_PROCESSORMODULEPATH, new String[0]);
        }
        if (ep.getProperty(ProjectProperties.RUN_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.RUN_MODULEPATH, new String[] {
                ref(ProjectProperties.JAVAC_MODULEPATH, true)
            });
        }
        if (ep.getProperty(ProjectProperties.DEBUG_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.DEBUG_MODULEPATH, new String[] {
                ref(ProjectProperties.RUN_MODULEPATH, true)
            });
        }
        if (ep.getProperty(ProjectProperties.JAVAC_TEST_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.JAVAC_TEST_MODULEPATH,
                new String[] {
                    ref(ProjectProperties.JAVAC_MODULEPATH, true)
                });
        }
        if (ep.getProperty(ProjectProperties.RUN_TEST_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.RUN_TEST_MODULEPATH, new String[] {
                ref(ProjectProperties.JAVAC_TEST_MODULEPATH, true)
            });
        }
        if (ep.getProperty(ProjectProperties.DEBUG_TEST_MODULEPATH) == null) {
            ep.setProperty(ProjectProperties.DEBUG_TEST_MODULEPATH, new String[] {
                ref(ProjectProperties.RUN_TEST_MODULEPATH, true)
            });
        }
    }

    private static AntProjectHelper createProject(
            FileObject dirFO,
            String name,
            SpecificationVersion sourceLevel,
            String srcRoot,
            String testRoot,
            boolean skipTests,
            String buildXmlName,
            String distFolder,
            String mainClass,
            String manifestFile,
            boolean isLibrary,
            String librariesDefinition,
            String jvmArgs,
            String[] compileClassPath,
            String[] runtimeClassPath,
            @NonNull final String platformId
            ) throws IOException {
        final String antName = PropertyUtils.getUsablePropertyName(name);
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProject.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (!DEFAULT_PLATFORM_ID.equals(platformId)) {
            final Element platformEl = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform");   //NOI18N
            final SpecificationVersion jdk13 = new SpecificationVersion("1.3");     //NOI18N
            final boolean supportsExplicitSource = jdk13.compareTo(sourceLevel) < 0;
            platformEl.setAttribute("explicit-source-supported", Boolean.toString(supportsExplicitSource)); //NOI18N
            data.appendChild(platformEl);
        }
        Element sourceRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            testRoots.appendChild (root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        ep.setProperty("dist.dir", distFolder != null ? distFolder : "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + antName + ".jar"); // NOI18N
        ep.setProperty("javac.classpath", compileClassPath); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[] {ref(ProjectProperties.JAVAC_CLASSPATH, true)}); // NOI18N
        ep.setProperty("javac.test.processorpath", new String[] {ref(ProjectProperties.JAVAC_TEST_CLASSPATH,true)}); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", runtimeClassPath);
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            ref(ProjectProperties.RUN_CLASSPATH,true)
        });
        ep.setComment("debug.classpath", new String[] { // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_debug.transport"),
            "#debug.transport=dt_socket"
        }, false);
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (mainClass != null) {
            ep.setProperty("main.class", mainClass); // NOI18N
        } else if (!isLibrary) {
            ep.setProperty("main.class", ""); // NOI18N
        }

        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", skipTests ? new String[] { // NOI18N
            ref(ProjectProperties.JAVAC_CLASSPATH, false),
            ref(ProjectProperties.BUILD_CLASSES_DIR, true)
        } : new String[] { // NOI18N
            ref(ProjectProperties.JAVAC_CLASSPATH, false),
            ref(ProjectProperties.BUILD_CLASSES_DIR, false),
            ref("libs.junit.classpath", false), // NOI18N
            ref("libs.junit_4.classpath", true)  //NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            ref(ProjectProperties.JAVAC_TEST_CLASSPATH, false),
            ref(ProjectProperties.BUILD_TEST_CLASSES_DIR, true)
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            ref(ProjectProperties.RUN_TEST_CLASSPATH, true)
        });

        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N

        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", platformId); // NOI18N

        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, jvmArgs); // NOI18N
        ep.setComment(ProjectProperties.RUN_JVM_ARGS, new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(ProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_HTML5, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ENCODING, "${"+ProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(ProjectProperties.SOURCE_ENCODING, enc.name());
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }
        if (buildXmlName != null) {
            ep.put(ProjectProperties.BUILD_SCRIPT, buildXmlName);
        }
        ep.setProperty(J2SEProjectProperties.MKDIST_DISABLED, isLibrary ? "true" : "false");
        ep.setProperty(ProjectProperties.DIST_ARCHIVE_EXCLUDES,""); //NOI18N
        ep.setComment(ProjectProperties.DIST_ARCHIVE_EXCLUDES,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_dist.archive.excludes") //NOI18N
                },
                false);
        ep.setProperty(J2SEProjectProperties.JAVAC_EXTERNAL_VM, "true");    //NOI18N
        //Modules
        createDefaultModuleProperties(ep, !skipTests);
        //JLink
        ep.setProperty(ProjectProperties.DIST_JLINK_DIR, "${"+ProjectProperties.DIST_DIR+"}/jlink");
        ep.setProperty(ProjectProperties.DIST_JLINK_OUTPUT, "${"+ProjectProperties.DIST_JLINK_DIR+"}/"+antName);
        ep.setProperty(ProjectProperties.JLINK_ADDITIONALMODULES, "");
        ep.setComment(ProjectProperties.JLINK_ADDITIONALMODULES,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_jlink.additionalmodules") //NOI18N
                },
                false);
        ep.setProperty(ProjectProperties.JLINK_ADDITIONALPARAM, "");
        ep.setComment(ProjectProperties.JLINK_ADDITIONALPARAM,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_jlink.additionalparam") //NOI18N
                },
                false);
        ep.setProperty(ProjectProperties.JLINK_LAUNCHER, "true");
        ep.setProperty(ProjectProperties.JLINK_LAUNCHER_NAME, antName);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        logUsage();
        return h;
    }

    private static void registerRoots(
            final AntProjectHelper helper,
            final ReferenceHelper refHelper,
            final Collection<? extends File> sourceFolders,
            final boolean tests) {
        if (sourceFolders.isEmpty()) {
            //Nothing to do.
            return;
        }
        final Element data = helper.getPrimaryConfigurationData(true);
        final Document doc = data.getOwnerDocument();
        NodeList nl = data.getElementsByTagNameNS(
                J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,
                tests ? "test-roots" : "source-roots");
        assert nl.getLength() == 1;
        final Element sourceRoots = (Element) nl.item(0);
        boolean first = true;
        for (File sourceFolder : sourceFolders) {
            String name;
            if (first) {
                //Name the first src root src.dir to be compatible with NB 4.0
                name = "src";               //NOI18N
                first = false;
            } else {
                name = sourceFolder.getName();
            }
            String propName = (tests ? "test." : "") + name + ".dir";    //NOI18N
            int rootIndex = 1;
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            while (props.containsKey(propName)) {
                rootIndex++;
                propName = (tests ? "test." : "") + name + rootIndex + ".dir";   //NOI18N
            }
            String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
            Element root = doc.createElementNS (J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id",propName);   //NOI18N
            sourceRoots.appendChild(root);
            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.put(propName,srcReference);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
        }
        helper.putPrimaryConfigurationData(data,true);
    }

    private static final String loggerName = "org.netbeans.ui.metrics.j2se"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_J2SE"; // NOI18N

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage() {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        //logRecord.setParameters(new Object[] {""}); // NOI18N
        Logger.getLogger(loggerName).log(logRecord);
    }

    private SpecificationVersion getSourceLevel () {
        if (defaultSourceLevel != null) {
            return defaultSourceLevel;
        } else {
            final SpecificationVersion v = platform.getSpecification().getVersion();
            return v;
        }
    }

    private String[] toClassPathElements(
            final @NonNull Collection<? extends Library> libraries,
            final @NonNull String... additionalEntries) {
        final String[] result = new String[libraries.size() + additionalEntries.length];
        final Iterator<? extends Library> it = libraries.iterator();
        for (int i=0; it.hasNext(); i++) {
            final Library lib = it.next();
            result[i] = "${libs." + lib.getName() + ".classpath}" + (it.hasNext() || additionalEntries.length != 0 ? ":":"");    //NOI18N
        }
        System.arraycopy(additionalEntries, 0, result, libraries.size(), additionalEntries.length);
        return result;
    }

    private static void createMainClass(
            final @NonNull String mainClassName,
            final @NonNull FileObject srcFolder,
            @NullAllowed String mainClassTemplate) throws IOException {

        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        }
        else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }

        if ( mName.length() == 0 ) {
            return;
        }

        if (mainClassTemplate == null) {
            mainClassTemplate = "Templates/Classes/Main.java";  //NOI18N
        }
        final FileObject mainTemplate = FileUtil.getConfigFile(mainClassTemplate);

        if ( mainTemplate == null ) {
            LOG.log(
                Level.WARNING,
                "Template {0} not found!",  //NOI18N
                mainClassTemplate);
            return; // Don't know the template
        }

        DataObject mt = DataObject.find( mainTemplate );

        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );
        DataObject res = mt.createFromTemplate( pDf, mName );
        if (res == null || !res.isValid()) {
            LOG.log(
                Level.WARNING,
                "Template {0} created an invalid DataObject in folder {1}!",  //NOI18N
                new Object[] {
                    mainClassTemplate,
                    FileUtil.getFileDisplayName(pkgFolder)
                });
        }
    }

    private static void copyRequiredLibraries(
            final AntProjectHelper h,
            final ReferenceHelper rh,
            final Collection<? extends Library> libraries) throws IOException {
        if (!h.isSharableProject()) {
            return;
        }
        for (Library library : libraries) {
            final String libName = library.getName();
            if (rh.getProjectLibraryManager().getLibrary(libName) == null
                && LibraryManager.getDefault().getLibrary(libName) != null) {
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libName)); // NOI18N
            }
        }
    }

    private static Collection<? extends Library> getMandatoryLibraries(boolean skipTests) {
        final List<Library> result = new ArrayList<Library>();
        final LibraryManager manager = LibraryManager.getDefault();
        for (final String mandatoryLib : skipTests ? new String[] {"CopyLibs"} : new String[] {"junit", "junit_4", "CopyLibs"}) {   //NOI18N
            final Library lib = manager.getLibrary(mandatoryLib);
            if (lib != null) {
                result.add(lib);
            }
        }
        return result;
    }
}
