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

package org.netbeans.modules.profiler.nbimpl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tools.ant.types.LogLevel;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;


/**
 * Ant task to start the NetBeans profiler profile action.
 * <p/>
 * Will put the profiler into listening mode, placing the port number into the "profiler.port" property.
 * The target app then should be started through the profiler agent passing it this port number.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class NBProfileDirectTask extends Task {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /**
     * Enumerated attribute with the values "asis", "add" and "remove".
     */
    public static class YesNoAuto extends EnumeratedAttribute {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public String[] getValues() {
            return new String[] { "yes", "true", "no", "false", "auto" }; //NOI18N
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int INTERACTIVE_AUTO = 0;
    private static final int INTERACTIVE_YES = 1;
    private static final int INTERACTIVE_NO = 2;
    private static final String DEFAULT_AGENT_JVMARGS_PROPERTY = "profiler.info.jvmargs.agent"; // NOI18N
    private static final String DEFAULT_JVM_PROPERTY = "profiler.info.jvm"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    /**
     * Explicit classpath of the profiled process.
     */
    private Path classpath = null;
    private Path rootsPath = null;
    private String jvmArgsPrefix = ""; // NOI18N
    private String jvmArgsProperty = DEFAULT_AGENT_JVMARGS_PROPERTY;
    private String jvmProperty = DEFAULT_JVM_PROPERTY;
    private String mainClass = null;
    private int interactive = INTERACTIVE_AUTO;
    
    private AtomicBoolean connectionCancel = new AtomicBoolean();

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public void setInteractive(NBProfileDirectTask.YesNoAuto arg) {
        String value = arg.getValue();

        if (value.equals("auto")) { //NOI18N
            interactive = INTERACTIVE_AUTO;
        } else if (value.equals("yes") || value.equals("true")) { // NOI18N
            interactive = INTERACTIVE_YES;
        } else if (value.equals("no") || value.equals("false")) { // NOI18N
            interactive = INTERACTIVE_NO;
        }
    }

    public void setJvmArgsPrefix(String value) {
        jvmArgsPrefix = value;
    }

    public void setJvmArgsProperty(String value) {
        jvmArgsProperty = value;
    }

    public void setJvmProperty(String value) {
        jvmProperty = value;
    }

    // -- Properties -------------------------------------------------------------------------------------------------------
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * "classpath" subelements, only one is allowed
     *
     * @param path the classpath
     */
    public void addClasspath(final Path path) {
        if (classpath != null) {
            throw new BuildException("Only one classpath subelement is supported"); //NOI18N
        }

        classpath = path;
    }

    /**
     * "classpath" subelements, only one is allowed
     *
     * @param path the classpath
     */
    public void addRootspath(final Path path) {
        if (rootsPath != null) {
            throw new BuildException("Only one classpath subelement is supported"); //NOI18N
        }

        rootsPath = path;
    }

    // -- Main methods -----------------------------------------------------------------------------------------------------
    public void execute() throws BuildException {
        ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
        if (s != null && s.isConfigured()) {
            Map<String, String> props = s.getProperties();
            if (props != null) {
                for(Map.Entry<String, String> e : props.entrySet()) {
                    getProject().setProperty(e.getKey(), e.getValue());
                }
                getProject().setProperty("profiler.jvmargs", "-J-Dprofiler.pre72=true"); // NOI18N
                
                getProject().addBuildListener(new BuildEndListener(connectionCancel));
                
                if (!NetBeansProfiler.getDefaultNB().startEx(s.getProfilingSettings(), s.getSessionSettings(), connectionCancel)) {
                    throw new BuildException("User abort"); // NOI18N
                }
            }
        } else {
            throw new BuildException("User abort");// NOI18N
        }
    }
}
