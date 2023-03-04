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

package org.netbeans.modules.java.j2semodule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a J2SEModularProject from scratch according to some initial configuration.
 */
public class J2SEModularProjectGenerator {
    
    private static final String DEFAULT_PLATFORM_ID = "default_platform";   //NOI18N
    private static final String PROP_PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    private static final String METRICS_LOGGER = "org.netbeans.ui.metrics.projects"; //NOI18N
    private static final String J2SE_MODULAR_METRICS_LOGGER = "org.netbeans.ui.metrics.j2se-modular";  //NOI18N
    
    enum Action {
        CREATE("USG_PROJECT_CREATE", "USG_PROJECT_CREATE_J2SE_MODULAR"),   //NOI18N
        OPEN("USG_PROJECT_OPEN", "USG_PROJECT_OPEN_J2SE_MODULAR"),       //NOI18N
        CLOSE("USG_PROJECT_CLOSE", "USG_PROJECT_CLOSE_J2SE_MODULAR");     //NOI18N
        
        private final String genericLogMessage;
        private final String specificLogMessage;
        
        private Action(
            @NonNull final String genericLogMessage,
            @NonNull final String specificLogMessage) {
            assert genericLogMessage != null;
            assert specificLogMessage != null;
            this.genericLogMessage = genericLogMessage;
            this.specificLogMessage = specificLogMessage;
        }
        
        @NonNull
        public String getGenericLogMessage() {
            return genericLogMessage;
        }
        
        @NonNull
        public String getSpecificLogMessage() {
            return specificLogMessage;
        }
    }

    private J2SEModularProjectGenerator() {}
    
    /**
     * Create a new empty J2SE modular project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final File dir, final String name, final JavaPlatform platform) throws IOException {
        Parameters.notNull("dir", dir); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        
        final FileObject dirFO = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        dirFO.getFileSystem().runAtomicAction(() -> {
            final SpecificationVersion sourceLevel = getSourceLevel(platform);
            h[0] = createProject(dirFO, name, sourceLevel, "src", "classes", "tests", platform.getProperties().get(PROP_PLATFORM_ANT_NAME));   //NOI18N
            final J2SEModularProject p = (J2SEModularProject) ProjectManager.getDefault().findProject(dirFO);
            ProjectManager.getDefault().saveProject(p);
            try {
                ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>) () -> {
                    ProjectManager.getDefault().saveProject (p);
                    ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_MODULES);
                    return null;
                });
            } catch (MutexException ex) {
                Exceptions.printStackTrace(ex.getException());
            }
            
            dirFO.createFolder("src"); //NOI18N
        });
        return h[0];
    }

    private static AntProjectHelper createProject(
            FileObject dirFO,
            String name,
            SpecificationVersion sourceLevel,
            String srcRoot,
            String srcRootPath,
            String testSrcRootPath,
            @NonNull final String platformId
            ) throws IOException {
        final String antName = PropertyUtils.getUsablePropertyName(name);
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEModularProject.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (!DEFAULT_PLATFORM_ID.equals(platformId)) {
            final Element platformEl = doc.createElementNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform");   //NOI18N
            platformEl.setAttribute("explicit-source-supported", "true"); //NOI18N
            data.appendChild(platformEl);
        }
        Element sourceRoots = doc.createElementNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            root.setAttribute ("pathref","src.dir.path");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
            ep.setProperty("src.dir.path", srcRootPath); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            root.setAttribute ("pathref","test.src.dir.path");   //NOI18N
            testRoots.appendChild (root);
            ep.setProperty("test.src.dir", srcRoot); // NOI18N
            ep.setProperty("test.src.dir.path", testSrcRootPath); // NOI18N
        }
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("javac.classpath", ""); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[] {ref(ProjectProperties.JAVAC_CLASSPATH, true)}); // NOI18N
        ep.setProperty("javac.test.processorpath", new String[] {ref(ProjectProperties.JAVAC_TEST_CLASSPATH,true)}); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", ""); // NOI18N
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            ref(ProjectProperties.RUN_CLASSPATH,true)
        });
        ep.setComment("debug.classpath", new String[] { // NOI18N
            "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_debug.transport"),
            "#debug.transport=dt_socket"
        }, false);
        ep.setProperty("jar.compress", "false"); // NOI18N

        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] {
            "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", new String[] { // NOI18N
            ref(ProjectProperties.JAVAC_CLASSPATH, true)
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            ref(ProjectProperties.JAVAC_TEST_CLASSPATH, true)
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            ref(ProjectProperties.RUN_TEST_CLASSPATH, true)
        });

        //Modules
        ep.setProperty(ProjectProperties.JAVAC_MODULEPATH, ""); //NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORMODULEPATH, ""); //NOI18N
        ep.setProperty(ProjectProperties.RUN_MODULEPATH, new String[] {
            ref(ProjectProperties.JAVAC_MODULEPATH, false),
            ref(ProjectProperties.BUILD_MODULES_DIR, true)
        });
        ep.setProperty(ProjectProperties.DEBUG_MODULEPATH, new String[] {
            ref(ProjectProperties.RUN_MODULEPATH, true)
        });
        ep.setProperty(ProjectProperties.JAVAC_TEST_MODULEPATH, new String[] {
                ref(ProjectProperties.JAVAC_MODULEPATH, false),
                ref(ProjectProperties.BUILD_MODULES_DIR, true)
        });
        ep.setProperty(ProjectProperties.RUN_TEST_MODULEPATH, new String[] {
            ref(ProjectProperties.JAVAC_TEST_MODULEPATH, false),
            ref(ProjectProperties.BUILD_TEST_MODULES_DIR, true)
        });
        ep.setProperty(ProjectProperties.DEBUG_TEST_MODULEPATH, new String[] {
            ref(ProjectProperties.RUN_TEST_MODULEPATH, true)
        });

        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N

        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N  //TODO: For What?
        ep.setProperty(ProjectProperties.BUILD_MODULES_DIR, "${build.dir}/modules"); // NOI18N  //TODO: For What?
        ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.modules.dir", "${build.dir}/test/modules"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", platformId); // NOI18N

        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, ""); // NOI18N
        ep.setComment(ProjectProperties.RUN_JVM_ARGS, new String[] {
            "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(ProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_HTML5, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ENCODING, "${"+ProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(ProjectProperties.SOURCE_ENCODING, enc.name());
        ep.setProperty(ProjectProperties.DIST_ARCHIVE_EXCLUDES,""); //NOI18N
        ep.setComment(ProjectProperties.DIST_ARCHIVE_EXCLUDES,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_dist.archive.excludes") //NOI18N
                },
                false);
        //JLink
        ep.setProperty(ProjectProperties.DIST_JLINK_DIR, "${"+ProjectProperties.DIST_DIR+"}/jlink");
        ep.setProperty(ProjectProperties.DIST_JLINK_OUTPUT, "${"+ProjectProperties.DIST_JLINK_DIR+"}/"+antName);
        ep.setProperty(ProjectProperties.JLINK_ADDITIONALMODULES, "");
        ep.setComment(ProjectProperties.JLINK_ADDITIONALMODULES,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_jlink.additionalmodules") //NOI18N
                },
                false);
        ep.setProperty(ProjectProperties.JLINK_ADDITIONALPARAM, "");
        ep.setComment(ProjectProperties.JLINK_ADDITIONALPARAM,
                new String[] {
                    "# " + NbBundle.getMessage(J2SEModularProjectGenerator.class, "COMMENT_jlink.additionalparam") //NOI18N
                },
                false);
        ep.setProperty(ProjectProperties.JLINK_LAUNCHER, "true");
        ep.setProperty(ProjectProperties.JLINK_LAUNCHER_NAME, antName);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        logUsage(Action.OPEN);
        return h;
    }

    private static SpecificationVersion getSourceLevel (final JavaPlatform platform) {
        if (defaultSourceLevel != null) {
            return defaultSourceLevel;
        } else {            
            return platform.getSpecification().getVersion();
        }
    }

    private static String ref(@NonNull final String propertyName, final boolean lastEntry) {
        return String.format("${%s}%s", propertyName, lastEntry ? "" : ":");  //NOI18N
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    static void logUsage(@NonNull Action action) {
        assert action != null;
        Logger logger = Logger.getLogger(J2SEModularProjectGenerator.METRICS_LOGGER);
        LogRecord logRecord = new LogRecord(Level.INFO, action.getGenericLogMessage());
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new Object[]{
            J2SEModularProject.TYPE
        });
        logger.log(logRecord);
        
        logger = Logger.getLogger(J2SEModularProjectGenerator.J2SE_MODULAR_METRICS_LOGGER);
        logRecord = new LogRecord(Level.INFO, action.getSpecificLogMessage());
        logRecord.setLoggerName(logger.getName());
        logger.log(logRecord);
    }

    //------------ Used by unit tests -------------------

    private static SpecificationVersion defaultSourceLevel;
                
    /**
     * Unit test only method. Sets the default source level for tests
     * where the default platform is not available.
     * @param version the default source level set to project when it is created
     *
     */
    public static void setDefaultSourceLevel (SpecificationVersion version) {
        defaultSourceLevel = version;
    }
}


