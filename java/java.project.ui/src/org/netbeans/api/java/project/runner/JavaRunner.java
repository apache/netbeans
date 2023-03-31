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

package org.netbeans.api.java.project.runner;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.project.runner.JavaRunnerImplementation;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * <p>Class that allows to execute given file(s). API clients can check whether given
 * command is support, by calling
 * {@link #isSupported(String, Map)} and execute the command by calling
 * {@link #execute(String, Map)}. Please consult documentation of particular
 * commands for the list of supported properties.</p>
 *
 * The following "standard" properties are supported by most commands (unless stated otherwise):
 * <table>
 * <caption>properties list</caption>
 * <tr><td>{@link #PROP_EXECUTE_FILE}      </td> <td>file to be executed (optional)</td> <td>{@link String} (absolute path) or {@link FileObject}</td></tr>
 * <tr><td>{@link #PROP_WORK_DIR}          </td> <td> working directory, project directory of execute.file will be used if missing </td> <td> {@link String} or {@link FileObject} or {@link java.io.File}</td></tr>
 * <tr><td>{@link #PROP_CLASSNAME}         </td> <td> class to execute, will be autodetected from execute.file if missing </td> <td> {@link String}</td></tr>
 * <tr><td>{@link #PROP_EXECUTE_CLASSPATH} </td> <td> execute classpath, will be autodetected from execute.file if missing </td> <td> {@link ClassPath}</td></tr>
 * <tr><td>{@link #PROP_PLATFORM_JAVA}     </td> <td> java tool which should be used for execution, will be autodetected from platform property if missing </td> <td> {@link String} or {@link FileObject} or {@link java.io.File}</td></tr>
 * <tr><td>{@link #PROP_PLATFORM}          </td> <td> java platform on which the class should be executed, default if missing, not needed if platform.java is set </td> <td> {@link JavaPlatform}</td></tr>
 * <tr><td>{@link #PROP_PROJECT_NAME}      </td> <td> name of the current project, will be autodetected from execute.file if missing </td> <td> {@link String}</td></tr>
 * <tr><td>{@link #PROP_RUN_JVMARGS}       </td> <td> JVM arguments </td> <td> {@link Iterable} of {@link String}s</td></tr>
 * <tr><td>{@link #PROP_APPLICATION_ARGS}  </td> <td> application arguments </td> <td> {@link Iterable} of {@link String}s</td></tr>
 * </table>
 * 
 * @see JavaRunnerImplementation
 * @since 1.22
 *
 * @author Jan Lahoda
 */
public final class JavaRunner {

    /**
     * <p>"Test" run the given file. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR}, {@link #PROP_RUN_JVMARGS}
     * and {@link #PROP_APPLICATION_ARGS}.</p>
     *
     * @since 1.22
     */
    public static final String QUICK_RUN = "run";

    /**
     * <p>"Test" run the given file in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR}, {@link #PROP_RUN_JVMARGS}
     * and {@link #PROP_APPLICATION_ARGS}.</p>
     *
     * <p>Property <code>stopclassname</code> can be set to a classname to support starting debugger using
     * the Step Into command.</p>
     *
     * @since 1.22
     */
    public static final String QUICK_DEBUG = "debug";
    
    /**
     * <p>"Test" run the given file in the profiling mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR}, {@link #PROP_RUN_JVMARGS}
     * and {@link #PROP_APPLICATION_ARGS}.</p>
     *
     * @since 1.44
     */
    public static final String QUICK_PROFILE = "profile";
    
    /**
     * <p>"Test" run the given test. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS}.</p>
     *
     * <p><strong>application.args</strong> property is not supported.</p>
     *
     * @since 1.22
     */
    public static final String QUICK_TEST = "junit";

    /**
     * <p>"Test" run the given test in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS}.</p>
     * 
     * <strong>application.args</strong> property is not supported.
     *
     * @since 1.22
     */
    public static final String QUICK_TEST_DEBUG = "junit-debug";

    /**
     * <p>"Test" run the given test in the profiling mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     *
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_CLASSNAME}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS}.</p>
     *
     * <p><strong>application.args</strong> property is not supported.</p>
     *
     * @since 1.44
     */
    public static final String QUICK_TEST_PROFILE = "junit-profile";
    
    /** <p>"Test" run the given applet. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     * 
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_EXECUTE_FILE}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS},
     * <code>applet.url</code>.</p>
     * 
     * @since 1.22
     */
    public static final String QUICK_RUN_APPLET = "run-applet";
    
    /** <p>"Test" run the given applet in debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     * 
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_EXECUTE_FILE}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS},
     * <code>applet.url</code>.</p>
     * 
     * @since 1.22
     */
    public static final String QUICK_DEBUG_APPLET = "debug-applet";
    
    /** <p>"Test" run the given applet in the profiling mode. Classfiles produced by the Java infrastructure will be
     * executed.</p>
     * 
     * <p>These properties are should be set in the properties, or inferable: {@link #PROP_EXECUTE_CLASSPATH},
     * {@link #PROP_EXECUTE_FILE}, {@link #PROP_PLATFORM_JAVA}, {@link #PROP_WORK_DIR} and {@link #PROP_RUN_JVMARGS},
     * <code>applet.url</code>.</p>
     * 
     * @since 1.44
     */
    public static final String QUICK_PROFILE_APPLET = "profile-applet";
    
    /** Clean classfiles produced by the Java infrastructure.
     * 
     * @since 1.22
     */
    public static final String QUICK_CLEAN = "clean";

    /** File to execute. Should be either {@link String} (absolute path) or {@link FileObject}.
     *
     * @since 1.22
     */
    public static final String PROP_EXECUTE_FILE = "execute.file";

    /** Working directory for execution. Should be either {@link String} (absolute path) or {@link FileObject} or {@link java.io.File}.
     *
     * @since 1.22
     */
    public static final String PROP_WORK_DIR = "work.dir";

    /** JVM arguments to be used for the execution. Should be an {@link Iterable} of {@link String}s.
     * <p>Arguments may also be contributed by <a href="@org-netbeans-modules-extexecution@/org/netbeans/api/extexecution/startup/StartupExtender.html" >StartupExtender</a>s;
     * the {@link JavaPlatform} (see {@link #PROP_PLATFORM}) will be in the context,
     * as will a {@link Project} if available from {@link #PROP_EXECUTE_FILE} or {@link #PROP_WORK_DIR}.
     * @since 1.22
     */
    public static final String PROP_RUN_JVMARGS = "run.jvmargs";

    /** The name of the class to execute. Should be {@link String} - fully qualified binary name.
     *  Will be autodetected from {@link #PROP_EXECUTE_FILE} if missing.
     * 
     * @since 1.22
     */
    public static final String PROP_CLASSNAME = "classname";

    /** Execute classpath to use for execution of the class. Should be {@link ClassPath}.
     *  Will be autodetected from {@link #PROP_EXECUTE_FILE} if missing.
     *
     * @since 1.22
     */
    public static final String PROP_EXECUTE_CLASSPATH = "execute.classpath";

    /** Execute modulepath to use for execution of the class. Should be {@link ClassPath}.
     *  Will be autodetected from {@link #PROP_EXECUTE_FILE} if missing.
     *
     * @since 1.71
     */
    public static final String PROP_EXECUTE_MODULEPATH = "execute.modulepath";

    /** Java tool to use for execution. Should be {@link String} (absolute path) or {@link FileObject} or {@link java.io.File}.
     *  Will be autodetected from {@link #PROP_PLATFORM} if missing.
     *
     * @since 1.22
     */
    public static final String PROP_PLATFORM_JAVA = "platform.java";

    /** Java platform to use for execution. Should be {@link JavaPlatform}.
     *  Will be used to autodetect {@link #PROP_PLATFORM_JAVA}.
     * 
     * @since 1.22
     */
    public static final String PROP_PLATFORM = "platform";

    /** Project name to use for Output Window caption. Should be {@link String}.
     *  Will be autodetected from {@link #PROP_EXECUTE_FILE} if missing.
     * 
     * @since 1.22
     */
    public static final String PROP_PROJECT_NAME = "project.name";

    /** Application arguments to be used for the execution. Should be an {@link Iterable} of {@link String}s.
     *
     * @since 1.22
     */
    public static final String PROP_APPLICATION_ARGS = "application.args";

    /**
     * Runtime file encoding passed to the jvm (-Dfile.encoding).
     * If not given the {@link org.netbeans.api.queries.FileEncodingQuery} is used
     * to obtain the encoding.
     * @since 1.28
     */
    public static final String PROP_RUNTIME_ENCODING = "runtime.encoding";  //NOI18N

    private static final Logger LOG = Logger.getLogger(JavaRunner.class.getName());

    /**
     * Check whether the given command is supported.
     *
     * @param command command name
     * @param properties properties
     * @return true if and only if the given command is supported for given file/folder
     *
     * @since 1.22
     */
    public static boolean isSupported(String command, Map<String, ?> properties) {
        Parameters.notNull("command", command);
        Parameters.notNull("properties", properties);

        for (JavaRunnerImplementation i : Lookup.getDefault().lookupAll(JavaRunnerImplementation.class)) {
            if (i.isSupported(command, properties)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Execute the given command with given parameters. Please refer to the documentation
     * of the given command for supported properties.
     *
     * @param command command to execute
     * @param properties properties
     * @throws java.io.IOException if execution fails
     * @throws java.lang.UnsupportedOperationException if the given command is not supported
     *
     * @since 1.22
     */
    public static ExecutorTask execute(String command, Map<String, ?> properties) throws IOException, UnsupportedOperationException {
        Parameters.notNull("command", command);
        Parameters.notNull("properties", properties);
        
        final Collection<? extends JavaRunnerImplementation> runners = Lookup.getDefault().lookupAll(JavaRunnerImplementation.class);
        for (JavaRunnerImplementation i : runners) {
            if (i.isSupported(command, properties)) {
                return i.execute(command, properties);
            }
        }

        throw new UnsupportedOperationException(MessageFormat.format(
            "command: {0}, JavaRunner impls: {1}",  //NOI18N
            command,
            runners));
    }

}
